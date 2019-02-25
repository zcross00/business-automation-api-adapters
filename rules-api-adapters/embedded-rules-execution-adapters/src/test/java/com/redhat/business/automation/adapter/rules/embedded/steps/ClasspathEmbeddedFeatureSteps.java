package com.redhat.business.automation.adapter.rules.embedded.steps;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.business.automation.adapter.rules.api.RulesExecutionAdapter;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequestStatus;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionResponse;
import com.redhat.business.automation.adapter.rules.embedded.ClasspathEmbeddedRulesExecutionAdapter;
import com.redhat.business.automation.test.domain.model.Input;
import com.redhat.business.automation.test.domain.model.Output;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ClasspathEmbeddedFeatureSteps {
	
	private RulesExecutionAdapter target = new ClasspathEmbeddedRulesExecutionAdapter();
	
	private RulesExecutionRequest request = null;
	private RulesExecutionResponse response = null;
	
	Collection<Object> facts = null;

	@Given("^I have the facts: \"([^\"]*)\" and the query: \"([^\"]*)\" for a Classpath adapter$")
	public void i_have_the_facts_and_the_query_for_a_Classpath_adapter(String factString, String query) throws Exception {
		facts = Arrays.stream(factString.split(","))
				.map(fact -> new Input(fact))
				.collect(Collectors.toList());

        request = new RulesExecutionRequest.Builder()
                                           .useGAV( "com.redhat.business.automation", "test-rules-kjar", "0.0.1-SNAPSHOT" )
                                           .ksession( "test-rules-stateless-ksession" )
                                           .addFacts( facts )
                                           .addQuery( query, "$output" )
                                           .addQuery( "Get Inputs", "$input" )
                                           .build();
	}

	@When("^I ask for the output from the Classpath adapter$")
	public void i_ask_for_the_output_from_the_Classpath_adapter() throws Exception {
		response = target.executeStatelessRules(request);
	}

	@Then("^the Classpath adapter should return status of \"([^\"]*)\" and output of (\\d+)$")
	public void the_Classpath_adapter_should_return_status_of_and_output_of(String status, int output) throws Exception {
		List<Output> expectedOutput = null;
		expectedOutput = new ArrayList<>();
        expectedOutput.add( new Output((long) output) );
        
        assertEquals( response.getStatus(), RulesExecutionRequestStatus.valueOf(status));
		
        if(RulesExecutionRequestStatus.valueOf(status).equals(RulesExecutionRequestStatus.SUCCESS)) {
        	assertIterableEquals( expectedOutput, response.getQueryOutput( "Get Output" ) );
        	assertIterableEquals( facts, response.getQueryOutput( "Get Inputs" ) );
        }
	}

}
