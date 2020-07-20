package me.Skimm.chatChat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.Skimm.chatCommands.*;
import net.md_5.bungee.api.ChatColor;

public class ChatHandler  {
	private Main plugin;
	private RequestScheduler requestScheduler;
	
	ConfigurationSection section, p, r;
	ArrayList<String> titles = new ArrayList<String>();
	
	public ChatHandler(Main plugin) {
		this.plugin = plugin;
		
		titles.add("owner");
		titles.add("moderator");
		titles.add("member");
	}
    
    private boolean updateMode(Player player, String mode) {
    	try {
			plugin.updateDisplayName(player, 1, mode);
			plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.mode", mode);
			plugin.playerInfo.saveConfig();
		}
		catch (IllegalArgumentException e) {
			player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Invalid chat mode");
			return false;
		}
    	
    	return true;
    }
    
    public String stripName(Player player) {
    	String curName, curTitle, curMode;
    	curTitle = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".title").toLowerCase();
    	curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
    	
    	// [&fMODE&f][&fTITLE&f] <NAME>
    	// Remove title and chat mode text to get normal name
    	try {
    		curName = player.getDisplayName().substring(curTitle.length() + curMode.length() + 13, player.getDisplayName().length());
    	}
    	catch (StringIndexOutOfBoundsException e) {
    		curName = player.getDisplayName();
    	}
    	
    	return curName;
    }
	
	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command = "", type = "", msg = "", color = "&f";
		if (argv.length >= 2 && label.equalsIgnoreCase("chat")) {
			command = argv[0].toLowerCase();
			type = argv[1].toLowerCase();
		}

		section = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
		
		switch(command) {
		case "list": // /chat list <modes/groups>
			/*
			 * List all available chat modes
			 */
			
			switch(type) {
			case "modes":
				player.sendMessage(ChatColor.AQUA + "Available chat modes");
				for (String mode : plugin.config.getConfig().getConfigurationSection("general.chat.chatModes").getKeys(false)) {
					player.sendMessage(ChatColor.DARK_AQUA + "- " + mode.substring(0, 1).toUpperCase() + mode.substring(1));
				}
				break;
			case "groups":
				if (plugin.chat.getConfig().getConfigurationSection("groups") == null) {
					player.sendMessage(ChatColor.RED + "No active groups");
					break;
				}
				
				player.sendMessage(ChatColor.AQUA + "Current active group(s)");
				for (String name : plugin.chat.getConfig().getConfigurationSection("groups").getKeys(false)) {
					player.sendMessage(ChatColor.DARK_AQUA + "- " + name);
				}
				break;
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
		
		case "create": // /chat create <group/party> <name>
			/*
			 * Create a group/party
			 */
			/*
			 * Edit group 
			 * - MOTD (MODS AND ABOVE)
			 * - Policy (ONLY OWNER)
			 *   - Open
			 *   - Closed
			 * - Description (MODS AND ABOVE)
			 * - Name (ONLY OWNER)
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender is already in a group
				 * - If group name already exists
				 * - If name is specified (NEED NAME)
				 */
				
				if (argv.length == 3) {
					// Check if player is already in a group
					if (plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".group") == null) {
						// Check if group already exists
						if (plugin.chat.getConfig().getConfigurationSection("groups") == null || 
								!plugin.chat.getConfig().getConfigurationSection("groups").contains(argv[2])) {
							// Create group
							ConfigurationSection newGroup = plugin.chat.getConfig().createSection("groups." + argv[2]);
							newGroup.set("ownerUUID", player.getUniqueId().toString());
							newGroup.set("name", argv[2]);
							newGroup.set("description", "This is a default description");
							newGroup.set("MOTD", "Welcome to " + argv[2]);
							newGroup.set("policy", "closed");
							
							List<String> members = new ArrayList<String>();
							members.add(player.getUniqueId().toString());
							newGroup.set("members", members);
							
							List<String> requests = new ArrayList<String>();
							newGroup.set("requests", requests);
							
							List<String> invited = new ArrayList<String>();
							newGroup.set("invited", invited);
							
							List<String> invitetimer = new ArrayList<String>();
							newGroup.set("timers", invitetimer);
							
							// Update player info
							ConfigurationSection playerInfo = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
							playerInfo.set("group.name", argv[2]);
							playerInfo.set("group.title", "owner");
							
							plugin.chat.saveConfig();
							plugin.playerInfo.saveConfig();
						}
						else {
							player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Group already exists");
						}
					}
					else {
						player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] You're already in a group!");
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				
				
				break;
				
			case "party":
				/*		
				 * Checks
				 * - If command sender is already in a party
				 * - If name is specified (NO NAME)
				 */
				if (argv.length == 2) {
					// Check 
					if (plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat.party") == null) {
						ConfigurationSection newParty = plugin.chat.getConfig().createSection("parties." + player.getUniqueId().toString());
						List<String> members = new ArrayList<String>();
						members.add(player.getUniqueId().toString());
						newParty.set("members", members);
						newParty.set("count", 1);
						
						List<String> requests = new ArrayList<String>();
						newParty.set("requests", requests);
						
						List<String> invited = new ArrayList<String>();
						newParty.set("invited", invited);
						
						List<String> invitetimer = new ArrayList<String>();
						newParty.set("timers", invitetimer);
						
						// Update player info
						ConfigurationSection playerInfo = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
						playerInfo.set("party", player.getUniqueId().toString());
						
						plugin.chat.saveConfig();
						plugin.playerInfo.saveConfig();
					}
					else {
						player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] You're already in a party!");
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
			
		case "invite":
		case "kick": // /chat <invite/kick> <group/party> <player>
			/*
			 * Kick a member from the group/party
			 * Only owner can do this
			 * (Maybe group titles later on)
			 */
			
			// Check if player is in a group
			if (argv.length == 3) {
				switch(type) {
				case "group":
					/*
					 * Checks:
					 * - If command sender has permissions to deny
					 * - If player exists
					 * - If player has requested
					 */
					
					// Check if player is in a group
					color = plugin.config.getConfig().getString("general.chat.chatModes.group.color");
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("group")) {
						String groupName = p.getString("group.name");
						List<String> members = plugin.chat.getConfig().getStringList("groups." + groupName + ".members");
						
						// Check if player has permission to invite/kick
						if (titles.indexOf(p.getString("group.title")) <= titles.indexOf("moderator")) {
							Player member = null;
							try {
								member = player.getServer().getPlayer(argv[2]);
				    		}
				    		catch (Exception e) {
				    			player.sendMessage("Invalid player");
				    			break;
				    		}
							
							// Player is in the group
							if (members.contains(member.getUniqueId().toString())) {
								// Kick player
								if (command.equalsIgnoreCase("kick")) {
									members.remove(member.getUniqueId().toString());
									plugin.chat.getConfig().set("groups." + groupName + ".members", members);
									
									plugin.playerInfo.getConfig().set("players." + member.getUniqueId().toString() + ".chat.group", null);
									
									member.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've been kicked from " + groupName));
									updateMode(member, "all");
								}
								else { // accept
									player.sendMessage(ChatColor.RED + "Player is already in the group");
								}
							}
							// Player isn't in the group
							else {
								if (command.equalsIgnoreCase("invite")) {
									this.requestScheduler = new RequestScheduler(this.plugin, "group", groupName, player.getUniqueId().toString(), member.getUniqueId().toString());
									@SuppressWarnings("unused")
									BukkitTask task = requestScheduler.runTaskTimer(this.plugin, 0, 6000); // 6000 = 5 minute timer
	
									member.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've been invited to " + groupName+ ". Type /chat join group " + groupName + " to join"));
								}
								else { // kick
									player.sendMessage(ChatColor.RED + "Player isn't in the group");
								}
							}
							
							plugin.playerInfo.saveConfig();
							plugin.chat.saveConfig();
						}
						else {
							player.sendMessage(ChatColor.RED + "You do not have the required permissions");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a group");
					}
					break;
					
				case "party":
					/*
					 * Checks:
					 * - If command sender is owner
					 * - If player exists
					 * - If player has requested
					 */
					// Check if player is in a party
					color = plugin.config.getConfig().getString("general.chat.chatModes.party.color");
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("party")) {
						if (plugin.chat.getConfig().contains("parties." + player.getUniqueId())) {
							List<String> members = plugin.chat.getConfig().getStringList("parties." + player.getUniqueId().toString() + ".members");

							Player member = null;
							try {
								member = player.getServer().getPlayer(argv[2]);
				    		}
				    		catch (Exception e) {
				    			player.sendMessage("Invalid player");
				    			break;
				    		}
							
							// Player is in the party
							if (members.contains(member.getUniqueId().toString())) {
								// Kick player
								if (command.equalsIgnoreCase("kick")) {
									members.remove(member.getUniqueId().toString());
									plugin.chat.getConfig().set("parties." + player.getUniqueId() + ".members", members);
									plugin.playerInfo.getConfig().set("players." + member.getUniqueId().toString() + ".chat.party", null);
									plugin.chat.getConfig().set("parties." + player.getUniqueId().toString() + ".count", plugin.chat.getConfig().getInt("parties." + player.getUniqueId().toString() + ".count") - 1);
									plugin.chat.saveConfig();
									plugin.playerInfo.saveConfig();
									
									member.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've been kicked from the party"));
									break;
								}
								else { // accept
									player.sendMessage(ChatColor.RED + "Player is already in the group");
								}
							}
							// Player isn't in the party
							else {
								if (command.equalsIgnoreCase("invite")) {
									this.requestScheduler = new RequestScheduler(this.plugin, "party", "", player.getUniqueId().toString(), member.getUniqueId().toString());
									@SuppressWarnings("unused")
									BukkitTask task = requestScheduler.runTaskTimer(this.plugin, 0, 6000); // 6000 = 5 minute timer
	
									member.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've been invited to join " + stripName(player) + "'s party"));
								}
								else { // kick
									player.sendMessage(ChatColor.RED + "Player isn't in the group");
								}
							}
							
							plugin.playerInfo.saveConfig();
							plugin.chat.saveConfig();
						}
						else {
							player.sendMessage(ChatColor.RED + "You're not the party leader");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a party");
					}
					break;
					
				default:
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
					break;
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
			
		case "accept":
		case "deny": 
			/*
			 * Accept/Deny player invitation request
			 */
			
			/*
			 * Might be a bug if the player to be denied is offline
			 * Need to look into this further
			 */
			
			// Accept/deny join request
			if (argv.length == 3) { // /chat <accept/deny> <group/party> <player>
				switch(type) {
				case "group":
					/*
					 * Checks:
					 * - If command sender has permissions to deny
					 * - If player exists
					 * - If player has requested
					 */
					
					// Check if player is in a group
					color = plugin.config.getConfig().getString("general.chat.chatModes.group.color");
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("group")) {
						String groupName = p.getString("group.name");
						List<String> requests = plugin.chat.getConfig().getStringList("groups." + groupName + ".requests");
						
						// Check if player has permission to accept/deny requests
						if (titles.indexOf(p.getString("group.title")) <= titles.indexOf("moderator")) {
							Player requestPlayer = null;
							try {
								requestPlayer = player.getServer().getPlayer(argv[2]);
				    		}
				    		catch (Exception e) {
				    			player.sendMessage("Invalid player");
				    			break;
				    		}
							
							if (requests.contains(requestPlayer.getUniqueId().toString())) {
								requests.remove(requestPlayer.getUniqueId().toString());
								plugin.chat.getConfig().set("groups." + groupName + ".requests", requests);
								
								if (command.equalsIgnoreCase("accept")) {
									// Safety check incase player was accepted into another group
									if (plugin.playerInfo.getConfig().contains("players." + requestPlayer.getUniqueId().toString() + ".chat.group")) {
										player.sendMessage(ChatColor.RED + "Player belongs to another group");
										break;
									}
									
									List<String> members = plugin.chat.getConfig().getStringList("groups." + groupName + ".members");
									members.add(requestPlayer.getUniqueId().toString());
									plugin.chat.getConfig().set("groups." + groupName + ".members", members);
									plugin.chat.saveConfig();
									
									ConfigurationSection editPlayer = plugin.playerInfo.getConfig().createSection("players." + requestPlayer.getUniqueId().toString() + ".chat.group");
									editPlayer.set("title", "member");
									editPlayer.set("name", groupName);
									plugin.playerInfo.saveConfig();
									
									requestPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Your group request has been accepted"));
								}
								else {
									requestPlayer.sendMessage(ChatColor.RED + "Your group request has been denied");
								}
								
								// Save changes
								plugin.chat.saveConfig();
							}
							else {
								player.sendMessage(ChatColor.RED + "Couldn't find specified player");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "You do not have the required permissions");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a group");
					}
					break;
					
				case "party":
					/*
					 * Checks:
					 * - If command sender is owner
					 * - If player exists
					 * - If player has requested
					 */
					// Check if player is in a group
					color = plugin.config.getConfig().getString("general.chat.chatModes.party.color");
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("party")) {
						if (plugin.chat.getConfig().contains("parties." + player.getUniqueId())) {
							List<String> requests = plugin.chat.getConfig().getStringList("parties." + player.getUniqueId().toString() + ".requests");
							
							Player requestPlayer = null;
							try {
								requestPlayer = player.getServer().getPlayer(argv[2]);
				    		}
				    		catch (Exception e) {
				    			player.sendMessage("Invalid player");
				    			break;
				    		}
							
							if (requests.contains(requestPlayer.getUniqueId().toString())) {
								requests.remove(requestPlayer.getUniqueId().toString());
								plugin.chat.getConfig().set("parties." + player.getUniqueId().toString() + ".requests", requests);
								plugin.chat.saveConfig();
								
								if (command.equalsIgnoreCase("accept")) {
									// Safety check incase player was accepted into another group
									if (plugin.playerInfo.getConfig().contains("players." + requestPlayer.getUniqueId().toString() + ".chat.party")) {
										player.sendMessage(ChatColor.RED + "Player belongs to another party");
										break;
									}
									
									List<String> members = plugin.chat.getConfig().getStringList("parties." + player.getUniqueId().toString() + ".members");
									members.add(requestPlayer.getUniqueId().toString());
									plugin.chat.getConfig().set("parties." + player.getUniqueId().toString() + ".members", members);
									plugin.chat.getConfig().set("parties." + player.getUniqueId().toString() + ".count", plugin.chat.getConfig().getInt("parties." + player.getUniqueId().toString() + ".count") + 1);
									plugin.chat.saveConfig();

									plugin.playerInfo.getConfig().set("players." + requestPlayer.getUniqueId().toString() + ".chat.party", player.getUniqueId().toString());
									plugin.playerInfo.saveConfig();
									
									requestPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Your party request has been accepted"));
								}
								else {
									requestPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Your party request has been denied"));
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "Couldn't find specified player");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "You're not the party leader");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a party");
					}
					break;
					
				default:
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
					break;
				}
			}
			// Accept/deny invite request
			else if (argv.length == 2) {
				
			}
			else {
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
		
		case "members": // /chat members <group/party>
			// Display all members in group or party
			if (argv.length == 2) {
				switch(type) {
				case "group":
					// Check if player is in a group
					color = plugin.config.getConfig().getString("general.chat.chatModes.group.color");
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("group")) {
						String groupName = p.getString("group.name");
						List<String> members = plugin.chat.getConfig().getStringList("groups." + groupName + ".members");
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Group members"));
						
						for (String memberUUID : members) {
							String title = plugin.playerInfo.getConfig().getString("players." + memberUUID + ".chat.group.title");
							Player member = (Player) Bukkit.getOfflinePlayer(UUID.fromString(memberUUID));
							
							player.sendMessage("- " + stripName(member) + ", " + title);
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a group");
					}
					break;
					
				case "party":
					color = plugin.config.getConfig().getString("general.chat.chatModes.party.color");
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("party")) {
						String partyOwner = p.getString("party");
						List<String> members = plugin.chat.getConfig().getStringList("parties." + partyOwner + ".members");
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Party members"));
						for (String memberUUID : members) {
							Player member = (Player) Bukkit.getOfflinePlayer(UUID.fromString(memberUUID));
							
							player.sendMessage("- " + stripName(member));
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a party");
					}
					break;
					
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
		
		case "requests": // /chat requests <group/party>
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender has permissions to see list
				 */
				// Check if player is in a group
				p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
				if (p.contains("group")) {
					String groupName = p.getString("group.name");
					player.sendMessage(groupName);
					List<String> requests = plugin.chat.getConfig().getStringList("groups." + groupName + ".requests");
					
					// Check if player has permission to use command
					for (String t : requests) {
						player.sendMessage(t);
					}
					if (titles.indexOf(p.getString("group.title")) < 2) {
						if (requests.size() > 0) {
							player.sendMessage(ChatColor.AQUA + "Join requests for " + groupName);
							for (String request : requests) {
								player.sendMessage("- " + stripName(Bukkit.getServer().getPlayer(UUID.fromString(request))));
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "There are no current requests");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You do not have the required permissions");
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "You're not in a group");
				}
				break;
				
			case "party":
				/*
				 * Checks
				 * - If command sender is owner
				 */
				
				// Check if player is in a group
				p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
				if (p.contains("party")) {
					if (plugin.chat.getConfig().contains("parties." + player.getUniqueId())) {
						List<String> requests = plugin.chat.getConfig().getStringList("parties." + player.getUniqueId().toString() + ".requests");
						if (requests.size() > 0) {
							player.sendMessage(ChatColor.AQUA + "Join requests for your party");
							for (String request : requests) {
								player.sendMessage("- " + stripName(Bukkit.getServer().getPlayer(UUID.fromString(request))));
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "There are no current requests");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not the party leader");
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "You're not in a party");
				}
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "join": // /chat join <group/party> <group name/player name>
			/*
			 * Ask to join group
			 * If closed, need to be accepted
			 * If open, join right away
			 * 
			 * Checks:
			 * - If command sender is in a group
			 */
			
			if (argv.length == 3) {
				String name = argv[2];
				
				switch(type) {
				case "group":
					/*
					 * Player request will be saved in a list where any mod or owner can
					 * look at and accept at anytime
					 * 
					 * Checks
					 * - If command sender is in a group
					 * - If group exists
					 * - If command sender has already requested an invite
					 */
					
					color = plugin.config.getConfig().getString("general.chat.chatModes.group.color");
					if (!plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.group")) {
						if (plugin.chat.getConfig().contains("groups." + name)) {
							// Check if already invited
							List<String> invited = plugin.chat.getConfig().getStringList("groups." + name + ".invited");
							if (invited.contains(player.getUniqueId().toString())) {
								requestScheduler.removeListing("group", name, player.getUniqueId().toString());

								List<String> members = plugin.chat.getConfig().getStringList("groups." + name + ".members");
								members.add(player.getUniqueId().toString());
								plugin.chat.getConfig().set("groups." + name + ".members", members);
								
								plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.group.name", name);
								plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.group.title", "member");
								
								plugin.playerInfo.saveConfig();
								plugin.chat.saveConfig();
								
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've joined " + name));
								
								for (String memberUUID : plugin.chat.getConfig().getStringList("groups." + name + ".members"))
									Bukkit.getServer().getPlayer(UUID.fromString(memberUUID)).sendMessage(ChatColor.translateAlternateColorCodes('&', color + stripName(player) + color + " has joined the group"));
							}
							// Not invited, need to request
							else {
								String policy = plugin.chat.getConfig().getString("groups." + name + ".policy");
								if (policy.equalsIgnoreCase("closed")) {
									List<String> requests = plugin.chat.getConfig().getStringList("groups." + name + ".requests");
									if (!requests.contains(player.getUniqueId().toString())) {
										requests.add(player.getUniqueId().toString());
										plugin.chat.getConfig().set("groups." + name + ".requests", requests);
										plugin.chat.saveConfig();
										
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've requested to join " + name));
										
										for (String memberUUID : plugin.chat.getConfig().getStringList("groups." + name + ".members")) {
											if (titles.indexOf(plugin.playerInfo.getConfig().getString("players." + memberUUID + ".chat.group.title")) <= 1) {
												Player mod = (Player) Bukkit.getServer().getPlayer(UUID.fromString(memberUUID));
												mod.sendMessage(ChatColor.translateAlternateColorCodes('&', color + stripName(player) + " has requested to join the group"));
											}
										}
									}
									else {
										player.sendMessage(ChatColor.RED + "You've already requested to join this group");
									}
								}
								else {
									List<String> members = plugin.chat.getConfig().getStringList("groups." + name + ".members");
									members.add(player.getUniqueId().toString());
									plugin.chat.getConfig().set("groups." + name + ".members", members);
									
									plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.group.name", name);
									plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.group.title", "member");
									
									plugin.playerInfo.saveConfig();
									plugin.chat.saveConfig();
									
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've joined " + name));
									for (String memberUUID : plugin.chat.getConfig().getStringList("groups." + name + ".members"))
										Bukkit.getServer().getPlayer(UUID.fromString(memberUUID)).sendMessage(ChatColor.translateAlternateColorCodes('&', color + stripName(player) + color + " has joined the group"));
								}
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "Group doesn't exists");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're already in a group");
					}
					
					break;
					
				case "party":
					/*
					 * Player request will go directly to party owner
					 * Player request will timeout after 2 minutes if no response
					 * 
					 * Checks
					 * - If command sender is in a party
					 * - If party exists
					 * - If command sender has already requested an invite
					 */
					Player partyMember = null;
					try {
		    			partyMember = player.getServer().getPlayer(argv[2]);
		    		}
		    		catch (Exception e) {
		    			player.sendMessage("Invalid player");
		    			break;
		    		}
					
					color = plugin.config.getConfig().getString("general.chat.chatModes.party.color");
					if (!plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.party")) {
						if (plugin.chat.getConfig().contains("parties." + partyMember.getUniqueId().toString())) {
							if (plugin.chat.getConfig().getInt("parties." + partyMember.getUniqueId().toString() + ".count") <= plugin.config.getConfig().getInt("general.chat.maxpartycap")) {
								List <String> invited = plugin.chat.getConfig().getStringList("parties." + partyMember.getUniqueId().toString() + ".invited");
								// If already invited
								if (invited.contains(player.getUniqueId().toString())) {
									requestScheduler.removeListing("party", partyMember.getUniqueId().toString(), player.getUniqueId().toString());
									
									for (String memberUUID : plugin.chat.getConfig().getStringList("parties." + partyMember.getUniqueId().toString() + ".members"))
										Bukkit.getServer().getPlayer(UUID.fromString(memberUUID)).sendMessage(ChatColor.translateAlternateColorCodes('&', color + stripName(player) + color + " has joined the party"));
									
									List<String> members = plugin.chat.getConfig().getStringList("parties." + partyMember.getUniqueId().toString() + ".members");
									members.add(player.getUniqueId().toString());
									plugin.chat.getConfig().set("parties." + partyMember.getUniqueId().toString() + ".members", members);
									
									plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.party", partyMember.getUniqueId());
									plugin.chat.getConfig().set("parties." + partyMember.getUniqueId().toString() + ".count", plugin.chat.getConfig().getInt("parties." + partyMember.getUniqueId().toString() + ".count") + 1);
								
									plugin.playerInfo.saveConfig();
									plugin.chat.saveConfig();
									
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've joined " + name + "'s party"));
								}
								else {
									List<String> requests = plugin.chat.getConfig().getStringList("parties." + partyMember.getUniqueId().toString() + ".requests");
									if (!requests.contains(player.getUniqueId().toString())) {
										requests.add(player.getUniqueId().toString());
										plugin.chat.getConfig().set("parties." + partyMember.getUniqueId().toString() + ".requests", requests);
										plugin.chat.saveConfig();
										
										color = plugin.config.getConfig().getString("general.chat.chatModes.party.color");
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've requested to join " + stripName(partyMember) +"'s party"));
										partyMember.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Player " + stripName(player) + " has requested to join your party"));	
									}
									else {
										player.sendMessage(ChatColor.RED + "You've already requested to join this party");
									}
								}	
							}
							else {
								player.sendMessage(ChatColor.RED + "Party is full");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "Player either doesn't belong to a party or isn't the owner of it");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're already in a party");
					}
					break;
					
				default:
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
			}
			else {
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
			
		case "leave": // /chat leave <group/party>
			/*
			 * Leave the current active group
			 * If last in group it gets disbanded
			 */

			switch(type) {
			case "group":
				// Check if player is in a group
				if (plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.group")) {
					String groupName = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.group.name");
					String groupOwner = plugin.chat.getConfig().getString("groups." + groupName + ".ownerUUID");
					
					List<String> members = plugin.chat.getConfig().getStringList("groups." + groupName + ".members");
					
					// Player isn't group owner
					if (!player.getUniqueId().toString().equalsIgnoreCase(groupOwner)) {
						// Remove player from members
						members.remove(player.getUniqueId().toString());
						plugin.chat.getConfig().set("groups." + groupName + ".members", members);
						
						// Update player info
						plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.group", null);
						
						plugin.playerInfo.saveConfig();
						plugin.chat.saveConfig();
						
						for (String memberUUID : members) {
							Player member = (Player) Bukkit.getServer().getPlayer(UUID.fromString(memberUUID));
							color = plugin.config.getConfig().getString("general.chat.chatModes.group.color");
							member.sendMessage(ChatColor.translateAlternateColorCodes('&', color + stripName(player) + " has left the group"));
						}
						
						updateMode(player, "all");
					}
					// Player is group owner
					else {
						// Only owner left
						if (members.size() == 1) {
							// Remove group listing
							plugin.chat.getConfig().set("groups." + groupName, null);
							
							// Update player info
							plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.group", null);
							
							plugin.playerInfo.saveConfig();
							plugin.chat.saveConfig();
							
							updateMode(player, "all");
						}
						else {
							player.sendMessage(ChatColor.RED + "There are still members left in the group");
						}
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "You're not in a group");
				}
				/*
				 * Checks
				 * - If command sender is in a group
				 * - If owner, check if last, if so disband else deny leave request
				 */
				break;
				
			case "party":
				/*
				 * Checks
				 * - If command sender is in party
				 * - If owner, group gets disbanded
				 * - If member, just leave
				 */
				
				// Check if player is in a party
				color = plugin.config.getConfig().getString("general.chat.chatModes.party.color");
				if (plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.party")) {
					// Check if player is party owner
					String partyOwner = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.party");
					
					List<String> members = plugin.chat.getConfig().getStringList("parties." + partyOwner + ".members");
					
					// Player is party owner
					if (player.getUniqueId().toString().equalsIgnoreCase(partyOwner)) {
						// Update all party member information
						for (String memberUUID : plugin.chat.getConfig().getStringList("parties." + player.getUniqueId() + ".members")) {
							Player member = (Player) Bukkit.getServer().getPlayer(UUID.fromString(memberUUID));
							member.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "Party has been disbanded!"));
							
							plugin.playerInfo.getConfig().set("players." + memberUUID + ".chat.party", null);
						}
						
						plugin.chat.getConfig().set("parties." + partyOwner, null);

						plugin.playerInfo.saveConfig();
						plugin.chat.saveConfig();
					}
					// Player isn't party owner
					else {
						// Remove player from members
						members.remove(player.getUniqueId().toString());
						plugin.chat.getConfig().set("parties." + partyOwner + ".members", members);
						plugin.chat.getConfig().set("parties." + partyOwner + ".count", plugin.chat.getConfig().getInt("parties." + partyOwner + ".count") - 1);
						
						// Update player info
						plugin.playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.party", null);
						
						plugin.playerInfo.saveConfig();
						plugin.chat.saveConfig();
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "You're not in a party");
				}
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "append": // /chat append group <owner/moderator/member> <player>
			/*
			 * Append a player a title
			 * Owner can give any title (Even owner)
			 * Title ideas:
			 * - Owner, Moderator, Member
			 * 
			 * Party
			 * - Append new party owner
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender is in a group
				 * - If command sender has permissions to append title
				 *  - Get list of titles, if player title index is smaller they have permission
				 */
				if (argv.length == 4) {
					// Check if player is in a group
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("group")) {
						String groupName = p.getString("group.name");
						List<String> groupMembers = plugin.chat.getConfig().getStringList("groups." + groupName + ".members");

						// Check if player has permission to give specified title
						if (titles.indexOf(p.getString("group.title")) < titles.indexOf(argv[2].toLowerCase()) || titles.indexOf(p.getString("group.title")) == 0) {
							Player groupMember = null;
							try {
								groupMember = player.getServer().getPlayer(argv[3].toLowerCase());
				    		}
				    		catch (Exception e) {
				    			player.sendMessage("Invalid player");
				    			break;
				    		}
							
							if (groupMember.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
								player.sendMessage(ChatColor.RED + "You can't give yourself a new title");
								break;
							}
							
							// Check if member is a member
							if (groupMembers.contains(groupMember.getUniqueId().toString())) {
								r = plugin.playerInfo.getConfig().getConfigurationSection("players." + groupMember.getUniqueId().toString() + ".chat");
								color = plugin.config.getConfig().getString("general.chat.chatModes.group.color");
								
								// Check if player is owner
								if (titles.indexOf(p.getString("group.title")) == 0) {
									
									// Owner appending another owner
									if (argv[2].toLowerCase().equalsIgnoreCase("owner")) {
										plugin.chat.getConfig().set("groups." + groupName + ".ownerUUID", groupMember.getUniqueId().toString());
										r.set("group.title", "owner");
										p.set("group.title", "moderator");
										
										plugin.chat.saveConfig();
										plugin.playerInfo.saveConfig();
										
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You made " + stripName(groupMember) + " owner of the group"));
										groupMember.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You've been made owner of the group"));
									}
									// Owner appending any other title
									else {
										r.set("group.title", argv[2].toLowerCase());
										plugin.playerInfo.saveConfig();
										
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You made " + stripName(groupMember) + " " + argv[2]));
										groupMember.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You received the title " + argv[2]));
									}
								}
								else {
									// Player is below owner
									// Check if player permission is above recipient permissions
									if (titles.indexOf(p.getString("group.title")) < titles.indexOf(r.getString("group.title"))) {
										r.set("group.title", argv[2].toLowerCase());
										plugin.playerInfo.saveConfig();
										
										player.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You made " + stripName(groupMember) + " " + argv[2]));
										groupMember.sendMessage(ChatColor.translateAlternateColorCodes('&', color + "You received the title " + argv[2]));
									}
									else {
										player.sendMessage(ChatColor.RED + "You do not have the correct permissions");
									}
								}
							}
							else {
								player.sendMessage(ChatColor.RED + "Player is not in your group");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "You do not have permissions to do this");
						}
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a group");
					}
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
					
				}
				break;

			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "edit": // /chat edit group <motd/policy/description/name> <new val>
			/*
			 * Edit group 
			 * - MOTD (MODS AND ABOVE)
			 * - Policy (ONLY OWNER)
			 *   - Open
			 *   - Closed
			 * - Description (MODS AND ABOVE)
			 */
			
			switch(type) {
			case "group":
				/*
	 			 * Checks
				 * - If command sender is in a group
				 * - If command sender has permissions to edit
				 * - If edit option is valid
				 */
				if (argv.length >= 4) {
					String option, groupName;
					option = argv[2].toLowerCase();
					
					p = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
					if (p.contains("group")) {
						groupName = p.getString("group.name");
					}
					else {
						player.sendMessage(ChatColor.RED + "You're not in a group!");
						break;
					}
					
					for (int i = 3; i < argv.length; i++)
						msg += argv[i] + " ";
					msg = msg.substring(0, msg.length() - 1);
					
					player.sendMessage(msg + "EXTRA");
					
					ConfigurationSection group = plugin.chat.getConfig().getConfigurationSection("groups." + groupName);
					switch(option) {
					case "motd":
						// Check if player has permission to give specified title
						if (titles.indexOf(p.getString("group.title")) <= titles.indexOf("moderator")) {
							group.set("MOTD", msg);
							player.sendMessage(ChatColor.GREEN + "Updated MOTD to: " + msg);
						}
						else {
							player.sendMessage(ChatColor.RED + "Invalid permissions");
						}
						break;
					case "policy":
						// Check if player has permission to give specified title
						if (titles.indexOf(p.getString("group.title")) == titles.indexOf("owner")) {
							if ((msg.equalsIgnoreCase("open") || msg.equalsIgnoreCase("closed"))) {
								group.set("policy", msg);
								player.sendMessage(ChatColor.GREEN + "Changed policy to: " + msg);
							}
							else {
								player.sendMessage(ChatColor.RED + "Invalid policy");
							}
						}
						else {
							player.sendMessage(ChatColor.RED + "Invalid permissions");
						}
						break;
					case "description":
						// Check if player has permission to give specified title
						if (titles.indexOf(p.getString("group.title")) <= titles.indexOf("moderator")) {
							group.set("description", msg);
							player.sendMessage(ChatColor.GREEN + "Updated group description to: " + msg);
						}
						else {
							player.sendMessage(ChatColor.RED + "Invalid permissions");
						}
						break;
					}
					plugin.chat.saveConfig();
				}
				break;

			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		default:
			switch (label) {
			/*
			 * Region, all, Shout, Whisper, Party, Group, Local
			 * Region - White, Overworld, Nether, The End
			 * All - White, Entire server
			 * Shout - Yellow/Gold, Everyone within 1000 blocks
			 * Whisper - Purple, 
			 * Party - Blue
			 * Group - Green
			 * Local - White
			 */	
			case "all":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "a": // /<a/all> <msg>
				if (argv.length >= 1) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "all");

					// Get list of all online players
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("general.chat.chatModes.all.color") + msg);
						onlinePlayer.sendMessage(player.getDisplayName() + ": " + msg);
					}
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "region":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "r": // /<r/region> <msg>
				if (argv.length >= 1) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "region");
					
					for (Player regionPlayer: Bukkit.getOnlinePlayers()) {
						// If inside same region (world)
						if (regionPlayer.getWorld() == player.getWorld()) {
							msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("general.chat.chatModes.region.color") + msg);
							regionPlayer.sendMessage(player.getDisplayName() + ": " + msg);
					
						}
					}
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "shout":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "s": // /s <msg>
				if (argv.length >= 1) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "shout");
					
					for (Player shoutPlayer : Bukkit.getOnlinePlayers()) {
						if (shoutPlayer.getLocation().distance(player.getLocation()) < plugin.config.getConfig().getInt("general.chat.shoutdistance")) {
							msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("gnereal.chat.chatModes.shout.color") + msg);
							shoutPlayer.sendMessage(player.getDisplayName() + ": " + msg);				
						}
					}
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "whisper": 
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0) {
					if (plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.lastsent").isEmpty()) {
						player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Found no last recipient");
					}
				}
					break;
			case "w": // /w <player> <msg>			
				if (argv.length >= 2) {
					if (player.getServer().getPlayer(argv[0]) == null) {
						player.sendMessage("[" + ChatColor.RED + "ERROR" + ChatColor.WHITE + "] Player '" + argv[0] + "' doesn't exist or is offline");
						break;
					}
					
					Player receiver = player.getServer().getPlayer(argv[0]);

					for (int i = 1; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "whisper");
					
					receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("general.chat.chatModes." + label + ".color") + stripName(player) + " whispers to you: " + msg));
				
					plugin.playerInfo.getConfig().set("players." + player.getUniqueId() + ".chat.lastsent", receiver.getUniqueId().toString());
					plugin.playerInfo.saveConfig();
					
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "group":
				if (!plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.group")) {
					player.sendMessage("You're not in a group");
					break;
				}
				
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "g":
				if (argv.length >= 1) {
					if (!plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.group")) {
						player.sendMessage("You're not in a group");
						break;
					}
					
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "group");
					
					String groupName = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.group.name");
					List<String> groupMembers = plugin.chat.getConfig().getStringList("groups." + groupName + ".members");
					
					for (String memberUUID : groupMembers) {
						Player groupMember = null;
						try {
			    			groupMember = player.getServer().getPlayer(UUID.fromString(memberUUID));
			    		}
			    		catch (Exception e) {
			    			continue;
			    		}
						
						groupMember.sendMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + ": " + plugin.config.getConfig().getString("general.chat.chatModes.group.color") + msg));
					}
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "party":
				if (!plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.party")) {
					player.sendMessage("You're not in a party");
					break;
				}
				
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "p":
				if (argv.length >= 1) {
					if (!plugin.playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.party")) {
						player.sendMessage("You're not in a party");
						break;
					}
					
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "party");
					
					String partyOwner = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.party");
					List<String> partyMembers = plugin.chat.getConfig().getStringList("parties." + partyOwner + ".members");
					
					for (String memberUUID : partyMembers) {
						Player partyMember = null;
						try {
			    			partyMember = player.getServer().getPlayer(UUID.fromString(memberUUID));
			    		}
			    		catch (Exception e) {
			    			continue;
			    		}
						
						partyMember.sendMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + ": " + plugin.config.getConfig().getString("general.chat.chatModes.party.color") + msg));
					}
					
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
				
			case "local":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "l":
				if (argv.length >= 1) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String curMode = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId() + ".chat.mode").toLowerCase();
			    	updateMode(player, "local");
					
					for (Player localPlayer : Bukkit.getOnlinePlayers()) {
						if (localPlayer.getLocation().distance(player.getLocation()) < plugin.config.getConfig().getInt("general.chat.shoutdistance")) {
							localPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + ": " + plugin.config.getConfig().getString("general.chat.chatModes.local.color") + msg));				
						}
					}
					updateMode(player, curMode);
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
		}
	}
}