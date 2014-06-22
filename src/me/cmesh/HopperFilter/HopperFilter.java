package me.cmesh.HopperFilter;

import org.bukkit.plugin.java.JavaPlugin;

public class HopperFilter extends JavaPlugin {
	private HopperListener listener;
	
	public HopperFilter() {
		listener = new HopperListener();
	}
	
	public void onEnable() {
		this.getServer().getPluginManager().registerEvents(listener, this);
	}
}
