package me.Skimm.chatEmotes;

import me.Skimm.chatCommands.*;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EmoteHandler {
	private Main main;
	private MessageHandler msg;
	
	public EmoteHandler(Main plugin) {
		this.main = plugin;
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		this.msg = new MessageHandler();
		
		String command = argv[0].toLowerCase();
		
		switch (command) {
    	// Special functions
    	case "list":
    		if (argv.length == 2) {
    			int numEmotes, pageLimit, pageNum;
        		numEmotes = main.emote.getConfig().getInt("emotes.amount");
    			pageLimit = (int) Math.ceil(numEmotes / 5.0);
    			pageNum = Integer.parseInt(argv[1]);
    			
    			if (pageNum > 0 && pageNum <= pageLimit) {
    				int count, toSkip;
    				count = 0;
    				toSkip = pageNum * 5 - 5;
    				
    				player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Emote list (" + pageNum + "/" + pageLimit + ")");
    				
    				for (String key1 : main.emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
    					if (key1.equalsIgnoreCase("amount"))
    						continue;
    					
    					if (toSkip > 0)
    						toSkip--;

    					if (count >= 5)
    						break;
    					
    					player.sendMessage(ChatColor.AQUA + key1 + ":");
    					for (String key2 : main.emote.getConfig().getConfigurationSection("emotes." + key1).getKeys(false)) {
    						String info = main.emote.getConfig().getString("emotes." + key1 + "." + key2);
    						
    						if (key2.equalsIgnoreCase("usage")) {
    							player.sendMessage(ChatColor.DARK_AQUA + info);
    						}
    						else if (key2.equalsIgnoreCase("description")) {
    							player.sendMessage(ChatColor.DARK_AQUA + "- " +  info);
    						}
    					}
    					
    					count++;
    				}
    			}
    			
    			else {
    				player.sendMessage("Invalid use of function, enter a number between 1 - " + pageLimit);
    			}
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
        	break;

    	case "add":
    		if (argv.length == 2) {
    			// /emote add <name>
    			String emoteName = argv[1];

    			// Scan for exisitng name
    			if (main.emote.getConfig().contains("emotes." + emoteName)) {
    				player.sendMessage("Emote " + emoteName + " already exists");
					return;
    			}
    			
    			// Update amount count
    			int amount;
    			amount = main.emote.getConfig().getInt("emotes.amount");
    			main.emote.getConfig().set("emotes.amount", (amount + 1));

    			// Setup new emote slot
    			ConfigurationSection newEmote = main.emote.getConfig().createSection("emotes." + emoteName);
    			
    			// General info tab
    			newEmote.set("description", "<BLANK>");
    			newEmote.set("usage", "/emote use " + emoteName + " <optional receiver>");
    			newEmote.set("maxdistance", -1);
    			
    			// Single tab
    			newEmote.set("single.args", -1);
    			newEmote.set("single.sender", "<BLANK>");
    			newEmote.set("single.receiver", "<BLANK>");
    			
    			// Multiple tab
    			newEmote.set("multiple.args", -1);
    			
    			// Multiple close tab
    			newEmote.set("multiple.close.sender", "<BLANK>");
    			newEmote.set("multiple.close.broadcast", "<BLANK>");
    			newEmote.set("multiple.close.receiver", "<BLANK>");
    			
    			// Multiple far tab
    			newEmote.set("multiple.far.sender", "<BLANK>");
    			newEmote.set("multiple.far.broadcast", "<BLANK>");
    			newEmote.set("multiple.far.receiver", "<BLANK>");
    			
    			main.emote.saveConfig();
    		}
    		else if (argv.length == 3) {
    			// /emote add <permission> <player>
    			if (main.checkSpecificPermission(player, "emote.admin")) {
    				List<String> allPerms = main.permissions.getConfig().getStringList("permissions." + label + ".names");
    				
    				if (!(allPerms.contains(argv[1]))) {
    					player.sendMessage("This permission does not exist, check permission list to see available ones");
    					return;
    				}
    				
    				try {
    	    			player.getServer().getPlayer(argv[2]);
    	    		}
    	    		catch (Exception e) {
    	    			player.sendMessage("Can't find player '" + argv[2] + "'");
    	    		}

    				ConfigurationSection newPerm = main.permissions.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".perms.emote");
    				String[] tokens = argv[1].split("\\.");
    				newPerm.set(tokens[1], argv[1]);

    				main.permissions.saveConfig();
    			}
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
    		break;
    	
    	case "edit":
    		if (argv.length == 2) {
    			switch(argv[1]) {
    			case "help":
    				for (String key : main.emote.getConfig().getConfigurationSection("emote_commands.options").getKeys(false)) {
    					for (String info : main.emote.getConfig().getConfigurationSection("emote_commands.options." + key).getKeys(false)) {
    						if (info.equalsIgnoreCase("path"))
    							continue;
    						
    						if (!(key.equalsIgnoreCase("extra")) && !(key.equalsIgnoreCase("info")))
    							player.sendMessage(ChatColor.AQUA + key);
    						
    						player.sendMessage(ChatColor.translateAlternateColorCodes('&', main.emote.getConfig().getString("emote_commands.options." + key + "." + info)));
    					}
    				}
    				break;
    			default:
    				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    			}
    		}
    		else if (argv.length >= 4 || argv.length <= 20) {
    			// /emote edit <name> <option> <new value>
    			String emoteName = argv[1];
    			String option = argv[2].toLowerCase();
    			String newdesc = "";
    			
    			for (int j = 3; j < argv.length; j++) {
    				newdesc += argv[j];
    				newdesc += " ";
    			}

    			// Scan for exisitng name
    			if (!(main.emote.getConfig().contains("emotes." + emoteName))) {
    				player.sendMessage("Emote " + emoteName + " doesn't exist");
					return;
    			}
    			
    			if (!(main.emote.getConfig().contains("emote_commands.options." + option)) || option.equalsIgnoreCase("info")) {
    				player.sendMessage("Option " + option + " doesn't exist");
    				return;
    			}
    			
    			// Setup new emote slot
    			ConfigurationSection editEmote = main.emote.getConfig().getConfigurationSection("emotes." + emoteName);
    			String path = main.emote.getConfig().getString("emote_commands.options." + option + ".path");
    			editEmote.set(path, newdesc);

    			// Update amount count
    			int amount;
    			amount = main.emote.getConfig().getInt("emotes.amount");
    			main.emote.getConfig().set("emotes.amount", (amount - 1));
    			
    			main.emote.saveConfig();
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
    		break;
    	
    	case "remove":
    		if (argv.length == 2) {
    			// /emote remove <name>
    			String emoteName = argv[1];

    			// Scan for exisitng name
    			if (!main.emote.getConfig().contains("emotes." + emoteName)) {
    				player.sendMessage("Emote " + emoteName + " doesn't exist");
					return;
    			}
    			
    			// Update amount count
    			int amount;
    			amount = main.emote.getConfig().getInt("emotes.amount");
    			main.emote.getConfig().set("emotes.amount", (amount - 1));
    			
    			main.emote.getConfig().set("emotes." + emoteName, null);
    			
    			main.emote.saveConfig();
    		}
    		else if (argv.length == 3) {
    			if (main.checkSpecificPermission(player, "emote.admin")) {
    				List<String> allPerms = main.permissions.getConfig().getStringList("permissions." + label + ".names");
    				
    				if (!(allPerms.contains(argv[1]))) {
    					player.sendMessage("This permission does not exist, check permission list to see available ones");
    					return;
    				}
    				
    				try {
    	    			player.getServer().getPlayer(argv[2]);
    	    		}
    	    		catch (Exception e) {
    	    			player.sendMessage("Can't find player '" + argv[2] + "'");
    	    		}

    				ConfigurationSection newPerm = main.permissions.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".perms.emote");
    				String[] tokens = argv[1].split("\\.");
    				newPerm.set(tokens[1], null);

    				main.permissions.saveConfig();
    			}
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
    		break;
    		
    	case "use":
    		if (argv.length == 2) {
        		String emoteName = argv[1].toLowerCase();
        		ArrayList<String> emoteGenInfo = new ArrayList<String>();
        		ArrayList<String> emoteSingleInfo = new ArrayList<String>();
        		ArrayList<String> emoteMultipleInfo = new ArrayList<String>();
        		ArrayList<ArrayList<String>> emoteAllInfo = new ArrayList<ArrayList<String>>();

        		for (String key1 : main.emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
        			if (!key1.equalsIgnoreCase(emoteName)) {
						continue;
					}
        			
        			for (String key2 : main.emote.getConfig().getConfigurationSection("emotes." + key1).getKeys(false)) {
						String arg;
						switch(key2) {
						case "maxdistance":
							arg = String.valueOf(main.emote.getConfig().getInt("emotes." + key1 + "." + key2));
							emoteGenInfo.add(arg);
							break;
							
						case "single":
							for (String args : main.emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2).getKeys(false)) {
								arg = main.emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args);
								emoteSingleInfo.add(arg);
							}
							break;
							
						case "multiple":
							for (String args : main.emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2).getKeys(false)) {
								if (args.equalsIgnoreCase("args")) {
									arg = main.emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args);
									emoteMultipleInfo.add(arg);
									continue;
								}

								for (String args2 : main.emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2 + "." + args).getKeys(false)) {
									arg = main.emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args + "." + args2);
									emoteMultipleInfo.add(arg);
									
								}
							}
							break;
						}
					}
        		}
        		emoteAllInfo.add(emoteGenInfo);
        		emoteAllInfo.add(emoteSingleInfo);
        		emoteAllInfo.add(emoteMultipleInfo);
        		
        		msg.msgParser(player, argv, emoteAllInfo);
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
    		break;

    	default:
    		player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		break;
		}
		
	}
}
