package com.sirdrakeheart.plugin;

import java.io.File;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class SirDrakeHeart extends JavaPlugin {
	
	Logger log;
	public static String filepath = "plugins" + File.separator + "SirDrakeHeart" + File.separator;
	public static SirDrakeHeart main;
	
	public static Permission perms = null;
	public static Economy economy = null;
	public void onEnable(){ 
		log = this.getLogger();
		log.setFilter(new DCReasonFilter(this));
		SirDrakeHeart.main = this;
		
		PluginManager pm = getServer().getPluginManager();
	    
	    Listener events = new Events();
	    pm.registerEvents(events, this);
		
		setupPermissions();
		setupEconomy();
		
		log.info("SirDrakeHeart plugin enabled.");
	}
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return (perms != null);
    }
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		economy = economyProvider.getProvider();
		return (economy != null);
	}
	 
	public void onDisable(){ 
		log.info("SirDrakeHeart plugin disabled.");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender instanceof Player) {
			if(cmd.getName().equalsIgnoreCase("makeprivate")){
				return SDHCommands.makeprivate(sender, cmd, commandLabel, args);
			}
			else if(cmd.getName().equalsIgnoreCase("giveplot")) {
				return SDHCommands.giveplot(sender, cmd, commandLabel, args);
			}
			else if(cmd.getName().equalsIgnoreCase("pay")) {
				return SDHCommands.pay(sender, cmd, commandLabel, args);
			}
			else if(cmd.getName().equalsIgnoreCase("setupplot")) {
				return SDHCommands.setupplot(sender, cmd, commandLabel, args);
			}
			else if(cmd.getName().equalsIgnoreCase("flagall")) {
				return SDHCommands.flagall(sender, cmd, commandLabel, args);
			}
			return false; 
		}
		else {
			if(cmd.getName().equalsIgnoreCase("makeprivate")){
				return SDHCMDCommands.makeprivate(sender, cmd, commandLabel, args);
			}
			return false;
		}
	}
}
