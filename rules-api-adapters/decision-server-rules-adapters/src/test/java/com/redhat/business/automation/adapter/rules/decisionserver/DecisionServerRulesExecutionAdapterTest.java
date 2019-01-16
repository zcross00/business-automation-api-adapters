package com.redhat.business.automation.adapter.rules.decisionserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.redhat.business.automation.adapter.decisionserver.common.DecisionServerClient;
import com.redhat.business.automation.adapter.decisionserver.common.DecisionServerClientException;
import com.redhat.business.automation.adapter.decisionserver.common.DecisionServerClientType;
import com.redhat.business.automation.adapter.rules.api.GroupArtifactVersion;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;
import com.redhat.business.automation.test.domain.model.Input;
import com.redhat.business.automation.test.domain.model.Output;

@Tag( "integration" )
@Tag( "decision-server" )
@DisplayName( "Decision Server Stateless Rules Execution Integration Tests" )
public class DecisionServerRulesExecutionAdapterTest {

    private static final Logger LOG = LoggerFactory.getLogger( DecisionServerRulesExecutionAdapterTest.class );

    private static final String GROUP = "com.redhat.business.automation";
    private static final String ARTIFACT = "test-rules-kjar";
    private static final String VERSION = "0.0.3.Beta";

    private static final String URL = System.getProperty( "kie.server.url" ) != null ? System.getProperty( "kie.server.url" ) : "http://localhost:8080/kie-server/services/rest/server";
    private static final String USERNAME = System.getProperty( "kie.server.username" ) != null ? System.getProperty( "kie.server.username" ) : "kie";
    private static final String PASSWORD = System.getProperty( "kie.server.password" ) != null ? System.getProperty( "kie.server.password" ) : "kie";
    private static final long TIMEOUT = System.getProperty( "kie.server.timeout" ) != null ? Long.parseLong( System.getProperty( "kie.server.timeout" ) ) : 10000l;
    private static final String TEST_MODEL_PACKAGE = "com.redhat.business.automation.test.domain.model";

    private DecisionServerClient decisionServerClient = null;

    private RulesExecutionAdapter adapter = null;

    @BeforeEach
    public void setup() throws Exception {
        Set<String> modelPackages = new HashSet<>();
        modelPackages.add( TEST_MODEL_PACKAGE );

        this.decisionServerClient = new DecisionServerClient( URL, USERNAME, PASSWORD, false, TIMEOUT, modelPackages, DecisionServerClientType.REST, MarshallingFormat.XSTREAM );
        this.decisionServerClient.init();

        DecisionServerRulesExecutionAdapter dsAdapter = new DecisionServerRulesExecutionAdapter( decisionServerClient );

        ensureTestKieContainerIsDeployed();

        adapter = dsAdapter;
    }

    @Test
    @DisplayName( "Decision Server rule execution with a single query" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromOneQuery() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.3.Beta" )
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
    @DisplayName( "Decision Server rule execution with multiple query results" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromMultipleQueries() {

        Collection<Object> facts = Arrays.asList( new Input( "Input 1" ), new Input( "Input 2" ) );

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.3.Beta" )
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
    @DisplayName( "Decision Server rule execution fails because Query doesn't exist" )
    public void shouldHaveFailureResponseBecauseQueryDoesNotExist() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.3.Beta" )
                                                                 .ksession( "test-rules-stateless-ksession" )
                                                                 .addFact( new Input( "Input 1" ) )
                                                                 .addQuery( "XXX", "$output" )
                                                                 .build();
        
        RulesExecutionResponse response = adapter.executeStatelessRules( request );
        
        //@formatter:on

        assertEquals( RulesExecutionRequestStatus.FAILURE, response.getStatus() );
        assertTrue( response.getMessage().contains( "Query 'XXX' does not exist" ) );
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

    private void ensureTestKieContainerIsDeployed() {
        try {
            String containerId = decisionServerClient.getContainerId( new GroupArtifactVersion( GROUP, ARTIFACT, VERSION ) );
            if ( Strings.isNullOrEmpty( containerId ) ) {
                decisionServerClient.deployContainer( GROUP, ARTIFACT, VERSION );
            }
        } catch ( DecisionServerClientException e ) {
            fail( e.getMessage() );
        }
    }
}