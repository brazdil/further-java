package uk.ac.cam.db538.fjava.tick2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class TestMessageReadWrite {
	static boolean writeMessage(String message, String filename) {
		TestMessage msgObject = new TestMessage();
		msgObject.setMessage(message);
		
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(msgObject);
			out.close();
		} catch (FileNotFoundException ex) {
			return false;
		} catch (IOException e) {
			return false;
		}

		return true;		
	}
	
	static String readMessage(String location) {
		try {
			InputStream stream = null;
			if (location.startsWith("http://"))
				stream = new URL(location).openConnection().getInputStream();
			else
				stream = new FileInputStream(new File(location));
			
			ObjectInputStream in = new ObjectInputStream(stream);
			Object result = in.readObject();
			if (result instanceof TestMessage) 
				return ((TestMessage) result).getMessage();
			return null;
		} catch (FileNotFoundException ex) {
			return null;
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(readMessage(args[0]));
		writeMessage("You are a piece of shit!!!", "test.jobj");
		System.out.println(readMessage("test.jobj"));
	}
}
