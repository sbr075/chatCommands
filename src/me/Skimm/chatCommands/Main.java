package me.Skimm.chatCommands;

import me.Skimm.chatEmotes.*;
import me.Skimm.chatMod.ModHandler;
import me.Skimm.chatTitles.TitleHandler;
import me.Skimm.chatBroadcast.*;
import me.Skimm.chatChat.ChatHandler;

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
 * 2. emote admin not giving admin priviliges
 */

/*
 * Planned features
 * 1. Cooldown on commands
 * 2. Broadcast
 *    1. One time
 *       1. Send (msg)
 *    2. Interval
 *       1. Add (name, msg, interval)
 *       2. Remove (name)
 *       3. Edit (name, msg or interval)
 *    3. Sound (?)
 *    4. Broadcast info
 *       1. Name
 *       2. Message
 *       3. Interval
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
	public ConfigManager emote;
	public ConfigManager permissions;
	public ConfigManager commands;
	public ConfigManager broadcast;

	private EmoteHandler emoteCommands;
	private BroadcastHandler broadcastCommands;
	private ChatHandler chatCommands;
	private ModHandler modCommands;
	private TitleHandler titleCommands;

    @Override
    public void onEnable() {
    	this.emote = new ConfigManager(this, "emotes.yml");
    	this.permissions = new ConfigManager(this, "permissions.yml");
    	this.commands = new ConfigManager(this, "commands.yml");
    	this.broadcast = new ConfigManager(this, "broadcast.yml");
    	
    	this.emoteCommands = new EmoteHandler(this);
    	this.broadcastCommands = new BroadcastHandler(this);
    	this.chatCommands = new ChatHandler(this);
    	this.modCommands = new ModHandler(this);
    	this.titleCommands = new TitleHandler(this);
    	
    	getServer().getPluginManager().registerEvents(this, this);
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
    		// Set default permissions
    		ConfigurationSection newPlayer = permissions.getConfig().createSection("players." + player.getUniqueId().toString());
    		List<String> defaultPerms = permissions.getConfig().getStringList("permissions.default_user");
    		for (String key : defaultPerms) {
    			newPlayer.set("perms." + key, key);
    		}
    		
    		if (player.isOp()) {
    			List<String> defaultAdmin = permissions.getConfig().getStringList("permissions.default_admin");
    			for (String key : defaultAdmin) {
    				newPlayer.set("perms." + key, key);
    			}
    		}
    		
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
    public boolean checkPermissions(Player player, String label, String command) {
    	if (player.isOp())
    		return true;
    	
    	// Check users permissions
    	List<String> reqPerms = permissions.getConfig().getStringList("permissions." + label + ".commands." + command);
    	for (String perm : reqPerms) {
    		//player.sendMessage("Checking " + perm + " in players." + player.getUniqueId() + ".perms." + label);
    		if (permissions.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".perms." + label).contains(perm.split("\\.")[1]))
    			return true;
    	}
    	return false;
    }
    
    /* 
     * Check if player have specific permission
     * player - player who sent request
     * perm - i.e. "emote.help"
     *   tokens[0] = "emote"
     *   tokens[1] = "help"
     */
    public boolean checkSpecificPermission(Player player, String perm) {
    	if (player.isOp())
    		return true;
    	
    	String[] tokens = perm.split("\\.");
    	if (permissions.getConfig().getConfigurationSection("players." + player.getUniqueId() + ".perms." + tokens[0]).contains(tokens[1])) {
    		return true;
    	}
    	return false;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {
        if (argv.length == 0) {
        	sender.sendMessage("Invalid use of command");
        	return true;
        }

    	/*
    	 * Supported command list
    	 * /emote
    	 */
    	
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
	        		player.sendMessage("This feature is not yet implemented");
	        		break;
	        	
	        	default:
	        		player.sendMessage(ChatColor.RED + "Command not found!");
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