package net.oldschoolminecraft.dv;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DVAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length > 0 && args[0].equalsIgnoreCase("help") || args.length > 0 && args[0].equalsIgnoreCase("?"))
        {
            if (sender.isOp() || sender.hasPermission("DayVote.StaffMenu"))
            {
                sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
                sender.sendMessage("§eStaff Commands:");
                sender.sendMessage("§d/dvadmin debug §8- §7Enables debugging mode.");
                sender.sendMessage("§d/dvadmin reload §8- §7Reloads the config.yml.");
                sender.sendMessage("§d/dvadmin reset §8- §7Resets an active vote.");
                sender.sendMessage("§d/dvadmin togglerainvote §8- §7Toggles rain vote.");
                sender.sendMessage("§d/dvadmin togglethunder §8- §7Toggles chance for thunder.");
                sender.sendMessage("§d/dvadmin weatherinfo §8- §7Displays weather info.");
                return true;
            } else {
                sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
                sender.sendMessage("§eCommands:");
                sender.sendMessage("§d/vote day §8- §7Starts a vote for day.");
                sender.sendMessage("§d/vote rain §8- §7Starts a vote for rain.");
                sender.sendMessage("§d/vote help §8- §7Reveals this help page.");
                sender.sendMessage("§d/vote info §8- §7Displays plugin information.");
                sender.sendMessage("§d/vote <yes:no> §8- §7Casts a vote for day or night.");
                return true;
            }
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("debug") && (sender.isOp() || sender.hasPermission("DayVote.Debug")))
        {
            if (DayVote.getInstance().isDebugModeEnabled())
            {
                DayVote.getInstance().setDebugMode(false);
                sender.sendMessage("§7Debug mode §4disabled§7!");
            } else {
                DayVote.getInstance().setDebugMode(true);
                sender.sendMessage("§7Debug mode §aenabled§7!");
                return true;
            }
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && (sender.isOp() || sender.hasPermission("DayVote.Reload")))
        {
            DayVote.getInstance().getConfig().reload();
            sender.sendMessage("§7Reloaded §bconfig.yml§7!");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reset") && (sender.isOp() || sender.hasPermission("DayVote.Reset")))
        {
            Vote vote = DayVote.getInstance().getActiveVote();

            if (vote == null)
            {
                sender.sendMessage("§4Theres no active vote to reset!");
                return true;
            } else {
                if (DayVote.getInstance().getVoteType() == DayVoteType.DAY)
                {
                    DayVote.getInstance().processDayVote();
                    return true;
                } else if (DayVote.getInstance().getVoteType() == DayVoteType.RAIN) {
                    DayVote.getInstance().processRainVote();
                    return true;
                }
                DayVote.getInstance().sendDebugMessage("Active vote terminated.");
                return true;
            }
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("togglerainvote") && (sender.isOp() || sender.hasPermission("DayVote.ToggleRainVote")))
        {
            if (DayVote.getInstance().canVoteRain())
            {
                DayVote.getInstance().setAllowRainVote(false);
                sender.sendMessage("§7Rain vote §4disabled§7!");
                DayVote.getInstance().sendDebugMessage("allowRainVote was set to false!");
            } else {
                DayVote.getInstance().setAllowRainVote(true);
                sender.sendMessage("§7Rain vote §aenabled§7!");
                DayVote.getInstance().sendDebugMessage("allowRainVote was set to true!");
                return true;
            }
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("togglethunder") && (sender.isOp() || sender.hasPermission("DayVote.ToggleThunder")))
        {
            if (DayVote.getInstance().isThunderingAllowed())
            {
                DayVote.getInstance().setAllowThunder(false);
                sender.sendMessage("§7Thundering §4disabled§7!");
                DayVote.getInstance().sendDebugMessage("allowThunder was set to false!");
            } else {
                DayVote.getInstance().setAllowThunder(true);
                sender.sendMessage("§7Thundering §aenabled§7!");
                DayVote.getInstance().sendDebugMessage("allowThunder was set to true!");
                return true;
            }
            return true;
        }

        if (args.length > 0 && (args[0].equalsIgnoreCase("weatherinfo") || args[0].equalsIgnoreCase("forecast")) && (sender.isOp()) || (sender.hasPermission("DayVote.WeatherInfo")))
        {
            if (Bukkit.getServer().getWorld("world").hasStorm())
            {
                if (Bukkit.getServer().getWorld("world").isThundering())
                {
                    sender.sendMessage("§1[§bOSM§1] §7Current weather: §bSTORM");
                    sender.sendMessage("§1[§bOSM§1] §7Weather Duration Left: §b" + Bukkit.getServer().getWorld("world").getWeatherDuration() + " §8(§d" + DayVote.getInstance().formatTimeFromTicks(Bukkit.getServer().getWorld("world").getWeatherDuration()) + "§8)");
                    sender.sendMessage("§1[§bOSM§1] §7Thundering: §aYES");
                    sender.sendMessage("§1[§bOSM§1] §7Thunder Duration Left: §b" + Bukkit.getServer().getWorld("world").getWeatherDuration() + " §8(§d" + DayVote.getInstance().formatTimeFromTicks(Bukkit.getServer().getWorld("world").getThunderDuration()) + "§8)");
                    return true;
                }
                sender.sendMessage("§1[§bOSM§1] §7Current weather: §bRAIN");
                sender.sendMessage("§1[§bOSM§1] §7Weather Duration Left: §b" + Bukkit.getServer().getWorld("world").getWeatherDuration() + " §8(§d" + DayVote.getInstance().formatTimeFromTicks(Bukkit.getServer().getWorld("world").getWeatherDuration()) + "§8)");
                sender.sendMessage("§1[§bOSM§1] §7Thundering: §4NO");
                return true;
            }
            sender.sendMessage("§1[§bOSM§1] §7Current weather: §bSUN");
            sender.sendMessage("§1[§bOSM§1] §7Weather Duration Left: §b" + Bukkit.getServer().getWorld("world").getWeatherDuration() + " §8(§d" + DayVote.getInstance().formatTimeFromTicks(Bukkit.getServer().getWorld("world").getWeatherDuration()) + "§8)");
            sender.sendMessage("§1[§bOSM§1] §7Thundering: §4NO");
            return true;
        }

        else {
            if (sender.isOp() || sender.hasPermission("DayVote.StaffMenu"))
            {
                sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
                sender.sendMessage("§eStaff Commands:");
                sender.sendMessage("§d/dvadmin debug §8- §7Enables debugging mode.");
                sender.sendMessage("§d/dvadmin reload §8- §7Reloads the config.yml.");
                sender.sendMessage("§d/dvadmin reset §8- §7Resets an active vote.");
                sender.sendMessage("§d/dvadmin togglerainvote §8- §7Toggles rain vote.");
                sender.sendMessage("§d/dvadmin togglethunder §8- §7Toggles chance for thunder.");
                sender.sendMessage("§d/dvadmin weatherinfo §8- §7Displays weather info.");
                return true;
            } else {
                sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
                sender.sendMessage("§eCommands:");
                sender.sendMessage("§d/vote day §8- §7Starts a vote for day.");
                sender.sendMessage("§d/vote rain §8- §7Starts a vote for rain.");
                sender.sendMessage("§d/vote help §8- §7Reveals this help page.");
                sender.sendMessage("§d/vote info §8- §7Displays plugin information.");
                sender.sendMessage("§d/vote <yes:no> §8- §7Casts a vote for day or night.");
                return true;
            }
        }
    }
}
