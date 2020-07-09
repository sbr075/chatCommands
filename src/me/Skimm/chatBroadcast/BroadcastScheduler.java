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
	
    private final Main plugin;
    private String msg = "";
    private final String[] argv;
    private final Player player;

    public BroadcastScheduler(Main plugin, Player player, String[] argv) {
        this.plugin = plugin;
        this.argv = argv;
        this.player = player;
    }

    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	Date currentTime, endTime = null;
	
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
			switch(argv[3]) {
			case "false":
				duration = -1;
			default:
				try {
					duration = Integer.parseInt(argv[3].substring(0, argv[3].length() - 1));
				}
				catch (NumberFormatException e) {
					player.sendMessage(ChatColor.RED + "Unknown time format: " + argv[3]);
					this.cancel();
					break;
				}

				switch(argv[3].substring(argv[3].length() - 1)) {
				case "s":
					duration *= 1000;
					break;
				case "m":
					duration *= 60 * 1000;
					break;
				case "h":
					duration *= 60 * 60 * 1000;
					break;
				default:
					player.sendMessage(ChatColor.RED + "Unknown time format: " + argv[3]);
					this.cancel();
					break;
				}
				break;
			}

			// Set values in file
			ConfigurationSection newBroadcast = plugin.broadcast.getConfig().createSection("broadcasts." + argv[1]);
			newBroadcast.set("creator.display_name", player.getDisplayName());
			newBroadcast.set("creator.uuid", player.getUniqueId().toString());
			newBroadcast.set("msg", msg);
			newBroadcast.set("start_time", format.format(currentTime));
			
			if (duration > 0) {
				endTime = new Date(currentTime.getTime() + duration);
				newBroadcast.set("end_time", format.format(endTime));
			}
			else {
				newBroadcast.set("end_time", format.format("-1"));
			}

			plugin.broadcast.saveConfig();
			
			player.sendMessage("Created broadcast '" + argv[1] + "'");
			player.sendMessage("Start time: " + format.format(currentTime));
			player.sendMessage("End time: " + format.format(endTime));
		}
		
		currentTime = new Date();
		try {
			endTime = format.parse(plugin.broadcast.getConfig().getString("broadcasts." + argv[1] + ".end_time"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (currentTime.after(endTime)) {
			player.sendMessage("name: " + argv[1] + ", broadcast ended at: " + format.format(currentTime));
			// Need to remove from list before cancel
			this.cancel();
		}

		msg = plugin.broadcast.getConfig().getString("broadcasts." + argv[1] + ".msg");
		Bukkit.getServer().broadcastMessage(msg);
	}
}
