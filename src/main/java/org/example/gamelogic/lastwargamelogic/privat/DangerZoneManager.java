package org.example.gamelogic.lastwargamelogic.privat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DangerZoneManager {
    private final JavaPlugin plugin;
    private final List<DangerZone> zones = new ArrayList<>();

    public DangerZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Adds a danger zone to the list
    public void addZone(DangerZone zone) {
        zones.add(zone);
    }

    // Checks if the game is running based on scoreboard values
    private boolean isGameRunning() {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var generalVars = scoreboard.getObjective("GENERAL_VARIABLES");

        if (generalVars == null) return false;

        int isGameStarted = generalVars.getScore("isGameStarted").getScore();
        int map = generalVars.getScore("map").getScore();

        return isGameStarted == 1;
    }


    // Starts the repeating task to check for players in danger zones
    public void startMonitoring() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (DangerZone zone : zones) {
                        if (zone.isInZone(player)) {
                            zone.applyEffect(player);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Runs every 10 ticks = 0.5 seconds
    }
}
