package com.sirdrakeheart.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;


import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

public class Events implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerChat(PlayerChatEvent event) {
		if(event.isCancelled()) return;
		
		Player player = event.getPlayer();
		SDHPlayer sdhplayer = SDHPlayers.getPlayer(player.getName());
		PermissionUser user = PermissionsEx.getUser(player);
		String world = player.getWorld().getName();
		
		String group = SirDrakeHeart.perms.getPrimaryGroup(player);
		String prefix = user.getOption("prefix", world);
		String suffix = user.getOption("suffix", world);
		
		Integer level = sdhplayer.getPowerLevel();

		player.setDisplayName(convertColors(suffix)+player.getName()+ChatColor.GRAY);
		String format = convertColors("[{COLOR}"+level+"&f] ["+prefix+group+"&f] "+suffix+player.getName()+"&7: &f").replaceAll("&([0-9a-f])", "§$1");
		if(user.has("sirdrakeheart.color")) {
			format = convertColors(format+event.getMessage()).replaceAll("%", "%%");
		}
		else {
			format = format+event.getMessage().replaceAll("%", "%%");
		}
		
		event.setCancelled(true);
		
		
		for(Player to : event.getRecipients()) {
			SDHPlayer toSdhPlayer = SDHPlayers.getPlayer(to.getName());
			Integer toLvl = toSdhPlayer.getPowerLevel();
			String msg = format;
			if(toLvl > level) {
				msg = format.replace("{COLOR}", ""+ChatColor.GREEN);
			}
			else if(toLvl == level) {
				msg = format.replace("{COLOR}", ""+ChatColor.GOLD);
			}
			else {
				msg = format.replace("{COLOR}", ""+ChatColor.RED);
			}
			to.sendMessage(msg);
		}
		System.out.println("[PLAYER_CHAT] "+player.getName()+": "+event.getMessage());
	}
	
	@EventHandler
	public void onPlayerLevelUp(final McMMOPlayerLevelUpEvent event) {
		Player player = event.getPlayer();
		SDHPlayer sdhplayer = SDHPlayers.getPlayer(player.getName());
		ExperienceAPI xpapi = new ExperienceAPI();
		
		Integer currentLevel = sdhplayer.getPowerLevel();
		Integer levels = xpapi.getLevel(player, SkillType.ARCHERY) + xpapi.getLevel(player, SkillType.SWORDS) + xpapi.getLevel(player, SkillType.UNARMED);
		Integer newLevel = (int) Math.floor(levels/3);
		
		sdhplayer.setPowerLevel(newLevel);
		
		if(newLevel > currentLevel) {
			broadcastToPlayers(player,"[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" has upgraded up to Combat Level "+newLevel);
		}
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PermissionUser user = PermissionsEx.getUser(player);
		SDHPlayer sdhplayer = SDHPlayers.getPlayer(player.getName());
		ExperienceAPI xpapi = new ExperienceAPI();
		String world = player.getWorld().getName();
		
		Integer levels = xpapi.getLevel(player, SkillType.ARCHERY) + xpapi.getLevel(player, SkillType.SWORDS) + xpapi.getLevel(player, SkillType.UNARMED);
		Integer newLevel = (int) Math.floor(levels/3);
		
		sdhplayer.setPowerLevel(newLevel);
		
		String suffix = convertColors(user.getOption("suffix", world));
		player.setDisplayName(suffix+player.getName()+ChatColor.GRAY);
		event.setJoinMessage("[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" has joined the game.");
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		event.setQuitMessage("[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" has left the game.");
	}
	
	public void broadcastToPlayers(Player from,String msg) {
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
	
	public String convertColors(String str) {
		Pattern color_codes = Pattern.compile("&([0-9A-Fa-fkK])");
		Matcher find_colors = color_codes.matcher(str);
		while (find_colors.find()) {
		 str = find_colors.replaceFirst(new StringBuilder().append("§").append(find_colors.group(1)).toString());
		 find_colors = color_codes.matcher(str);
		}
		return str;
	}

	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		Player player = event.getPlayer();
		SDHPlayer sdhplayer = SDHPlayers.getPlayer(player.getName());
		
		Vector eventBlock = toVector(event.getBlock());
		if(sdhplayer.chestBankBlockSelectionMode == true) {
			Location blockLocation = event.getBlock().getLocation();
			Vector pt = toVector(blockLocation);
			sdhplayer.setChestBankBlock(pt);
			player.sendMessage(ChatColor.GRAY+"Right click the NPC to confirm.");
		}
		else if(sdhplayer.plotBuyMode == true) {
			// Plot Selection mode
			Location blockLocation = event.getBlock().getLocation();
			WorldGuardPlugin worldGuard = getWorldGuard();
			Vector pt = toVector(blockLocation); // This also takes a location
			World world = player.getWorld();
			
			RegionManager regionManager = worldGuard.getRegionManager(world);
			ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
			
			event.setCancelled(true);
			
			for (ProtectedRegion region : set) {
			    if(region.contains(pt)) {
			    	if(region.hasMembersOrOwners() == false && region.getId().contains("house_")) {
			    		String id = region.getId();
			    		
			    		String[] tmp = id.split("_");
			    		String[] parts = tmp[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
			    		String zone = (parts[1] == null) ? "" : parts[0];
			    		String number = (parts[0] == null) ? "" : parts[1];
			    		Double price = region.getFlag(DefaultFlag.PRICE);
			    		sdhplayer.setCurrentPlot(region);
			    		player.sendMessage(ChatColor.YELLOW+"You have selected plot number "+number+" in the "+zone+" zone.");
			    		player.sendMessage(ChatColor.GRAY+"This will cost you "+ChatColor.GREEN+price+ChatColor.GOLD+"g");
			    		player.sendMessage(ChatColor.GRAY+"Click the accept button to buy, another plot to rechoose, or the agent to cancel.");
			    		sdhplayer.setPurchaseMode(true);
			    		sdhplayer.setAcceptDenyMode(true);
			    		break;
			    	}
			    	else {
			    		String Owners = region.getOwners().toPlayersString();
			    		player.sendMessage(ChatColor.RED+"This plot already belongs to "+Owners);
			    		player.sendMessage(ChatColor.GRAY+"To cancel plot selection, click the NPC.");
			    	}
			    }
			}
		}
		if(sdhplayer.acceptBlockSelect == true) {
			sdhplayer.setAcceptBlock(eventBlock);
			player.sendMessage(ChatColor.GRAY+"Right click the NPC to confirm accept block.");
			event.setCancelled(true);
		}
		else if(sdhplayer.denyBlockSelect == true) {
			sdhplayer.setDenyBlock(eventBlock);
			player.sendMessage(ChatColor.GRAY+"Right click the NPC to confirm deny block.");
			event.setCancelled(true);
		}
		else if(sdhplayer.acceptDenyMode == true) {
			// Accept / Deny Mode
			Vector acceptBlock = sdhplayer.getAcceptBlock();
			Vector denyBlock   = sdhplayer.getDenyBlock();
			if(acceptBlock != null && denyBlock != null) {
				if(eventBlock.getY() == acceptBlock.getY() &&
				   eventBlock.getX() == acceptBlock.getX() &&
				   eventBlock.getZ() == acceptBlock.getZ()) {
					// Its the accept block.
					if(sdhplayer.purchaseMode == true) {
						ProtectedRegion region = sdhplayer.currentPlot;
						Double price = region.getFlag(DefaultFlag.PRICE);
						if(SirDrakeHeart.economy.has(player.getName(), price)) {
							SirDrakeHeart.economy.bankWithdraw(player.getName(), price);
							DefaultDomain owners = new DefaultDomain();
							owners.addPlayer(player.getName());
							region.setOwners(owners);
							
							String[] tmp = region.getId().split("_");
				    		String[] parts = tmp[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				    		String zone = (parts[1] == null) ? "" : parts[0];
				    		String number = (parts[0] == null) ? "" : parts[1];
							player.sendMessage(ChatColor.GREEN+"Congratulations! You now own plot number "+number+" in the "+zone+" zone.");
							sdhplayer.setAcceptDenyMode(false);
							sdhplayer.setPurchaseMode(false);
							sdhplayer.setPlotBuyMode(false);
							// Do Sign
							editPlotSign(player,zone,number,region);
						}
						else {
							player.sendMessage(ChatColor.RED+"You cannot afford this plot! Purchase cancelled.");
						}
					}
					else {
						sdhplayer.setAcceptDenyMode(false);
						sdhplayer.setPurchaseMode(false);
						sdhplayer.setPlotBuyMode(true);
						player.sendMessage(ChatColor.GRAY+"Please go and find your plot and left click anywhere inside it.");
					}
					event.setCancelled(true);
				}
				else if(eventBlock.getY() == denyBlock.getY() &&
						eventBlock.getX() == denyBlock.getX() &&
						eventBlock.getZ() == denyBlock.getZ()) {
					// Its the deny block.
					sdhplayer.setAcceptDenyMode(false);
					sdhplayer.setPurchaseMode(false);
					sdhplayer.setPlotBuyMode(false);
					player.sendMessage(ChatColor.GRAY+"Plot Purchase cancelled.");

					event.setCancelled(true);
				}
			}
			else {
				player.sendMessage(ChatColor.RED+"Error in configuration. Report to an administrator.");
			}
		}
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

	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = SirDrakeHeart.main.getServer().getPluginManager().getPlugin("WorldGuard");
		 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		SDHPlayer sdhplayer = SDHPlayers.getPlayer(player.getName());
		
		Vector eventBlock = toVector(event.getBlock());
		if(sdhplayer.chestBankBlockSelectionMode == true) {
			Location blockLocation = event.getBlock().getLocation();
			Vector pt = toVector(blockLocation);
			sdhplayer.setChestBankBlock(pt);
			player.sendMessage(ChatColor.GRAY+"Right click the NPC to confirm.");
		}
		else if(sdhplayer.plotBuyMode == true) {
			// Plot Selection mode
			Location blockLocation = event.getBlock().getLocation();
			WorldGuardPlugin worldGuard = getWorldGuard();
			Vector pt = toVector(blockLocation); // This also takes a location
			World world = player.getWorld();
			
			RegionManager regionManager = worldGuard.getRegionManager(world);
			ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
			
			event.setCancelled(true);
			
			for (ProtectedRegion region : set) {
			    if(region.contains(pt)) {
			    	if(region.hasMembersOrOwners() == false && region.getId().contains("house_")) {
			    		String id = region.getId();
			    		
			    		String[] tmp = id.split("_");
			    		String[] parts = tmp[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
			    		String zone = (parts[1] == null) ? "" : parts[0];
			    		String number = (parts[0] == null) ? "" : parts[1];
			    		Double price = region.getFlag(DefaultFlag.PRICE);
			    		sdhplayer.setCurrentPlot(region);
			    		player.sendMessage(ChatColor.YELLOW+"You have selected plot number "+number+" in the "+zone+" zone.");
			    		player.sendMessage(ChatColor.GRAY+"This will cost you "+ChatColor.GREEN+price+ChatColor.GOLD+"g");
			    		player.sendMessage(ChatColor.GRAY+"Click the accept button to buy, another plot to rechoose, or the agent to cancel.");
			    		sdhplayer.setPurchaseMode(true);
			    		sdhplayer.setAcceptDenyMode(true);
			    		break;
			    	}
			    	else {
			    		String Owners = region.getOwners().toPlayersString();
			    		player.sendMessage(ChatColor.RED+"This plot already belongs to "+Owners);
			    		player.sendMessage(ChatColor.GRAY+"To cancel plot selection, click the NPC.");
			    	}
			    }
			}
		}
		if(sdhplayer.acceptBlockSelect == true) {
			sdhplayer.setAcceptBlock(eventBlock);
			player.sendMessage(ChatColor.GRAY+"Right click the NPC to confirm accept block.");
			event.setCancelled(true);
		}
		else if(sdhplayer.denyBlockSelect == true) {
			sdhplayer.setDenyBlock(eventBlock);
			player.sendMessage(ChatColor.GRAY+"Right click the NPC to confirm deny block.");
			event.setCancelled(true);
		}
		else if(sdhplayer.acceptDenyMode == true) {
			// Accept / Deny Mode
			Vector acceptBlock = sdhplayer.getAcceptBlock();
			Vector denyBlock   = sdhplayer.getDenyBlock();
			if(acceptBlock != null && denyBlock != null) {
				if(eventBlock.getY() == acceptBlock.getY() &&
				   eventBlock.getX() == acceptBlock.getX() &&
				   eventBlock.getZ() == acceptBlock.getZ()) {
					// Its the accept block.
					if(sdhplayer.purchaseMode == true) {
						ProtectedRegion region = sdhplayer.currentPlot;
						Double price = region.getFlag(DefaultFlag.PRICE);
						if(SirDrakeHeart.economy.has(player.getName(), price)) {
							SirDrakeHeart.economy.bankWithdraw(player.getName(), price);
							DefaultDomain owners = new DefaultDomain();
							owners.addPlayer(player.getName());
							region.setOwners(owners);
							
							String[] tmp = region.getId().split("_");
				    		String[] parts = tmp[1].split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				    		String zone = (parts[1] == null) ? "" : parts[0];
				    		String number = (parts[0] == null) ? "" : parts[1];
							player.sendMessage(ChatColor.GREEN+"Congratulations! You now own plot number "+number+" in the "+zone+" zone.");
							sdhplayer.setAcceptDenyMode(false);
							sdhplayer.setPurchaseMode(false);
							sdhplayer.setPlotBuyMode(false);
							// Do Sign
							editPlotSign(player,zone,number,region);
						}
						else {
							player.sendMessage(ChatColor.RED+"You cannot afford this plot! Purchase cancelled.");
						}
					}
					else {
						sdhplayer.setAcceptDenyMode(false);
						sdhplayer.setPurchaseMode(false);
						sdhplayer.setPlotBuyMode(true);
						player.sendMessage(ChatColor.GRAY+"Please go and find your plot and left click anywhere inside it.");
					}
					event.setCancelled(true);
				}
				else if(eventBlock.getY() == denyBlock.getY() &&
						eventBlock.getX() == denyBlock.getX() &&
						eventBlock.getZ() == denyBlock.getZ()) {
					// Its the deny block.
					sdhplayer.setAcceptDenyMode(false);
					sdhplayer.setPurchaseMode(false);
					sdhplayer.setPlotBuyMode(false);
					player.sendMessage(ChatColor.GRAY+"Plot Purchase cancelled.");

					event.setCancelled(true);
				}
			}
			else {
				player.sendMessage(ChatColor.RED+"Error in configuration. Report to an administrator.");
			}
		}
	}
}
