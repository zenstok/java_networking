package clase;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements AutoCloseable {
	
	private ServerSocket socket;
	private ExecutorService executor;
	public List<NewsChannel> Channels = new ArrayList<NewsChannel>();
	private final static String[] BadMouth = {"muie", "cur", "pula", "pizda"};

	public Server(int port) throws IOException {
		socket = new ServerSocket(port);
		NewsChannel nc = new NewsChannel("Default-channel", "The owner of this channel is his Majesty the server himself! Unfortunately he doesn't have any power here.", null);
		Channels.add(nc);
		executor = Executors.newFixedThreadPool(50 * Runtime.getRuntime().availableProcessors());
		executor.execute(() -> {
			while (!socket.isClosed()) {
				try {
					Client client = new Client(socket.accept());
					sendChannelList(client);
					executor.submit(() -> {
						while (!client.isClosed()) {
							try {
								String message = client.receive();
								
								String[] ms = message.split(":");
								if (ms[0].equals("broadcast")) broadcastMessage(ms[1]);
								
								String[] space = message.split(" ");
								if(space.length == 2) {
									switch(space[0]) {
									case "publish": Channels.add(new NewsChannel(space[1],null,client)); notify("publish", space[1], client); break; 
									case "delete": deleteChannel(space[1], client); break; 
									case "subscribe": subscribeToChannel(space[1],client); break;
									case "unsubscribe": unsubscribeFromChannel(space[1], client); break;  
									}
								}
//								int startIndex = message.indexOf('"') + 1;
//								int endIndex = message.indexOf('"', startIndex);
//								String quoteMessage = message.substring(startIndex, endIndex);
//								if(space.length >= 3) {
//									if(space[0].equals("publish")) {
//										Channels.add(new NewsChannel(space[1],quoteMessage,client));
//										notify("publish", space[1], client);
//									}
//									if(space[0].equals("news")) sendNewsMessage(space[1],quoteMessage, client);
//								}
								if(space.length >= 3) {
									int begin = space[0].length() + space[1].length() + 2;
								if(space[0].equals("publish")) {
									Channels.add(new NewsChannel(space[1],message.substring(begin),client));
									notify("publish", space[1], client);
								}
								if(space[0].equals("news")) sendNewsMessage(space[1],message.substring(begin), client);
							}
								
								
								
								switch (message) {
								case "list channels": sendChannelList(client); break;
								case "/help": sendHelp(client); break;  
								case "close": handleClose(client); break;
								}
							} catch (Exception e) {
							}
						}
						try {
							client.close();
						} catch (Exception e) {
						}
					});
				} catch (Exception e) {
				}
			}
		});
	}

	private void subscribeToChannel(String chName, Client client) {
		boolean Exists = false;
		for (NewsChannel ch: Channels) {
			if(ch.getName().equals(chName)) {
				Exists = true;
				ch.getSubscribers().add(client);
				client.send("Successfully subscribed to channel " + chName);
			}
		}
		
		if(!Exists)
			client.send("There is no channel with name " + chName);
			
	}
	
	private void unsubscribeFromChannel(String chName, Client client) {
		boolean Exists = false;
		for (NewsChannel ch: Channels) {
			if(ch.getName().equals(chName)) {
				Exists = true;
				ch.getSubscribers().remove(client);
				client.send("Successfully unsubscribed from channel " + chName);
			}
		}
		
		if(!Exists)
			client.send("There is no channel with name " + chName);
		
	}

	private void deleteChannel(String chName, Client client) {
		boolean Exists = false;
		for (NewsChannel ch: Channels) {
			if(ch.getName().equals(chName)) {
				Exists = true;
				if(ch.getChannelOwner() == client) {
					Channels.remove(ch);
					client.send("Successfully deleted channel " + chName);
					notify("delete", chName, client);
				} else {
					client.send("Permission denied! You are not the owner of this channel.");
				}
			}
		}
		
		if(!Exists)
			client.send("There is no channel with name " + chName);
		
	}

	private void sendNewsMessage(String chName, String message, Client client) {

	  if(!isOffensive(message)) {
		boolean Exists = false;
		for (NewsChannel ch: Channels) {
			if(ch.getName().equals(chName)) {
				Exists = true;
				if(ch.getChannelOwner() == client) {
					for(Client c: Client.getClients()) {
						if(ch.getSubscribers().contains(c))
								c.send(message);
					}
				} else {
					client.send("Permission denied! You are not the owner of " + chName);
				}
			}
		}
		
		if(!Exists)
			client.send("There is no channel with name " + chName);
		
	  }
		
	}
	
	private void sendChannelList(Client client) {
		client.send("To subscribe to a channel type: subscribe [channel_name]\n");
		client.send("---------------------- AVAILABLE CHANNELS -----------------");
		for(NewsChannel ch: Channels) {
			client.send(ch.getName());
			if(ch.getDescription() != null)
				client.send(ch.getDescription());
			client.send("Number of subscribers: " + ch.getSubscribers().size());
			client.send(System.lineSeparator());
		}
		client.send("Type /help for help");
	}

	private void sendHelp(Client client) {
		client.send("\nlist channels -> Lists all the available channels"
				  + "\nclose -> Closes your session"
				  + "\npublish [channel_name] " +'"'+"[channel_description --optional]" + '"'+ " -> publishes a new channel"
				  + "\ndelete [channel_name] -> deletes a channel"
				  + "\nsubscribe [channel_name] -> Subscribes to a channel"
				  + "\nunsubscribe [channel_name] ->Unsubscribes from a channel"
				  + "\nnews [channel_name] " +'"'+"[your message]" + '"'+ " -> Broadcasts news to a channel");
		
	}

	private void handleClose(Client client) throws Exception {
		client.close();
	}

	@Override
	public void close() throws Exception {
		if (!executor.isTerminated()) {
			executor.shutdown();
		}
		if (!socket.isClosed()) {
			socket.close();
		}
	}

	private void broadcastMessage(String message) {
		for (Client c : Client.getClients()) 
			if(!isOffensive(message))
				c.send(message);
			else
				c.send("******Cenzurat******");	
	}
	
	private boolean isOffensive(String message) {
		String msg = message.toLowerCase();
		for(String badWord: BadMouth) 
			if(msg.indexOf(badWord) != -1) 
				return true;
		return false;
		
	}
	
	private void notify(String verb, String chName, Client client) {
		String msgContent = null;
		if(verb.equals("publish"))
			msgContent = "A new channel has been published: "+chName;
		if(verb.equals("delete"))
			msgContent = chName+" channel has been deleted";
		for(Client c : Client.getClients()) {
			if(c != client)
				c.send(msgContent);
		}
	}
		 	

}
