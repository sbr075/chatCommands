package me.Skimm.chatChat;

import org.bukkit.entity.Player;

import me.Skimm.chatCommands.*;
import net.md_5.bungee.api.ChatColor;

public class ChatHandler {
	private Main main;
	
	public ChatHandler(Main plugin) {
		this.main = plugin;
	}
	
	public void commandHandler(Player player, String label, String argv[]) {
		// Save command to string
		String command, type;
		if (argv.length < 2) {
			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /chat help for more information");
			return;
		}
		
		command = argv[0].toLowerCase();
		type = argv[1].toLowerCase();
		
		switch(command) {
		case "mode": // /chat mode <new mode>
			/*
			 * Global, Shout, Whisper, Party, Group, Local
			 * Global - White
			 * Shout - Yellow/Gold
			 * Whisper - Purple
			 * Party - Blue
			 * Group - Green
			 * Local - White
			 * 
			 * Checks
			 * - If mode exists
			 * - If player is already in specified mode (DO NOTHING, just inform)
			 */
			break;
			
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
				break;
			}
			break;
			
		default:
			break;
		}
	}
}