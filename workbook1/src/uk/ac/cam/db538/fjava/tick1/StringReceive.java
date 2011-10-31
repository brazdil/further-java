package uk.ac.cam.db538.fjava.tick1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class StringReceive {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		
		String machine = args[0];
		int port = 0;
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		
		try {
			Socket socket = new Socket(machine, port);
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(true) {
				System.out.println(reader.readLine());
			}
		} catch (NumberFormatException e) {
			System.err.println("Cannot connect to " + machine + " on port " + port);
		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to " + machine + " on port " + port);
		} catch (IOException e) {
			System.err.println("Cannot connect to " + machine + " on port " + port);
		}
	}
}
