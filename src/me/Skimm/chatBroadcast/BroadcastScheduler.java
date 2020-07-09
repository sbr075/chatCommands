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
			player.sendMessage(ChatColor.RED + "Unknown time format: " + time);
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
			player.sendMessage(ChatColor.RED + "Unknown time format: " + argv[2]);
			return -1;
		}
		
		return newTime;
	}
	
	// Remove broadcast listing from list
	public void removeListing(String name) {
		// Check if broadcast exists
		if (!plugin.broadcast.getConfig().contains("broadcasts." + name)) {
			player.sendMessage(ChatColor.RED + "Broadcast '" + name + "' doesn't exist");
			return;
		}
		
		// Update broadcast amount, set listing to null and save
		ConfigurationSection editListing = plugin.broadcast.getConfig().getConfigurationSection("broadcasts");
		editListing.set("current_broadcasts", editListing.getInt("current_broadcasts") - 1);
		
		plugin.broadcast.getConfig().set("broadcasts." + name, null);
		plugin.broadcast.saveConfig();
	}
	
	@Override
	public void run() {
		// First time setup
		if (!plugin.broadcast.getConfig().contains("broadcasts." + argv[1])) {
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

			// Set values in file
			ConfigurationSection newBroadcast = plugin.broadcast.getConfig().createSection("broadcasts." + argv[1]);
			newBroadcast.set("creator.display_name", player.getDisplayName());
			newBroadcast.set("creator.uuid", player.getUniqueId().toString());
			newBroadcast.set("msg", msg);
			newBroadcast.set("start_time", format.format(currentTime));

			endTime = new Date(currentTime.getTime() + duration);
			newBroadcast.set("end_time", format.format(endTime));

			ConfigurationSection editAmount = plugin.broadcast.getConfig().getConfigurationSection("broadcasts");
			editAmount.set("current_broadcasts", editAmount.getInt("current_broadcasts") + 1);
			plugin.broadcast.saveConfig();
			
			player.sendMessage(ChatColor.GREEN + "Successfully" + ChatColor.WHITE + " created broadcast");
			player.sendMessage("Name: " + argv[1]);
			player.sendMessage("Start time: " + format.format(currentTime));
			player.sendMessage("End time: " + format.format(endTime));
		}
		
		// Get current time
		currentTime = new Date();
		
		// Get end time specified on listing
		try {
			endTime = format.parse(plugin.broadcast.getConfig().getString("broadcasts." + argv[1] + ".end_time"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Check if current is after end
		if (currentTime.after(endTime)) {
			player.sendMessage("name: " + argv[1] + ", broadcast ended at: " + format.format(currentTime));
			removeListing(argv[1]);
			this.cancel();
			return;
		}

		// Broadcast message
		msg = plugin.broadcast.getConfig().getString("broadcasts." + argv[1] + ".msg");
		Bukkit.getServer().broadcastMessage(ChatColor.RED + "[BROADCAST] " + ChatColor.WHITE + msg);
	}
}
