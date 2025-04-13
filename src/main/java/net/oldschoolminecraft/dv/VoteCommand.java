package net.oldschoolminecraft.dv;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoteCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length > 0 && args[0].equalsIgnoreCase("help") || args.length > 0 && args[0].equalsIgnoreCase("?"))
        {
            if (sender.isOp() || sender.hasPermission("DayVote.StaffHelp"))
            {
                sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
                sender.sendMessage("§eCommands:");
                sender.sendMessage("§d/vote day §8- §7Starts a vote for day.");
                sender.sendMessage("§d/vote rain §8- §7Starts a vote for rain.");
                sender.sendMessage("§d/vote help §8- §7Reveals this help page.");
                sender.sendMessage("§d/vote info §8- §7Displays plugin information.");
                sender.sendMessage("§d/vote reload §8- §7Reloads the config.yml.");
                sender.sendMessage("§d/vote reset §8- §7Resets an active vote.");
                sender.sendMessage("§d/vote togglerainvote §8- §7Toggles rain vote.");
                sender.sendMessage("§d/vote <yes:no> §8- §7Casts a vote for day or night.");
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

        if (args.length > 0 && args[0].equalsIgnoreCase("togglerainvote") && (sender.isOp() || sender.hasPermission("DayVote.ToggleRainVote")))
        {
            if (DayVote.getInstance().canVoteRain())
            {
                DayVote.getInstance().setAllowRainVote(false);
                sender.sendMessage("§aRain vote §bdisabled§a!");
            } else {
                DayVote.getInstance().setAllowRainVote(true);
                sender.sendMessage("§aRain vote §benabled§a!");
                return true;
            }
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("info"))
        {
            sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
            sender.sendMessage("§7Website: §egithub.com/OldSchoolMinecraft/DayVote");
            sender.sendMessage("§7Authors(s): §emoderator_man");
            sender.sendMessage("§7Contributors(s): §ematjaklol§7, §eSavageUser§7");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload") && (sender.isOp() || sender.hasPermission("DayVote.Reload")))
        {
            DayVote.getInstance().getConfig().reload();
            sender.sendMessage("§aReloaded §bconfig.yml§a!");
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

                sender.sendMessage("§aCurrent vote §bterminated§a!");
                return true;
            }
        }

        if (!(sender instanceof Player))
        {
            sender.sendMessage("§4Command can only be executed by players!");
            return true;
        }

        Vote vote = DayVote.getInstance().getActiveVote();

        if (args.length == 0)
        {
            if (vote == null)
            { // no vote active
                sender.sendMessage("§4No vote is active! Use §b/vote day §4or §b/vote rain §4to start a vote.");
                return true;
            } else {
                int required = (int) DayVote.getInstance().getConfig().getConfigOption("yesVotePercentageRequired");
                int requiredRain = (int) DayVote.getInstance().getConfig().getConfigOption("yesRainVotePercentageRequired");

                if (DayVote.getInstance().getVoteType() == DayVoteType.DAY)
                {
                    sender.sendMessage("§1[§bOSM§1] §7Time Remaining: §b" + DayVote.getInstance().formatTime(DayVote.getInstance().getVoteTimeLeft()));
                } else if (DayVote.getInstance().getVoteType() == DayVoteType.RAIN) {
                    sender.sendMessage("§1[§bOSM§1] §7Time Remaining: §b" + DayVote.getInstance().formatTime(DayVote.getInstance().getRainVoteTimeLeft()));
                }

                sender.sendMessage("§1[§bOSM§1] §7Vote Type: §b" + DayVote.getInstance().getVoteType());

                if (DayVote.getInstance().getVoteType() == DayVoteType.DAY)
                {
                    sender.sendMessage("§1[§bOSM§1] §7Required: §e" + required + "%");
                } else if (DayVote.getInstance().getVoteType() == DayVoteType.RAIN) {
                    sender.sendMessage("§1[§bOSM§1] §7Required: §e" + requiredRain + "%");
                }

                sender.sendMessage("§1[§bOSM§1] §7Current Results: §a" + vote.getYesVotes() + "%§8/§4" + vote.getNoVotes() + "%");

                if (DayVote.getInstance().getVoteType() == DayVoteType.DAY)
                {
                    sender.sendMessage("§1[§bOSM§1] §7Vote for day or night using §a/vote yes §7or §c/vote no§7.");
                } else if (DayVote.getInstance().getVoteType() == DayVoteType.RAIN) {
                    sender.sendMessage("§1[§bOSM§1] §7Vote for rain using §a/vote yes §7or §c/vote no§7.");
                }

                return true;
            }
        }

        if (args[0].equalsIgnoreCase("day"))
        {
            if (DayVote.getInstance().getVoteType() == DayVoteType.NONE)
            {
                if (vote != null)
                {
                    countYesVote(vote, (Player) sender);
                    return true;
                } else {
                    vote = DayVote.getInstance().startNewDayVote();
                    if (vote == null) // cooldown prevented new vote start
                    {
                        sender.sendMessage("§4Cooldown time left: §b" + DayVote.getInstance().formatTime(DayVote.getInstance().getCooldownTimeLeft()));
                        return true;
                    }
                    countYesVote(vote, (Player) sender);
                    return true;
                }
            } else {
                sender.sendMessage("§4A vote is currently active!");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("rain") || args[0].equalsIgnoreCase("rainstorm"))
        {
            if (DayVote.getInstance().getVoteType() == DayVoteType.NONE)
            {
                if (DayVote.getInstance().canVoteRain())
                {
                    if (vote != null)
                    {
                        countYesVote(vote, (Player) sender);
                        return true;
                    } else {
                        vote = DayVote.getInstance().startNewRainVote();
                        if (vote == null)
                        { // cooldown prevented new vote start
                            sender.sendMessage("§4Cooldown time left: §b" + DayVote.getInstance().formatTime(DayVote.getInstance().getRainCooldownTimeLeft()));
                            return true;
                        }
                        countYesVote(vote, (Player) sender);
                        return true;
                    }
                } else {
                    sender.sendMessage("§4Voting for rain is currently disabled!");
                    return true;
                }
            } else {
                sender.sendMessage("§4A vote is currently active!");
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("yes") || args[0].equalsIgnoreCase("y"))
        {
            if (vote != null) countYesVote(vote, (Player) sender);
            else sender.sendMessage("§4No vote is active! Use §b/vote day §4or §b/vote rain §4to start a vote.");
            return true;
        }

        if (args[0].equalsIgnoreCase("no") || args[0].equalsIgnoreCase("n"))
        {
            if (vote != null) countNoVote(vote, (Player) sender);
            else sender.sendMessage("§4No vote is active! Use §b/vote day §4or §b/vote rain §4to start a vote.");
            return true;
        } else {
            sender.sendMessage("§aDayVote §7version §b" + DayVote.getInstance().getDescription().getVersion());
            sender.sendMessage("§eCommands:");
            sender.sendMessage("§d/vote day §8- §7Starts a vote for day.");
            sender.sendMessage("§d/vote rain §8- §7Starts a vote for rain.");
            sender.sendMessage("§d/vote help §8- §7Reveals this help page.");
            sender.sendMessage("§d/vote info §8- §7Displays plugin information.");

            if (sender.isOp() || sender.hasPermission("DayVote.StaffHelp"))
            {
                sender.sendMessage("§d/vote reload §8- §7Reloads the config.yml.");
                sender.sendMessage("§d/vote reset §8- §7Resets an active vote.");
                sender.sendMessage("§d/vote togglerainvote §8- §7Toggles rain vote.");
            }

            sender.sendMessage("§d/vote <yes:no> §8- §7Casts a vote for day or night.");

            return true;
        }
    }

    private void countYesVote(Vote vote, Player player)
    {
        if (vote.hasVoted(player))
        {
            player.sendMessage("§4You can only vote once!");
            return;
        }
        vote.incrementYes(player);
        player.sendMessage("§fYour vote has been counted!");
    }

    private void countNoVote(Vote vote, Player player)
    {
        if (vote.hasVoted(player))
        {
            player.sendMessage("§4You can only vote once!");
            return;
        }
        vote.incrementNo(player);
        player.sendMessage("§fYour vote has been counted!");
    }
}
