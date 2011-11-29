package uk.ac.cam.db538.fjava.tick5;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.Message;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;

public class ClientHandler {
	private Socket socket;
	private MultiQueue<Message> multiQueue;
	private String nickname;
	private MessageQueue<Message> clientMessages;
	private Database database;

	public ClientHandler(Socket s, MultiQueue<Message> q, Database d) {
		socket = s;
		multiQueue = q;
		database = d;

		clientMessages = new SafeMessageQueue<Message>();
		multiQueue.register(clientMessages);

		// get last 10 messages
		try {
			List<RelayMessage> recent = database.getRecent();
			for (RelayMessage msg : recent)
				clientMessages.put(msg);
		} catch (SQLException ex) {
			ex.printStackTrace();
			System.err.println("Error while reading the database");
		}

		// increment number of logins
		try {
			database.increaseLogins();
		} catch (SQLException e) {
			e.printStackTrace();
			System.err.println("Error while updating the database");
		}

		nickname = "Anonymous" + (new Random()).nextInt(100000);
		multiQueue.put(new StatusMessage(nickname + " connected from "
				+ socket.getInetAddress().getHostName()));

		Thread handlerInput = new Thread() {
			@Override
			public void run() {
				super.run();

				try {
					ObjectInputStream stream = new ObjectInputStream(
							socket.getInputStream());
					while (!socket.isClosed()) {
						Object obj = stream.readObject();
						if (obj instanceof ChangeNickMessage) {
							ChangeNickMessage msg = (ChangeNickMessage) obj;
							multiQueue.put(new StatusMessage(nickname
									+ " is now known as " + msg.name));
							nickname = msg.name;
						} else if (obj instanceof ChatMessage) {
							ChatMessage msg = (ChatMessage) obj;
							RelayMessage msgR = new RelayMessage(nickname, msg);
							multiQueue.put(msgR);
							try {
								database.addMessage(msgR);
							} catch (SQLException e) {
								e.printStackTrace();
								System.err
										.println("Error while updating the database");
							}
						}
					}
				} catch (IOException e) {
					multiQueue.deregister(clientMessages);
					multiQueue.put(new StatusMessage(nickname
							+ " has disconnected"));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		};

		Thread handlerOutput = new Thread() {
			@Override
			public void run() {
				super.run();

				try {
					ObjectOutputStream stream = new ObjectOutputStream(
							socket.getOutputStream());
					while (!socket.isClosed()) {
						Message msg = clientMessages.take();
						stream.writeObject(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		handlerInput.setDaemon(true);
		handlerInput.start();

		handlerOutput.setDaemon(true);
		handlerOutput.start();
	}
}