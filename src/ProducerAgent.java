import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class ProducerAgent extends Agent {
    private Integer id = 0;
    private int maxTokens = 100;
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

        // Add a CyclicBehaviour that
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Prepare template to get messages from consumer agents
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                // Receive requests from consumer agents
                ACLMessage requests = receive(mt);
                if (requests != null) {
                    // Create replies for consumer agents
                    ACLMessage reply = requests.createReply();

                    Iterator<String> itr = tokens.iterator();
                    if (itr.hasNext()) {
                        String token = itr.next().toString();
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContent(token);
                        itr.remove();
                        System.out.println(getAID().getLocalName() + ": gives token nr" + token);
                        if (givenTokens.get(requests.getContent()) == null) {
                            givenTokens.put(requests.getContent(), 1);
                        } else {
                            int count = givenTokens.get(requests.getContent());
                            givenTokens.remove(requests.getContent());
                            givenTokens.put(requests.getContent(), count + 1);
                        }

                    } else {
                        if (id == maxTokens) {
                            reply.setPerformative(ACLMessage.FAILURE);
                            reply.setContent("not-available");
                            System.out.println("All tokens created and taken by consumers");
                        } else {
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent("not-available");
                            System.out.println(getAID().getLocalName() + ": there are no available tokens now");
                        }
                    }
                    send(reply);
                } else {
                    block();
                }
            }
        });

    }
}
