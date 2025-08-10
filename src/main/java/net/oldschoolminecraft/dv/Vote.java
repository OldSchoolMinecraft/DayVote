package net.oldschoolminecraft.dv;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Vote
{
    private int yes, no;
    private ArrayList<String> voters;

    public Vote()
    {
        voters = new ArrayList<>();
        yes = 0;
        no = 0;
    }

    public void incrementYes(Player player)
    {
        yes++;
        voters.add(player.getName());
    }

    public void incrementNo(Player player)
    {
        no++;
        voters.add(player.getName());
    }

    public boolean hasVoted(Player player)
    {
        return voters.contains(player.getName());
    }

    public boolean didVotePass()
    {
        if (no > yes) return false;
        double percentage = calculatePercentage(yes, voters.size());
        int required = (int) DayVote.getInstance().getConfig().getConfigOption("yesVotePercentageRequired");
        System.out.println("Vote finished with percentage: " + percentage + " (" + required + " required)");
        DayVote.getInstance().sendDebugMessage("Vote finished with percentage: " + percentage + " (" + required + " required)");
        return percentage >= required;
    }

    public boolean didRainVotePass()
    {
        if (no > yes) return false;
        double percentage = calculatePercentage(yes, voters.size());
        int required = (int) DayVote.getInstance().getConfig().getConfigOption("yesRainVotePercentageRequired");
        System.out.println("Vote finished with percentage: " + percentage + " (" + required + " required)");
        DayVote.getInstance().sendDebugMessage("Vote finished with percentage: " + percentage + " (" + required + " required)");
        return percentage >= required;
    }

    public Integer getYesVotes()
    {
        return (int) calculatePercentage(yes, voters.size());
    }

    public Integer getNoVotes()
    {
        return (int) calculatePercentage(no, voters.size());
    }

    private double calculatePercentage(double obtained, double total)
    {
        if (total <= 0) return 0; // Prevent division by zero
        return obtained * 100 / total;
    }
}
