package me.Skimm.chatEmotes;

import me.Skimm.chatCommands.*;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class EmoteHandler {
	private Main main;
	private MessageHandler msg;
	
	public EmoteHandler(Main plugin) {
		this.main = plugin;
		this.msg = new MessageHandler(plugin);
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		String command = argv[0].toLowerCase();
		
		switch (command) {
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
    					
    					if (toSkip > 0) {
    						toSkip--;
    						continue;
    					}

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
    				player.sendMessage(ChatColor.RED + "Invalid use of function, enter a number between 1 - " + pageLimit);
    			}
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
        	break;

    	case "add":
    		if (argv.length == 2) { // /emote add <name>
    			String emoteName = argv[1];

    			// Scan for existing name
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
    			newEmote.set("maxdistance", 0);
    			
    			// Single tab
    			newEmote.set("single.sender", "<BLANK>");
    			newEmote.set("single.receiver", "<BLANK>");

    			// Multiple close tab
    			newEmote.set("multiple.close.sender", "<BLANK>");
    			newEmote.set("multiple.close.broadcast", "<BLANK>");
    			newEmote.set("multiple.close.receiver", "<BLANK>");
    			
    			// Multiple far tab
    			newEmote.set("multiple.far.sender", "<BLANK>");
    			newEmote.set("multiple.far.broadcast", "<BLANK>");
    			newEmote.set("multiple.far.receiver", "<BLANK>");
    			
    			player.sendMessage(ChatColor.GREEN + "Successfully" + ChatColor.WHITE + " created emote '" + emoteName + "'");
    			
    			main.emote.saveConfig();
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
    		else if (argv.length >= 4 || argv.length <= 20) { // /emote edit <name> <option> <new value>
    			String emoteName = argv[1];
    			String option = argv[2].toLowerCase();
    			String newdesc = "";
    			
    			// Check if name exists
    			if (!(main.emote.getConfig().contains("emotes." + emoteName))) {
    				player.sendMessage("Emote " + emoteName + " doesn't exist");
					return;
    			}
    			
    			// Check if option exists
    			if (!(main.emote.getConfig().contains("emote_commands.options." + option)) || option.equalsIgnoreCase("info")) {
    				player.sendMessage("Option " + option + " doesn't exist");
    				return;
    			}
    			
    			// Fetch new value
    			for (int j = 3; j < argv.length; j++) {
    				newdesc += argv[j];
    				newdesc += " ";
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
    		if (argv.length == 2) { // /emote remove <name>
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
    			
    			// Remove emote and save
    			main.emote.getConfig().set("emotes." + emoteName, null);
    			main.emote.saveConfig();
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
    		}
    		break;
    		
    	case "use":
    		if (argv.length == 2 || argv.length == 3) {
    			String emoteName = argv[1].toLowerCase();
    			if (!main.emote.getConfig().getConfigurationSection("emotes").contains(emoteName)) {
    				player.sendMessage(ChatColor.RED + "[ERROR]" + ChatColor.WHITE + " Emote '" + emoteName + "' doesn't exist");
    				break;
    			}
    			
    			ConfigurationSection emoteInfo = main.emote.getConfig().getConfigurationSection("emotes." + emoteName);
    			ArrayList<String> emoteAllInfo = new ArrayList<String>();

    			emoteAllInfo.add(emoteInfo.getString("maxdistance"));

        		for (String infoTab : emoteInfo.getKeys(false)) {
        			switch (infoTab) {
        			case "single":
        				for (String info : emoteInfo.getConfigurationSection(infoTab).getKeys(false)) {
        					emoteAllInfo.add(emoteInfo.getString(infoTab + "." + info));
        				}
        				
        				// This is added to be a receiver placeholders
        				emoteAllInfo.add("<BLANK>");
        				break;
        			case "multiple":
        				for (String info : emoteInfo.getConfigurationSection(infoTab).getKeys(false)) {
        					for (String dist: emoteInfo.getConfigurationSection(infoTab + "." + info).getKeys(false)) {
        						emoteAllInfo.add(emoteInfo.getString(infoTab + "." + info + "." + dist));
        					}
        				}
        				break;
        			}
        		}

        		// Parse message
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
