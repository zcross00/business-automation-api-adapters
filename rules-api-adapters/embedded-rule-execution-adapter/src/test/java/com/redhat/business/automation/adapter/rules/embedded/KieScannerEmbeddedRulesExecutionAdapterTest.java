package com.redhat.business.automation.adapter.rules.embedded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.drools.core.runtime.rule.impl.FlatQueryResultRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;

import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;
import com.redhat.business.automation.test.domain.model.Input;
import com.redhat.business.automation.test.domain.model.Output;

@DisplayName( "Embedded Kie Scanner Rules Adapter Unit Tests" )
public class KieScannerEmbeddedRulesExecutionAdapterTest {

    private RulesExecutionAdapter adapter = new KieScannerEmbeddedRulesExecutionAdapter();

    @Test
    @DisplayName( "Embedded Kie Scanner rule execution with a single query" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromOneQuery() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1-SNAPSHOT" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addFact( new Input( "Input 1" ) )
                                                                 .addQuery( "Get Output", "$output" )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.SUCCESS, response.getStatus() );

        List<Output> expectedOutput = new ArrayList<>();
        expectedOutput.add( new Output( 1L ) );

        assertIterableEquals( expectedOutput, response.getQueryOutput( "Get Output" ) );
        assertEquals( response.getStatusMessage(), "Rules execution successful" );
    }

    @Test
    @DisplayName( "Embedded Kie Scanner rule execution with multiple query results" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromMultipleQueries() {

        Collection<Object> facts = Arrays.asList( new Input( "Input 1" ), new Input( "Input 2" ) );

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1-SNAPSHOT" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addFacts( facts )
                                                                 .addQuery( "Get Output", "$output" )
                                                                 .addQuery( "Get Inputs", "$input" )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.SUCCESS, response.getStatus() );

        List<Output> expectedOutput = new ArrayList<>();
        expectedOutput.add( new Output( 2L ) );

        assertIterableEquals( expectedOutput, response.getQueryOutput( "Get Output" ) );
        assertIterableEquals( facts, response.getQueryOutput( "Get Inputs" ) );
        assertEquals( response.getStatusMessage(), "Rules execution successful" );
    }

    @Test
    @DisplayName( "Embedded Kie Scanner rule execution fails because Query doesn't exist" )
    public void shouldHaveFailureResponseBecauseQueryDoesNotExist() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1-SNAPSHOT" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addFact( new Input( "Input 1" ) )
                                                                 .addQuery( "XXX", "$output" )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.FAILURE, response.getStatus() );
        assertEquals( "Query 'XXX' does not exist", response.getStatusMessage() );
    }

    @Test
    @DisplayName( "Rule artifact is not resolved dynamically results in FAILURE response from embedded engine" )
    public void shouldGetErrorForMissingKJarArtifact() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "does", "not", "exist" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addFact( new Input( "Input 1" ) )
                                                                 .addQuery( "Get Output", "$output" )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.FAILURE, response.getStatus() );
    }
}