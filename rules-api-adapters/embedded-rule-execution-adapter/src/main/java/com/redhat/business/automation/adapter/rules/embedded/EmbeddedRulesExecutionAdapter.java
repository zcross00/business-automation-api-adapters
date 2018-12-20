package com.redhat.business.automation.adapter.rules.embedded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;
import com.redhat.business.automation.adapter.rules.api.InternalRuleIO;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;

public class EmbeddedRulesExecutionAdapter implements RulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( EmbeddedRulesExecutionAdapter.class );
    private static final KieContainer CLASSPATH_KCONTAINER = KieServices.Factory.get().newKieClasspathContainer();
    private Map<GroupArtifactVersion, KieContainer> containers = new HashMap<>();

    private boolean auditLogEnabled = false;

    @Override
    public RulesExecutionResponse executeStatelessRules( RulesExecutionRequest request ) {
        RulesExecutionResponse response = new RulesExecutionResponse();

        Command<?> batchExecutionCommand = buildBatchExecutionCommand( request );
        StatelessKieSession session = null;

        if ( request.getGav() != null ) {
            if ( !isKieContainerInCache( request.getGav() ) ) {
                try {
                    loadNewKieContainer( request );
                    LOG.info( "Using rules artifact for " + request.getGav().toString() );
                    session = containers.get( request.getGav() ).newStatelessKieSession( request.getKsession() );
                } catch ( RulesExecutionException e ) {
                    response.setStatus( RulesExecutionRequestStatus.FAILURE );
                    response.setStatusMessage( e.getMessage() );
                }
            }
        } else {
            LOG.info( "Using classpath based rules" );
            session = CLASSPATH_KCONTAINER.newStatelessKieSession( request.getKsession() );
        }

        if ( session != null ) { // I hate having to rely on nulls for logic 
            KieRuntimeLogger audit = null;

            if ( auditLogEnabled ) {
                audit = KieServices.Factory.get().getLoggers().newFileLogger( session, "out.log" );
            }

            ExecutionResults results = (ExecutionResults) session.execute( batchExecutionCommand );

            processResults( results, response );
            response.setStatus( RulesExecutionRequestStatus.SUCCESS );

            // @michael - TODO - need to check the drools code, I think this happens automagically
            if ( audit != null ) {
                audit.close();
            }
        }

        return response;
    }

    private Command<?> buildBatchExecutionCommand( RulesExecutionRequest request ) {
        List<Command<?>> commands = new ArrayList<>();

        commands.addAll( insertFacts( request ) );

        if ( request.getProcessId() != null ) {
            LOG.info( "Start of process execution, sessionID= " + request.getKsession() + ", processID=" + request.getProcessId() );
            commands.add( CommandFactory.newStartProcess( request.getProcessId() ) );
        } else {
            commands.add( CommandFactory.newFireAllRules() );
        }

        // out only facts need to be added to the end of the list, after fire all rules/start process
        if ( hasOutOnlyFacts( request ) ) {
            commands.addAll( processOutOnlyFacts( request ) );
        }

        return CommandFactory.newBatchExecution( commands );
    }

    private List<Command<?>> insertFacts( RulesExecutionRequest request ) {
        List<Command<?>> commands = new ArrayList<>();
        request.getFacts().stream().forEach( f -> {
            if ( f.getType().equals( InternalRuleIO.Type.IN_ONLY ) ) {
                commands.add( CommandFactory.newInsert( f.getFact() ) );
            } else if ( f.getType().equals( InternalRuleIO.Type.IN_OUT ) ) {
                commands.add( CommandFactory.newInsert( f.getFact(), f.getIdentifier() ) );
            }
        } );

        return commands;
    }

    private List<Command<?>> processOutOnlyFacts( RulesExecutionRequest request ) {
        List<Command<?>> commands = new ArrayList<>();
        request.getFacts().stream().filter( f -> f.getType() == InternalRuleIO.Type.OUT_ONLY ).forEach( fact -> {
            commands.add( CommandFactory.newGetObjects( new ClassObjectFilter( fact.getOutputClass() ), fact.getIdentifier() ) );
        } );

        return commands;
    }

    private boolean hasOutOnlyFacts( RulesExecutionRequest request ) {
        return request.getFacts().stream().filter( f -> f.getType() == InternalRuleIO.Type.OUT_ONLY ).collect( Collectors.toList() ).size() > 0;
    }

    public boolean isKieContainerInCache( GroupArtifactVersion gav ) {
        return containers.containsKey( gav );
    }

    private void loadNewKieContainer( RulesExecutionRequest request ) throws RulesExecutionException {
        if ( !containers.containsKey( request.getGav() ) ) {
            try {
                ReleaseId releaseId = KieServices.Factory.get().newReleaseId( request.getGav().getGroup(), request.getGav().getArtifact(), request.getGav().getVersion() );
                KieContainer container = KieServices.Factory.get().newKieContainer( releaseId );
                KieScanner scanner = KieServices.Factory.get().newKieScanner( container );
                scanner.scanNow();
                scanner.stop();
                containers.put( request.getGav(), container );
            } catch ( RuntimeException e ) {
                LOG.error( "Unable to load rules for " + request.getGav().toString() );
                throw new RulesExecutionException( "There was an error loading the dynamic rules artifact " + request.getGav().toString(), e );
            }
        }

    }

    private void processResults( ExecutionResults results, RulesExecutionResponse response ) {
        for ( String identifier : results.getIdentifiers() ) {
            response.getOutput().put( identifier, results.getValue( identifier ) );
        }
    }
}