package me.Skimm.chatCommands;

import java.util.ArrayList;

import org.bukkit.Bukkit;
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
    
    private boolean msgParser(Player sender, String[] argv, String message) {
    	String[] tokens = message.split(" ");

    	ArrayList<String> thSplit = new ArrayList<String>();

    	int sCount, rCount;
    	sCount = rCount = 0;
    	
    	String tmp = "";
    	for (String word : tokens) {
    		if (sCount > 1 || rCount > 1) {
    			sender.sendMessage(ChatColor.RED + "Message can't have more than one sender/reciever");
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
    		}
    		
    		else if (word.equalsIgnoreCase("r")) {
    			thSplit.add(tmp);
    			
    			tmp = "";
    			rCount++;
    		}
    	}
    	
    	/* Check for all cases */
    	if (sCount != 0 || rCount != 0)
    		thSplit.add(tmp);
    	
    	/* No sender/reciever present */
    	if (thSplit.size() == 0) {
    		sender.sendMessage(message);
    	}
    	/* Only one sender/reciever present */
    	else if (thSplit.size() == 2) {
    		if (thSplit.get(0).equalsIgnoreCase("")) {
    			switch(sCount) {
    			case 0:
    				Player reciever = sender.getServer().getPlayer(argv[1]);
    				sender.sendMessage(reciever.getDisplayName() + " " + ChatColor.translateAlternateColorCodes('&', thSplit.get(1)));
    				break;
    			case 1:
    				sender.sendMessage(sender.getDisplayName() + " " + ChatColor.translateAlternateColorCodes('&', thSplit.get(1)));
    				break;
    			}
    		}
    		else if (thSplit.get(1).equalsIgnoreCase("")) {
    			switch(sCount) {
    			case 0:
    				Player reciever = sender.getServer().getPlayer(argv[1]);
    				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', thSplit.get(0)) + "&f" + reciever.getDisplayName()));
    				break;
    			case 1:
    				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', thSplit.get(0)) + "&f" + sender.getDisplayName()));
    				break;
    			}
    		}
    		else {
    			switch(sCount) {
    			case 0:
    				Player reciever = sender.getServer().getPlayer(argv[1]);
    				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',  ChatColor.translateAlternateColorCodes('&', thSplit.get(0)) + " &f" + reciever.getDisplayName() + ChatColor.translateAlternateColorCodes('&', thSplit.get(1))));
    				break;
    			case 1:
    				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',  ChatColor.translateAlternateColorCodes('&', thSplit.get(0)) + " &f" + sender.getDisplayName() + ChatColor.translateAlternateColorCodes('&', thSplit.get(1))));
    			}
    		}
    	}
    	else if (thSplit.size() == 2) {
    		
    	}
    	else {
    		
    	}
    	
    	return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        if (label.equalsIgnoreCase("emote") || label.equalsIgnoreCase("e")) {
        	
        	/*/emote use kiss <reciever> */
        	
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
            						else {
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
            	
            	// Emotes
            	case "hug":
        			if (argv.length == 1) {
        				msgParser(player, argv, "s &4hugs &fthe thin air");
        				msgParser(player, argv, "&4hugs s &fthe thin air");
        				msgParser(player, argv, "&4hugs &fthe thin air s");
        				
        				//msgParser(player, argv, "r &4hugs &fthe thin air");
        				//msgParser(player, argv, "&4hugs r &fthe thin air");
        				//msgParser(player, argv, "&4hugs &fthe thin air r");
        				
        				//msgParser(player, argv, "s &4 hugs r");
        				//msgParser(player, argv, "s &4 hugs r hello");
        				//msgParser(player, argv, "hello s &4 hugs r");
        				//msgParser(player, argv, "hello s &4 hugs r hello");
        				
        				//msgParser(player, argv, "r &4 hugs s");
        				//msgParser(player, argv, "r &4 hugs s hello");
        				//msgParser(player, argv, "hello s &4 hugs s");
        				//msgParser(player, argv, "hello s &4 hugs s hello");
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + (ChatColor.RED + "" + ChatColor.BOLD + " hugs ") + (ChatColor.WHITE + "the thin air."));
        			}
        			
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + (ChatColor.RED + "" + ChatColor.BOLD + " hugs ") + (ChatColor.WHITE + reciever.getDisplayName()));
        			
        				if (distance < 10) {
        					player.sendMessage(player.getDisplayName() + (ChatColor.RED + "" + ChatColor.BOLD + " hugs ") + (ChatColor.WHITE + reciever.getDisplayName()));
            				reciever.sendMessage(player.getDisplayName() + (ChatColor.RED + "" + ChatColor.BOLD + " hugs ") + (ChatColor.WHITE + "you tightly!"));
        				}
        				else {
        					player.sendMessage(player.getDisplayName() + (ChatColor.RED + "" + ChatColor.BOLD + " hugs ") + (ChatColor.WHITE + reciever.getDisplayName()) + " from far away!");
            				reciever.sendMessage(player.getDisplayName() + " sends you a virtual " + ChatColor.RED + "" + ChatColor.BOLD + "hug");
        				}
        			}
        			
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        				
        			break;
        			
        		case "kiss":
					if (argv.length == 1) {
						Bukkit.broadcastMessage(player.getDisplayName() + " sent a " + (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "kiss") + (ChatColor.WHITE + " to no one."));
        			}
					
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " kisses ") + reciever.getDisplayName());
        				
        				if (distance < 10) {
        					player.sendMessage(player.getDisplayName() + (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " kisses ") + reciever.getDisplayName());
        					reciever.sendMessage("Surprise!, " + player.getDisplayName() + (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " kisses you!"));
        				}
        				else {
        					player.sendMessage(player.getDisplayName() + (ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + " kisses ") + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() + " throws you a " + (ChatColor.LIGHT_PURPLE + "kiss") + (ChatColor.WHITE + " from " + distance + " blocks away!"));
        				}
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        			
        		case "cut":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.RED + "cut" + ChatColor.WHITE + " themselves, ouch!");
        				player.damage(1);
        			}
        			
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			break;
        		
        		case "goodbye":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + " takes their final breath");
        				player.damage(20);
        			}
        			
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			break;
        			
        		case "wave":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " waves " + ChatColor.WHITE + "to, uh, no one?");
        			}
					
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				if (distance < 100) {
        					Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " waves " + ChatColor.WHITE + "at " + reciever.getDisplayName());
        					
        					player.sendMessage("You " + ChatColor.GREEN + "wave" + ChatColor.WHITE + " at " + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() + ChatColor.GREEN + "waves" + ChatColor.WHITE + " at you!");
        				}
        				else {
        					player.sendMessage("Who are you waving at? Try to find someone first.");
        				}
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        			
        		case "smile":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " smiles " + ChatColor.WHITE + "randomly");
        			}
					
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " smiles" + ChatColor.WHITE + " at " + reciever.getDisplayName());
        				
        				if (distance < 100) {
        					player.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " smiles" + ChatColor.WHITE + " towards " + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " smiles " + ChatColor.WHITE + "at you");
        				}
        				else {
        					player.sendMessage(player.getDisplayName() + " thinks of " + reciever.getDisplayName() + "and " + ChatColor.YELLOW + "smile");
        					reciever.sendMessage("You get the feeling someone is " + ChatColor.YELLOW + "smiling" + ChatColor.WHITE + " at you. Might be a good time to run");
        				}
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        			
        		case "flush":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.RED + "flushes");
        				player.sendMessage("Your cheeks heath up. Oh no, you're " + ChatColor.RED + "flushing!");
        			}
					
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.RED + " flushes " + ChatColor.WHITE + "at the thought of " + reciever.getDisplayName());
        				
        				if (distance < 100) {
        					player.sendMessage(player.getDisplayName() + ChatColor.DARK_PURPLE + " flushes " + ChatColor.WHITE + "while looking at " + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() + ChatColor.DARK_PURPLE + " flushes " + ChatColor.WHITE + "looking at you");
        				}
        				else {
        					player.sendMessage(player.getDisplayName() + ChatColor.DARK_PURPLE + " flushes " + ChatColor.WHITE + "at " + reciever.getDisplayName() + " from far away!");
        					reciever.sendMessage("Someone, somewhere, flushes at you");
        				}
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        		
        		case "laugh":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " laughs " + ChatColor.WHITE + "by themselves");
        			}
					
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.YELLOW + " laughs " + ChatColor.WHITE + "at " + reciever.getDisplayName());
        				
        				if (distance < 50) {
        					player.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " laughs " + ChatColor.WHITE + "at " + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " laughs " + ChatColor.WHITE + "at you!");
        				}
        				else {
        					player.sendMessage(player.getDisplayName() + ChatColor.YELLOW + " laughs " + ChatColor.WHITE + "while thinking about " + reciever.getDisplayName());
        					reciever.sendMessage("You get the feeling someone is " + ChatColor.YELLOW + "laughing " + ChatColor.WHITE + "at you!");
        				}
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        			
        		case "angry":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + " is really " + ChatColor.RED + "angry" + ChatColor.WHITE + ". Might want to stay away for a while");
        			}
					
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + " is " + ChatColor.RED + "angry" + ChatColor.WHITE + " at " + reciever.getDisplayName());

        				player.sendMessage(player.getDisplayName() + " is " + ChatColor.RED + "angry" + ChatColor.WHITE + " at " + reciever.getDisplayName());
        				reciever.sendMessage(player.getDisplayName() + " is " + ChatColor.RED + "angry" + ChatColor.WHITE + " at you");
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        			
        		case "cry":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.BLUE + "cries" + ChatColor.WHITE + " :'(");
        			}
					
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        			
        		case "rate":
        			if (argv.length == 3) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.GOLD + " rates " + ChatColor.WHITE + reciever.getDisplayName() + " " + Integer.parseInt(argv[2]) + " out of 10");
        				
        				player.sendMessage(player.getDisplayName() + ChatColor.GOLD + " rates " + ChatColor.WHITE + reciever.getDisplayName() + " " + Integer.parseInt(argv[2]) + " out of 10");
        				reciever.sendMessage(player.getDisplayName() + ChatColor.GOLD + " rates " + ChatColor.WHITE + "you " + Integer.parseInt(argv[2]) + " out of 10");
        			}
	
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			
        			break;
        		
        		case "dance":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + " is dancing in front of the mirror");
        			}
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				if (distance < 50) {
        					Bukkit.broadcastMessage(player.getDisplayName() + " shows their moves to " + reciever.getDisplayName());
        					
        					player.sendMessage(player.getDisplayName() + " tries to impress " + reciever.getDisplayName() + " with their moves");
        					reciever.sendMessage(player.getDisplayName() + " tries to impress you with their dance");
        				}
        				else {
        					player.sendMessage(reciever.getDisplayName() + " isn't looking at you");
        				}
        			}
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			break;
        		
        		case "flirt":
        			if (argv.length == 1) {
        				player.sendMessage("I hope you're practising");
        			}
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				if (distance < 50) {
        					Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.LIGHT_PURPLE + " flirts " + ChatColor.WHITE + "with " + reciever.getDisplayName());
        					
        					player.sendMessage("You" + ChatColor.LIGHT_PURPLE + " flirt " + ChatColor.WHITE + "with " + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() +  ChatColor.LIGHT_PURPLE + " flirt " + ChatColor.WHITE + "with you");
        				}
        				else {
        					Bukkit.broadcastMessage(player.getDisplayName() + " sends a " + ChatColor.LIGHT_PURPLE + "love letter " + ChatColor.WHITE + "to " + reciever.getDisplayName());
        				}
        			}
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			break;
        			
        		case "pinch":
        			if (argv.length == 1) {
        				player.sendMessage("You pinch yourself!");
        				player.damage(1);
        			}
        			else if (argv.length == 2) {
        				Player reciever = player.getServer().getPlayer(argv[1]);
        				double distance = player.getLocation().distance(reciever.getLocation());
        				
        				if (distance < 5) {
        					Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.BLACK + " pinches " + reciever.getDisplayName());
        					
        					player.sendMessage("You" + ChatColor.BLACK + " pinch " + reciever.getDisplayName());
        					reciever.sendMessage(player.getDisplayName() +  ChatColor.BLACK + " pinches " + ChatColor.WHITE + "you");
        				}
        				else {
        					player.sendMessage(reciever.getDisplayName() + "is too far away!");
        				}
        			}
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			break;
        		
        		case "facepalm":
        			if (argv.length == 1) {
        				Bukkit.broadcastMessage(player.getDisplayName() + ChatColor.BLUE + " facepalms");
        			}
        			else {
        				player.sendMessage(ChatColor.RED + "Invalid use of command!");
        			}
        			break;
        			
        		default:
        			player.sendMessage("No such emote!");
        			break;
        		}
            	
                return true;
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