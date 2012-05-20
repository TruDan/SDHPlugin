package com.sirdrakeheart.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static void broadcastPvPMessage(String message) {
		SirDrakeHeart.main.getServer().broadcastMessage(ChatColor.WHITE+"[ "+ChatColor.BLUE+"PvP "+ChatColor.WHITE+"] "+ChatColor.GRAY+convertColors(message));
	}
	
	public static String convertColors(String str) {
		Pattern color_codes = Pattern.compile("&([0-9A-Fa-fkK])");
		Matcher find_colors = color_codes.matcher(str);
		while (find_colors.find()) {
		 str = find_colors.replaceFirst(new StringBuilder().append("§").append(find_colors.group(1)).toString());
		 find_colors = color_codes.matcher(str);
		}
		return str;
	}
}
