package com.sirdrakeheart.plugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
	public static void broadcastToPlayers(Player from,String msg) {
		if(msg.contains("{COLOR}")) {
			Player[] players = SirDrakeHeart.main.getServer().getOnlinePlayers();
			SDHPlayer fromSdhPlayer = SDHPlayers.getPlayer(from.getName());
			
			Integer fromLvl =  fromSdhPlayer.getPowerLevel();
			
			for(Player to : players) {
				SDHPlayer toSdhPlayer = SDHPlayers.getPlayer(to.getName());
				Integer toLvl = toSdhPlayer.getPowerLevel();
				if(toLvl > fromLvl) {
					msg = msg.replace("{COLOR}", ""+ChatColor.GREEN);
				}
				else if(toLvl == fromLvl) {
					msg = msg.replace("{COLOR}", ""+ChatColor.GOLD);
				}
				else {
					msg = msg.replace("{COLOR}", ""+ChatColor.RED);
				}
				to.sendMessage(msg);
			}
		}
		else {
			SirDrakeHeart.main.getServer().broadcastMessage(msg);
		}
	}
}
