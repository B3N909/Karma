package me.savant.karma;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class Karma extends JavaPlugin implements Listener{
	
	
	public void onEnable()
	{
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, this);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run()
			{
				giveGlobalPoints();
			}
		}, 3600L, 3600L);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("karma"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player) sender;
				if(args.length == 0)
				{
					p.sendMessage(ChatColor.GREEN + p.getName() + "'s karma is: " + getLevel(p));
					return true;
				}
				
				if(p.hasPermission("karma.admin"))
				{
					if(args.length == 3)
					{
						if(Float.parseFloat(args[1])!= 0)
						{
							float amount = Float.parseFloat(args[1]);
							if(Bukkit.getPlayer(args[2]) != null)
							{
								Player target = Bukkit.getPlayer(args[2]);
								if(args[0].equalsIgnoreCase("give"))
								{
									givePoint(p, amount);
									p.sendMessage(ChatColor.GREEN + "Added " + amount + " point(s) from " + p.getName() + " to make a balance of " + getPoints(p) + ". " + p.getName() + " is now " + getLevel(p));
									return true;
								}
								else if(args[0].equalsIgnoreCase("remove"))
								{
									removePoint(p, amount);
									p.sendMessage(ChatColor.GREEN + "Removed " + amount + " point(s) from " + p.getName() + " to make a balance of " + getPoints(p) + ". " + p.getName() + " is now " + getLevel(p));
									return true;
								}
								else
									p.sendMessage(ChatColor.RED + "(Unknown Modifer) /karma give amount Player  or   /karma remove amount Player");
							}
							else
								p.sendMessage(ChatColor.RED + "(Unknown Player) /karma give amount Player  or   /karma remove amount Player");
						}
						else
						{
							p.sendMessage(ChatColor.RED + "(Unknown Amount) /karma give amount Player  or   /karma remove amount Player");
						}
					}
					else
					{
						if(Bukkit.getPlayer(args[0]) != null)
						{
							Player target = Bukkit.getPlayer(args[0]);
							p.sendMessage(ChatColor.GREEN + target.getName() + " karm is: " + getLevel(target));
							return true;
						}
						else
							p.sendMessage(ChatColor.RED + "(Unknown) /karma give amount Player  or   /karma remove amount Player");
					}
				}
				else
					p.sendMessage(ChatColor.RED + "You do not have permission to use this command. (karma.admin)");
			}
			else
				System.out.println("Console cannot use that command.");
		}
		return false;
	}
	
	private void giveGlobalPoints()
	{
		for(Player p : Bukkit.getOnlinePlayers())
		{
			givePoint(p, 0.04f);
		}
	}
	
	private void givePoint(Player p, float amount)
	{
		getConfig().set(p.getName().toLowerCase().toString(), getPoints(p) + amount);
		reformat(p);
		update();
	}
	
	private void removePoint(Player p, float amount)
	{
		getConfig().set(p.getName().toLowerCase().toString(), getPoints(p) - amount);
		reformat(p);
		update();
	}
	
	private float getPoints(Player p)
	{
		if(getConfig().getString(p.getName().toLowerCase().toString()) == null)
		{
			getConfig().set(p.getName().toLowerCase().toString(), "0");
			return 0f;
		}
		else
			return Float.parseFloat(getConfig().getString(p.getName().toLowerCase().toString()));
	}
	
	private void reformat(Player p)
	{
		getConfig().set(p.getName().toLowerCase().toString(), clamp(getPoints(p)));
		saveConfig();
	}
	
	private float clamp(float fl)
	{
		if(fl > 10)
		{
			return 10;
		}
		else if(fl < -10)
		{
			return -10;
		}
		else return fl;
	}
	
	private String getLevel(Player p)
	{
		if(getPoints(p) > 5)
		{
			return ChatColor.GREEN + "GOOD";
		}
		else if(getPoints(p) < -5)
		{
			return ChatColor.RED + "EVIL";
		}
		else
		{
			return ChatColor.GRAY + "NEUTRAL";
		}
	}
	
	@EventHandler
	public void kill(PlayerDeathEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			if(e.getEntity().getKiller() instanceof Player)
			{
				removePoint(e.getEntity().getKiller(), 1);
				e.getEntity().getKiller().sendMessage(ChatColor.RED + "You lost a Karma point.");
			}
		}
	}
	
	@EventHandler
	public void join(PlayerJoinEvent e)
	{
		update();
	}
	
	private void update()
	{
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();
		
		Objective objective = board.registerNewObjective("karma", "dummy");
		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		objective.setDisplayName("Karma");
		
		for(Player p : Bukkit.getOnlinePlayers())
		{
			Score score = objective.getScore(p);
			score.setScore((int)getPoints(p));
			
		}
		for(Player p : Bukkit.getOnlinePlayers())
		{
			p.setScoreboard(board);
		}
		
	}
}
