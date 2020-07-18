package me.Skimm.chatChat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.Skimm.chatCommands.*;
import net.md_5.bungee.api.ChatColor;

public class ChatHandler  {
	private Main plugin;
	ConfigurationSection section;
	
	public ChatHandler(Main plugin) {
		this.plugin = plugin;
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
		String command = "", type = "", msg = "";
		if (argv.length >= 2 && label.equalsIgnoreCase("chat")) {
			command = argv[0].toLowerCase();
			type = argv[1].toLowerCase();
		}
		
		//player.sendMessage("label: " + label + "\ncommand: " + command + "\ntype: " + type);

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
							newGroup.set("MOTD", "Welcome to " + argv[2]);
							newGroup.set("policy", "closed");
							
							List<String> members = new ArrayList<String>();
							members.add(player.getUniqueId().toString());
							newGroup.set("members", members);
							
							// Update player info
							ConfigurationSection playerInfo = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".chat");
							playerInfo.set("group", argv[2]);
							
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
					if (plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString() + ".party") == null) {
						ConfigurationSection newParty = plugin.chat.getConfig().createSection("parties." + player.getUniqueId().toString());
						List<String> members = new ArrayList<String>();
						members.add(player.getUniqueId().toString());
						newParty.set("members", members);
						
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
			/*
			 * Invite a player to the group/party
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender has permissions to invite
				 * - If group is full (MAX 20)
				 * - If player exists
				 * - If player is already in a group
				 */
				break;
				
			case "party":
				/*
				 * Checks
				 * - If party is full (MAX 5)
				 * - If player exists
				 * - If player is already in a party
				 */
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "kick":
			/*
			 * Kick a member from the group/party
			 * Only owner can do this
			 * (Maybe group titles later on)
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender has permissions to kick
				 * - If player is in group
				 */
				break;
				
			case "party":
				/*
				 * Checks
				 * - If command sender is the owner
				 * - If player is in the party
				 */
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "accept":
			/*
			 * Accept player invitation request
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender has permissions to accept
				 * - If player exists
				 * - If player has requested
				 */
				
				break;
				
			case "party":
				/*
				 * Checks
				 * - If command sender is owner
				 * - If player exists
				 * - If player has requested
				 */
				
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "deny":
			/*
			 * Deny player invitation request
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks:
				 * - If command sender has permissions to deny
				 * - If player exists
				 * - If player has requested
				 */
				break;
				
			case "party":
				/*
				 * Checks:
				 * - If command sender is owner
				 * - If player exists
				 * - If player has requested
				 */
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "leave":
			/*
			 * Leave the current active group
			 * If last in group it gets disbanded
			 */

			switch(type) {
			case "group":
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
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "append":
			/*
			 * Append a player a title
			 * Owner can give any title (Even owner)
			 * Title ideas:
			 * - Owner, Moderator, Member
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender is in a group
				 * - If command sender has permissions to append title
				 *  - Get list of titles, if player title index is smaller they have permission
				 */
				break;

			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "edit":
			/*
			 * Edit group 
			 * - MOTD (MODS AND ABOVE)
			 * - Hostility (ONLY OWNER)
			 *   - PvE or PvP
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
				 * - If command sender is in a group
				 * - If command sender has permissions to edit
				 * - If edit option is valid
				 */
				break;

			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				break;
			}
			break;
			
		case "join":
			/*
			 * Ask to join group
			 * If closed, need to be accepted
			 * If open, join right away
			 * 
			 * Checks:
			 * - If command sender is in a group
			 */
			
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
				break;
				
			case "party":
				/*
				 * Player request will go directly to party owner
				 * Player request will timeout after 2 minutes if no response
				 * 
				 * Checks
				 * - If command sender is in a party
				 * - If party exists
				 * - If command sender ahs already requested an invite
				 */
				break;
				
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
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

					// Get list of all online players
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("general.chat.chatModes.all.color") + msg);
						onlinePlayer.sendMessage(player.getDisplayName() + ": " + msg);
					}
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
					
					for (Player regionPlayer: Bukkit.getOnlinePlayers()) {
						// If inside same region (world)
						if (regionPlayer.getWorld() == player.getWorld()) {
							msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("general.chat.chatModes.region.color") + msg);
							regionPlayer.sendMessage(player.getDisplayName() + ": " + msg);
					
						}
					}
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
					
					for (Player shoutPlayer : Bukkit.getOnlinePlayers()) {
						if (shoutPlayer.getLocation().distance(player.getLocation()) < plugin.config.getConfig().getInt("general.chat.shoutdistance")) {
							msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("gnereal.chat.chatModes.shout.color") + msg);
							shoutPlayer.sendMessage(player.getDisplayName() + ": " + msg);				
						}
					}
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
					
					receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("general.chat.chatModes." + label + ".color") + stripName(player) + " whispers to you: " + msg));
				
					plugin.playerInfo.getConfig().set("players." + player.getUniqueId() + ".chat.lastsent", receiver.getUniqueId().toString());
					plugin.playerInfo.saveConfig();
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "group":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "g":
				if (argv.length >= 1) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String groupName = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.group");
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
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
			
			case "party":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "p":
				if (argv.length >= 1) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					String partyOwner = plugin.playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.party");
					List<String> partyMembers = plugin.chat.getConfig().getStringList("parties." + partyOwner);
					
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
					
					for (Player localPlayer : Bukkit.getOnlinePlayers()) {
						if (localPlayer.getLocation().distance(player.getLocation()) < plugin.config.getConfig().getInt("general.chat.shoutdistance")) {
							msg = ChatColor.translateAlternateColorCodes('&', plugin.config.getConfig().getString("gnereal.chat.chatModes.local.color") + msg);
							localPlayer.sendMessage(player.getDisplayName() + ": " + msg);				
						}
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
		}
	}
}