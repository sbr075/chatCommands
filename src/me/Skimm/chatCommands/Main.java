package me.Skimm.chatCommands;

import me.Skimm.chatEmotes.*;
import me.Skimm.chatMod.ModHandler;
import me.Skimm.chatTitles.TitleHandler;
import me.Skimm.chatBroadcast.*;
import me.Skimm.chatChat.ChatHandler;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Known issues
 * 1. emote list is none functional
 * 2. emote admin not giving admin priviliges
 */

/*
 * Planned features
 * 1. Cooldown on commands
 * 
 * 2. Broadcast - IMPLEMENTED
 *    1. One time
 *       1. Send (msg)
 *    2. Interval
 *       1. Add (name, msg, interval, duration)
 *       2. Remove (name)
 *       3. Edit (duration or msg)
 *    3. Sound (?)
 *    4. Broadcast info
 *       1. Name
 *       2. Message
 *       4. Who made iet
 *    5. List of all broadcasts
 *       
 * 3. Moderator utilities
 *    1. Warn
 *       1. Log for keeping track of warnings
 *    2. List to show all warned players
 *       1. Player UUID and display name
 *       2. Show warning description
 *       3. Show number of warnings
 *    3. Timeouts
 *       1. Mute
 *       2. Prevent join access
 *    4. Info
 *       1. Who warned
 *       2. Who got warned
 *       3. When was played warned
 *       4. Reason
 *    5. Log all
 * 4. Chat system
 *    1. Chat modes
 *       1. Whisper
 *       2. Local
 *       3. Shout
 *       4. Global
 *       5. Muted/unmuted
 *    2. Group/party chat
 *       1. Group is permanent
 *          1. Group leader
 *             1. Remove others
 *	           2. Disband
 *	              1. Deletes group
 *	           3. Can't leave, needs to disband
 *          2. Group roles
 *             1. Owner
 *                1. All perms
 *             2. Trusted
 *                2. Invite
 *             3. Member
 *                3. Leave
 *       2. Party is temporary
 *          1. Leave
 *       3. Add/invite and remove/kick
 *    3. Turned on/off
 * 5. Titles
 *    1. Permissions can be tied to them (?)
 * 6. Logs
 *    1. Block break
 *    2. Player open chest
 */

/*
 * TODO
 * 1. Clean up
 */

public class Main extends JavaPlugin implements Listener {
	// All config files
	public ConfigManager emote;
	public ConfigManager permissions;
	public ConfigManager commands;
	public ConfigManager broadcast;
	
	// All feature packages
	private EmoteHandler emoteCommands;
	private BroadcastHandler broadcastCommands;
	private ChatHandler chatCommands;
	private ModHandler modCommands;
	private TitleHandler titleCommands;

    @Override
    public void onEnable() {
    	// Initialize config files
    	this.emote = new ConfigManager(this, "emotes.yml");
    	this.permissions = new ConfigManager(this, "permissions.yml");
    	this.commands = new ConfigManager(this, "commands.yml");
    	this.broadcast = new ConfigManager(this, "broadcast.yml");
    	
    	// Initialize packages
    	this.emoteCommands = new EmoteHandler(this);
    	this.broadcastCommands = new BroadcastHandler(this);
    	this.chatCommands = new ChatHandler(this);
    	this.modCommands = new ModHandler(this);
    	this.titleCommands = new TitleHandler(this);
    	
    	getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Used during shutdown and reloads
    }
    
    // Change chat format from <name> text to name: text
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
    	e.setFormat("%s: %s"); 
    }
    
    // When player joins
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    	Player player = event.getPlayer();
    	
    	// Check player titles
    	// If op give Admin which gives all trusted and user permissions + admin permissions
    	// If Trusted give all user permissions + trusted permissions
    	// Owner has all permissions
    	// If user already has permission on title given, don't give (prevent duplicates)
    	
    	// If player doesn't exist in registered players
    	if (permissions.getConfig().getConfigurationSection("players." + player.getUniqueId()) == null) {
    		// Set default permissions
    		ConfigurationSection newPlayer = permissions.getConfig().createSection("players." + player.getUniqueId().toString());

    		String title;
    		// If player is op, assign admin permissions
    		if (player.isOp()) {
    			newPlayer.set("title", "admin");
    			title = ChatColor.translateAlternateColorCodes('&', "[" + permissions.getConfig().getString("permissions.titles.names.admin.color") + "ADMIN&f] " + player.getDisplayName());
    		}
    		else {
    			newPlayer.set("title", "user");
    			title = ChatColor.translateAlternateColorCodes('&', "[" + permissions.getConfig().getString("permissions.titles.names.user.color") + "USER&f] " + player.getDisplayName());
    		}
    		player.setDisplayName(title);
    		
    		// Set up cooldown section
    		newPlayer.set("cooldown", "0");

    		// Notify player
    		player.sendMessage("Player data has been created!");
    		permissions.saveConfig();
    	}
    	else {
    		permissions.saveDefaultConfig();
    	}
    }
    
    /*
     * Check player permission against command they wish to execute
     * player - player who sent request
     * label - i.e. emote
     * command - i.e. help
     */
    
    private ArrayList<String> getPermList(Player player) {
    	// Fetch player title from config file
    	String playerTitle = permissions.getConfig().getString("players." + player.getUniqueId() + ".title");
    	
    	ArrayList<String> playerPerms = new ArrayList<String>();
    	
		// Iterate through all extra permissions and add them to playerPerms
    	while (permissions.getConfig().getConfigurationSection("permissions.titles." + playerTitle + ".parent") != null) {
    		// Add perms from current title
    		ArrayList<String> titlePerms = (ArrayList<String>) permissions.getConfig().getStringList("permissions.titles." + playerTitle + ".perms");
    		
    		// Remove duplicates from titlePerms
    		titlePerms.removeAll(playerPerms);
    		
    		// Add non duplicates to playerPerms
    		playerPerms.addAll(titlePerms);
    		
    		// Set playerTitle to extra player title
    		playerTitle = permissions.getConfig().getString("permissions.titles" + playerTitle + ".parent");
    	}
    	
    	// Will fail current title if extra is null
    	
    	if (permissions.getConfig().getConfigurationSection("permissions.titles." + playerTitle + ".parent") == null) {
    		if (permissions.getConfig().getConfigurationSection("permissions.titles." + playerTitle + ".perms") != null) {
    			// Add perms from current title
        		ArrayList<String> titlePerms = (ArrayList<String>) permissions.getConfig().getStringList("permissions.titles." + playerTitle + ".perms");
        		
        		// Remove duplicates from titlePerms
        		titlePerms.removeAll(playerPerms);
        		
        		// Add non duplicates to playerPerms
        		playerPerms.addAll(titlePerms);
    		}
    	}
    	
    	if (playerPerms.isEmpty())
    		player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + playerTitle + "' has no permissions");
    	
    	return playerPerms;
    }
    
    public boolean checkPermissions(Player player, String label, String command) {
    	if (player.isOp())
    		return true;
    	
    	// label = emote, broadcast ect.
    	// command = help, send, use ect.
    	
    	player.sendMessage("Checking permissions: permissions." + label + ".commands." + command);
    	// get player title, check title permissions and compare against command permissions
    	// Compare lists and check if list is 0,
    	// if 0 no permission else permission
    	// Check users permissions
    	
    	ArrayList<String> playerPerms = getPermList(player);
    	if (playerPerms.isEmpty())
    		return false;
    	
    	ArrayList<String> cmdPerms = (ArrayList<String>) permissions.getConfig().getStringList("permissions." + label + ".commands." + command);
    	
    	if (playerPerms.retainAll(cmdPerms)) {
    		return true;
    	}
    	return false;
    }
    
    /* 
     * Check if player have specific permission
     */
    public boolean checkSpecificPermission(Player player, String perm) {
    	if (player.isOp())
    		return true;
    	
    	ArrayList<String> playerPerms = getPermList(player);
    	if (playerPerms.isEmpty() || !playerPerms.contains(perm))
    		return false;

    	return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        if (argv.length == 0) {
        	sender.sendMessage("Invalid use of command");
        	return true;
        }
        
        // Check if player
    	if (sender instanceof Player) {
    		// Cast sender to player
    		Player player = (Player) sender;
    		
    		// Check permissions of player
    		if (!checkPermissions(player, label, argv[0].toLowerCase())) {
    			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command.\n If you believe this is a mistake contact a server administrator");
    			return true;
    		}
    		
    		String command = argv[0].toLowerCase();
    		
    		/* 
    		 * Checks general functions, if not match
    		 * it will check special functions for given label
    		 */
    		
    		switch (command) {
    		case "help":
    			if (argv.length == 1) {
    				// titleName - user, admin
    				player.sendMessage("label: " + label);
    				for (String titleName : commands.getConfig().getConfigurationSection("commands." + label).getKeys(false)) {
    					// Prevent normal players from seeing admin help tab
    					if (titleName.equalsIgnoreCase("admin")) {
    						if (!checkSpecificPermission(player, label + ".admin"))
    							break;
    					}
    					player.sendMessage(" ");
    					// key1 - info, <label>
    					for (String labelName : commands.getConfig().getConfigurationSection("commands." + label + "." + titleName).getKeys(false)) {
    						// key2 - description, usage
    						for (String infoName: commands.getConfig().getConfigurationSection("commands." + label + "." + titleName + "." + labelName).getKeys(false)) {
    							player.sendMessage(ChatColor.translateAlternateColorCodes('&', commands.getConfig().getString("commands." + label + "." + titleName + "." + labelName + "." + infoName)));
    						}
                		}
    				}
        		}
        		else {
        			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /" + label + " help for more information");
        		}
    			break;
    		// This is exclusively an "emote" feature for now
    		case "permlist":
        		if (argv.length == 1) {
        			player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Permission name list");
        			List<String> allPerms = permissions.getConfig().getStringList("permissions." + label + ".names");
        			
        			for (String key : allPerms) {
        				player.sendMessage("- " + ChatColor.AQUA + key);
        			}
        		}
        		else {
        			player.sendMessage(ChatColor.RED + "Invalid use of command. Type /emote help for more information");
        		}
        		break;
    		
    		default: // Special functions
	    		switch (label) {
	        	case "emote":
	        		emoteCommands.commandHandler(player, label, argv);
	        		break;
	        		
	        	case "broadcast":
	        		broadcastCommands.commandHandler(player, label, argv);
	        		break;
	        	
	        	case "mod":
	        		modCommands.commandHandler(player, label, argv);
	        		player.sendMessage("This feature is not yet implemented");
	        		break;
	        	
	        	case "chat":
	        		chatCommands.commandHandler(player, label, argv);
	        		player.sendMessage("This feature is not yet implemented");
	        		break;
	        	
	        	case "title":
	        		titleCommands.commandHandler(player, label, argv);
	        		break;
	        	
	        	default:
	        		player.sendMessage(ChatColor.RED + "Command not found!");
	        		break;
	        	}
    		}
    	}
    	else {
        	sender.sendMessage(ChatColor.RED + "[ERROR] " + "Console can't use these commands!");
            return true;
            // Console
        }
        
        return true;
    }
}