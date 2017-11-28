import java.util.Random;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

@SuppressWarnings("serial")
public class ConsumerAgent extends Agent {
    final private int interval = new Random().nextInt(150) + 100;
    private boolean active = true;
    private AID[] producerAgents;
    private int tokensTaken = 0;
    public TickerBehaviour tickerBehaviour;

    protected void setup() {
        tickerBehaviour = takeToken(this, interval);
    }

    private TickerBehaviour takeToken(final ConsumerAgent agent, int interval) {
        TickerBehaviour tickerBehaviour = new TickerBehaviour(this, interval) {
            @Override
            protected void onTick() {
                if (active) {
                    // Register the token-producing service in the yellow pages
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("token-producing");
                    DFAgentDescription dfd = new DFAgentDescription();
                    dfd.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(agent, dfd);
                        producerAgents = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            producerAgents[i] = result[0].getName();
                        }
                    } catch (FIPAException ex) {
                        ex.printStackTrace();
                    }

                    if (producerAgents.length > 0)
                        addBehaviour(new RequestPerformer(agent));
                } else {
                    //System.out.println(getAID().getLocalName() + ":WAITING...");
                    active = true;
                }
            }
        };

        addBehaviour(tickerBehaviour);

        return tickerBehaviour;
    }

    private class RequestPerformer extends Behaviour {
        private int step = 0;
        // The template to receive replies
        private MessageTemplate mt;

        public RequestPerformer(ConsumerAgent agent) {
            super(agent);
        }

        @Override
        public void action() {
            switch(step) {
                case 0:
                    // Send request to producer
                    ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
                    cfp.addReceiver(producerAgents[0]);
                    cfp.setContent(getLocalName());
                    cfp.setReplyWith(getName() + System.currentTimeMillis());
                    send(cfp);
                    mt = MessageTemplate.MatchInReplyTo(cfp.getReplyWith());
                    step = 1;
                    break;

                case 1:
                    // Receive reply from producer
                    ACLMessage reply = receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.CONFIRM) {
                            tokensTaken++;
                        } else if (reply.getPerformative() == ACLMessage.FAILURE) {
                            System.out.println(getAID().getLocalName() + ": " + tokensTaken);
                            removeBehaviour(tickerBehaviour);
                        }
                        step = 2;
                        active = false;
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return step == 2;
        }
    }
}