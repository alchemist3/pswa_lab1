import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.ServiceConfigurationError;
import java.util.concurrent.CyclicBarrier;

public class ProducerAgent extends Agent {
    private Integer id = 0;
    private int maxTokens = 150;
    private Queue<String> tokens = new LinkedList<String>();
    private Map<String, Integer> givenTokens = new HashMap<String, Integer>();
    private CyclicBehaviour mainBehaviour;

    private AID[] consumerAgents = {new AID("consumer1", AID.ISLOCALNAME),
            new AID("consumer2", AID.ISLOCALNAME)};

    // Agent initialization
    protected void setup() {
        // Welcome message
        System.out.println("Producer-agent " + getAID().getName() + " is ready.");

        // Register the token-producing service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("token-producing");
        sd.setName("JADE-token-managing");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add a TickerBehaviour that schedules a token creation every 200 ms
        addBehaviour(new TickerBehaviour(this, 200) {
            @Override
            protected void onTick() {
                if (id < maxTokens) {
                    tokens.add(id.toString());
                    System.out.println(getAID().getLocalName() + " created token nr " + id);
                    id++;
                } else {
                    removeBehaviour(this);
                }
            }
        });
        giveToken();

    }


    private void giveToken() {
        // TODO
    }
}
