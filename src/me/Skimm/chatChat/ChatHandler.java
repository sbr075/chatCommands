package me.Skimm.chatChat;

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
			if (section.getString(".chat.mode").equalsIgnoreCase(mode))
				player.sendMessage("[" + ChatColor.RED + "WARNING" + ChatColor.WHITE + "] You're already in specified mode");

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
		if (argv.length >= 2) {
			command = argv[0].toLowerCase();
			type = argv[1].toLowerCase();
			return;
		}
		
		
		section = plugin.playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId().toString());
		
		switch(command) {
		case "list": // /chat list <modes/groups>
			/*
			 * List all available chat modes
			 */
			
			switch(type) {
			case "modes":
				break;
			case "groups":
				break;
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
		
		case "create": // /chat create <group/party> <name>
			/*
			 * Create a group/party
			 */
			
			switch(type) {
			case "group":
				/*
				 * Checks
				 * - If command sender is already in a group
				 * - If group name already exists
				 * - If name is specified (NEED NAME)
				 */
				break;
				
			case "party":
				/*
				 * Checks
				 * - If command sender is already in a party
				 * - If name is specified (NO NAME)
				 */
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
			 * 
			 * Checks
			 * - If mode exists
			 * - If player is already in specified mode (DO NOTHING, just inform)
			 */	
			case "all":
				if (!updateMode(player, label)) 
					break;
				
				if (argv.length == 0)
					break;
			case "a": // /a <msg>
				if (argv.length >= 2) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";

					// Get list of all online players
					for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
						onlinePlayer.sendMessage(player.getDisplayName() + ":" + msg);
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
			case "r": // /r <msg>
				if (argv.length >= 2) {
					for (int i = 0; i < argv.length; ++i)
						msg += argv[i] + " ";
					
					for (Player regionPlayer: Bukkit.getOnlinePlayers()) {
						// If inside same region (world)
						if (regionPlayer.getWorld() == player.getWorld())
							regionPlayer.sendMessage(player.getDisplayName() + ":" + msg);
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
						if (shoutPlayer.getLocation().distance(player.getLocation()) < plugin.chatConfig.getConfig().getInt("general.shoutdistance"))
							shoutPlayer.sendMessage(player.getDisplayName() + ":" + msg);				
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
					
					receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.chatConfig.getConfig().getString("chatModes.whisper.color") + stripName(player) + " whispers to you: " + msg));
				
					plugin.playerInfo.getConfig().set("players." + player.getUniqueId() + ".chat.lastsent", receiver.getUniqueId().toString());
					plugin.playerInfo.saveConfig();
				}
				else {
					player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
				}
				break;
				
			case "g":
				for (int i = 0; i < argv.length; ++i)
					msg += argv[i] + " ";
				break;
				
			case "p":
				for (int i = 0; i < argv.length; ++i)
					msg += argv[i] + " ";
				break;
				
			case "l":
				for (int i = 0; i < argv.length; ++i)
					msg += argv[i] + " ";
				break;
			default:
				player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			}
			break;
		}
	}
}