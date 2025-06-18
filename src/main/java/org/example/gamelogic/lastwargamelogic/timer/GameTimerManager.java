package org.example.gamelogic.lastwargamelogic.timer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class GameTimerManager {

    private final JavaPlugin plugin;

    public GameTimerManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective general = scoreboard.getObjective("GENERAL_VARIABLES");

                if (general == null) return;

                int isFrozen = general.getScore("isFrozen").getScore();
                int isGameStarted = general.getScore("isGameStarted").getScore();

                if (isFrozen == 0 && isGameStarted == 1) {
                    int currentTime = general.getScore("Timer").getScore();
                    general.getScore("Timer").setScore(currentTime + 1);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // 20 ticks = 1 second
    }
}
