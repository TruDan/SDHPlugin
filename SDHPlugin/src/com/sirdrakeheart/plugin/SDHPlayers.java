package com.sirdrakeheart.plugin;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SDHPlayers implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static Map<String,SDHPlayer> players = new HashMap<String,SDHPlayer>();

	public static SDHPlayer getPlayer(String name) {
		// Check if player is new
		if(SDHPlayers.check(name) && players.containsKey(name)) {
			// Player is existing, load data
			SDHPlayer pd = players.get(name);
			return pd;
		}
		else {
			// Player is new, create data
			SDHPlayer pd = new SDHPlayer(name);
			pd.save();
			players.put(name, pd);
			return pd;
		}
	}
	
	public static boolean check(String name) {
		File file = new File(SirDrakeHeart.filepath+"players" + File.separator + name + ".dat");
		boolean exists = file.exists();
		if (!exists) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public static void PlayerJoin(final PlayerJoinEvent event) {
		
		String name = event.getPlayer().getName();
		// Check if player is new
		if(SDHPlayers.check(name) == true) {
			// Player is existing, load data
			SDHPlayer pd = new SDHPlayer(name);
			pd.load();
			SDHPlayers.players.put(name,pd);
		}
		else {
			// Player is new, create data
			SDHPlayer pd = new SDHPlayer(name);
			pd.save();
			SDHPlayers.players.put(name, pd);
		}
		
	}
	
	public static void PlayerQuit(final PlayerQuitEvent event) {
		String name = event.getPlayer().getName();
		SDHPlayers.players.remove(name);
	}
	
	public static Collection<SDHPlayer> getPlayers() {
		return SDHPlayers.players.values();
	}
}
