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

import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.base.BaseRulesExecutionAdapter;

public class KieScannerEmbeddedRulesExecutionAdapter extends BaseRulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( KieScannerEmbeddedRulesExecutionAdapter.class );
    private Map<GroupArtifactVersion, KieContainer> containers = new HashMap<>();

    @Override
    protected ExecutionResults executeRules( RulesExecutionRequest request, Command<?> commands ) throws RulesExecutionException {
        if ( !containers.containsKey( request.getGav() ) ) {
            loadNewKieContainer( request.getGav() );
        }
        KieContainer container = containers.get( request.getGav() );

        try {
            StatelessKieSession session = container.newStatelessKieSession( request.getKsession() );
            return (ExecutionResults) session.execute( commands );
        } catch ( RuntimeException e ) {
            LOG.error( "Error executing embedded classpath rules - " + e.getMessage() );
            throw new RulesExecutionException( e.getMessage(), e.getCause() );
        }

    }

    private void loadNewKieContainer( GroupArtifactVersion gav ) throws RulesExecutionException {
        if ( !containers.containsKey( gav ) ) {
            try {
                ReleaseId releaseId = KieServices.Factory.get().newReleaseId( gav.getGroup(), gav.getArtifact(), gav.getVersion() );
                KieContainer container = KieServices.Factory.get().newKieContainer( releaseId );
                KieScanner scanner = KieServices.Factory.get().newKieScanner( container );
                scanner.scanNow();
                scanner.stop();
                containers.put( gav, container );
            } catch ( RuntimeException e ) {
                LOG.error( "Unable to load rules for " + gav.toString() );
                throw new RulesExecutionException( "There was an error loading the dynamic rules artifact " + gav.toString(), e );
            }
        }
    }
}