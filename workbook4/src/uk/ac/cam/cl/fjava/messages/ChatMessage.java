package uk.ac.cam.cl.fjava.messages;

import java.io.Serializable;

/**
 * Message sent from the client to the server
 *
 */
public class ChatMessage extends Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String message;

	public ChatMessage(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
