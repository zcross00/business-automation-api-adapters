package com.redhat.business.automation.adapter.rules.embedded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;
import com.redhat.business.automation.test.domain.model.Input;
import com.redhat.business.automation.test.domain.model.Output;

@Tag( "unit" )
@Tag( "embedded" )
@DisplayName( "Embedded Classpath Rules Adapter Unit Tests" )
public class ClasspathEmbeddedRulesExecutionAdapterTest {

    private RulesExecutionAdapter adapter = new ClasspathEmbeddedRulesExecutionAdapter();

    @Test
    @DisplayName( "Embedded Classpath rule execution with a single query" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromOneQuery() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
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
        assertEquals( response.getMessage(), "Rules execution successful" );
    }

    @Test
    @DisplayName( "Embedded Classpath rule execution with multiple query results" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromMultipleQueries() {

        Collection<Object> facts = Arrays.asList( new Input( "Input 1" ), new Input( "Input 2" ) );

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
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
        assertEquals( response.getMessage(), "Rules execution successful" );
    }

    @Test
    @DisplayName( "Embedded Classpath rule execution fails because Query doesn't exist" )
    public void shouldHaveFailureResponseBecauseQueryDoesNotExist() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addFact( new Input( "Input 1" ) )
                                                                 .addQuery( "XXX", "$output" )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.FAILURE, response.getStatus() );
        assertEquals( "Query 'XXX' does not exist", response.getMessage() );
    }
}