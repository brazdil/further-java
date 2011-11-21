package uk.ac.cam.db538.fjava.tick4;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

	public ClientHandler(Socket s, MultiQueue<Message> q) {
		socket = s;
		multiQueue = q;

		clientMessages = new SafeMessageQueue<Message>();
		multiQueue.register(clientMessages);

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
							multiQueue.put(new RelayMessage(nickname, msg));
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