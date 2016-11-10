package lab;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class BookSellerAgent extends Agent {
	private static final long serialVersionUID = 1L;

	
	private Hashtable<String, Integer> catalogue;
	
	private BookSellerGui myGui;

	protected void setup() {
		// Create the catalogue
		catalogue = new Hashtable<String, Integer>();

		myGui = new BookSellerGui(this);
		myGui.showGui();

	
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	
		addBehaviour(new OfferRequestsServer());

		
		addBehaviour(new PurchaseOrdersServer());
	}


	protected void takeDown() {
		
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	
		myGui.dispose();

	
		System.out.println("Seller-agent " + getAID().getName() + " terminating.");
	}

	
	public void updateCatalogue(final String title, final int price) {
		addBehaviour(new OneShotBehaviour() {
			
			private static final long serialVersionUID = 1L;

			public void action() {
				catalogue.put(title, new Integer(price));
				System.out.println(title + " inserted into catalogue. Price = " + price);
			}
		});
	}

	/**
	 * private class OfferRequestsServer.
	 */
	private class OfferRequestsServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.get(title);
				if (price != null) {
					// The requested book is available for sale. Reply with the
					// price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				} else {
					// The requested book is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End class OfferRequestsServer

	/* Inner class PurchaseOrdersServer.*/
	private class PurchaseOrdersServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.remove(title);
				if (price != null) {
					reply.setPerformative(ACLMessage.INFORM);
					System.out.println(title + " sold to agent " + msg.getSender().getName());
				} else {
					// The requested book has been sold to another buyer in the
					// meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			} else {
				block();
			}
		}
	} // End  OfferRequestsServer
}