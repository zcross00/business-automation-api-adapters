package com.redhat.business.automation.adapter.rules.embedded;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.business.automation.adapter.embedded.common.EmbeddedKieScannerException;
import com.redhat.business.automation.adapter.embedded.common.KieScannerClient;
import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.base.BaseRulesExecutionAdapter;

public class KieScannerEmbeddedRulesExecutionAdapter extends BaseRulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( KieScannerEmbeddedRulesExecutionAdapter.class );
    private KieScannerClient kieScannerClient = new KieScannerClient();
    private Map<GroupArtifactVersion, KieContainer> containers = new HashMap<>();

    @Override
    protected ExecutionResults executeRules( RulesExecutionRequest request, Command<?> commands ) throws RulesExecutionException {
        try {
            if ( !containers.containsKey( request.getGav() ) ) {
                containers.put( request.getGav(), kieScannerClient.loadNewKieContainer( request.getGav() ) );
            }
            KieContainer container = containers.get( request.getGav() );

            StatelessKieSession session = container.newStatelessKieSession( request.getKsession() );
            return (ExecutionResults) session.execute( commands );
        } catch ( RuntimeException e ) {
            LOG.error( "Error executing embedded classpath rules - " + e.getMessage() );
            throw new RulesExecutionException( e.getMessage(), e.getCause() );
        } catch ( EmbeddedKieScannerException e ) {
            LOG.error( "Error loading from KieContainer from KieScanner" );
            throw new RulesExecutionException( e.getMessage(), e );
        }

    }

}