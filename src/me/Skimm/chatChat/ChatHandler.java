package me.Skimm.chatChat;

import org.bukkit.entity.Player;

import me.Skimm.chatCommands.*;

public class ChatHandler {
	private Main main;
	
	public ChatHandler(Main plugin) {
		this.main = plugin;
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command = argv[0].toLowerCase();
		
		switch(command) {
		case "mode":
			break;
		case "list":
			break;
		case "create":
			break;
		case "invite":
			break;
		case "remove":
			break;
		case "leave":
			break;
		case "disband":
			break;
		case "role":
			break;
		}
	}
}