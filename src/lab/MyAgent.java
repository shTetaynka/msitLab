package lab;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

public class MyAgent extends Agent {
	protected void setup() {
		System.out.println("Adding waker behaviour");
		addBehaviour(new WakerBehaviour(this, 10000) {
			protected void handleElapsedTimeout() {
				// perform operation X
			}
		});
	}
}

/*public class MyAgent extends Agent {
	protected void setup() {
	 addBehaviour(new TickerBehaviour(this, 10000) {
	 protected void onTick() {
	 // perform operation Y
	 }
	 } );
	 }
}*/
