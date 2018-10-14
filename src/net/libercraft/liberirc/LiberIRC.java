package net.libercraft.liberirc;

import org.bukkit.ChatColor;

import net.libercraft.libercore.interfaces.Module;

public class LiberIRC extends Module {
	private static LiberIRC instance;

	// TODO make configurable
	public static String server = "libercraft.net";
	public static String nickname = "flowerbot";
	public static String alt = "flowerbotalt";
	public static String channel = "#test";
	private ConnectionManager cnm;
	private MessageManager mm;

	@Override
	public ChatColor colour() {
		return null;
	}

	@Override
	public void onActivate() {

		instance = this;
		cnm = new ConnectionManager();
		mm = new MessageManager();
		
		boolean result = cnm.connect(nickname);
		if (!result) {
			nickname = alt;
			cnm.connect(nickname);
		}
		
		this.getServer().getPluginManager().registerEvents(mm, this);
	}

	@Override
	public void onClose() {
	}
	
	public static LiberIRC get() {
		return instance;
	}
	
	public static ConnectionManager getCNM() {
		return instance.cnm;
	}
	
	public static MessageManager getMM() {
		return instance.mm;
	}
}
