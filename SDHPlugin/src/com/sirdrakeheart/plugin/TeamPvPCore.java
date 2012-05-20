package com.sirdrakeheart.plugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeamPvPCore {
	
	// Core vars
	public static Boolean gameInProgress = false;
	public static Integer currentRound = 1;
	public static Map<String,Player> players = new HashMap<String,Player>();
	public static Map<String,Player> redTeamAlive = new HashMap<String,Player>();
	public static Map<String,Player> blueTeamAlive = new HashMap<String,Player>();
	public static Map<String,Player> killed = new HashMap<String,Player>();
	private static Map<Integer,String> roundWinners = new HashMap<Integer,String>();
	public static Location lobbyLocation;
	
	
	// Modes
	public static Boolean joinPvP = false; // Initial Participate process
	public static Boolean teamApplicants = false; // Join Teams Process
	
	// Settings
	public static Double roundPrize = 1000.00;
	
	// Teams
	public static Map<String,Player> redTeam = new HashMap<String,Player>();
	public static Map<String,Player> blueTeam = new HashMap<String,Player>();

	public static void startGame() {
		players.clear();
		redTeam.clear();
		blueTeam.clear();
		redTeamAlive.clear();
		blueTeamAlive.clear();
		killed.clear();
		roundWinners.clear();
		gameInProgress = true;
		
		Utils.broadcastPvPMessage("PvP Game is about to commence. Type &a/pvp play&7 To enter!");
		Utils.broadcastPvPMessage("Team PvP will start in 2 minutes.");
		TeamPvPCore.joinPvP = true;
		SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

		   public void run() {
			   Utils.broadcastPvPMessage("Team PvP will start in 1 minute and 30 seconds. Type &a/pvp play&7 To enter!");
			   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

				   public void run() {
					   Utils.broadcastPvPMessage("Team PvP will start in 1 minute. Type &a/pvp play&7 To enter!");
					   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

						   public void run() {
							   Utils.broadcastPvPMessage("Team PvP will start in 30 seconds. Type &a/pvp play&7 To enter!");
							   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

								   public void run() {
									   Utils.broadcastPvPMessage("Team PvP is about to commence. Applications closed. Teleporting players.");
									   TeamPvPCore.beginGame();
									   TeamPvPCore.joinPvP = false;
								   }
								}, 600L);
						   }
						}, 600L);
				   }
				}, 600L);
		   }
		}, 600L);
	}
	
	public static void beginGame() {
		// Teleport all participating players
		if(lobbyLocation != null) {
			for(Player p : players.values()) {
				p.teleport(lobbyLocation);
			}
		}
		else {
			Utils.broadcastPvPMessage("Error with teleportation! Please make your own way");
		}
		Utils.broadcastPvPMessage("Please select your team by &a/pvp join blue&7 or &a/pvp join red");
		teamApplicants = true;
	}
	
	public static void joinGame(Player player) {
		if(joinPvP = true) {
			if(players.containsKey(player.getName())) {
				player.sendMessage(ChatColor.GREEN+"You are already participating.");
			}
			else {
				players.put(player.getName(), player);
				Utils.broadcastPvPMessage(player.getDisplayName()+ChatColor.GRAY+" has joined the PvP battle.");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"Applications are closed.");
		}
	}
	
	public static void leaveGame(Player player) {
		if(joinPvP = true) {
			if(!players.containsKey(player.getName())) {
				player.sendMessage(ChatColor.GREEN+"You were not participating.");
			}
			else {
				players.remove(player.getName());
				Utils.broadcastPvPMessage(player.getDisplayName()+ChatColor.GRAY+" has left the PvP battle.");
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"Applications are closed.");
		}
	}
	
	public static void joinTeam(Player player, String team) {
		String playerName = player.getName();
		if(teamApplicants == true) {
			if(team.toLowerCase().contains("b")) {
				blueTeam.put(playerName, player);
				if(redTeam.containsKey(playerName)) {
					redTeam.remove(playerName);
				}
				Utils.broadcastPvPMessage(player.getDisplayName()+"&7 is on the Blue team.");
			}
			else {
				redTeam.put(player.getName(), player);
				if(blueTeam.containsKey(playerName)) {
					blueTeam.remove(playerName);
				}
				Utils.broadcastPvPMessage(player.getDisplayName()+"&7 is on the Red team.");
			}
			
			if(players.size() > (redTeam.size() + blueTeam.size())) {
				Utils.broadcastPvPMessage("Still waiting for players to select team...");
			}
			else {
				if(redTeam.size() > (Math.floor(players.size()/2)+1)) {
					Utils.broadcastPvPMessage("There are too many people on the red team!");
				}
				else if(blueTeam.size() > (Math.floor(players.size()/2)+1)) {
					Utils.broadcastPvPMessage("There are too many people on the blue team!");
				}
				else {
					Utils.broadcastPvPMessage("Teams verified. Prepare yourselves.");
					//openDoors(1);
					startRound(1);
				}
			}
		}
		else {
			player.sendMessage(ChatColor.RED+"You cannot join a team at this moment.");
		}
	}
	
	public static void endGame() {
		gameInProgress = false;
		
		Integer blueCount = 0, redCount = 0;
		
		for(String t : roundWinners.values()) {
			if(t.equalsIgnoreCase("Blue")) {
				blueCount++;
			}
			else {
				redCount++;
			}
		}
		String winningTeam = "";
		if(blueCount > redCount) {
			winningTeam = ChatColor.BLUE+"Blue Team";
			for(String p : blueTeam.keySet()) {
				SirDrakeHeart.economy.depositPlayer(p, 2500);
			}
			for(String p : redTeam.keySet()) {
				SirDrakeHeart.economy.depositPlayer(p, 250);
			}
		}
		else {
			winningTeam = ChatColor.RED+"Red Team";
			for(String p : redTeam.keySet()) {
				SirDrakeHeart.economy.depositPlayer(p, 2500);
			}
			for(String p : blueTeam.keySet()) {
				SirDrakeHeart.economy.depositPlayer(p, 250);
			}
		}
		
		Utils.broadcastPvPMessage("Team PvP over. The winning team is: "+winningTeam);
		Utils.broadcastPvPMessage("Winning team has won 2500g each, losing team has recieved 250g each for participating.");
	}
	
	public static void startRound(final Integer roundNumber) {
		currentRound = roundNumber;
		Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 10 seconds.");
		SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

		   public void run() {
			   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 9 seconds.");
			   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

				   public void run() {
					   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 8 seconds.");
					   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

						   public void run() {
							   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 7 seconds.");
							   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

								   public void run() {
									   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 6 seconds.");
									   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

										   public void run() {
											   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 5 seconds.");
											   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

												   public void run() {
													   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 4 seconds.");
													   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

														   public void run() {
															   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 3 seconds.");
															   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																   public void run() {
																	   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 2 seconds.");
																	   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																		   public void run() {
																			   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin in 1 second.");
																			   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																				   public void run() {
																					   Utils.broadcastPvPMessage("Round "+roundNumber+" is about to begin.");
																					   TeamPvPCore.beginRound(roundNumber);
																				   }
																				}, 20L);
																		   }
																		}, 20L);
																   }
																}, 20L);
														   }
														}, 20L);
												   }
												}, 20L);
										   }
										}, 20L);
								   }
								}, 20L);
						   }
						}, 20L);
				   }
				}, 20L);
		   }
		}, 20L);
	}
	
	public static void beginRound(final Integer roundNumber) {
		redTeamAlive = redTeam;
		blueTeamAlive = blueTeam;
		
		String redTeamList = "";
		Integer i = 0;
		for(String p : redTeam.keySet()) {
			if(i > 0) {
				redTeamList += ", ";
			}
			redTeamList += p;
			i++;
		}
		
		String blueTeamList = "";
		i = 0;
		for(String p : blueTeam.keySet()) {
			if(i > 0) {
				blueTeamList += ", ";
			}
			blueTeamList += p;
			i++;
		}
		
		Utils.broadcastPvPMessage("&cRed Team:&7 "+redTeamList);
		Utils.broadcastPvPMessage("&bBlue Team:&7 "+blueTeamList);
		Utils.broadcastPvPMessage("Round ends in 6 minutes.");
		SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

		   public void run() {
			   Utils.broadcastPvPMessage("Round ends in 5 minutes");
			   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

				   public void run() {
					   Utils.broadcastPvPMessage("Round ends in 4 minutes");
					   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

						   public void run() {
							   Utils.broadcastPvPMessage("Round ends in 3 minutes");
							   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

								   public void run() {
									   Utils.broadcastPvPMessage("Round ends in 2 minutes");
									   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

										   public void run() {
											   Utils.broadcastPvPMessage("Round ends in 1 minute 30 seconds");
											   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

												   public void run() {
													   Utils.broadcastPvPMessage("Round ends in 1 minute");
													   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

														   public void run() {
															   Utils.broadcastPvPMessage("Round ends in 30 seconds");
															   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																   public void run() {
																	   Utils.broadcastPvPMessage("Round ends in 20 seconds");
																	   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																		   public void run() {
																			   Utils.broadcastPvPMessage("Round ends in 10 seconds");
																			   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																				   public void run() {
																					   Utils.broadcastPvPMessage("Round ends in 9 seconds");
																					   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																						   public void run() {
																							   Utils.broadcastPvPMessage("Round ends in 8 seconds");
																							   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																								   public void run() {
																									   Utils.broadcastPvPMessage("Round ends in 7 seconds");
																									   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																										   public void run() {
																											   Utils.broadcastPvPMessage("Round ends in 6 seconds");
																											   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																												   public void run() {
																													   Utils.broadcastPvPMessage("Round ends in 5 seconds");
																													   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																														   public void run() {
																															   Utils.broadcastPvPMessage("Round ends in 4 seconds");
																															   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																																   public void run() {
																																	   Utils.broadcastPvPMessage("Round ends in 3 seconds");
																																	   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																																		   public void run() {
																																			   Utils.broadcastPvPMessage("Round ends in 2 seconds");
																																			   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																																				   public void run() {
																																					   Utils.broadcastPvPMessage("Round ends in 1 second");
																																					   SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

																																						   public void run() {
																																							   Utils.broadcastPvPMessage("Round "+roundNumber+" ended.");
																																							   TeamPvPCore.endRound(roundNumber);
																																						   }
																																						}, 20L);
																																				   }
																																				}, 20L);
																																		   }
																																		}, 20L);
																																   }
																																}, 20L);
																														   }
																														}, 20L);
																												   }
																												}, 20L);
																										   }
																										}, 20L);
																								   }
																								}, 20L);
																						   }
																						}, 20L);
																				   }
																				}, 20L);
																		   }
																		}, 200L);
																   }
																}, 200L);
														   }
														}, 360L);
												   }
												}, 360L);
										   }
										}, 360L);
								   }
								}, 1200L);
						   }
						}, 1200L);
				   }
				}, 1200L);
		   }
		}, 1200L);
	}
	
	public static void endRound(final Integer roundNumber) {
		SirDrakeHeart.main.getServer().getScheduler().cancelTasks(SirDrakeHeart.main);
		
		String survivorsList = "";
		Integer i = 0;
		for(String p : redTeamAlive.keySet()) {
			if(i > 0) {
				survivorsList += ", ";
			}
			survivorsList += ChatColor.RED+p+ChatColor.GRAY;
			SirDrakeHeart.economy.depositPlayer(p, roundPrize);
			i++;
		}
		
		for(String p : blueTeamAlive.keySet()) {
			if(i > 0) {
				survivorsList += ", ";
			}
			survivorsList += ChatColor.BLUE+p+ChatColor.GRAY;
			SirDrakeHeart.economy.depositPlayer(p, roundPrize);
			i++;
		}
		
		if(blueTeamAlive.size() > redTeamAlive.size()) {
			Utils.broadcastPvPMessage("The "+ChatColor.BLUE+"Blue Team"+ChatColor.GRAY+" has won this round!");
			roundWinners.put(roundNumber, "Blue");
		}
		else {
			Utils.broadcastPvPMessage("The "+ChatColor.RED+"Red Team"+ChatColor.GRAY+" has won this round!");
			roundWinners.put(roundNumber, "Red");
		}
		
		Utils.broadcastPvPMessage("The survivors of this round were: "+survivorsList);
		if(roundNumber < 3) {
			Utils.broadcastPvPMessage("Next round will begin in 1 minute.");
			SirDrakeHeart.main.getServer().getScheduler().scheduleSyncDelayedTask(SirDrakeHeart.main, new Runnable() {

				   public void run() {
					   startRound(roundNumber+1);
				   }
				}, 1200L);
		}
		else {
			endGame();
		}
	}
	
	
	public static void processKill(Player killer, Player victim) {
		ChatColor victimColour = ChatColor.BLUE;
		ChatColor killerColour = ChatColor.BLUE;
		if(redTeam.containsKey(victim.getName())) {
			victimColour = ChatColor.RED;
		}
		if(redTeam.containsKey(killer.getName())) {
			killerColour = ChatColor.RED;
		}
		
		redTeamAlive.remove(victim.getName());
		blueTeamAlive.remove(victim.getName());
		killed.put(victim.getName(), victim);
		
		Utils.broadcastPvPMessage(victimColour+victim.getName()+ChatColor.GRAY+" was killed by "+killerColour+killer.getName());
		
		if(redTeamAlive.size() == 0 || blueTeamAlive.size() == 0) {
			endRound(currentRound);
		}
	}
	
}
