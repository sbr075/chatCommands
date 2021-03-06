package me.Skimm.chatTitles;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.Skimm.chatCommands.*;

public class TitleHandler {
	private Main plugin;
	
	ConfigurationSection title;
	String option, name, value, perm;
	Player receiver = null;
	
	public TitleHandler(Main plugin) {
		this.plugin = plugin;
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command = argv[0].toLowerCase();
		
		switch(command) {
		case "add":
			if (argv.length == 2) { // /title add <name>, add title
				// Check file if title already exists
				if (plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").contains(argv[1]) ) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + argv[1] + "' already exists");
					break;
				}
				
				// Open file
				plugin.permissions.getConfig().createSection("permissions.titles." + argv[1]);
				title = plugin.permissions.getConfig().createSection("permissions.titles.names." + argv[1]);
				title.set("color", "&f");
				title.set("uses", "0");
				plugin.permissions.saveConfig();
				
				player.sendMessage(ChatColor.GREEN + "[SUCCESS]:" + ChatColor.WHITE + " Title '" + argv[1] + "' created");
				player.sendMessage(ChatColor.GRAY + "NB!" + ChatColor.WHITE + " Remember to edit permissions and optional parent title");
			}
			else if (argv.length == 4) { //title add <perm/parent/color> <name> <perm/parent title/color code>, add permission to title
				option = argv[1].toLowerCase();
				name = argv[2].toLowerCase();
				value = argv[3].toLowerCase();
				
				// Check file if title exists at all
				if (!plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").contains(name) ) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + name + "' doesn't exist");
					break;
				}
				
				switch(option) {
				case "perm":
					// Split perm name into tokens
					// token[0] = label
					// token[1] = function name
					String[] tokens = value.split("\\.");
					
					// Check if permission exists
					if (!plugin.permissions.getConfig().getStringList("permissions." + tokens[0] + ".names").contains(value)) {
						player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Permission '" + value + "' doesn't exist");
						break;
					}
					
					// Check if section exists
					if (!plugin.permissions.getConfig().contains("permissions.titles." + name + ".perms")) {
						plugin.permissions.getConfig().createSection("permissions.titles." + name + ".perms");
					}
					
					// Open file, set in new value and save
					title = plugin.permissions.getConfig().getConfigurationSection("permissions.titles." + name);
					
					// Get already existing permissions
					List<String> perms = title.getStringList("perms");
					
					if (perms.contains(value)) {
						player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Title '" + name + "' already has permissions '" + value + "'");
						return;
					}
					
					perms.add(value);
					
					// Write over list with new modified list and save
					title.set("perms", perms);
					plugin.permissions.saveConfig();
					break;

				case "parent":
					// Check if parent exists
					if (!plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").contains(value)) {
						player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + value + "' doesn't exist");
						break;
					}
					
					// Check if section exists
					if (!plugin.permissions.getConfig().contains("permissions.titles." + name + ".parent")) {
						plugin.permissions.getConfig().createSection("permissions.titles." + name + ".parent");
					}
					
					List<String> parents = plugin.permissions.getConfig().getStringList("permissions.titles." + name + ".parent");
					parents.add(value);
					
					
					// Open file, set in new value and save
					title = plugin.permissions.getConfig().getConfigurationSection("permissions.titles." + name);
					title.set("parent", parents);
					plugin.permissions.saveConfig();
					break;
					
				case "color":
					// Check if color is valid
					// NEED THIS CHECK
					// [A-Z][a-zA-Z]*,\\s[A-Z][a-zA-Z]*"
					if (!value.matches("^[&][A-Fa-f0-9]+$") || value.length() != 2) {
						player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Invalid color option");
						return;
					}
					
					plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names." + name).set("color", value);
					plugin.permissions.saveConfig();

					// Change display name for all active title users to match new color
					for (String uuid : plugin.permissions.getConfig().getConfigurationSection("players").getKeys(false)) {
						title = plugin.playerInfo.getConfig().getConfigurationSection("players." + uuid);
						if (title.getString("title").equalsIgnoreCase(name)) {
							receiver = Bukkit.getPlayer(UUID.fromString(uuid));
							plugin.updateDisplayName(receiver, 0, name);
						}
					}
					break;
					
				default:
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Option + '" + option + "' is invalid");
				}
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /title help for more information");
    		}
			break;
		case "default":
			name = argv[1].toLowerCase();
			
			// If title doesn't exists
			if (!plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").contains(name) ) {
				player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + name + "' doesn't exist");
				break;
			}
			
			title = plugin.permissions.getConfig().getConfigurationSection("permissions.titles");
			title.set("default", name);
			plugin.permissions.saveConfig();
			break;
			
		case "remove":
			name = argv[1].toLowerCase();
			
			// If title doesn't exists
			if (!plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").contains(name) ) {
				player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + name + "' doesn't exist");
				break;
			}

			if (argv.length == 2) { // /title remove <name>, remove title
				// If title is default title
				if (plugin.permissions.getConfig().getString("permissions.titles.default").equalsIgnoreCase(name)) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + name + "' is default title and can't be removed. Change default before removing");
				}
				
				// Set title users title to specified default title
				String[] args = new String[3];
				args[0] = "give";
				
				for (String uuid : plugin.permissions.getConfig().getConfigurationSection("players").getKeys(false)) {
					title = plugin.playerInfo.getConfig().getConfigurationSection("players." + uuid);
					if (title.getString("title").equalsIgnoreCase(name)) {

						receiver =  Bukkit.getPlayer(UUID.fromString(uuid));
						args[1] = player.getDisplayName().substring(name.length() + 7, receiver.getDisplayName().length());
						player.sendMessage("Changing title for player: " + args[1]);
						args[2] = plugin.permissions.getConfig().getString("permissions.titles.default");
						commandHandler(player, "give", args);
						
						title.set("title", plugin.permissions.getConfig().getString("permissions.titles.default"));
					}
				}
				// Set title to null in file (this deletes it) and save				
				plugin.permissions.getConfig().getConfigurationSection("permissions.titles").set(name, null);
				plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").set(name, null);
				plugin.permissions.saveConfig();
				plugin.playerInfo.saveConfig();
			}
			else if (argv.length == 3) { // title remove <name> <perm>, remove permission from title
				perm = argv[2];

				// Open file, set in new value and save
				title = plugin.permissions.getConfig().getConfigurationSection("permissions.titles." + name);
				
				// Get already existing permissions
				List<String> perms = title.getStringList("perms");
				if (!perms.contains(perm)) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + name + "' doesn't have permission '" + perm + "'");
					break;
				}
				
				// Remove permission from list
				perms.remove(perm);
				
				// Write over list with new modified list and save
				title.set("perms", perms);
				plugin.permissions.saveConfig();
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /title help for more information");
    		}
			break;
			
		case "give":
			if (argv.length == 3) { // /title give <player> <title>, give player a title
				String curTitle;
				name = argv[2].toLowerCase();
				
		    	if (argv.length >= 3) {
		    		try {
		    			receiver = player.getServer().getPlayer(argv[1]);
		    		}
		    		catch (Exception e) {
		    			player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Can't find player '" + argv[1] + "'");
		    		}
		    	}
				
				// Check file if title exists
				if (!plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").contains(name) ) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + name + "' doesn't exist");
					break;
				}
				
				
				curTitle = plugin.playerInfo.getConfig().getString("players." + receiver.getUniqueId() + ".title");
				if (curTitle.equalsIgnoreCase(name)) {
					player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Player '" + player.getName() + "' already has the speicifed title");
					return;
				}
				
				plugin.updateDisplayName(receiver, 0, name);
				
				plugin.permissions.getConfig().set("permissions.titles.names." + curTitle + ".uses", plugin.permissions.getConfig().getInt("permissions.titles.names." + curTitle + ".uses") - 1);
				plugin.permissions.getConfig().set("permissions.titles.names." + name + ".uses", plugin.permissions.getConfig().getInt("permissions.titles.names." + name + ".uses") + 1);
				plugin.permissions.saveConfig();
				
				plugin.playerInfo.getConfig().getConfigurationSection("players." + receiver.getUniqueId()).set("title", name);
				plugin.playerInfo.saveConfig();
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /title help for more information");
    		}
			break;
		
		case "list":
			if (argv.length == 1) { // /title list, list all titles
				// Iterate through whole title list and send them to player
				player.sendMessage(ChatColor.DARK_AQUA + "Title list");
				for (String title : plugin.permissions.getConfig().getConfigurationSection("permissions.titles.names").getKeys(false)) {
					player.sendMessage(ChatColor.AQUA + "- " + title);
				}
			}
			else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /title help for more information");
    		}
			break;
			
		case "info":
			if (argv.length == 2) { // /title info <player> or /title info <title>, check player titles or title info
				/*
				 *  Check if title exists
				 *  If not, check if player exists
				 *  If not return error
				 */

				// Optional arguments is invalid
				if (!plugin.permissions.getConfig().contains("permissions.titles.names." +  argv[1]) && player.getServer().getPlayer(argv[1]) == null) {
					player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Invalid argument. Argument has to be either title name or player");
					return;
				} // Title given as argument
				else if (player.getServer().getPlayer(argv[1]) == null) {
					player.sendMessage(ChatColor.DARK_AQUA + "Title '" + argv[1] + "' information");
					player.sendMessage(ChatColor.AQUA + "Color: " + plugin.permissions.getConfig().getString("permissions.titles.names." + argv[1] + ".color"));
					player.sendMessage(ChatColor.AQUA + "Parent(s): " + plugin.permissions.getConfig().getString("permissions.titles." + argv[1] + ".parent"));
					player.sendMessage(ChatColor.AQUA + "Used by: " + plugin.permissions.getConfig().getString("permissions.titles.names." + argv[1] + ".uses") + " players");
					player.sendMessage(ChatColor.AQUA + "Permissions");
					
					// Get list of title permissions
					List<String> perms = plugin.permissions.getConfig().getStringList("permissions.titles." + argv[1] + ".perms");
					for (String perm : perms)
						player.sendMessage(ChatColor.AQUA + "- " + perm);
					
				} // Player given argument
				else {
					// Get receiver
					receiver = player.getServer().getPlayer(argv[1]);
					player.sendMessage("Player '" + receiver.getDisplayName() + "' has title: " + plugin.playerInfo.getConfig().getString("players." + receiver.getUniqueId() + ".title"));
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /title help for more information");
			}
			break;
		
		default:
			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /title help for more information");
			break;
		}
	}
}