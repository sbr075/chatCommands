package me.Skimm.chatCommands;

import me.Skimm.chatEmotes.*;
import me.Skimm.chatMod.ModHandler;
import me.Skimm.chatTitles.TitleHandler;
import me.Skimm.chatBroadcast.*;
import me.Skimm.chatChat.ChatHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/*
 * Known issues
 * 1. Broadcast removal doesn't work (gets remade)
 * 2. Title removal doesn't set default title
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
 * 1. Clean up title package
 * 2. Fix known issues
 * 3. If removal of default, need to specify new default or if rename default, rename default tab too
 */

public class Main extends JavaPlugin implements Listener {
	// All config files
	public ConfigManager permissions;
	public ConfigManager commands;
	public ConfigManager playerInfo;
	public ConfigManager config;
	
	public ConfigManager emote;
	public ConfigManager chat;
	
	// All feature packages
	private EmoteHandler emoteCommands;
	private BroadcastHandler broadcastCommands;
	private ChatHandler chatCommands;
	private ModHandler modCommands;
	private TitleHandler titleCommands;

    @Override
    public void onEnable() {
    	// Initialize packages
    	this.emoteCommands = new EmoteHandler(this);
    	this.broadcastCommands = new BroadcastHandler(this);
    	this.chatCommands = new ChatHandler(this);
    	this.modCommands = new ModHandler(this);
    	this.titleCommands = new TitleHandler(this);
    	
    	// Initialize config files
    	this.emote = new ConfigManager(this, "emotes.yml");
    	this.permissions = new ConfigManager(this, "permissions.yml");
    	this.commands = new ConfigManager(this, "commands.yml");
    	this.playerInfo = new ConfigManager(this, "playerinfo.yml");
    	this.config = new ConfigManager(this, "config.yml");
    	this.chat = new ConfigManager(this, "chat.yml");

    	getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Used during shutdown and reloads
    }
    
	// Change chat format from <name> text to name: text
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
    	Player player = event.getPlayer();
    	String curMode;
    	curMode = playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.mode");
    	
    	chatCommands.commandHandler(player, curMode, event.getMessage().split(" "));
    	event.setCancelled(true);
    }
    
    /*
     * If in group, send group MOTD
     * Notify all online group members player joined
     */
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
    	if (playerInfo.getConfig().getConfigurationSection("players." + player.getUniqueId()) == null) {
    		// Set default permissions
    		ConfigurationSection newPlayer = playerInfo.getConfig().createSection("players." + player.getUniqueId().toString());
    		ConfigurationSection editTitle = permissions.getConfig().getConfigurationSection("permissions.titles.names");

    		// If player is op, assign admin permissions
    		if (player.isOp()) {
    			newPlayer.set("title", "admin");
    			editTitle.set("admin.uses", editTitle.getInt("admin.uses") + 1);
    		}
    		else {
    			newPlayer.set("title", "user");
    			editTitle.set("user.uses", editTitle.getInt("user.uses") + 1);
    		}
    		newPlayer.set("chat.mode", "all");
    		
    		// Set up cooldown section
    		newPlayer.set("cooldown", "0");

    		// Notify player
    		player.sendMessage("Player data has been created!");
    		permissions.saveConfig();
    		playerInfo.saveConfig();
    		
    		updateDisplayName(player, 2, null);
    	}
    	else {
    		if (config.getConfig().getString("general.chat.removejoinnotif").equalsIgnoreCase("true")) {
    			event.setJoinMessage("");
	    		if (playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.group")) {
	    			String groupName = playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.group.name");
					List<String> groupMembers = chat.getConfig().getStringList("groups." + groupName + ".members");
					
					// Send player group MOTD
					player.sendMessage(ChatColor.GREEN + "Group MOTD: " + chat.getConfig().getString("groups." + groupName + ".MOTD"));
					
					// Notify all group members
					for (String memberUUID : groupMembers) {
						Player groupMember = null;
						try {
			    			groupMember = player.getServer().getPlayer(UUID.fromString(memberUUID));
			    		}
			    		catch (Exception e) {
			    			continue;
			    		}
						
						groupMember.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("general.chat.chatModes.group.color") + chatCommands.stripName(player) + " has joined the server!"));
					}
				}
    		}
    	}
    }
    
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
    	Player player = event.getPlayer();
    	
    	playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.mode", "all");
    	playerInfo.saveConfig();
    	
    	if (config.getConfig().getString("general.chat.removeleavenotif").equalsIgnoreCase("true")) {
    		event.setQuitMessage("");
    		
    		// If player belongs to group
        	if (playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.group")) {
    			String groupName = playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.group.name");
    			List<String> groupMembers = chat.getConfig().getStringList("groups." + groupName + ".members");

    			// Notify all group members
    			for (String memberUUID : groupMembers) {
    				Player groupMember = null;
    				try {
    	    			groupMember = player.getServer().getPlayer(UUID.fromString(memberUUID));
    	    		}
    	    		catch (Exception e) {
    	    			continue;
    	    		}
    				
    				groupMember.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getConfig().getString("general.chat.chatModes.group.color") + chatCommands.stripName(player) + " has left the server!"));
    			}
    		}
    	}
    	
    	// Already have this function
    	if (playerInfo.getConfig().contains("players." + player.getUniqueId().toString() + ".chat.party")) {
			// Check if player is party owner
			String partyOwner = playerInfo.getConfig().getString("players." + player.getUniqueId().toString() + ".chat.party");
			
			List<String> members = chat.getConfig().getStringList("parties." + partyOwner + ".members");
			
			// Player is party owner
			if (player.getUniqueId().toString().equalsIgnoreCase(partyOwner)) {
				if (members.size() > 1) {
					chat.getConfig().set("parties." + partyOwner, "parties." + members.get(1));
				}
				else {
					chat.getConfig().set("parties." + partyOwner, null);
				}
				
				// Update player info
				playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.party", null);
				
				playerInfo.saveConfig();
				chat.saveConfig();
			}
			// Player isn't party owner
			else {
				// Remove player from members
				members.remove(player.getUniqueId().toString());
				chat.getConfig().set("parties." + partyOwner + ".members", members);
				
				// Update player info
				playerInfo.getConfig().set("players." + player.getUniqueId().toString() + ".chat.party", null);
				
				playerInfo.saveConfig();
				chat.saveConfig();
			}
		}
    }
    
    public void updateDisplayName(Player receiver, int option, String newVal) {
    	String curName, curTitle, curMode, titleColor, modeColor, newName = "";
    	curTitle = playerInfo.getConfig().getString("players." + receiver.getUniqueId() + ".title").toLowerCase();
    	curMode = playerInfo.getConfig().getString("players." + receiver.getUniqueId() + ".chat.mode").toLowerCase();
    	
    	
    	// [&fMODE&f][&fTITLE&f] <NAME>
    	// Remove title and chat mode text to get normal name
    	try {
    		curName = receiver.getDisplayName().substring(curTitle.length() + curMode.length() + 13, receiver.getDisplayName().length());
    	}
    	catch (StringIndexOutOfBoundsException e) {
    		curName = receiver.getDisplayName();
    	}
    	
    	/*
    	 * 0, update only title
    	 * 1, update only chat mode
    	 * 2, only used for first time setup, updates both
    	 */
    	switch(option) {
    	case 0:
    		titleColor = permissions.getConfig().getString("permissions.titles.names." + newVal + ".color");
    		if (!titleColor.matches("^[&][A-Fa-f0-9]+$") || !(titleColor.length() == 2))
    			titleColor = "&f";
    		
    		newName = ChatColor.translateAlternateColorCodes('&', "[" + config.getConfig().getString("general.chat.chatModes." + curMode + ".color") + curMode.toUpperCase() + "&f]"
					+ "[" + titleColor + newVal.toUpperCase() + "&f] " 
					+ curName);
    		break;
    	case 1:
    		modeColor = config.getConfig().getString("general.chat.chatModes." + newVal + ".color");
    		if (!modeColor.matches("^[&][A-Fa-f0-9]+$") || !(modeColor.length() == 2))
    			modeColor = "&f";
    		
    		newName = ChatColor.translateAlternateColorCodes('&', "[" + modeColor + newVal.toUpperCase() + "&f]"
					+ "[" + permissions.getConfig().getString("permissions.titles.names." + curTitle + ".color") + curTitle.toUpperCase() + "&f] " 
					+ curName);
    		break;
    	case 2:
    		modeColor = config.getConfig().getString("general.chat.chatModes." + curMode + ".color");
    		if (!modeColor.matches("^[&][A-Fa-f0-9]+$") || !(modeColor.length() == 2))
    			modeColor = "&f";
    		
    		titleColor = permissions.getConfig().getString("permissions.titles.names." + curTitle + ".color");
    		if (!titleColor.matches("^[&][A-Fa-f0-9]+$") || !(titleColor.length() == 2))
    			titleColor = "&f";
    		
    		newName = ChatColor.translateAlternateColorCodes('&', "[" + config.getConfig().getString("general.chat.chatModes." + curMode + ".color") + curMode.toUpperCase() + "&f]"
					+ "[" + permissions.getConfig().getString("permissions.titles.names." + curTitle + ".color") + curTitle.toUpperCase() + "&f] " 
					+ curName);
    	}

		receiver.setDisplayName(newName);
    }
    
    private void traverseParents(Player player, ArrayList<String> perms, String titleName) {
    	ArrayList<String> titleParents = (ArrayList<String>) permissions.getConfig().getStringList("permissions.titles." + titleName + ".parent");
    	ArrayList<String> titlePerms = (ArrayList<String>) permissions.getConfig().getStringList("permissions.titles." + titleName + ".perms");
    	
    	// Add permissions belonging to title
    	if (titlePerms.size() > 0) {
    		for (int i = 0; i < titlePerms.size(); i++) {
    			if (!perms.contains(titlePerms.get(i)))
					perms.add(titlePerms.get(i));
    		}
    	}
    	
    	// Iterate through all parents of title
    	if (titleParents.size() > 0) {
    		for (int i = 0; i < titleParents.size(); i++) {
        		traverseParents(player, perms, titleParents.get(i));
    		}
    	}
    }
    
    public boolean checkPermissions(Player player, String label, String command) {
    	if (player.isOp())
    		return true;
    	
    	// label = emote, broadcast ect.
    	// command = help, send, use ect.
    	
    	// get player title, check title permissions and compare against command permissions
    	// Compare lists and check if list is 0,
    	// if 0 no permission else permission
    	// Check users permissions
    	
    	// Fetch player title from config file
    	String playerTitle = playerInfo.getConfig().getString("players." + player.getUniqueId() + ".title");
    	
    	ArrayList<String> playerPerms = new ArrayList<String>();
    	traverseParents(player, playerPerms, playerTitle);

    	if (playerPerms.isEmpty()) {
    		player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Title '" + playerTitle + "' has no permissions");	
    		return false;
    	}
    	
    	ArrayList<String> cmdPerms = (ArrayList<String>) permissions.getConfig().getStringList("permissions." + label + "." + command);
    	playerPerms.addAll(cmdPerms);
    	
    	Set<String> set = new HashSet<String>(playerPerms);
    	if (playerPerms.size() > set.size())
    		return true;

    	return false;
    }
    
    /* 
     * Check if player have specific permission
     */
    public boolean checkSpecificPermission(Player player, String perm) {
    	if (player.isOp())
    		return true;
    	// Fetch player title from config file
    	String playerTitle = playerInfo.getConfig().getString("players." + player.getUniqueId() + ".title");
    	
    	ArrayList<String> playerPerms = new ArrayList<String>();
    	traverseParents(player, playerPerms, playerTitle);
    	if (playerPerms.isEmpty() || !playerPerms.contains(perm))
    		return false;

    	return true;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] argv) {        
        // Check if player
    	if (sender instanceof Player) {
    		// Cast sender to player
    		Player player = (Player) sender;
    			
    		String command = "";
    		if (argv.length > 0) {
    			// Check permissions of player
        		if (!checkPermissions(player, label, argv[0].toLowerCase())) {
        			player.sendMessage(ChatColor.RED + "You do not have permissions to use this command.\n If you believe this is a mistake contact a server administrator");
        			return true;
        		}
        		
        		command = argv[0].toLowerCase();
    		}

    		/* 
    		 * Checks general functions, if not match
    		 * it will check special functions for given label
    		 */
    		
			switch (command) {
    		case "help":
    			if (argv.length == 1) {
    				// titleName - user, admin
    				for (String titleName : commands.getConfig().getConfigurationSection("commands." + label.toLowerCase()).getKeys(false)) {
    					// Prevent normal players from seeing admin help tab
    					if (titleName.equalsIgnoreCase("admin")) {
    						if (!checkSpecificPermission(player, label + ".admin"))
    							break;
    					}
    					
    					// key1 - info, <label>
    					for (String labelName : commands.getConfig().getConfigurationSection("commands." + label.toLowerCase() + "." + titleName).getKeys(false)) {
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
	        	
	        	case "region":
	        	case "r":
	        	case "all":
	        	case "a":
	        	case "shout":
	        	case "s":
	        	case "whisper":
	        	case "w":
	        	case "group":
	        	case "g":
	        	case "party":
	        	case "p":
	        	case "local":
	        	case "l":
	        	case "chat":
	        		chatCommands.commandHandler(player, label, argv);
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