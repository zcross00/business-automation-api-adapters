package com.redhat.business.automation.adapter.rules.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.business.automation.adapter.rules.api.QueryDescriptor;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;

/**
 *
 * This is the base class that encapsulates the shared functionality required for both Embredded and Decision Server
 * implementations
 *
 */
public abstract class BaseRulesExecutionAdapter implements RulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( BaseRulesExecutionAdapter.class );

    /**
     * 
     * Implementations should override this with the behavior based on various configuration
     * dimensions (classpath vs. KieScanner, embedded vs. remote) etc...
     * 
     * @param commands - batch execution command to execute against the engine
     * @throws RulesExecutionException
     */
    abstract protected ExecutionResults executeRules( RulesExecutionRequest request, Command<?> commands ) throws RulesExecutionException;

    @Override
    public RulesExecutionResponse executeStatelessRules( RulesExecutionRequest request ) {
        RulesExecutionResponse response = new RulesExecutionResponse();

        Command<?> batchCommand = buildBatchCommand( request );
        try {
            ExecutionResults results = executeRules( request, batchCommand );
            Map<String, Collection<Object>> queryOutput = processQueryResults( results, request.getQueries() );
            response.setOutput( queryOutput );
            response.setStatus( RulesExecutionRequestStatus.SUCCESS );
            response.setMessage( "Rules execution successful" );

        } catch ( RulesExecutionException e ) {
            LOG.error( "Error executing stateless rules execution request - " + e.getMessage() );
            if ( e.getCause() != null ) {
                LOG.error( "Root cause was : " + e.getCause().getClass() + " - " + e.getCause().getMessage() );
            }
            response.setMessage( e.getMessage() );
            response.setStatus( RulesExecutionRequestStatus.FAILURE );
        }
        return response;
    }

    protected Command<?> buildBatchCommand( RulesExecutionRequest request ) {
        List<Command<?>> commands = new ArrayList<>();

        for ( Object fact : request.getFacts() ) {
            commands.add( CommandFactory.newInsert( fact ) );
        }

        if ( request.getProcessId() != null ) {
            commands.add( CommandFactory.newStartProcess( request.getProcessId() ) );
        } else {
            commands.add( CommandFactory.newFireAllRules() );
        }

        for ( QueryDescriptor query : request.getQueries() ) {
            commands.add( CommandFactory.newQuery( query.getName(), query.getName() ) );
        }

        return CommandFactory.newBatchExecution( commands, request.getKsession() );
    }

    protected Map<String, Collection<Object>> processQueryResults( ExecutionResults results, Set<QueryDescriptor> queries ) {
        Map<String, Collection<Object>> output = new HashMap<>();

        for ( QueryDescriptor query : queries ) {
            QueryResults queryResults = (QueryResults) results.getValue( query.getName() );
            Iterator<QueryResultsRow> rows = queryResults.iterator();
            List<Object> objects = new ArrayList<>();
            while ( rows.hasNext() ) {
                objects.add( rows.next().get( query.getOutputId() ) );
            }
            output.put( query.getName(), objects );
        }

        return output;
    }

}