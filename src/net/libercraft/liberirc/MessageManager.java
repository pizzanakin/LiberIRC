package net.libercraft.liberirc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MessageManager implements Listener {
	
	public void receiveLine(String line) {
		String[] args = line.split(" ", 4);
		
		if (!args[0].contains("!"))
			return;
		
		String sender = args[0].substring(1,args[0].indexOf('!'));
		if (sender.equals(LiberIRC.nickname))
			return;
		
		// Handle join message
		if (args[1].equals("JOIN"))
			sendIRCMessage(sender + " has joined on IRC!");
		else if (args[1].equals("PART"))
			sendIRCMessage(sender + " has left on IRC!");

		// Handle incoming messages
		if (!args[1].equals("PRIVMSG"))
			return;
		
		String channelName = args[2];
		String transcript = args[3].substring(1);
		if (channelName.equals(LiberIRC.nickname))
			return;
		else
			onIRCMessage(sender, channelName, transcript);
	}
	 
	@EventHandler
	public void onMCJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		 
		sendIRCMessage("["+player.getName()+" joined the game]");
	}
	 
	@EventHandler
	public void onMCLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		 
		sendIRCMessage("["+player.getName()+" left the game]");
	}
	
	private void onIRCMessage(String sender, String channelName, String transcript) {
		// Show message in minecraft chat
		iRCtoMC(sender + ": " + transcript);
	}
	
	@EventHandler
    public void onMCMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        // Show message in irc chat
        sendIRCMessage("<"+player.getName()+"> " + event.getMessage());
    }
	
	// Send message
	private void sendIRCMessage(String transcript) {
		LiberIRC.getCNM().write("PRIVMSG", LiberIRC.channel + " " + transcript);
	}
	
	private void iRCtoMC(String message) {
		Bukkit.broadcastMessage("[IRC] "+message);
	}
}

