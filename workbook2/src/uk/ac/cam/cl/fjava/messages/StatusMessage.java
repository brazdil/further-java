package uk.ac.cam.cl.fjava.messages;

import java.io.Serializable;

public class StatusMessage extends Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String message;

	public StatusMessage(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
