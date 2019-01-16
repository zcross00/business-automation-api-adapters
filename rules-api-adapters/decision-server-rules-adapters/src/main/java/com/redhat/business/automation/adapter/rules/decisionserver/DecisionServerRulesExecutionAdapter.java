package com.redhat.business.automation.adapter.rules.decisionserver;

import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.redhat.business.automation.adapter.decisionserver.common.DecisionServerClient;
import com.redhat.business.automation.adapter.decisionserver.common.DecisionServerClientException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionException;
import com.redhat.business.automation.adapter.rules.api.RulesExecutionRequest;
import com.redhat.business.automation.adapter.rules.base.BaseRulesExecutionAdapter;

public class DecisionServerRulesExecutionAdapter extends BaseRulesExecutionAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( DecisionServerRulesExecutionAdapter.class );

    private RuleServicesClient rulesClient;
    private DecisionServerClient decisionServerClient;

    public DecisionServerRulesExecutionAdapter( DecisionServerClient client ) {
        this.decisionServerClient = client;
    }

    @Override
    protected ExecutionResults executeRules( RulesExecutionRequest request, Command<?> commands ) throws RulesExecutionException {
        try {
            if ( !decisionServerClient.isInitialized() || rulesClient == null ) {
                decisionServerClient.init();
                rulesClient = decisionServerClient.getRulesClient();
            }

            String containerId = decisionServerClient.getContainerId( request.getGav() );

            if ( Strings.isNullOrEmpty( containerId ) ) {
                throw new RulesExecutionException( "No suitable KieContainer found for " + request.getGav().toString() );
            }

            ServiceResponse<ExecutionResults> response = rulesClient.executeCommandsWithResults( containerId, commands );

            if ( response.getType() == ResponseType.SUCCESS ) {
                return response.getResult();
            } else {
                LOG.error( "Error executing commands on Decision Server : " + response.getMsg() );
                throw new RulesExecutionException( response.getMsg() );
            }

        } catch ( DecisionServerClientException e ) {
            throw new RulesExecutionException( e.getMessage() );
        }
    }
}