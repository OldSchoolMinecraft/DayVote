package net.oldschoolminecraft.dv;

import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.weather.WeatherListener;

import java.io.File;

public class WeatherHandler extends WeatherListener
{
    public void onWeatherChange(WeatherChangeEvent event)
    {
        if (event.toWeatherState() && !DayVote.getInstance().shouldWeatherBeOn)
        {
            System.out.println("[DayVote] Weather change event was cancelled as it was not voted on.");
            event.setCancelled(true);
            return;
        }

        // we just changed it, no further changes should be accepted until the next vote
        if (event.toWeatherState())
            DayVote.getInstance().shouldWeatherBeOn = false;
    }
}
