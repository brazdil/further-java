package uk.ac.cam.db538.fjava.tick3;

public class SafeMessageQueue<T> implements MessageQueue<T> {
	private static class Link<L> {
		L val;
		Link<L> next;
		Link(L val) { this.val = val; this.next = null; }
	}

	private Link<T> first = null;
	private Link<T> last = null;

	public synchronized void put(T val) {
		Link<T> newLink = new Link<T>(val);
		if (last != null)
			last.next = newLink;
		last = newLink;
		if (first == null)
			first = newLink;
		this.notify();
	}

	public synchronized T take() {
		while(first == null) //use a loop to block thread until data is available
			try { this.wait(); } catch(InterruptedException ie) {}
		Link<T> firstLink = first;
		first = firstLink.next;
		return firstLink.val;
	}
}
