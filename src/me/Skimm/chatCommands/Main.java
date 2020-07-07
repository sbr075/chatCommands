package me.Skimm.chatCommands;

import me.Skimm.chatEmotes.*;

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
 * TODO
 * 1. Clean up
 */

public class Main extends JavaPlugin implements Listener {
	public ConfigManager emote;
	public ConfigManager permissions;
	public ConfigManager commands;

	private EmoteHandler emoteCommands;

    @Override
    public void onEnable() {
    	this.emote = new ConfigManager(this, "emotes.yml");
    	this.permissions = new ConfigManager(this, "permissions.yml");
    	this.commands = new ConfigManager(this, "commands.yml");
    	
    	this.emoteCommands = new EmoteHandler(this);
    	
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

    		switch (label) {
        	case "emote":
        		emoteCommands.commandHandler(player, label, argv);
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