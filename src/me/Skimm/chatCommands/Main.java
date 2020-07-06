package me.Skimm.chatCommands;

import me.Skimm.chatEmotes.*;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Known issues
 * 1. emote list is none functional
 */

/*
 * TODO
 * 1. Clean up
 */

public class Main extends JavaPlugin implements Listener {
	
	public MainConfigManager emote;
	public MainConfigManager permissions;
	public MainConfigManager commands;
	
	public MessageHandling emoteMessage;

    @Override
    public void onEnable() {
    	emote = new MainConfigManager(this, "emotes.yml");
    	permissions = new MainConfigManager(this, "permissions.yml");
    	commands = new MainConfigManager(this, "commands.yml");
    	
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
    		List<String> defaultPerms = permissions.getConfig().getStringList("permissions.emote.default");
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
    	// Check users permissions
    	String token = perm.split("\\.")[1];
    	if (player.isOp() || permissions.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".perms.emote").contains(token)) {
    		return true;
    	}
    	else {
    		// Permission not found
        	player.sendMessage(ChatColor.RED + "You do not have permissions to use this command. If you believe this is a mistake please report it to the server administrator");
        	return false;
    	}
    }

	

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        if (argv.length == 0) {
        	sender.sendMessage("Invalid use of command");
        	return true;
        }
        
    	String command = argv[0].toLowerCase();
    	
    	/*
    	 * Supported command list
    	 * emote/e
    	 */
    	
    	if (sender instanceof Player) {
    		Player player = (Player) sender;
    		
    		// Check what label was called
    		switch (label) {
        	case "e":
        	case "emote":
        		switch (command) {
            	// General functions
            	case "help":
        			if (checkPerms(player, "emote.basic") || player.isOp()) {
        				/*
        				 * cmdName - any cmd
        				 */
        				for (String cmdName : commands.getConfig().getConfigurationSection("commands").getKeys(false)) {
        					/*
            				 * titleName - user, admin
            				 */
            				for (String titleName : commands.getConfig().getConfigurationSection("commands." + cmdName).getKeys(false)) {
            					// Prevent normal players from seeing admin help tab
            					if (titleName.equalsIgnoreCase("admin")) {
            						if (!checkPerms(player, cmdName + ".admin") || !player.isOp()) {
            							break;
            						}
            					}
            					/*
            					 * key1 - info, <label>
            					 */
            					for (String labelName : commands.getConfig().getConfigurationSection("commands." + cmdName + "." + titleName).getKeys(false)) {
            						/*
        	            			 * key2 - description, usage
        	            			 */
            						for (String infoName: commands.getConfig().getConfigurationSection("commands." + cmdName + "." + titleName + "." + labelName).getKeys(false)) {
            							player.sendMessage(ChatColor.translateAlternateColorCodes('&', commands.getConfig().getString("commands." + cmdName + "." + titleName + "." + labelName + "." + infoName)));
            						}
        	            		}
            				}
        				}
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
	            			player.sendMessage("Invalid use of function, enter a number between 1 - " + pageLimit);
	            		}
            		}
	            	break;
	            
            	case "permlist":
            		if (checkPerms(player, "emote.admin")) {
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
            		break;

            	case "add":
            		if (argv.length == 2) {
            			if (checkPerms(player, "emote.add")) {
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
            		}
            		else if (argv.length == 3) {
            			// /emote add <permission> <player>
            			if (checkPerms(player, "emote.admin")) {
            				List<String> allPerms = permissions.getConfig().getStringList("permissions.names");
            				
            				if (!(allPerms.contains(argv[1]))) {
            					player.sendMessage("This permission does not exist, check permission list to see available ones");
            					return true;
            				}
            				
            				try {
            	    			sender.getServer().getPlayer(argv[2]);
            	    		}
            	    		catch (Exception e) {
            	    			sender.sendMessage("Can't find player '" + argv[2] + "'");
            	    		}

            				ConfigurationSection newPerm = permissions.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".perms.emote");
            				String[] tokens = argv[1].split("\\.");
            				newPerm.set(tokens[1], argv[1]);

            				permissions.saveConfig();
            			}
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
            		}
            		break;
            	
            	case "edit":
            		if (checkPerms(player, "emote.edit")) {
	            		if (argv.length == 2) {
	            			switch(argv[1]) {
	            			case "help":
	            				for (String key : emote.getConfig().getConfigurationSection("emote_commands.options").getKeys(false)) {
	            					for (String info : emote.getConfig().getConfigurationSection("emote_commands.options." + key).getKeys(false)) {
	            						if (info.equalsIgnoreCase("path"))
	            							continue;
	            						
	            						if (!(key.equalsIgnoreCase("extra")) && !(key.equalsIgnoreCase("info")))
	            							player.sendMessage(ChatColor.AQUA + key);
	            						
	            						player.sendMessage(ChatColor.translateAlternateColorCodes('&', emote.getConfig().getString("emote_commands.options." + key + "." + info)));
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
	            			if (!(emote.getConfig().contains("emotes." + emoteName))) {
	            				player.sendMessage("Emote " + emoteName + " doesn't exist");
	        					return true;
	            			}
	            			
	            			if (!(emote.getConfig().contains("emote_commands.options." + option)) || option.equalsIgnoreCase("info")) {
	            				player.sendMessage("Option " + option + " doesn't exist");
	            				return true;
	            			}
	            			
	            			// Setup new emote slot
	            			ConfigurationSection editEmote = emote.getConfig().getConfigurationSection("emotes." + emoteName);
	            			String path = emote.getConfig().getString("emote_commands.options." + option + ".path");
	            			editEmote.set(path, newdesc);

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
            		break;
            	
            	case "remove":
            		if (argv.length == 2) {
            			if (checkPerms(player, "emote.remove")) {
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
            		}
            		else if (argv.length == 3) {
            			if (checkPerms(player, "emote.admin")) {
            				List<String> allPerms = permissions.getConfig().getStringList("permissions.names");
            				
            				if (!(allPerms.contains(argv[1]))) {
            					player.sendMessage("This permission does not exist, check permission list to see available ones");
            					return true;
            				}
            				
            				try {
            	    			sender.getServer().getPlayer(argv[2]);
            	    		}
            	    		catch (Exception e) {
            	    			sender.sendMessage("Can't find player '" + argv[2] + "'");
            	    		}

            				ConfigurationSection newPerm = permissions.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".perms.emote");
            				String[] tokens = argv[1].split("\\.");
            				newPerm.set(tokens[1], null);

            				permissions.saveConfig();
            			}
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
            		}
            		break;
            		
            	case "use":
            		if (checkPerms(player, "emote.use")) {
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

	            		emoteMessage = new MessageHandling(player, argv, emoteAllInfo);
            		}
            		break;

            	default:
            		player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
            		break;
        		}
        		
        		break;
        		
        	default:
        		player.sendMessage(ChatColor.RED + "Command not found!");
        		break;
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