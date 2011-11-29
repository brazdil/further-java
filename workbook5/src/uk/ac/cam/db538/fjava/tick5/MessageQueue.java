package uk.ac.cam.db538.fjava.tick5;

public interface MessageQueue<T> {
	public void put(T msg);
	public T take();
}
