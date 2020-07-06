package me.Skimm.chatEmotes;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class EmoteHandler extends JavaPlugin implements Listener {
	
	public EmoteConfigManager emote;
	public EmoteConfigManager permissions;
	
	@Override
	public void onEnable() {
		this.emote = new EmoteConfigManager(this, "emotes.yml");
		this.permissions = new EmoteConfigManager(this, "permissions.yml");
	}
	
	public boolean emotePrintHelp() {		
		return true;
	}
	
	private boolean doesnothing() {
		return false;
	}
}
