package com.sirdrakeheart.plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;


import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.ApplicableRegionSet;

import static com.sk89q.worldguard.bukkit.BukkitUtil.*;

public class Events implements Listener {
	
	public static boolean filterCheckGeneric = false;
	public static boolean filterCheckStream = false;
	public static boolean filterCheckOverflow = false;
	public static boolean filterCheckQuitting = false;
	public static boolean filterCheckTimeout = false;
	public static boolean filterCheckJoin = false;
	
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
		String format = convertColors("[{COLOR}"+level+"&f] ["+prefix+group+"&f] "+suffix+player.getName()+"&7: &f").replaceAll("&([0-9a-f])", "ยง$1");
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
		sdhplayer.updatePowerLevel(true);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		PermissionUser user = PermissionsEx.getUser(player);
		SDHPlayer sdhplayer = SDHPlayers.getPlayer(player.getName());
		String world = player.getWorld().getName();
		sdhplayer.updatePowerLevel();
		
		String suffix = convertColors(user.getOption("suffix", world));
		player.setDisplayName(suffix+player.getName()+ChatColor.GRAY);
		event.setJoinMessage("[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" has joined the game.");
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if(TeamPvPCore.players.containsKey(playerName)) {
			TeamPvPCore.players.remove(playerName);
			TeamPvPCore.redTeamAlive.remove(playerName);
			TeamPvPCore.blueTeamAlive.remove(playerName);
			TeamPvPCore.redTeam.remove(playerName);
			TeamPvPCore.blueTeam.remove(playerName);
			TeamPvPCore.killed.remove(playerName);
			Utils.broadcastPvPMessage(player.getDisplayName()+ChatColor.GRAY+" has resigned from the game.");
		}
		
		String message = "has left the game.";
		if (filterCheckGeneric) {
			message = "lost connection to the game.";
		}
		if (filterCheckStream) {
			message = "lost connection to the game.";
		}
		if (filterCheckOverflow) {
			message = "lost connection to the game.";
		}
		if (filterCheckQuitting) {
			message = "has quit the game.";
		}
		if (filterCheckTimeout) {
			message = "lost connection to the game.";
		}
		event.setQuitMessage("[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" "+message);
		SirDrakeHeart.log.info(player.getName()+" "+message);
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if(TeamPvPCore.players.containsKey(playerName)) {
			TeamPvPCore.redTeamAlive.remove(playerName);
			TeamPvPCore.blueTeamAlive.remove(playerName);
			TeamPvPCore.redTeam.remove(playerName);
			TeamPvPCore.blueTeam.remove(playerName);
			TeamPvPCore.killed.remove(playerName);
			Utils.broadcastPvPMessage(player.getDisplayName()+ChatColor.GRAY+" has resigned from the game.");
		}
		
		event.setLeaveMessage("[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" has been kicked from the game.");
	}
	
	public String convertColors(String str) {
		Pattern color_codes = Pattern.compile("&([0-9A-Fa-fkK])");
		Matcher find_colors = color_codes.matcher(str);
		while (find_colors.find()) {
		 str = find_colors.replaceFirst(new StringBuilder().append("ง").append(find_colors.group(1)).toString());
		 find_colors = color_codes.matcher(str);
		}
		return str;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		if (event.isCancelled()) {
			return;
		}

		Entity p1 = event.getDamager();
		Entity p2 = event.getEntity();
		
		if (p1 instanceof Projectile) {
			p1 = ((Projectile) p1).getShooter();
		}
		
		if (event.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) event.getEntity();
			if (wolf.getOwner() != null) {
				try {
					p1 = (Entity) wolf.getOwner();
				} catch (Exception e) {
					
				}
			}
		}
		
		if ((p2 == null) || (!(p2 instanceof Player))) {
			return;
		}
		
		if ((p1 == null) || (!(p1 instanceof Player))) {
			return;
		}
		

		Player attacker = (Player) p1;
		Player defender = (Player) p2;
		
		// Check if both are in PvP
		if(TeamPvPCore.players.containsKey(attacker.getName()) && TeamPvPCore.players.containsKey(defender.getName())) {
			// Check if they are both "alive"
			if((TeamPvPCore.redTeamAlive.containsKey(attacker.getName()) || TeamPvPCore.blueTeamAlive.containsKey(attacker.getName())) && (TeamPvPCore.redTeamAlive.containsKey(defender.getName()) || TeamPvPCore.blueTeamAlive.containsKey(defender.getName()))) {
				// Check they are on different teams
				if((TeamPvPCore.redTeamAlive.containsKey(attacker.getName()) && TeamPvPCore.blueTeamAlive.containsKey(defender.getName())) || (TeamPvPCore.redTeamAlive.containsKey(defender.getName()) && TeamPvPCore.blueTeamAlive.containsKey(attacker.getName()))) {
					//Process the death
					TeamPvPCore.processKill(attacker, defender);
				}
				else {
					attacker.sendMessage(ChatColor.RED+"This player is on your own team! You cannot hurt them.");
					event.setCancelled(true);
				}
			}
		}
		
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
							PlotManager.editPlotSign(player,zone,number,region);
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
							//editPlotSign(player,zone,number,region);
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
