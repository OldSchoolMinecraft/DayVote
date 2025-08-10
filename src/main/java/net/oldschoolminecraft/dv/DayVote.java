package net.oldschoolminecraft.dv;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DayVote extends JavaPlugin implements Listener {

    private static DayVote instance;

    private VoteConfig config;
    private Vote vote;
    private long lastVote;
    private long lastStartVote;
    private long lastRainVote;
    private long lastRainStartVote;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public DayVoteType voteType;
    public boolean shouldWeatherBeOn = false;

    @Override
    public void onEnable()
    {
        instance = this;
        config = new VoteConfig(new File(getDataFolder(), "config.yml"));
        lastVote = Math.max(0, UnixTime.now() - (int)config.getConfigOption("cooldownSeconds"));
        lastRainVote = Math.max(0, UnixTime.now() - (int)config.getConfigOption("rainCooldownSeconds"));
        voteType = DayVoteType.NONE;
        getCommand("vote").setExecutor(new VoteCommand());
        getCommand("dvadmin").setExecutor(new DVAdminCommand());

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvent(Event.Type.WEATHER_CHANGE, new WeatherHandler(), Event.Priority.Lowest, this);

        System.out.println("DayVote version: " + getDescription().getVersion() + " enabled!");
        System.out.println("Debug mode: " + (isDebugModeEnabled() ? "ON" : "OFF"));
        System.out.println("Last Vote Time: " + lastVote);
        System.out.println("Last Rain Vote Time: " + lastRainVote);
        System.out.println("Current Unix Time: " + UnixTime.now());
        System.out.println("Vote Day Cooldown Setting: " + config.getConfigOption("cooldownSeconds"));
        System.out.println("Vote Rain Cooldown Setting: " + config.getConfigOption("rainCooldownSeconds"));
        System.out.println("Can start Day vote? " + (canStartVote() ? "Yes" : "No"));
        System.out.println("Can start Rain vote? " + (canStartRainVote() ? "Yes" : "No"));
    }

    @Override
    public void onDisable()
    {
        forceCancelVote();
        System.out.println("DayVote version: " + getDescription().getVersion() + " disabled!");
    }

    public boolean isDebugModeEnabled()
    {
        if (config.getConfigOption("debugMode").equals(true))
            return true;
        else return false;
    }

    public void setDebugMode(boolean option)
    {
        try
        {
            config.setProperty("debugMode", option);
            config.save();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void sendDebugMessage(String message) {
        if (DayVote.getInstance().isDebugModeEnabled()) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                if (all.isOp() || all.hasPermission("DayVote.Notify")) all.sendMessage("§e[Debug] §f" + message);
            }
        }
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        if (DayVote.getInstance().isDebugModeEnabled()) {
            if (event.getPlayer().isOp() || event.getPlayer().hasPermission("DayVote.Notify")) {
                event.getPlayer().sendMessage("§1[§bOSM§1] §7DayVote debug mode is turned §aon§7!");
                event.getPlayer().sendMessage("§1[§bOSM§1] §7Events & all other actions from the plugin will be §avisible §7to §cAdministrators §7or those with §bDayVote.Notify§7!");
                event.getPlayer().sendMessage("§1[§bOSM§1] §7To turn off debug mode, type §d/dvadmin debug§7!");
            }
        }
    }

    public Vote getActiveVote()
    {
        return vote;
    }

    public boolean canVoteRain()
    {
        if (config.getConfigOption("allowRainVote").equals(true))
            return true;
        else return false;
    }

    public void setAllowRainVote(boolean option)
    {
        try
        {
            config.setProperty("allowRainVote", option);
            config.save();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public boolean isThunderingAllowed()
    {
        if (config.getConfigOption("allowThunder").equals(true))
            return true;
        else return false;
    }

    public void setAllowThunder(boolean option)
    {
        try
        {
            config.setProperty("allowThunder", option);
            config.save();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    //  DAY
    public synchronized boolean canStartVote()
    {
        long timeSinceLastVote = (UnixTime.now() - lastVote);
        int cooldown = (int) config.getConfigOption("cooldownSeconds");
        return timeSinceLastVote >= cooldown;
    }

    public int getCooldownTimeLeft()
    {
        long timeSinceLastVote = (UnixTime.now() - lastVote);
        int cooldown = (int) config.getConfigOption("cooldownSeconds");
        return (int) Math.max(0, cooldown-timeSinceLastVote);
    }

    public int getVoteTimeLeft()
    {
        long timeSinceLastVoteStart = (UnixTime.now() - lastStartVote);
        int voteDurationSeconds = (int) config.getConfigOption("voteDurationSeconds");
        return (int) Math.max(0, voteDurationSeconds - timeSinceLastVoteStart);
    }

    //  RAIN
    public synchronized boolean canStartRainVote()
    {
        long timeSinceLastRainVote = (UnixTime.now() - lastRainVote);
        int cooldown = (int) config.getConfigOption("rainCooldownSeconds");
        return timeSinceLastRainVote >= cooldown;
    }

    public int getRainCooldownTimeLeft()
    {
        long timeSinceLastRainVote = (UnixTime.now() - lastRainVote);
        int cooldown = (int) config.getConfigOption("rainCooldownSeconds");
        return (int) Math.max(0, cooldown-timeSinceLastRainVote);
    }

    public int getRainVoteTimeLeft()
    {
        long timeSinceLastRainVoteStart = (UnixTime.now() - lastRainStartVote);
        int voteDurationSeconds = (int) config.getConfigOption("voteDurationSeconds");
        return (int) Math.max(0, voteDurationSeconds-timeSinceLastRainVoteStart);
    }

    public String formatTime(final long seconds)
    {
        final long minute = TimeUnit.SECONDS.toMinutes(seconds);
        final long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60L;
        return minute + "m" + second + "s";
    }

    public String formatTimeFromTicks(final long ticks)
    {
        final long totalSeconds = ticks / 20;

        final long minute = (totalSeconds % 3600) / 60;
        final long second = totalSeconds % 60;
        return minute + "m" + second + "s";
    } //For /dvadmin weatherinfo command. (Covert ticks of the weather duration to the time format seen in /vote)

    public synchronized Vote startNewDayVote()
    {
        if (!canStartVote()) return null;
        vote = new Vote();
        setVoteType(DayVoteType.DAY);
        sendDebugMessage("DayVoteType set to DAY.");
        broadcast(String.valueOf(config.getConfigOption("messages.started")));
        int voteDurationSeconds = (int) config.getConfigOption("voteDurationSeconds");
        scheduler.schedule(this::processDayVote, voteDurationSeconds, TimeUnit.SECONDS);
        lastStartVote = Math.max(0, UnixTime.now());

        sendDebugMessage("A DAY vote was initiated.");
        return vote;
    }

    public synchronized Vote startNewRainVote()
    {
        if (!canStartRainVote()) return null;
        vote = new Vote();
        setVoteType(DayVoteType.RAIN);
        sendDebugMessage("DayVoteType set to RAIN.");
        broadcast(String.valueOf(config.getConfigOption("messages.startedRain")));
        int voteDurationSeconds = (int) config.getConfigOption("voteDurationSeconds");
        scheduler.schedule(this::processRainVote, voteDurationSeconds, TimeUnit.SECONDS);
        lastRainStartVote = Math.max(0, UnixTime.now());


        sendDebugMessage("A RAIN vote was initiated.");
        return vote;
    }

    public synchronized void processDayVote()
    {
        if (vote == null)
        {
            return;
        }

        // Only process if this is a day vote.
        if (voteType == DayVoteType.DAY)
        {
            if (vote.didVotePass())
            {
                broadcast(String.valueOf(config.getConfigOption("messages.succeeded")));
                Bukkit.getServer().getWorld("world").setTime(0);
                sendDebugMessage("Time set to 0.");
            }
            else broadcast(String.valueOf(config.getConfigOption("messages.failed")));
        }
        resetDayVote();
    }

    public synchronized void processRainVote()
    {
        if (vote == null)
        {
            return;
        }

        // Only process if this is a rain vote.
        if (voteType == DayVoteType.RAIN)
        {
            if (vote.didRainVotePass())
            {
                if (Bukkit.getServer().getWorld("world").hasStorm())
                {
                    broadcast(String.valueOf(config.getConfigOption("messages.alreadyRaining")));
                } else {
                    int rainDuration = (int) config.getConfigOption("rainDurationTicks");
                    broadcast(String.valueOf(config.getConfigOption("messages.succeededRain")));
                    shouldWeatherBeOn = true;
                    Bukkit.getServer().getWorld("world").setStorm(true);
                    Bukkit.getServer().getWorld("world").setWeatherDuration(rainDuration);
                    sendDebugMessage("Weather has been changed.");

                    if (config.getConfigOption("allowThunder").equals(true))
                    {
                        Random random = new Random();
                        int result = random.nextInt(100)+1;
                        int chance = (int) config.getConfigOption("chanceForThunder");
                        if (result <= chance) { //Passed with a percentage (in Integer form) set in the config.
                            System.out.println("[DayVote] Thundering passed with a result of " + result + "! (Under " + chance + ")");
                            sendDebugMessage("Thundering passed with a result of " + result + "! (Under " + chance + ")");
                            int thunderDuration = (int) config.getConfigOption("thunderDurationTicks");
                            Bukkit.getServer().getWorld("world").setThundering(true);
                            Bukkit.getServer().getWorld("world").setThunderDuration(thunderDuration);
                        } else { //Failed. No Thunder.
                            System.out.println("[DayVote] Thundering failed with a result of " + result + "! (Over " + chance + ")");
                            sendDebugMessage("Thundering failed with a result of " + result + "! (Over " + chance + ")");
                            Bukkit.getServer().getWorld("world").setThundering(false);
                        }

                    } else {
                        Bukkit.getServer().getWorld("world").setThundering(false);
                        sendDebugMessage("Thunder is waived as allowThundering is set to false.");
                        System.out.println("[DayVote] Thunder is waived as allowThundering is set to false.");
                    }
                }
            } else broadcast(String.valueOf(config.getConfigOption("messages.failedRain")));
        }
        resetRainVote();
    }

    private synchronized void resetDayVote()
    {
        vote = null;
        setVoteType(DayVoteType.NONE);
        sendDebugMessage("DayVoteType set to NONE.");
        lastVote = Math.max(0, UnixTime.now());
    }

    private synchronized void resetRainVote()
    {
        vote = null;
        setVoteType(DayVoteType.NONE);
        sendDebugMessage("DayVoteType set to NONE.");
        lastRainVote = Math.max(0, UnixTime.now());
    }

    private synchronized void forceCancelVote()
    {
        if (vote != null)
        {
            resetDayVote();
            resetRainVote();
        }
    }

    private void broadcast(String msg)
    {
        for (Player all : getServer().getOnlinePlayers())
            all.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public VoteConfig getConfig()
    {
        return config;
    }

    public static DayVote getInstance()
    {
        return instance;
    }

    public void setVoteType(DayVoteType voteType)
    {
        this.voteType = voteType;
    }

    public DayVoteType getVoteType()
    {
        return voteType;
    }
}
