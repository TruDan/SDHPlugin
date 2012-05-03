package com.sirdrakeheart.plugin;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlotManager {
	
	public static void givePlot(Player player, String zone, String number) {
		WorldGuardPlugin worldGuard = getWorldGuard();
		World world = player.getWorld();
		
		RegionManager regionManager = worldGuard.getRegionManager(world);
		ProtectedRegion region = regionManager.getRegion("house_"+zone.toLowerCase()+number);
		DefaultDomain owners = region.getOwners();
		owners.addPlayer(player.getName());
		region.setOwners(owners);
		editPlotSign(player,zone,number,region);
	}
	
	public static void editPlotSign(Player player, String zone, String number, ProtectedRegion region) {
		String line1 = "######";
		String line2 = zone+" Zone - "+number;
		String line3 = player.getName();
		String line4 = "######";
		
		World world = player.getWorld();
		
		
		BlockVector max = region.getMaximumPoint();
		BlockVector min = region.getMinimumPoint();
		
		int minx = Math.min(max.getBlockX()+1, min.getBlockX()-1),
	    miny = Math.min(max.getBlockY()+1, min.getBlockY()-1),
	    minz = Math.min(max.getBlockZ(), min.getBlockZ()),
	    maxx = Math.max(max.getBlockX()+1, min.getBlockX()-1),
	    maxy = Math.max(max.getBlockY()+1, min.getBlockY()-1),
	    maxz = Math.max(max.getBlockZ(), min.getBlockZ());
		int a = 0;
	    for(int x = minx; x<=maxx;x++){
	        for(int y = miny; y<=maxy;y++){
	            for(int z = minz; z<=maxz;z++){
	            	a++;
	                Block b = world.getBlockAt(x, y, z);
                	if (b.getType().equals(Material.SIGN_POST) || b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN)) {
                        Sign sign = (Sign) b.getState();
                        if (sign.getLine(0).contains("#")) {
                            sign.setLine(0, line1);
                            sign.setLine(1, line2);
                            sign.setLine(2, line3);
                            sign.setLine(3, line4);
                            sign.update(true);
                            
                            player.sendMessage("yes");
                        }
                        else {
                        	player.sendMessage("no");
                        }
                        break;
                    }
	            }
	        }
	    }
	    player.sendMessage("Number: "+a);
	}
	
	private static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = SirDrakeHeart.main.getServer().getPluginManager().getPlugin("WorldGuard");
		 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
}
