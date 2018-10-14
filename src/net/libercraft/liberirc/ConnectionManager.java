package net.libercraft.liberirc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

	private BufferedReader reader;
	private BufferedWriter writer;
	
	private List<Channel> channels;
	
	public boolean connect(String nickname) {
		this.channels = new ArrayList<Channel>();
        
        // Connect to the IRC server.
        try {
            @SuppressWarnings("resource")
    		Socket socket = new Socket(LiberIRC.server, 6667);
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream( )));
	        reader = new BufferedReader(new InputStreamReader(socket.getInputStream( )));
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        // Log on to the server.
        write("NICK", nickname);
        write("USER", nickname + " 8 * : FlowerBot: developed in Java by Martyn Corsair");
        
        // Read lines from the server until it tells us we have connected.
        String line = null;
        try {
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("PING")) {
					String ping = line.split(" ", 2)[1];
					write("PONG", ping);
				}
			    if (line.indexOf("004") >= 0) {
			        break;
			    }
			    else if (line.indexOf("433") >= 0) {
			        System.out.println("Nickname is already in use.");
			        return false;
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        write("ns identify", "j8G4D6gk");
        getChannel(LiberIRC.channel);
		
		Thread thread = new Thread() {
			public void run() {
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						processLine(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
		return true;
	}
	
	public BufferedReader getReader() {
		return reader;
	}
	
	public void processLine(String line) {

		// Return server ping
		if (line.startsWith("PING")) {
			ping(line.split(" ", 2)[1]);
			return;
		}
		
		// Print and process line
        System.out.println(line);
        if (line.split(" ")[0].endsWith(LiberIRC.server)) {
        	// process server messages
        } else try {
        	LiberIRC.getMM().receiveLine(line);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
	}
	
	public List<String> whois(String username) {
		write("WHOIS", username);
		List<String> list = new ArrayList<String>();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("318") >= 0)
					break;
				list.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public boolean isOnline(String username) {
		write("ISON", username);
		boolean returnvalue = false;
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				if (line.indexOf("303") >= 0) {
					if (line.split(" ")[3].substring(1).equalsIgnoreCase(username))
						returnvalue = true;
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnvalue;
	}
	
	// Send command to server
	public void write(String command, String text) {
		System.out.println(">>> " + command + " " + text);
		try {
			writer.write(command + " " + text + "\r\n");
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Respond to server ping
	public void ping(String ping) {
		try {
			writer.write("PONG " + ping + "\r\n");
	        writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Leave the specified channel if connected to it
	public void leaveChannel(String channelname) {
		Channel channel = null;
		for (Channel chan:channels) 
			if (chan.getName().equals(channelname)) 
				channel = chan;
		if (channel == null)
			return;
		write("PART", channelname);
		channels.remove(channel);
	}
	
	// Load or connect to a specific channel
	public Channel getChannel(String channelname) {
		String name;
		if (channelname.startsWith("#"))
			name = channelname;
		else
			name = "#" + channelname;
		Channel channel = null;
		for (Channel chan:channels) {
			if (chan.getName().equals(name))
				channel = chan;
		}
		if (channel != null)
			return channel;
		else {
			channel = new Channel(name);
			channels.add(channel);
			return channel;
		}
	}
	
	// Load or connect to a specific channel with password 
	public Channel getChannel(String channelname, String password) {
		String name;
		if (channelname.startsWith("#"))
			name = channelname;
		else
			name = "#" + channelname;
		Channel channel = null;
		for (Channel chan:channels) {
			if (chan.getName().equals(name))
				channel = chan;
		}
		if (channel != null)
			return channel;
		else {
			channel = new Channel(name);
			channels.add(channel);
			return channel;
		}
	}
	
	public List<Channel> getChannels() {
		return channels;
	}
	
	public static class Channel {

		private String name;
		
		protected Channel(String name) {
			this.name = name;
			join();
		}
		
		public void join() {
			LiberIRC.getCNM().write("JOIN", name);
		}
		
		public void leave() {
			LiberIRC.getCNM().write("PART", name);
		}
		
		public String getName() {
			return name;
		}
	}
}
