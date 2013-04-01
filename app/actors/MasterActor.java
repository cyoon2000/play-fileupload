package actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import dto.CountResult;

import java.lang.System;
import java.util.Map;

public class MasterActor extends UntypedActor {

	private ActorRef aggregateActor = getContext().actorOf(
			new Props(AggregateActor.class), "aggregate");

	private ActorRef reduceActor = getContext().actorOf(
			new Props(new UntypedActorFactory() {
				public UntypedActor create() {
					return new ReduceActor(aggregateActor);
				}
			}), "reduce");

	private ActorRef mapActor = getContext().actorOf(
			new Props(new UntypedActorFactory() {
				public UntypedActor create() {
					return new MapActor(reduceActor);
				}
			}), "map");

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof String) {
			mapActor.tell(message);
		} else if (message instanceof CountResult) {
            CountResult countResult = (CountResult)message;
            if ( countResult.getFinalResultMap().size() == 0 ) {
                // tell aggregateActor to give me the result
                aggregateActor.tell(message);
            } else {
                // got the result
                getContext().parent().tell(message);
            }

		} else if (message instanceof Map) {
            // tell the result back to parent
            getContext().parent().tell(message);
        } else
			unhandled(message);
	}
}
