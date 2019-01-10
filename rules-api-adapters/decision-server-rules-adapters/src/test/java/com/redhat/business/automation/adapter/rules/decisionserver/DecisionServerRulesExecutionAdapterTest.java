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
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.common.rest.NoEndpointFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String VERSION = "0.0.1.Beta";

    private static final String URL = System.getProperty( "kie.server.url" ) != null ? System.getProperty( "kie.server.url" ) : "http://localhost:8080/kie-server/services/rest/server";
    private static final String USERNAME = System.getProperty( "kie.server.username" ) != null ? System.getProperty( "kie.server.username" ) : "kie";
    private static final String PASSWORD = System.getProperty( "kie.server.password" ) != null ? System.getProperty( "kie.server.password" ) : "kie";

    private static final long TIMEOUT = System.getProperty( "kie.server.timeout" ) != null ? Long.parseLong( System.getProperty( "kie.server.timeout" ) ) : 10000l;

    private RulesExecutionAdapter adapter = null;

    @BeforeEach
    public void setup() {

        // this tests whether or not we can connect to the KIE Server
        KieServicesClient testKieServicesClient = connectKieServicesClient();

        if ( testKieServicesClient != null ) {
            ensureTestKieContainerIsDeployed( testKieServicesClient );
        } else {
            LOG.error( String.format( "Cannot connect to kie server : url=%s : username=%s", URL, USERNAME ) );
            fail( "Cannot connect to Decision Server @ " + URL );
        }

        DecisionServerRulesExecutionAdapter dsAdapter = new DecisionServerRulesExecutionAdapter();
        dsAdapter.setUrl( URL );
        dsAdapter.setUsername( USERNAME );
        dsAdapter.setPassword( PASSWORD );
        dsAdapter.setUseSSL( false );
        dsAdapter.setTimeout( TIMEOUT );

        Set<String> modelPackages = new HashSet<>();
        modelPackages.add( "com.redhat.business.automation.test.domain.model" );
        dsAdapter.setModelPackages( modelPackages );

        adapter = dsAdapter;

    }

    @Test
    @DisplayName( "Embedded Kie Scanner rule execution with a single query" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromOneQuery() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1.Beta" )
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
    @DisplayName( "Embedded Kie Scanner rule execution with multiple query results" )
    public void shouldSuccessfullyExecuteRulesAndGetOutputFromMultipleQueries() {

        Collection<Object> facts = Arrays.asList( new Input( "Input 1" ), new Input( "Input 2" ) );

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1.Beta" )
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
    @DisplayName( "Embedded Kie Scanner rule execution fails because Query doesn't exist" )
    public void shouldHaveFailureResponseBecauseQueryDoesNotExist() {

        //@formatter:off
        RulesExecutionRequest request = new RulesExecutionRequest.Builder()
                                                                 .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1.Beta" )
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

    private void ensureTestKieContainerIsDeployed( KieServicesClient testKieServicesClient ) {
        KieContainerResourceFilter filter = new KieContainerResourceFilter.Builder().status( KieContainerStatus.STARTED ).build();
        KieContainerResourceList kcontainers = testKieServicesClient.listContainers( filter ).getResult();
        if ( kcontainers.getContainers().size() == 0 ) {
            String.format( "Test KIE Container %s : %s : %s was not found. Deploying it now", GROUP, ARTIFACT, VERSION );
            String id = GROUP + ":" + ARTIFACT + ":" + VERSION;
            KieContainerResource kcontainer = new KieContainerResource( id, new ReleaseId( GROUP, ARTIFACT, VERSION ) );
            ServiceResponse<KieContainerResource> response = testKieServicesClient.createContainer( id, kcontainer );
            if ( response.getType() != ResponseType.SUCCESS ) {
                fail( "Unable to deploy the test KIE Container" );
            }
        }

    }

    private KieServicesClient connectKieServicesClient() {
        KieServicesConfiguration config = KieServicesFactory.newRestConfiguration( URL, USERNAME, PASSWORD );
        KieServicesClient kieServicesClient = null;

        config.setMarshallingFormat( MarshallingFormat.XSTREAM );
        config.setUseSsl( false );
        config.setTimeout( TIMEOUT );

        try {
            kieServicesClient = KieServicesFactory.newKieServicesClient( config );
        } catch ( NoEndpointFoundException e ) {
            LOG.error( "No Decision Server endpoint available at : " + URL );
        } catch ( KieServicesHttpException e ) {
            LOG.error( "Decision Server endpoint is available @ " + URL + ", however, there is a problem connecting to the endpoint." );
            if ( e.getMessage().contains( "Unauthorized" ) ) {
                LOG.error( "There appears to be an issue with the credentials used to connect to Decision Server" );
            }
        }
        return kieServicesClient;
    }
}