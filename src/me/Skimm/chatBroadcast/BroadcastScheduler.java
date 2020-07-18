package me.Skimm.chatBroadcast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.Skimm.chatCommands.Main;

public class BroadcastScheduler extends BukkitRunnable {
	
	ConfigurationSection broadcast;
	SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	Date currentTime, endTime = null;
    private final Main plugin;
    private String msg = "";
    private final String[] argv;
    private final Player player;

    public BroadcastScheduler(Main plugin, Player player, String[] argv) {
        this.plugin = plugin;
        this.argv = argv;
        this.player = player;
    }

    // Converts time specified by player into given conversion rate
	public int convertFromTimeformat(Player player, String time, int conversionRate) {
		int newTime;
		// Check if integer given
		try {
			newTime = Integer.parseInt(time.substring(0, time.length() - 1));
		}
		catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Unknown time format: " + time);
			return -1;
		}
		
		// Convert depending on format
		switch(time.substring(time.length() - 1)) {
		case "s": // Seconds
			newTime *= conversionRate;
			break;
		case "m": // Minutes
			newTime *= 60 * conversionRate;
			break;
		case "h": // Hours
			newTime *= 60 * 60 * conversionRate;
			break;
		default:
			player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Unknown time format: " + argv[2]);
			return -1;
		}
		
		return newTime;
	}
	
	// Remove broadcast listing from list
	public void removeListing(String name) {
		// Check if broadcast exists
		if (!plugin.config.getConfig().contains("general.broadcasts." + name)) {
			player.sendMessage(ChatColor.RED + "[ERROR]:" + ChatColor.WHITE + " Broadcast '" + name + "' doesn't exist");
			return;
		}
		
		// Update broadcast amount, set listing to null and save
		broadcast = plugin.config.getConfig().getConfigurationSection("general.broadcasts");
		broadcast.set("current_broadcasts", broadcast.getInt("current_broadcasts") - 1);
		
		Bukkit.getScheduler().cancelTask(broadcast.getInt(name + ".taskID"));

		plugin.config.getConfig().set("general.broadcasts." + name, null);
		plugin.config.saveConfig();
		
		
	}
	
	@Override
	public void run() {
		broadcast = plugin.config.getConfig().getConfigurationSection("general.broadcasts");
		
		// First time setup
		if (!plugin.config.getConfig().contains("general.broadcasts." + argv[1])) {
			int duration = 0;
			currentTime = new Date();

			// Fetch message
			for (int i = 4; i < argv.length; i++)
				msg += argv[i] + " ";

			// Fetch duration (in ticks)
			duration = convertFromTimeformat(player, argv[3], 1000);
			if (duration == -1) {
				this.cancel();
				return;
			}
			
			endTime = new Date(currentTime.getTime() + duration);

			// Set values in file
			broadcast.set(argv[1] + ".creator.display_name", player.getDisplayName());
			broadcast.set(argv[1] + ".creator.uuid", player.getUniqueId().toString());
			broadcast.set(argv[1] + ".msg", msg);
			broadcast.set(argv[1] + ".start_time", format.format(currentTime));
			broadcast.set(argv[1] + ".end_time", format.format(endTime));
			broadcast.set(argv[1] + ".taskID", this.getTaskId());
			broadcast.set("current_broadcasts", broadcast.getInt("current_broadcasts") + 1);
			
			// Save config
			plugin.config.saveConfig();
			
			// Inform player
			player.sendMessage(ChatColor.GREEN + "[INFO]" + ChatColor.WHITE + " Successfully created broadcast");
			player.sendMessage("Name: " + argv[1]);
			player.sendMessage("Start time: " + format.format(currentTime));
			player.sendMessage("End time: " + format.format(endTime));
		}
		
		// Get current time
		currentTime = new Date();
		
		// Get end time specified on listing
		try {
			endTime = format.parse(broadcast.getString(argv[1] + ".end_time"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Check if current is after end
		if (currentTime.after(endTime)) {
			player.sendMessage(ChatColor.GREEN + "[INFO]:" + ChatColor.WHITE + " Broadcast: " + argv[1] + " ended at: " + format.format(currentTime));
			removeListing(argv[1]);
			this.cancel();
			return;
		}

		// Broadcast message
		msg = plugin.config.getConfig().getString("general.broadcasts." + argv[1] + ".msg");
		Bukkit.getServer().broadcastMessage(ChatColor.RED + "[BROADCAST] " + ChatColor.WHITE + msg);
	}
}
