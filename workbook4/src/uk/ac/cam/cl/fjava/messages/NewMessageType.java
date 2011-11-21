package uk.ac.cam.cl.fjava.messages;


public class NewMessageType extends Message {

	private static final long serialVersionUID = 1L;
	private String name;
	private byte[] classData;

	public NewMessageType(String name, byte[] classData) {
		super();
		this.name = name;
		this.classData = classData;
	}

	public String getName() {
		return name;
	}

	public byte[] getClassData() {
		return classData;
	}

}
