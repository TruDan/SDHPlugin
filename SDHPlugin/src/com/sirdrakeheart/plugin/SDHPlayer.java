package com.sirdrakeheart.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.SkillType;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class SDHPlayer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public String name;
	public HashSet<SDHPlayer> data = new HashSet<SDHPlayer>();
	
	public Integer powerLevel = 1;
	
	// Estate Agent
	public Boolean plotBuyMode = false;
	public Boolean acceptDenyMode = false;
	public Boolean purchaseMode = false;
	public Boolean acceptBlockSelect = false;
	public Boolean denyBlockSelect = false;
	public Location BlockClickPosition = null;
	public ProtectedRegion currentPlot = null;
	public Vector acceptBlock;
	public Vector denyBlock;
	
	// Bankers
	public Boolean chestBankBlockSelectionMode = false;
	public Vector chestBankBlock;
	
	
	public SDHPlayer(String name) {
		this.name = name;
		this.plotBuyMode = false;
		this.acceptDenyMode = false;
	}
	
	// Options & Values
	
	public void setPlotBuyMode(Boolean mode) {
		this.plotBuyMode = mode;
	}
	
	public void setCurrentPlot(ProtectedRegion region) {
		this.currentPlot = region;
	}
	
	public void setAcceptDenyMode(Boolean mode) {
		this.acceptDenyMode = mode;
	}
	
	public void setChestBankBlockSelectionMode(Boolean mode) {
		this.chestBankBlockSelectionMode = mode;
	}
	
	public void setChestBankBlock(Vector block) {
		this.chestBankBlock = block;
	}
	
	public Vector getAcceptBlock() {
		return this.acceptBlock;
	}
	
	public Vector getDenyBlock() {
		return this.denyBlock;
	}
	
	public Vector getChestBankBlock() {
		return this.chestBankBlock;
	}
	
	public Integer getPowerLevel() {
		return this.powerLevel;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void save() {
		try {
			data.add(this);
			String path = SirDrakeHeart.filepath + "players" + File.separator + this.name + ".dat";
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(data);
			oos.flush();
			oos.close();
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		try{
			String name = this.name;
			String path = SirDrakeHeart.filepath + "players" + File.separator + name + ".dat";
			
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
			Object result = ois.readObject();
			HashSet<SDHPlayer> inputhashset = null;
			
			if (result instanceof HashSet<?>) {
				inputhashset = (HashSet<SDHPlayer>) result;
			}
			if (inputhashset != null) {
				this.data = inputhashset;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void setPurchaseMode(boolean b) {
		this.purchaseMode = b;
	}

	public void setAcceptBlockSelect(boolean b) {
		this.acceptBlockSelect = b;
	}
	
	public void setDenyBlockSelect(boolean b) {
		this.denyBlockSelect = b;
	}

	public void setAcceptBlock(Vector acceptblock2) {
		this.acceptBlock = acceptblock2;
	}
	
	public void setDenyBlock(Vector denyblock2) {
		this.denyBlock = denyblock2;
	}

	public void setPowerLevel(Integer newLevel) {
		this.powerLevel = newLevel;		
	}
	
	public void updatePowerLevel(Boolean broadcast) {
		Player player = SirDrakeHeart.main.getServer().getPlayer(this.getName());
		Integer currentLevel = this.getPowerLevel();
		this.updatePowerLevel();
		Integer newLevel = this.getPowerLevel();
		if(newLevel > currentLevel) {
			Utils.broadcastToPlayers(player,"[ "+ChatColor.AQUA+"Info "+ChatColor.WHITE+"] "+ChatColor.YELLOW+player.getName()+" has upgraded up to Combat Level "+newLevel);
		}
	}
	
	public void updatePowerLevel() {
		Player player = SirDrakeHeart.main.getServer().getPlayer(this.getName());
		ExperienceAPI xpapi = new ExperienceAPI();
		
		Integer levels = xpapi.getLevel(player, SkillType.ARCHERY) + xpapi.getLevel(player, SkillType.SWORDS) + xpapi.getLevel(player, SkillType.UNARMED);
		Integer newLevel = (int) Math.floor(levels/3);
		
		this.setPowerLevel(newLevel);
	}
	
}
