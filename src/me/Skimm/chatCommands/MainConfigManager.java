package me.Skimm.chatCommands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class MainConfigManager {
	
	private Main plugin;
	private FileConfiguration customConfig = null;
	private File configFile = null;
	private String configName;
	
	public MainConfigManager(Main plugin, String name) {
		this.plugin = plugin;
		configName = name;
		
		/* Save/Initialize config */
		saveDefaultConfig();
	}
	
	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), configName);
		
		this.customConfig = YamlConfiguration.loadConfiguration(this.configFile);
		
		InputStream defaultStream = this.plugin.getResource(configName);
		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.customConfig.setDefaults(defaultConfig);
		}
		
	}
	
	public FileConfiguration getConfig() {
		if (this.customConfig == null)
			reloadConfig();
		
		return this.customConfig;
	}
	
	public void saveConfig() {
		if (this.customConfig == null || this.configFile == null)
			return;
		
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, e);
		}
	}
	
	public void saveDefaultConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), configName);
		
		if (!this.configFile.exists()) {
			this.plugin.saveResource(configName, false);
		}
	}
}
