package me.Skimm.chatCommands;

import java.util.ArrayList;

//import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.Skimm.chatEmotes.emoteManager;

/*
 * Known issues
 * 1. 
 */

/* 
 * Emotes
 * 1. Hug
 * 2. Kiss
 * 3. Cut (will do 1 heart of dmg to player)
 * 4. Wave
 * 5. Smile
 * 6. Flush
 * 7. Facepalm
 * 8. Laughing
 * 9. Angry
 * 10. Rating (1-10)
 * 11. Cry
 * 12. Flirt
 * 13. Dance
 * 14. Pinch
 * 15. Goodbye
 */

public class Main extends JavaPlugin {
	
	public emoteManager emote;

    @Override
    public void onEnable() {
    	this.emote = new emoteManager(this);
        // Used during startup and reloads
    }

    @Override
    public void onDisable() {
        // Used during shutdown and reloads
    }
    
    private boolean msgParser(Player sender, String[] argv, ArrayList<String> emoteInfo) {
    	//int maxDistance;
    	//maxDistance = Integer.parseInt(emoteInfo.get(0));

    	for (int i = 2; i < emoteInfo.size(); i++) {
    		if (emoteInfo.get(i).equalsIgnoreCase("<BLANK>") || i == 4)
    			continue;
    		
    		if (i < 4) {
    			if ((argv.length - 1) > Integer.parseInt(emoteInfo.get(1)) || emoteInfo.get(1).equalsIgnoreCase("<BLANK>"))
    				continue;
    		}
    		else {
    			if ((argv.length - 1) < Integer.parseInt(emoteInfo.get(4)) || emoteInfo.get(4).equalsIgnoreCase("<BLANKS>"))
    				break;
    		}
    		
    		String[] tokens = emoteInfo.get(i).split(" ");

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
        			
        			tmp = "";
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
        		sender.sendMessage(emoteInfo.get(i));
        		break;
        	
        	// sCount or rCount is 1 
        	case 2:
        		if (thSplit.get(0).equalsIgnoreCase("")) { 
        			switch(sCount) {
        			case 0:
        				Player receiver = sender.getServer().getPlayer(argv[1]);
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1)));
        				break;
        			case 1:
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1)));
        				break;
        			}
        		}
        		else if (thSplit.get(1).equalsIgnoreCase("")) { 
        			switch(sCount) {
        			case 0:
        				Player receiver = sender.getServer().getPlayer(argv[1]);
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName()));
        				break;
        			case 1:
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName()));
        				break;
        			}
        		}
        		else {
        			switch(sCount) {
        			case 0:
        				Player receiver = sender.getServer().getPlayer(argv[1]);
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName() + thSplit.get(1)));
        				break;
        			case 1:
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName() + thSplit.get(1)));
        				break;
        			}
        		}
        		break;
        	
        	// sCount and rCount is 1 
        	case 3:
        		Player receiver = sender.getServer().getPlayer(argv[2]);
        		
        		if (thSplit.get(0).equalsIgnoreCase("") && thSplit.get(2).equalsIgnoreCase("")) { // start end
        			switch(first) {
        			case 1: // s first
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName()));
        				break;
        			case 2: // r first
        				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1) + "&f" +  sender.getDisplayName()));
        				break;
        			}
        		}
        		else if (thSplit.get(0).equalsIgnoreCase("")) { // start middle
        			switch(first) {
    	    		case 1: // s first
    	    			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName()) + " " + thSplit.get(2));
    					break;
    				case 2: // r first
    					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', receiver.getDisplayName() + " " + thSplit.get(1) + "&f" + sender.getDisplayName()) + " " + thSplit.get(2));
    					break;
    				}
        		}
        		else if (thSplit.get(2).equalsIgnoreCase("")) { // middle end 
        			switch(first) {
    	    		case 1: // s first
    	    			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  sender.getDisplayName() + thSplit.get(1) + "&f" +  receiver.getDisplayName()));
    					break;
    				case 2: // r first
    					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" +  receiver.getDisplayName() + thSplit.get(1) + "&f" +  sender.getDisplayName()));
    					break;
    				}
        		}
        		else { // middle middle
        			switch(first) {
    	    		case 1: // s first
    	    			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + sender.getDisplayName() + " " + thSplit.get(1) + "&f" + receiver.getDisplayName() + " " + thSplit.get(2)));
    					break;
    				case 2: // r first
    					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', thSplit.get(0) + "&f" + receiver.getDisplayName() + " " + thSplit.get(1) + "&f" + sender.getDisplayName() + " " + thSplit.get(2)));
    					break;
    				}
        		}
        		
        		break;
        		
        	default:
        		sender.sendMessage(ChatColor.RED + "Something went wrong! Please report this to the mod author");
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
            		player.sendMessage(ChatColor.BLUE + "Available commands");
            		player.sendMessage(ChatColor.AQUA + "/emote help - " + ChatColor.WHITE + "Shows all available commands");
            		player.sendMessage(ChatColor.AQUA + "/emote list <num> - " + ChatColor.WHITE + "Returns a list of all available emotes");
            		player.sendMessage(ChatColor.AQUA + "/emote add <name> <usage> <description> - " + ChatColor.WHITE + "Add a new emote");
            		player.sendMessage(ChatColor.AQUA + "/emote edit <name> <usage/description> <new uusage/description> - " + ChatColor.WHITE + "Edit emote information");
            		player.sendMessage(ChatColor.AQUA + "/emote remove <name> - " + ChatColor.WHITE + "Remove an emote");
            		player.sendMessage(ChatColor.GREEN + "NB! /e also works");
            		break;
            	
            	case "list":
            		int numEmotes, pageLimit, pageNum;
            		numEmotes = this.emote.getConfig().getInt("emotes.amount");
        			pageLimit = (int) Math.ceil(numEmotes / 5.0);
        			pageNum = Integer.parseInt(argv[1]);
            		
            		if (argv.length == 2) {
            			if (pageNum > 0 && pageNum <= numEmotes) {
            				int count, toSkip;
            				count = 0;
            				toSkip = pageNum * 5 - 5;
            				
            				player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Emote list (" + pageNum + "/" + pageLimit + ")");
            				
            				for (String key1 : emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
            					if (key1.equalsIgnoreCase("amount") || toSkip > 0) {
            						toSkip--;
            						continue;
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
            		
            		break;

            	case "add":
            		if (argv.length == 4) {
            			String name = argv[1];
            			//String old = argv[2];
            			//String replace = argv[3];
            			
            			/* Scan for exisitng name */
            			for (String key1 : emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
            				if (key1.equalsIgnoreCase(name)) {
            					player.sendMessage("Emote already exists");
            					return true;
            				}
            			}
            			
            		}
            		else {
            			player.sendMessage(ChatColor.RED + "Invalid use of command!");
            		}
            		
            		break;
            	
            	case "edit":
            		break;
            	
            	case "remove":
            		break;
            		
            	case "use":
            		String emoteName = argv[1].toLowerCase();
            		ArrayList<String> emoteInfo = new ArrayList<String>();

            		for (String key1 : emote.getConfig().getConfigurationSection("emotes").getKeys(false)) {
            			if (!key1.equalsIgnoreCase(emoteName)) {
    						continue;
    					}
            			
            			for (String key2 : emote.getConfig().getConfigurationSection("emotes." + key1).getKeys(false)) {

    						String arg;
    						switch(key2) {
    						case "maxdistance":
    							arg = String.valueOf(emote.getConfig().getInt("emotes." + key1 + "." + key2));
    							emoteInfo.add(arg);
    							break;
    							
    						case "single":
    							for (String args : emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2).getKeys(false)) {
    								arg = emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args);
    								emoteInfo.add(arg);
    							}
    							break;
    							
    						case "multiple":
    							for (String args : emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2).getKeys(false)) {
    								if (args.equalsIgnoreCase("args")) {
    									arg = emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args);
    									emoteInfo.add(arg);
    									continue;
    								}

    								for (String args2 : emote.getConfig().getConfigurationSection("emotes." + key1 + "." + key2 + "." + args).getKeys(false)) {
    									arg = emote.getConfig().getString("emotes." + key1 + "." + key2 + "." + args + "." + args2);
    									emoteInfo.add(arg);
    									
    								}
    							}
    							break;
    						}
    					}
            		}
            		msgParser(player, argv, emoteInfo);
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