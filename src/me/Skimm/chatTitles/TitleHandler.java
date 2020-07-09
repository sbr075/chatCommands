package me.Skimm.chatTitles;

import org.bukkit.entity.Player;

import me.Skimm.chatCommands.*;

public class TitleHandler {
	private Main main;
	
	public TitleHandler(Main plugin) {
		this.main = plugin;
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command = argv[0].toLowerCase();
		
		switch(command) {
		case "give":
			break;
		case "remove":
			break;
		case "list":
			break;
		case "info":
			break;
		default:
			
		}
	}
}