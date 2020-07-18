package me.Skimm.chatMod;

import org.bukkit.entity.Player;

import me.Skimm.chatCommands.*;

public class ModHandler {
	@SuppressWarnings("unused")
	private Main main;
	
	public ModHandler(Main plugin) {
		this.main = plugin;
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command = argv[0].toLowerCase();
		
		switch(command) {
		case "warn":
			break;
		case "list":
			break;
		case "info":
			break;
		case "mute":
			break;
		case "unmute":
			break;
		case "timeout":
			break;
		}
	}
}