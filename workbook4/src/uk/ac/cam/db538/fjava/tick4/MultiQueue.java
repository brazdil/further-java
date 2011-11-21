package uk.ac.cam.db538.fjava.tick4;

import java.util.HashSet;
import java.util.Set;

public class MultiQueue<T> {
	private Set<MessageQueue<T>> outputs = new HashSet<MessageQueue<T>>();

	public void register(MessageQueue<T> q) {
		// add q to outputs
		outputs.add(q);
	}

	public void deregister(MessageQueue<T> q) {
		// remove q from outputs
		outputs.remove(q);
	}

	public void put(T message) {
		// copy "message" to all elements in "outputs"
		for (MessageQueue<T> output : outputs) {
			output.put(message);
		}
	}
}