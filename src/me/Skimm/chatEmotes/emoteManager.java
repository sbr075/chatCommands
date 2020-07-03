package me.Skimm.chatEmotes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.Skimm.chatCommands.Main;

public class emoteManager {
	
	private Main plugin;
	private FileConfiguration emoteConfig = null;
	private File configFile = null;
	
	public emoteManager(Main plugin) {
		this.plugin = plugin;
		
		/* Save/Initialize config */
		saveDefaultConfig();
	}
	
	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "emotes.yml");
		
		this.emoteConfig = YamlConfiguration.loadConfiguration(this.configFile);
		
		InputStream defaultStream = this.plugin.getResource("emotes.yml");
		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.emoteConfig.setDefaults(defaultConfig);
		}
		
	}
	
	public FileConfiguration getConfig() {
		if (this.emoteConfig == null)
			reloadConfig();
		
		return this.emoteConfig;
	}
	
	public void saveConfig() {
		if (this.emoteConfig == null || this.configFile == null)
			return;
		
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
		}
	}
	
	public void saveDefaultConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "emotes.yml");
		
		if (!this.configFile.exists()) {
			this.plugin.saveResource("emotes.yml", false);
		}
	}
}
