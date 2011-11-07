package uk.ac.cam.cl.fjava.messages;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private Date creationTime;
	
	public Message() {
		creationTime = new Date();
	}
	
	protected Message(Message copy) {
		creationTime = copy.creationTime;
	}

	protected Message(Date time) {
		creationTime = time;
	}

	public Date getCreationTime() {
		return creationTime;
	}	
}
