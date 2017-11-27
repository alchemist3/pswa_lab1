import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.TickerBehaviour;

import java.util.Random;


public class ConsumerAgent extends Agent {
    private int interval = new Random().nextInt(100) + 50;
    private boolean active = true;
    private AID[] producers;
    private int tokensTaken = 0;
    public TickerBehaviour tickerBehaviour;


    // Agent initialization
    protected void setup() {
        // Welcome message
        System.out.println("Consumer-agent " + getAID().getName() + " is ready.");
    }




}
