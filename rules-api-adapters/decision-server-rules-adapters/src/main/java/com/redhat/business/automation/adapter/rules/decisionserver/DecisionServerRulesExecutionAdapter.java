package com.redhat.business.automation.adapter.rules.decisionserver;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.base.BaseRulesExecutionAdapter;

public class DecisionServerRulesExecutionAdapter extends BaseRulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( DecisionServerRulesExecutionAdapter.class );

    private MarshallingFormat marshallingFormat = MarshallingFormat.XSTREAM;
    private Boolean useSSL = true;
    private Long timeout = 10000L;
    private String url;
    private String username;
    private String password;
    private Set<String> modelPackages;

    private RuleServicesClient rulesClient;
    private KieServicesClient kieServicesClient;

    @Override
    protected ExecutionResults executeRules( RulesExecutionRequest request, Command<?> commands ) throws RulesExecutionException {
        if ( kieServicesClient == null || rulesClient == null ) {
            init();
        }

        String containerId = getContainerId( request.getGav() );
        ServiceResponse<ExecutionResults> response = rulesClient.executeCommandsWithResults( containerId, commands );
        if ( response.getType() == ResponseType.SUCCESS ) {
            return response.getResult();
        } else {
            LOG.error( "Error executing commands on Decision Server : " + response.getMsg() );
            throw new RulesExecutionException( response.getMsg() );
        }
    }

    private void init() throws RulesExecutionException {
        LOG.info( String.format( "Initializing DecisionServiceAdapter: MARSHALLING = %s, USE_SSL = %s, TIMEOUT = %s, URL = %s", marshallingFormat, useSSL, timeout, url ) );
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration( url, username, password );
        config.setMarshallingFormat( marshallingFormat );
        config.setUseSsl( useSSL );
        config.setTimeout( timeout );
        addClassesFromModelPackages( config, modelPackages );
        kieServicesClient = KieServicesFactory.newKieServicesClient( config );
        rulesClient = kieServicesClient.getServicesClient( RuleServicesClient.class );
    }

    private String getContainerId( GroupArtifactVersion gav ) throws RulesExecutionException {
        String containerId = null;
        if ( "LATEST".equals( gav.getVersion() ) ) {
            LOG.error( "using LATEST as rules is not yet supported." );

        } else {
            containerId = getKieContainerForVersion( gav.getGroup(), gav.getArtifact(), gav.getVersion() );
        }

        LOG.debug( "Using Kie Container : " + containerId );
        return containerId;
    }

    private String getKieContainerForVersion( String group, String artifact, String version ) throws RulesExecutionException {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder().status( KieContainerStatus.STARTED ).releaseId( new ReleaseId( group, artifact, version ) ).build();

        List<KieContainerResource> kcontainers = kieServicesClient.listContainers( filter ).getResult().getContainers();

        if ( kcontainers.isEmpty() ) {
            throw new RulesExecutionException( String.format( "No suitable Kie Container found for - %s : %s : %s", group, artifact, version ) );
        } else {
            return kcontainers.get( 0 ).getContainerId();
        }
    }

    private void addClassesFromModelPackages( KieServicesConfiguration config, Set<String> packages ) {

        if ( packages == null || packages.isEmpty() ) {
            return;
        }
        try {
            Set<Class<?>> modelClasses = new HashSet<>();
            for ( String pkg : packages ) {
                final ClassLoader loader = Thread.currentThread().getContextClassLoader();
                ClassPath classpath = ClassPath.from( loader );
                classpath.getTopLevelClassesRecursive( pkg ).stream().map( ( ClassPath.ClassInfo t ) -> t.load() ).forEach( modelClasses::add );
            }

            modelClasses = modelClasses.stream().filter( c -> !c.isInterface() ).collect( Collectors.toSet() );

            LOG.info( "Adding these classes from model packages:" );
            modelClasses.stream().forEach( c -> LOG.info( "\t" + c.getCanonicalName() ) );

            config.addExtraClasses( modelClasses );
        } catch ( IOException ex ) {
            //TODO - @michael - I kind of forget exactly what this code is doing, I'll have to look into how to handle this properly
            ex.printStackTrace();
        }
    }

    public Boolean getUseSSL() {
        return useSSL;
    }

    public void setUseSSL( Boolean useSSL ) {
        this.useSSL = useSSL;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout( Long timeout ) {
        this.timeout = timeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl( String url ) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername( String username ) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public Set<String> getModelPackages() {
        return modelPackages;
    }

    public void setModelPackages( Set<String> modelPackages ) {
        this.modelPackages = modelPackages;
    }

}