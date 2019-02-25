package com.redhat.business.automation.adapter.rules.embedded;

import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.base.BaseRulesExecutionAdapter;

/**
 * 
 * This is the most simple implementation of the adapter. In this configuration the rules must be part of the classpath (ie: included as a dependency in pom.xml).
 * 
 * This implementation is primarily for usage in unit tests, although it could be used in production.
 *
 */
public class ClasspathEmbeddedRulesExecutionAdapter extends BaseRulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( ClasspathEmbeddedRulesExecutionAdapter.class );
    private static final KieContainer CLASSPATH_KCONTAINER = KieServices.Factory.get().newKieClasspathContainer();

    @Override
    protected ExecutionResults executeRules( RulesExecutionRequest request, Command<?> commands ) throws RulesExecutionException {
        if ( request.getGav() != null ) {
            LOG.warn( "A classpath embedded engine is configured, Maven Group:Artifact:Version coordinates will be ignored. KieBase will be loaded from the classpath instead" );
        }

        try {
            StatelessKieSession session = CLASSPATH_KCONTAINER.newStatelessKieSession( request.getKsession() );
            return (ExecutionResults) session.execute( commands );
        } catch ( RuntimeException e ) {
            LOG.error( "Error executing embedded classpath rules - " + e.getMessage() );
            throw new RulesExecutionException( e.getMessage(), e.getCause() );
        }
    }
}
