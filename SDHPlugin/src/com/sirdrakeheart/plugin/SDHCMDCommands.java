package com.sirdrakeheart.plugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SDHCMDCommands {
	public static boolean makeprivate(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
	
		if(SirDrakeHeart.perms.has(player, "sirdrakeheart.makeprivate")) {
			if(args.length > 0) {
				Player target = SirDrakeHeart.main.getServer().getPlayer(args[0]);
				if(SirDrakeHeart.perms.getPrimaryGroup(target).equalsIgnoreCase("guest")) {
					SirDrakeHeart.perms.playerRemoveGroup(target,"Guest");
					SirDrakeHeart.perms.playerAddGroup(target, "Private");
					player.sendMessage(ChatColor.GREEN+target.getName()+" has been set to the Private group!");
					target.sendMessage(ChatColor.GREEN+"You have been promoted to Private!");
				}
				else {
					player.sendMessage(ChatColor.RED+"That player is not in the Guest group! They are in: "+SirDrakeHeart.perms.getPrimaryGroup(player));
				}
			}
			else {
				player.sendMessage(ChatColor.RED+"Usage: /makeprivate [player]");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
		}
		return true;
	}
}
