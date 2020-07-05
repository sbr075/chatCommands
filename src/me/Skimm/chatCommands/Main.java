package me.Skimm.chatCommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Known issues
 * 1. Color issues on text
 * 2. New values in edit only supports one word
 * 3. If values aren't set correctly there are no error message
 */

/*
 * TODO
 * 1. Fix distance messages
 */

public class Main extends JavaPlugin implements Listener {
	
	public configManager emote;
	public configManager permissions;
	
	public HashMap<UUID, PermissionAttachment> playerPermissions = new HashMap<>();

    @Override
    public void onEnable() {
    	this.emote = new configManager(this, "emotes.yml");
    	this.permissions = new configManager(this, "permissions.yml");
    	
    	this.getServer().getPluginManager().registerEvents(this, this);
        // Used during startup and reloads
    }

    @Override
    public void onDisable() {
        // Used during shutdown and reloads
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	
    	// If player doesn't exist in registered players
    	if (permissions.getConfig().getConfigurationSection("players." + player.getUniqueId()) == null) {
    		// Set default perms
    		ConfigurationSection newPlayer = permissions.getConfig().createSection("players." + player.getUniqueId().toString());
    		List<String> defaultPerms = permissions.getConfig().getStringList("permissions.default");
    		for (String key : defaultPerms) {
    			newPlayer.set("perms." + key, key);
    		}
    		
    		if (player.isOp()) {
    			newPlayer.set("perms.emote.admin", "emote.admin");
    		}

    		// Notify player
    		player.sendMessage("Your permissions has been set!");
    		permissions.saveConfig();
    	}
    	else {
    		permissions.saveDefaultConfig();
    	}
    }
    
    private boolean checkPerms(Player player, String perm) {
    	for (String key : permissions.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".perms.emote").getKeys(false)) {
    		if (perm.equalsIgnoreCase(permissions.getConfig().getString("players." + player.getUniqueId() + ".perms.emote." + key))) {
    			return true;
    		}
    	}
    	return false;
    }

	private void msgSend(Player sender, String[] argv, String msg, int num) {
    	Player receiver = null;
    	if (argv.length >= 3) {
    		try {
    			receiver = sender.getServer().getPlayer(argv[2]);
    		}
    		catch (Exception e) {
    			sender.sendMessage("Can't find player '" + argv[2] + "'");
    		}
    	}
    	
    	switch(num) {
		case 1: // sender
			sender.sendMessage(msg);
			break;
			
		case 2: // broadcast
			Bukkit.broadcastMessage(msg);
			break;
			
		case 3: // receiver
			receiver.sendMessage(msg);
			break;	
		}
    }
    
    private boolean msgParser(Player sender, String[] argv, ArrayList<ArrayList<String>> emoteAllInfo) {
    	int maxDistance, snumArgs, mnumArgs;
    	maxDistance = Integer.parseInt(emoteAllInfo.get(0).get(0));
    	snumArgs = Integer.parseInt(emoteAllInfo.get(1).get(0));
    	mnumArgs = Integer.parseInt(emoteAllInfo.get(2).get(0));
    	

    	for (int i = 1; i < emoteAllInfo.size(); i++) {
    		if ((argv.length - 1 ) < Integer.parseInt(emoteAllInfo.get(i).get(0)))
    			continue;
    		
    		for (int j = 1; j < emoteAllInfo.get(i).size(); j++) {
    			if (emoteAllInfo.get(i).get(j).equalsIgnoreCase("<BLANK>"))
    				continue;
    			
    			// Skips empty cases
    			switch(i) {
    			case 1:
    				if (snumArgs <= 0 || (argv.length - 1) != snumArgs)
    					continue;
    				break;
    			case 2:
    				if (mnumArgs <= 0 || (argv.length - 1) != mnumArgs)
    					continue;
    				break;
    			}

    			Player receiver = null;
    	    	if (argv.length >= 3) {
    	    		try {
    	    			receiver = sender.getServer().getPlayer(argv[2]);
    	    		}
    	    		catch (Exception e) {
    	    			sender.sendMessage("Emote " + argv[1] + " is set up wrong or receiver doesn't exist, use /e edit to fix it");
    	    		}
    	    	}
        		
    			String msg = "";
        		String[] tokens = emoteAllInfo.get(i).get(j).split(" ");

            	ArrayList<String> thSplit = new ArrayList<String>();

            	int sCount, rCount, first;
            	sCount = rCount = first = 0;
            	
            	String tmp = "";
            	for (String word : tokens) {
            		if (sCount > 1 || rCount > 1) {
            			sender.sendMessage(ChatColor.RED + "Message can't have more than one sender/receiver");
            			return true;
            		}
            		
            		if (word.length() > 1) {
            			tmp += word + " ";
            			continue;
            		}
            		
            		if (word.equalsIgnoreCase("s")) {
            			thSplit.add(tmp);
            			
            			tmp = "";
            			sCount++;
            			
            			if (rCount == 0) {
            				first = 1;
            			}
            		}
            		
            		else if (word.equalsIgnoreCase("r")) {
            			thSplit.add(tmp);
            			
            			tmp = " ";
            			rCount++;
            			
            			if (sCount == 0) {
            				first = 2;
            			}
            		}
            	}
            	
            	if (sCount != 0 || rCount != 0)
            		thSplit.add(tmp);
            	
            	// sCount and rCount is 0
            	switch(thSplit.size()) {
            	case 0:
            		msg = ChatColor.translateAlternateColorCodes('&', emoteAllInfo.get(i).get(j));
            		break;
            	
            	// sCount or rCount is 1 (single arg)
            	case 2:
            		if (thSplit.get(0).equalsIgnoreCase("")) { // start
            			switch(sCount) {
            			case 0: // r
            				msg = ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1));
            				break;
            				
            			case 1: // s
            				msg = ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1));
            				break;
            			}
            		}
            		else if (thSplit.get(1).equalsIgnoreCase("")) { // end
            			switch(sCount) {
            			case 0:
            				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName());
            				break;
            			case 1:
            				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName());
            				break;
            			}
            		}
            		else { // middle
            			switch(sCount) {
            			case 0:
            				msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName() + thSplit.get(1));
            				break;
            			}
            		}
            		break;
            	
            	// sCount and rCount is 1 
            	case 3:
            		// Check if distance is negative or zero
            		if (maxDistance <= 0) {
            			// If so, skip close
            			if (i >= 5 && i > 8) {
            				continue;
            			}
            		}
            		if (thSplit.get(0).equalsIgnoreCase("") && thSplit.get(2).equalsIgnoreCase("")) { // start end
            			switch(first) {
            			case 1: // s first
            				msg = ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName());
            				break;
            			case 2: // r first
            				msg = ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1) + "&f" +  sender.getDisplayName());
            				break;
            			}
            		}
            		else if (thSplit.get(0).equalsIgnoreCase("")) { // start middle
            			switch(first) {
        	    		case 1: // s first
        	    			msg = ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName()) + " " + thSplit.get(2);
        					break;
        				case 2: // r first
        					msg = ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1) + "&f" + sender.getDisplayName()) + " " + thSplit.get(2);
        					break;
        				}
            		}
            		else if (thSplit.get(2).equalsIgnoreCase("")) { // middle end 
            			switch(first) {
        	    		case 1: // s first
        	    			msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  sender.getDisplayName() + thSplit.get(1) + "&f" +  receiver.getDisplayName());
        					break;
        				case 2: // r first
        					msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  receiver.getDisplayName() + thSplit.get(1) + "&f" +  sender.getDisplayName());
        					break;
        				}
            		}
            		else { // middle middle
            			switch(first) {
        	    		case 1: // s first
        	    			msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName() + " " + thSplit.get(2));
        					break;
        				case 2: // r first
        					msg = ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName() + " " + thSplit.get(1) + "&f" + sender.getDisplayName() + " " + thSplit.get(2));
        					break;
        				}
            		}
            		
            		break;
            		
            	default:
            		sender.sendMessage(ChatColor.RED + "Something went wrong! Please report this to the mod author");
            	}
            	
            	// Send message
            	msgSend(sender, argv, msg, j);
    		}
    	}
    	
    return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        if (label.equalsIgnoreCase("emote") || label.equalsIgnoreCase("e")) {
        	
            // Player
            if (sender instanceof Player) {
            	Player player = (Player) sender;
          
            	String command = argv[0].toLowerCase();

            	switch (command) {
            	// General functions
            	case "help":
        			if (checkPerms(player, "emote.basic") || player.isOp()) {
	            		player.sendMessage(ChatColor.BLUE + "Available commands");
	            		player.sendMessage(ChatColor.AQUA + "/emote help\n" + ChatColor.WHITE + "- Shows all available commands");
	            		player.sendMessage(ChatColor.AQUA + "/emote use <name>\n" + ChatColor.WHITE + "- Use an emote");
	            		player.sendMessage(ChatColor.AQUA + "/emote list <num>\n" + ChatColor.WHITE + "- Returns a list of all available emotes");
	            		player.sendMessage(ChatColor.AQUA + "/emote add <name>\n" + ChatColor.WHITE + "- Add a new emote. Remember to edit its values!");
	            		player.sendMessage(ChatColor.AQUA + "/emote edit <name> <option> <new value>\n" + ChatColor.WHITE + "- Edit emote information. New value can't be more than 15 words");
	            		player.sendMessage(ChatColor.AQUA + "/emote edit help\n" + ChatColor.WHITE + "- See all emote edit options");
	            		player.sendMessage(ChatColor.AQUA + "/emote remove <name>\n" + ChatColor.WHITE + "- Remove an emote");
	            		player.sendMessage(ChatColor.GREEN + "NB! /e also works");
	            		
	            		if (checkPerms(player, "emote.admin") || player.isOp()) {
	            			player.sendMessage(ChatColor.BLUE + "\nAdmin commands");
	            			player.sendMessage(ChatColor.AQUA + "/emote add <permission> <player>\n" + ChatColor.WHITE + "- Give a player permissions");
	            			player.sendMessage(ChatColor.AQUA + "/emote remove <permission> <player>\n" + ChatColor.WHITE + "- Remove permission from player");
	            			player.sendMessage(ChatColor.AQUA + "/emote permlist\n" + ChatColor.WHITE + "- Give a player permissions");
	            			player.sendMessage(ChatColor.GREEN + "NB! Use the option 'all' to give every online player the specified permission");
	            		}
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            		}
        			break;
        			
            	case "list":
            		if (checkPerms(player, "emote.basic") || player.isOp()) {
	            		int numEmotes, pageLimit, pageNum;
	            		numEmotes = this.emote.getConfig().getInt("emotes.amount");
	        			pageLimit = (int) Math.ceil(numEmotes / 5.0);
	        			pageNum = Integer.parseInt(argv[1]);
	            		
	            		if (argv.length == 2) {
	            			if (pageNum > 0 && pageNum <= pageLimit) {
	            				int count, toSkip;
	            				count = 0;
	            				toSkip = pageNum * 5 - 5;
	            				
	            				player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Emote list (" + pageNum + "/" + pageLimit + ")");
	            				
	            				for (String key1 : emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
	            					if (key1.equalsIgnoreCase("amount")) {
	            						continue;
	            					}
	            					
	            					if (toSkip > 0) {
	            						toSkip--;
	            					}
	            					
	            					if (count >= 5)
	            						break;
	            					
	            					player.sendMessage(ChatColor.AQUA + key1 + ":");
	            					for (String key2 : emote.getConfig().getConfigurationSection("emotes." + key1).getKeys(false)) {
	            						String info = emote.getConfig().getString("emotes." + key1 + "." + key2);
	            						
	            						if (key2.equalsIgnoreCase("usage")) {
	            							player.sendMessage(ChatColor.DARK_AQUA + info);
	            						}
	            						else if (key2.equalsIgnoreCase("description")) {
	            							player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.ITALIC + "- " +  info);
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
	            			player.sendMessage("Invalid use of function, enter a number between 1 - " + pageLimit);
	            		}
            		}	
            		else {
            			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            		}
	            	break;
	            
            	case "permlist":
            		if (checkPerms(player, "emote.admin") || player.isOp()) {
	            		if (argv.length == 1) {
	            			player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Permission name list");
	            			List<String> allPerms = permissions.getConfig().getStringList("permissions.names");
	            			
	            			for (String key : allPerms) {
	            				player.sendMessage("- " + ChatColor.AQUA + key);
	            			}
	            		}
	            		else {
	            			player.sendMessage("Invalid use of function");
	            		}
            		}	
            		else {
            			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            		}
            		break;

            	case "add":
            		if (argv.length == 2) {
            			if (checkPerms(player, "emote.add") || player.isOp()) {
	            			// /emote add <name>
	            			String emoteName = argv[1];
	
	            			// Scan for exisitng name
	            			if (emote.getConfig().contains("emotes." + emoteName)) {
	            				player.sendMessage("Emote " + emoteName + " already exists");
	        					return true;
	            			}
	            			
	            			// Update amount count
	            			int amount;
	            			amount = emote.getConfig().getInt("emotes.amount");
	            			emote.getConfig().set("emotes.amount", (amount + 1));
	
	            			// Setup new emote slot
	            			ConfigurationSection newEmote = emote.getConfig().createSection("emotes." + emoteName);
	            			
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
	            			
	            			emote.saveConfig();
            			}
            			else {
                			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
                		}
            		}
            		else if (argv.length == 3) {
            			// /emote add <permission> <player>
            			if (checkPerms(player, "emote.admin") || player.isOp()) {
            				List<String> allPerms = permissions.getConfig().getStringList("permissions.names");
            				
            				if (!(allPerms.contains(argv[1]))) {
            					player.sendMessage("This permission does not exist, check permission list to see available ones");
            					return true;
            				}
            				
            				Player receiver = null;
            				try {
            	    			receiver = sender.getServer().getPlayer(argv[2]);
            	    		}
            	    		catch (Exception e) {
            	    			sender.sendMessage("Can't find player '" + argv[2] + "'");
            	    		}

            				ConfigurationSection newPerm = permissions.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".perms.emote");
            				String[] tokens = argv[1].split("\\.");
            				newPerm.set(tokens[1], argv[1]);

            				permissions.saveConfig();
            			}
        				else {
                			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
                		}
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
            		}
            		break;
            	
            	case "edit":
            		if (checkPerms(player, "emote.edit") || player.isOp()) {
	            		if (argv.length == 2) {
	            			switch(argv[1]) {
	            			case "help":
	            				player.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Emote edit options");
	            				player.sendMessage(ChatColor.AQUA + "Description\n" + ChatColor.WHITE + "- Edit the description ");
	            				player.sendMessage(ChatColor.AQUA + "Usage\n " + ChatColor.WHITE + "- the user how to use the emote");
	            				player.sendMessage(ChatColor.AQUA + "Distance\n " + ChatColor.WHITE + "- How far users can be apart before it changes between close/far messages");
	            				player.sendMessage(ChatColor.AQUA + "SSender\n " + ChatColor.WHITE + "- No optional reciever, sender return message");
	            				player.sendMessage(ChatColor.AQUA + "SBroadcast\n " + ChatColor.WHITE + "- No optional receiver, broadcast return message");
	            				player.sendMessage(ChatColor.AQUA + "MCSender\n " + ChatColor.WHITE + "- Optional receiver, close distance, sender return message");
	            				player.sendMessage(ChatColor.AQUA + "MCBroadcast\n " + ChatColor.WHITE + "- Optional receiver, close distance, broadcast return message");
	            				player.sendMessage(ChatColor.AQUA + "MCReceiver\n " + ChatColor.WHITE + "- Optional receiver, close distance, receiver return message");
	            				player.sendMessage(ChatColor.AQUA + "MFSender\n " + ChatColor.WHITE + "- Optional receiver, far distance, sender return message");
	            				player.sendMessage(ChatColor.AQUA + "MFBroadcast\n " + ChatColor.WHITE + "- Optional receiver, far distance, broadcast return message");
	            				player.sendMessage(ChatColor.AQUA + "MFReceiver\n " + ChatColor.WHITE + "- Optional receiver, far distance, receiver return message");
	            				player.sendMessage(ChatColor.DARK_AQUA + "\nExtra information");
	            				player.sendMessage(ChatColor.AQUA + "If fields are set to <BLANK> they will not be sent out");
	            				player.sendMessage(ChatColor.AQUA + "If distance is set to -1 close messages will not be sent");
	            				player.sendMessage(ChatColor.AQUA + "If args (single/multiple) are set to -1 it will not send out messages in those categories");
	            				break;
	            			default:
	            				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
	            			}
	            			// /emote edit help
	            			
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
	            			if (!(emote.getConfig().contains("emotes." + emoteName))) {
	            				player.sendMessage("Emote " + emoteName + " doesn't exists");
	        					return true;
	            			}
	            			
	            			// Setup new emote slot
	            			ConfigurationSection editEmote = emote.getConfig().createSection("emotes." + emoteName);
	            			
	            			switch(option) {
	            			case "description":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("description", newdesc);
	            				break;
	            			case "usage":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("usage", newdesc);
	            				break;
	            			case "distance":
	            				if (newdesc instanceof String) {
	            					player.sendMessage(option + " needs to be a int");
	            					break;
	            				}
	            				editEmote.set("maxdistance", newdesc);
	            				break;
	            			case "ssender":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("single.sender", newdesc);
	            				break;
	            			case "sbroadcast":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("single.broadcast", newdesc);
	            				break;
	            			case "mcsender":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("multiple.close.sender", newdesc);
	            				break;
	            			case "mcreceiver":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("multiple.close.broadcast", newdesc);
	            				break;
	            			case "mcbroadcast":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("multiple.close.receiver", newdesc);
	            				break;
	            			case "mfsender":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("multiple.far.sender", newdesc);
	            				break;
	            			case "mfbroadcast":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("multiple.far.broadcast", newdesc);
	            				break;
	            			case "mfreceiver":
	            				if (!(newdesc instanceof String)) {
	            					player.sendMessage(option + " needs to be a string");
	            					break;
	            				}
	            				editEmote.set("multiple.far.receiver", newdesc);
	            				break;
	            			}
	            			
	            			// Update amount count
	            			int amount;
	            			amount = emote.getConfig().getInt("emotes.amount");
	            			emote.getConfig().set("emotes.amount", (amount - 1));
	            			
	            			emote.saveConfig();
	            		}
	            		else {
	            			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
	            		}
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            		}
            		break;
            	
            	case "remove":
            		if (argv.length == 2) {
            			if (checkPerms(player, "emote.remove") || player.isOp()) {
	            			// /emote remove <name>
	            			String emoteName = argv[1];
	
	            			// Scan for exisitng name
	            			if (!emote.getConfig().contains("emotes." + emoteName)) {
	            				player.sendMessage("Emote " + emoteName + " doesn't exists");
	        					return true;
	            			}
	            			
	            			// Update amount count
	            			int amount;
	            			amount = emote.getConfig().getInt("emotes.amount");
	            			emote.getConfig().set("emotes.amount", (amount - 1));
	            			
	            			emote.getConfig().set("emotes." + emoteName, null);
	            			
	            			emote.saveConfig();
            			}
            			else {
            				player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            			}
            		}
            		else if (argv.length == 3) {
            			if (checkPerms(player, "emote.admin") || player.isOp()) {
            				List<String> allPerms = permissions.getConfig().getStringList("permissions.names");
            				
            				if (!(allPerms.contains(argv[1]))) {
            					player.sendMessage("This permission does not exist, check permission list to see available ones");
            					return true;
            				}
            				
            				Player receiver = null;
            				try {
            	    			receiver = sender.getServer().getPlayer(argv[2]);
            	    		}
            	    		catch (Exception e) {
            	    			sender.sendMessage("Can't find player '" + argv[2] + "'");
            	    		}

            				ConfigurationSection newPerm = permissions.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".perms.emote");
            				String[] tokens = argv[1].split("\\.");
            				newPerm.set(tokens[1], null);

            				permissions.saveConfig();
            			}
            			else {
            				player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            			}
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
            		}
            		break;
            		
            	case "use":
            		if (checkPerms(player, "emote.use") || player.isOp()) {
	            		String emoteName = argv[1].toLowerCase();
	            		ArrayList<String> emoteGenInfo = new ArrayList<String>();
	            		ArrayList<String> emoteSingleInfo = new ArrayList<String>();
	            		ArrayList<String> emoteMultipleInfo = new ArrayList<String>();
	            		ArrayList<ArrayList<String>> emoteAllInfo = new ArrayList<ArrayList<String>>();
	
	            		for (String key1 : emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
	            			if (!key1.equalsIgnoreCase(emoteName)) {
	    						continue;
	    					}
	            			
	            			for (String key2 : emote.getConfig().getConfigurationSection("emotes." + key1).getKeys(false)) {
	    						String arg;
	    						switch(key2) {
	    						case "maxdistance":
	    							arg = String.valueOf(emote.getConfig().getInt("emotes." + key1 + "." + key2));
	    							emoteGenInfo.add(arg);
	    							break;
	    							
	    						case "single":
	    							for (String args : emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2).getKeys(false)) {
	    								arg = emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args);
	    								emoteSingleInfo.add(arg);
	    							}
	    							break;
	    							
	    						case "multiple":
	    							for (String args : emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2).getKeys(false)) {
	    								if (args.equalsIgnoreCase("args")) {
	    									arg = emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args);
	    									emoteMultipleInfo.add(arg);
	    									continue;
	    								}
	
	    								for (String args2 : emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2 + "." + args).getKeys(false)) {
	    									arg = emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args + "." + args2);
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
	            		
	            		msgParser(player, argv, emoteAllInfo);
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
            		}
            		break;

            	default:
            		player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
            		break;
            	}
            }
        }
        else {
        	sender.sendMessage("Console can't use these commands!");
            return true;
            // Console
        }
        
        return true;
    }
}