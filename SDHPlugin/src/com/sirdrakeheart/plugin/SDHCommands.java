package com.sirdrakeheart.plugin;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.InvalidFlagFormat;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class SDHCommands {
	
	private static WorldGuardPlugin plugin;

	public static boolean giveplot(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		if(SirDrakeHeart.perms.has(player, "sirdrakeheart.giveplot")) {
			if(args.length == 3) {
				player.chat("/region addowner house_"+args[1]+args[2]+" "+args[0]);
				Player target = SirDrakeHeart.main.getServer().getPlayer(args[0]);
				player.sendMessage(ChatColor.GREEN+target.getName()+" now owns plot number "+args[2]+" in the "+args[1].toLowerCase()+" zone.");
				target.sendMessage(ChatColor.YELLOW+player.getName()+" just gave you plot number "+args[2]+" in the "+args[1].toLowerCase()+" zone.");
			}
			else {
				player.sendMessage(ChatColor.RED+"Usage: /giveplot [player] [zone] [number]");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
		}
		return true;
	}
	
	public static boolean makeprivate(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
	
		if(SirDrakeHeart.perms.has(player, "sirdrakeheart.makeprivate")) {
			if(args.length > 0) {
				Player target = SirDrakeHeart.main.getServer().getPlayer(args[0]);
				if(SirDrakeHeart.perms.getPrimaryGroup(target).equalsIgnoreCase("guest")) {
					SirDrakeHeart.perms.playerRemoveGroup(target,"Guest");
					SirDrakeHeart.perms.playerAddGroup(target, "Private");
					player.sendMessage(ChatColor.GREEN+target.getName()+" has been set to the Private group!");
					target.sendMessage(ChatColor.GREEN+player.getName()+" has promoted you to Private!");
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
	
	public static boolean pay(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		if(SirDrakeHeart.perms.has(player, "sirdrakeheart.pay")) {
			if(args.length == 2) {
				String target = SirDrakeHeart.main.getServer().getPlayer(args[0]).getName();
				player.chat("/money pay "+target+" "+args[1]);
			}
			else {
				player.sendMessage(ChatColor.RED+"Usage: /pay [player] [amount]");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
		}
		return true;
	}
	
	public static boolean setupplot(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		if(SirDrakeHeart.perms.has(player, "sirdrakeheart.setupplot")) {
			if(args.length == 2) {
				String zone = args[0];
				String number = args[1];
				player.chat("//shift 1 up");
				player.chat("//walls fence");
				player.chat("//expand 7 4 up");
				player.chat("/region define House_"+zone+number);
				player.chat("/region flag House_"+zone+number+" chest-access deny");
			}
			else {
				player.sendMessage(ChatColor.RED+"Usage: /setupplot [zone] [number]");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
		}
		return true;
	}
	
	public static boolean flagall(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		Player player = (Player) sender;
		if(SirDrakeHeart.perms.has(player, "sirdrakeheart.setupplot")) {
			if(args.length == 2) {
				WorldGuardPlugin worldGuard = getWorldGuard();
				World world = player.getWorld();
				RegionManager regionManager = worldGuard.getRegionManager(world);
				Map<String, ProtectedRegion> vals = regionManager.getRegions();
				
				Flag<?> foundFlag = null;
		        
		        // Now time to find the flag!
		        for (Flag<?> flag : DefaultFlag.getFlags()) {
		            // Try to detect the flag
		            if (flag.getName().replace("-", "").equalsIgnoreCase(args[0].replace("-", ""))) {
		                foundFlag = flag;
		                break;
		            }
		        }
		        
		        if(foundFlag == null) {
		        	player.sendMessage(ChatColor.RED+"Flag '"+args[0]+"' not found!");
		        }
		        else {
		        
					String value = args[1];
					
					
					for (Entry<String, ProtectedRegion> entry : vals.entrySet()) {
					     ProtectedRegion region = entry.getValue();
					     
					     String id = region.getId();
					     if(id.contains("house_")) {
					    	 if (value != null) {
				                try {
				                    setFlag(region, foundFlag, sender, value);
				                } catch (InvalidFlagFormat e) {
				                    player.sendMessage(ChatColor.RED+"Error whilst processing command. FLAG ERROR");
				                }

				                sender.sendMessage(ChatColor.YELLOW
				                        + "Region flag '" + foundFlag.getName() + "' set.");
					            } else {
					                // Clear the flag
					                region.setFlag(foundFlag, null);
					            }
						     }
						}
			        }
			}
			else {
				player.sendMessage(ChatColor.RED+"Usage: /flagall [flag] [value]");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"You do not have permission to use this command!");
		}
		return true;
	}
	
	public static <V> void setFlag(ProtectedRegion region,
            Flag<V> flag, CommandSender sender, String value)
                throws InvalidFlagFormat {
        region.setFlag(flag, flag.parseInput(plugin, sender, value));
    }
	
	private static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = SirDrakeHeart.main.getServer().getPluginManager().getPlugin("WorldGuard");
		 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	    SDHCommands.plugin = (WorldGuardPlugin) plugin;
	 
	    return (WorldGuardPlugin) plugin;
	}
}