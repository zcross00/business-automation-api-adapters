package com.redhat.business.automation.test.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.runtime.ClassObjectFilter;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;

import com.redhat.business.automation.test.domain.model.Input;
import com.redhat.business.automation.test.domain.model.Output;

@DisplayName( "Basic Rules Test" )
public class RulesTest {

    @Test
    @DisplayName( "Verify rules work" )
    public void verifyRulesWork() {
        KieContainer container = KieServices.Factory.get().newKieClasspathContainer();

        StatelessKieSession session = container.newStatelessKieSession( "test-rules-stateless-ksession" );

        Input i1 = new Input( "1" );
        Input i2 = new Input( "2" );

        List<Command<?>> commands = new ArrayList<>();
        commands.add( CommandFactory.newInsert( i1 ) );
        commands.add( CommandFactory.newInsert( i2 ) );
        commands.add( CommandFactory.newFireAllRules() );
        commands.add( CommandFactory.newGetObjects( new ClassObjectFilter( Output.class ), "output" ) );

        Command<?> batch = CommandFactory.newBatchExecution( commands );

        ExecutionResults results = (ExecutionResults) session.execute( batch );

        @SuppressWarnings( "unchecked" ) // A real application would probably want to be safer, in this case I know exactly what I'm getting
        Output output = ( (List<Output>) results.getValue( "output" ) ).get( 0 );

        Output expected = new Output( 2l );

        assertEquals( expected, output );
    }

}
