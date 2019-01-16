package com.redhat.business.automation.adapter.planner.decisionserver;

import java.util.Set;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;

public class DecsionServerPlannerExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( DecsionServerPlannerExecutionAdapter.class );

    private MarshallingFormat marshallingFormat = MarshallingFormat.XSTREAM;
    private Boolean useSSL = true;
    private Long timeout = 10000L;
    private String url;
    private String username;
    private String password;
    private Set<String> modelPackages;

    private RuleServicesClient rulesClient;
    private KieServicesClient kieServicesClient;

}