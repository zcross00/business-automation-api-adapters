package com.redhat.business.automation.adapter.rules.embedded.runner;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(strict = true, plugin = {"json:target/json/provider-reports.json", "pretty"},
        glue = "com.redhat.business.automation.adapter.rules.embedded.steps",
        features = "src/test/resources/features/" )
public class EmbeddedRulesExecutionAdapterTest {

}
