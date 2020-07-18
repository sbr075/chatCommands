package me.Skimm.chatBroadcast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.Skimm.chatCommands.*;

public class BroadcastHandler {
	private Main plugin;
	private BroadcastScheduler broadcastScheduler;
	
	SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	Date newTime = null, endTime = null;
	String msg = "";
	
	public BroadcastHandler(Main plugin) {
		this.plugin = plugin;
	}

	public void commandHandler(Player player, String label, String[] argv) {
		// Save command to string
		String command = argv[0].toLowerCase();
		
		broadcastScheduler = new BroadcastScheduler(this.plugin, player, argv);
		
		switch(command) {
		case "send":
			// /broadcast send <message>
			if (argv.length >= 2) {
				for (int i = 1; i < argv.length; i++)
					msg += argv[i] + " ";

				Bukkit.broadcastMessage(ChatColor.RED + "[BROADCAST] " + ChatColor.WHITE + msg);
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		case "add": // /broadcast add <name> <interval> <runtime> <message>
			if (plugin.config.getConfig().getInt("general.broadcasts.current_broadcasts") >= plugin.config.getConfig().getInt("general.broadcasts.max_broadcasts")) {
				player.sendMessage(ChatColor.RED + "[LIMIT REACHED]" + ChatColor.WHITE + " Can't add more broadcasts, try removing some existing broadcasts");
				break;
			}
			
			/*
			 * Conversation would be good to have
			 * Question 1. State the name of the broadcast (this is not the message itself): 
			 * Question 2. Would you like for the message to loop forever? (YES/NO)
			 * If yes,
			 *   Optional question. For how long? (I.e <num>s, <num>m or <num>h):
			 * Question 4. Type in the message you would like broadcasted: 
			 */
			if (argv.length >= 4) {
				// Check if broadcast already exists
				if (plugin.config.getConfig().contains("general.broadcasts." + argv[1])) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Broadcast '" + argv[1] + "' already exists!");
					return;
				}
				
				// Calculate specified delay and check it against minimum delay allowed
				int set_delay, min_delay = plugin.config.getConfig().getInt("general.broadcasts.min_delay");
				set_delay = broadcastScheduler.convertFromTimeformat(player, argv[2], 20);
				
				if (set_delay < min_delay) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Interval can't be smaller than " + (min_delay / 20) + " seconds");
					return;
				}

				// Create task
				@SuppressWarnings("unused")
				BukkitTask task = broadcastScheduler.runTaskTimer(this.plugin, 0, set_delay);
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		case "edit": // /broadcast edit <name> <add runtime/message> <new value>
			if (argv.length >= 4 && argv[2].toLowerCase().equalsIgnoreCase("message")) {
				// Check if exists
				if (!plugin.config.getConfig().contains("general.broadcasts." + argv[1])) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Broadcast '" + argv[1] + "' doesn't exist");
					break;
				}
				
				// Save message
				String msg = "";
				for (int i = 3; i < argv.length; i++) {
					msg += argv[i] + " ";
				}
				
				// Update listing
				ConfigurationSection updateBroadcast = plugin.config.getConfig().getConfigurationSection("general.broadcasts." + argv[1]);
				updateBroadcast.set("msg", msg);
				plugin.config.saveConfig();
			}
			
			else if (argv.length == 5 && argv[2].toLowerCase().equalsIgnoreCase("add") && argv[3].toLowerCase().equalsIgnoreCase("runtime")) {
				// Check if listing exists
				if (!plugin.config.getConfig().contains("general.broadcasts." + argv[1])) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Broadcast '" + argv[1] + "' doesn't exist");
					break;
				}
				
				// Calculation extra duration
				int duration;
				duration = broadcastScheduler.convertFromTimeformat(player, argv[4], 1000);
				if (duration == -1) {
					return;
				}
				
				// Get current endtime
				try {
					endTime = format.parse(plugin.config.getConfig().getString("general.broadcasts." + argv[1] + ".end_time"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				// Makes sure newTime can't be negative
				if ((endTime.getTime() + duration) <= 0) {
					newTime = new Date();
				}
				else {
					newTime = new Date(endTime.getTime() + duration);
				}
				
				// Update listing
				ConfigurationSection updateBroadcast = plugin.config.getConfig().getConfigurationSection("general.broadcasts." + argv[1]);
				updateBroadcast.set("end_time", format.format(newTime));
				plugin.config.saveConfig();
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		case "remove": // /broadcast remove <name>
			if (argv.length == 2) {
				broadcastScheduler.removeListing(argv[1]);
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		case "list":  // /broadcast list
			if (argv.length == 1) {
				player.sendMessage(ChatColor.DARK_AQUA + "Broadcast list");
				for (String key1 : plugin.config.getConfig().getConfigurationSection("general.broadcasts").getKeys(false)) {
					if (key1.equalsIgnoreCase("min_delay") || key1.equalsIgnoreCase("max_broadcasts") || key1.equalsIgnoreCase("current_broadcasts"))
						continue;

					player.sendMessage(ChatColor.AQUA + "- " + key1);
				}
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		case "info":
			if (argv.length == 2) {
				// Check if listing exists
				if (!plugin.config.getConfig().contains("general.broadcasts." + argv[1])) {
					player.sendMessage("Broadcast '" + argv[1] + "' doesn't exist");
					return;
				}
				
				ConfigurationSection info = plugin.config.getConfig().getConfigurationSection("general.broadcasts." + argv[1]);
				String creator, msg;
				
				// Fetch information
				try {
					newTime = format.parse(info.getString("start_time"));
					endTime = format.parse(info.getString("end_time"));
				} catch (ParseException e) {
					e.printStackTrace();
					break;
				}
				
				msg = info.getString("msg");
				creator = info.getString("creator.display_name");
				
				// Send information to player
				player.sendMessage(ChatColor.DARK_GREEN + "Broadcast '" + argv[1] + "' information:");
				player.sendMessage(ChatColor.GREEN + "Creator: " + ChatColor.WHITE + creator);
				player.sendMessage(ChatColor.GREEN + "Message: " + ChatColor.WHITE + msg);
				player.sendMessage(ChatColor.GREEN + "Start time: " + ChatColor.WHITE + format.format(newTime));
				player.sendMessage(ChatColor.GREEN + "End time: " + ChatColor.WHITE + format.format(endTime));
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
    		}
			break;
			
		default:
			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /broadcast help for more information");
			break;
		}
	}
}