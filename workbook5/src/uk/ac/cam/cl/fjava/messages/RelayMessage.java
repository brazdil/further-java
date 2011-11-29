package uk.ac.cam.cl.fjava.messages;

import java.io.Serializable;
import java.util.Date;

public class RelayMessage extends Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String from;
	private String message;
	
	public RelayMessage(String from, ChatMessage original) {
		super(original);
		this.from = from;
		this.message = original.getMessage();
	}

	public RelayMessage(String from, String message, Date time) {
		super(time);
		this.from = from;
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}
}
