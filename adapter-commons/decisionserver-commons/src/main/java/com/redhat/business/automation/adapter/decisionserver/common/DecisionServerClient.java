package com.redhat.business.automation.adapter.decisionserver.common;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kie.server.api.exception.KieServicesHttpException;
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
import org.kie.server.client.SolverServicesClient;
import org.kie.server.common.rest.NoEndpointFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;

public class DecisionServerClient {

    private static final Logger LOG = LoggerFactory.getLogger( DecisionServerClient.class );

    private String url;
    private String username;
    private String password;
    private Boolean useSSL;
    private Long timeout;
    private Set<String> modelPackages;
    private KieServicesClient kieServicesClient;
    private DecisionServerClientType type;
    private MarshallingFormat marshallingFormat;

    private boolean initialized = false;

    public DecisionServerClient( String url, String username, String password, Boolean useSSL, Long timeout, Set<String> modelPackages, DecisionServerClientType type, MarshallingFormat marshallingFormat ) {
        super();
        this.url = url;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
        this.timeout = timeout;
        this.modelPackages = modelPackages;
        this.type = type;
        this.marshallingFormat = marshallingFormat;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Container methods
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getContainerId( GroupArtifactVersion gav ) throws DecisionServerClientException {

        if ( !initialized ) {
            init();
        }

        String containerId = null;
        if ( "LATEST".equals( gav.getVersion() ) ) {
            LOG.error( "using LATEST as rules is not yet supported." );

        } else {
            containerId = getContainerForGAV( gav.getGroup(), gav.getArtifact(), gav.getVersion() );
        }

        LOG.debug( "Using Kie Container : " + containerId );
        return containerId;
    }

    public String getContainerForGAV( String group, String artifact, String version ) throws DecisionServerClientException {
        if ( !initialized ) {
            init();
        }

        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder().status( KieContainerStatus.STARTED ).releaseId( new ReleaseId( group, artifact, version ) ).build();
        List<KieContainerResource> kcontainers = kieServicesClient.listContainers( filter ).getResult().getContainers();
        String containerId = !kcontainers.isEmpty() ? kcontainers.get( 0 ).getContainerId() : null;

        return containerId;

    }

    public void deployContainer( GroupArtifactVersion gav ) throws DecisionServerClientException {
        deployContainer( gav.getGroup(), gav.getArtifact(), gav.getVersion() );
    }

    public void deployContainer( String group, String artifact, String version ) throws DecisionServerClientException {
        if ( !initialized ) {
            init();
        }

        String id = group + ":" + artifact + ":" + version;
        KieContainerResource kcontainer = new KieContainerResource( id, new ReleaseId( group, artifact, version ) );
        ServiceResponse<KieContainerResource> response = kieServicesClient.createContainer( id, kcontainer );
        if ( response.getType() != ResponseType.SUCCESS ) {
            throw new DecisionServerClientException( "Could not deploy kie container " + id );
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Client methods
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 
     * Get client for Drools remote execution
     * 
     * @return
     * @throws DecisionServerClientException
     */
    public RuleServicesClient getRulesClient() throws DecisionServerClientException {
        if ( !initialized ) {
            init();
        }
        return (RuleServicesClient) kieServicesClient.getServicesClient( RuleServicesClient.class );
    }

    /**
     * 
     * Get client for Optaplanner remote execution
     * 
     * @return
     * @throws DecisionServerClientException
     */
    public SolverServicesClient getSolverServicesClient() throws DecisionServerClientException {
        if ( !initialized ) {
            init();
        }
        return (SolverServicesClient) kieServicesClient.getServicesClient( SolverServicesClient.class );
    }

    /**
     * 
     * Initializes the client connection with the Decision Server.
     * 
     * This method is public in order to give control to the application when this connection is established, as it can lead to Exceptions being thrown
     * 
     * @throws DecisionServerClientException
     */
    public void init() throws DecisionServerClientException {
        try {
            KieServicesConfiguration config = null;
            if ( this.type == DecisionServerClientType.REST ) {
                config = KieServicesFactory.newRestConfiguration( url, username, password );
            } else if ( this.type == DecisionServerClientType.JMS ) {
                LOG.error( "JMS type not yet supported" );
                throw new DecisionServerClientException( "JMS client connections are not yet supported. See:" );
            }

            config.setMarshallingFormat( marshallingFormat );
            config.setUseSsl( useSSL );
            config.setTimeout( timeout );
            addClassesFromModelPackages( config, modelPackages );
            kieServicesClient = KieServicesFactory.newKieServicesClient( config );
            initialized = true;
        } catch ( NoEndpointFoundException e ) {
            LOG.error( "No Decision Server endpoint available at : " + url );
            throw new DecisionServerClientException( "JMS client connections are not yet supported. See:" );
        } catch ( KieServicesHttpException e ) {
            LOG.error( "Decision Server endpoint is available @ " + url + ", however, there is a problem connecting to the endpoint." );
            String message = e.getMessage().contains( "Unauthorized" ) ? "There appears to be an issue with the credentials used to connect to Decision Server" : "Decision Server endpoint is available @ " + url + ", however, there is a problem connecting to the endpoint.";
            throw new DecisionServerClientException( message, e );
        }
    }

    /*
     * This method uses Guava to scan a list of domain model packages that Decision Server needs to be made aware about
     * mostly for JSON marshalling purposes 
     */
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

    public MarshallingFormat getMarshallingFormat() {
        return marshallingFormat;
    }

    public void setMarshallingFormat( MarshallingFormat marshallingFormat ) {
        this.marshallingFormat = marshallingFormat;
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

    public KieServicesClient getKieServicesClient() {
        return kieServicesClient;
    }

    public void setKieServicesClient( KieServicesClient kieServicesClient ) {
        this.kieServicesClient = kieServicesClient;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized( boolean initialized ) {
        this.initialized = initialized;
    }

    public DecisionServerClientType getType() {
        return type;
    }

    public void setType( DecisionServerClientType type ) {
        this.type = type;
    }

}