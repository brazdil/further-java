package uk.ac.cam.db538.fjava.tick4;

public interface MessageQueue<T> {
	public void put(T msg);
	public T take();
}
