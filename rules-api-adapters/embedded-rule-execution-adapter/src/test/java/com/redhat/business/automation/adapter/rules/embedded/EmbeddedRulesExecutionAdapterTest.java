package com.redhat.business.automation.adapter.rules.embedded;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;
import com.redhat.business.automation.test.domain.model.Input;
import com.redhat.business.automation.test.domain.model.Output;

@DisplayName( "Embedded Rules Adapter Unit Tests" )
public class EmbeddedRulesExecutionAdapterTest {

    private RulesExecutionAdapter adapter = new EmbeddedRulesExecutionAdapter();

    @Test
    @DisplayName( "Rules loaded via classpath with embedded engine" )
    public void shouldSuccessfullExecuteClasspathRules() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addInOnlyFact( new Input( "Input 1" ) )
                                                                 .addOutOnlyFact( "output", Output.class )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.SUCCESS, response.getStatus() );

        List<Output> expectedOutput = new ArrayList<>();
        expectedOutput.add( new Output( 1L ) );

        assertIterableEquals( expectedOutput, (List<?>) response.getOutput().get( "output" ) );
    }

    @Test
    @DisplayName( "Rules successfully loaded dynamically from Kjar and executed with embedded engine" )
    public void shouldSuccessfullExecuteDynamicallyLoadedRules() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1-SNAPSHOT" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addInOnlyFact( new Input( "Input 1" ) )
                                                                 .addOutOnlyFact( "output", Output.class )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.SUCCESS, response.getStatus() );

        List<Output> expectedOutput = new ArrayList<>();
        expectedOutput.add( new Output( 1L ) );

        assertIterableEquals( expectedOutput, (List<?>) response.getOutput().get( "output" ) );
    }

    @Test
    @DisplayName( "Rule artifact is not resolved dynamically results in FAILURE response from embedded engine" )
    public void shouldGetErrorForMissingKJarArtifact() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "does", "not", "exist" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addInOnlyFact( new Input( "Input 1" ) )
                                                                 .addOutOnlyFact( "output", Output.class )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.FAILURE, response.getStatus() );
    }
}