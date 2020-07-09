package me.Skimm.chatBroadcast;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.Skimm.chatCommands.*;

public class BroadcastHandler {
	private Main main;
	
	public BroadcastHandler(Main plugin) {
		this.main = plugin;
	}

	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command = argv[0].toLowerCase();
		
		switch(command) {
		case "send":
			// /broadcast send <message>
			if (argv.length >= 2) {
				String msg = "";
				for (String word : argv)
					msg += word + " ";
				
				Bukkit.broadcastMessage(msg);
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
			break;
			
		case "add":
			// /broadcast add <name> <interval> <runtime> <message>
			/*
			 * Conversation would be good to have
			 * Question 1. State the name of the broadcast (this is not the message itself): 
			 * Question 2. Would you like for the message to loop forever? (YES/NO)
			 * If yes,
			 *   Optional question. For how long? (I.e <num>s, <num>m or <num>h):
			 * Question 4. Type in the message you would like broadcasted: 
			 */
			if (argv.length >= 4) {
				if (main.broadcast.getConfig().contains("broadcasts." + argv[1])) {
					player.sendMessage("Broadcast '" + argv[1] + "' already exists!");
					return;
				}
				
				int set_delay, min_delay = main.broadcast.getConfig().getInt("broadcasts.min_delay");
				try {
					set_delay = Integer.parseInt(argv[2].substring(0, argv[2].length() - 1));
				}
				catch (NumberFormatException e) {
					player.sendMessage(ChatColor.RED + "Unknown time format: " + argv[2]);
					break;
				}

				switch(argv[2].substring(argv[2].length() - 1)) {
				case "s":
					set_delay *= 20;
					break;
				case "m":
					set_delay *= 60 * 20;
					break;
				case "h":
					set_delay *= 60 * 60 * 20;
					break;
				default:
					player.sendMessage(ChatColor.RED + "Unknown time format: " + argv[2]);
					return;
				}
				
				if (set_delay < min_delay) {
					player.sendMessage(ChatColor.RED + "Interval can't be smaller than " + (min_delay / 20) + " seconds");
					return;
				}

				BukkitTask task = new BroadcastScheduler(this.main, player, argv).runTaskTimer(this.main, 50, set_delay);
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		case "edit":
			break;
			
		case "remove":
			break;
			
		case "list":
			break;
			
		case "info":
			break;
			
		default:
			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
			break;
		}
	}
}