import jade.core.Agent;
import jade.core.AID;

public class ProducerAgent extends Agent {
    private Integer id = 0;
    private int maxTokens = 150;

    private AID[] consumerAgents = {new AID("consumer1", AID.ISLOCALNAME),
            new AID("consumer2", AID.ISLOCALNAME)};

    // Agent initialization
    protected void setup() {
        // Welcome message
        System.out.println("Producer-agent " + getAID().getName() + " is ready.");
    }
}
