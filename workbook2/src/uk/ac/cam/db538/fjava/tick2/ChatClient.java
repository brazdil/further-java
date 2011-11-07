package uk.ac.cam.db538.fjava.tick2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.ac.cam.cl.fjava.messages.ChangeNickMessage;
import uk.ac.cam.cl.fjava.messages.ChatMessage;
import uk.ac.cam.cl.fjava.messages.DynamicObjectInputStream;
import uk.ac.cam.cl.fjava.messages.Execute;
import uk.ac.cam.cl.fjava.messages.NewMessageType;
import uk.ac.cam.cl.fjava.messages.RelayMessage;
import uk.ac.cam.cl.fjava.messages.StatusMessage;
import uk.ac.cam.db538.fjava.tick2.FurtherJavaPreamble.Ticker;

@FurtherJavaPreamble(author = "David Brazdil", 
                     crsid = "db538", 
                     date = "07/11/2011", 
                     summary = "ChatClient from Workbook 2", 
                     ticker = Ticker.A)
public class ChatClient {
	static void print(Date when, String from, String what) {
		System.out.println(
			new SimpleDateFormat("HH:mm:ss").format(when) +
			" [" + from + "] " + what);
	}
	
	public static void main(String[] args) {
		String server = null;
		int port = 0;
		
		if (args.length != 2) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}
		
		server = args[0];
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			System.err.println("This application requires two arguments: <machine> <port>");
			return;
		}

		try {
			final Socket s = new Socket(server, port);
			print(new Date(), "Client", "Connected to " + server + " on port " + port + ".");
			
			Thread output = new Thread() {
				@Override
				public void run() {
					DynamicObjectInputStream in;
					try {
						in = new DynamicObjectInputStream(s.getInputStream());
					} catch (IOException ex) {
						print(new Date(), "Client", ex.getClass().getName() + ": " + ex.getMessage());
						return;
					}
					while(!s.isClosed()) {
						try {
							Object result = in.readObject();
							if (result instanceof StatusMessage) {
								StatusMessage msg = (StatusMessage) result;
								print(msg.getCreationTime(), "Server", msg.getMessage());
							} else if (result instanceof RelayMessage) {
								RelayMessage msg = (RelayMessage) result;
								print(msg.getCreationTime(), msg.getFrom(), msg.getMessage());
							} else if (result instanceof NewMessageType) {
								NewMessageType msg = (NewMessageType) result;
								in.addClass(msg.getName(), msg.getClassData());
								print(msg.getCreationTime(), "Client", "New class " + msg.getName() + " loaded.");
							} else {
								String text = result.getClass().getSimpleName() + ": ";
								Field[] fields = result.getClass().getDeclaredFields();
								for (int i = 0; i < fields.length; ++i) {
									try {
										fields[i].setAccessible(true);
										text += fields[i].getName() + "(" + fields[i].get(result).toString() + "), ";
									} catch (IllegalArgumentException e) {
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									}
								}
								if (text.endsWith(", "))
									text = text.substring(0, text.length() - 2);
								print(new Date(), "Client", text);
								
								Method[] methods = result.getClass().getDeclaredMethods();
								for (int i = 0; i < methods.length; ++i)
									if (methods[i].getParameterTypes().length == 0 && methods[i].isAnnotationPresent(Execute.class))
										try {
											methods[i].invoke(result);
										} catch (IllegalArgumentException e) {
										} catch (IllegalAccessException e) {
										} catch (InvocationTargetException e) {
										}
							}
						} catch (IOException ex) {
							// print(new Date(), "Client", ex.getClass().getName() + ": " + ex.getMessage());
						} catch (ClassNotFoundException e) {
							print(new Date(), "Client", "New message of unknown type received.");
						}
					}
				}
			};
			output.setDaemon(true);
			output.start();
			
			ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
			BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
			boolean go = true;
			while (go) {
				String line = r.readLine();
				if (line.startsWith("\\")) {
					// command mode
					String[] lineSplit = line.split(" ");
					String command = lineSplit[0].substring(1);
					if (command.equals("quit"))
						go = false;
					else if (command.equals("nick")) {
						if (lineSplit.length != 2)
							print(new Date(), "Client", "The nick command requires one argument - new nickname");
						else
							out.writeObject(new ChangeNickMessage(lineSplit[1]));
					} else
						print(new Date(), "Client", "Unknwown command \"" + command + "\"");
				} else
					out.writeObject(new ChatMessage(line));
			}
			
			s.close();
			print(new Date(), "Client", "Connection terminated.");
		} catch (NumberFormatException e) {
			System.err.println("Cannot connect to " + server + " on port " + port);
		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to " + server + " on port " + port);
		} catch (IOException e) {
			System.err.println("Cannot connect to " + server + " on port " + port);
		}
	}
}
