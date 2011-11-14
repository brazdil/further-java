package uk.ac.cam.db538.fjava.tick3;

public interface MessageQueue<T> {
	public void put(T msg);
	public T take();
}
