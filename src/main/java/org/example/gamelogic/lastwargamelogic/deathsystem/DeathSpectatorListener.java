package org.example.gamelogic.lastwargamelogic.deathsystem;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class DeathSpectatorListener implements Listener {

    private final JavaPlugin plugin;

    public DeathSpectatorListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getEntryTeam(player.getName());
        if (team == null) return;

        String teamName = team.getName().toUpperCase();
        String finalTeamName = teamName;

        if (!teamName.equals("RED") && !teamName.equals("BLUE")) return;

        // Remove player from team to make them invisible to enemies
        team.removeEntry(player.getName());

        // Get death count
        Objective deathObj = scoreboard.getObjective("DeathCount");
        if (deathObj == null) return;
        int deaths = deathObj.getScore(player.getName()).getScore();

        // Get time from GENERAL_VARIABLES.Timer
        Objective general = scoreboard.getObjective("GENERAL_VARIABLES");
        if (general == null) return;
        int time = general.getScore("Timer").getScore();

        // Respawn time formula: 10 + ((d-1)*5) + (t/90)
        int respawnSeconds = 10 + (Math.max(0, deaths - 1) * 5) + (time / 90);

        // Spectator mode immediately
        new BukkitRunnable() {
            @Override
            public void run() {
                player.setGameMode(GameMode.SPECTATOR);
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!isSameTeam(player, other, finalTeamName)) {
                        player.hidePlayer(plugin, other);
                    }
                }
            }
        }.runTaskLater(plugin, 1L);


        // Respawn timer
        new BukkitRunnable() {
            int remaining = respawnSeconds;

            @Override
            public void run() {
                if (remaining == 0) {
                    this.cancel();
                    return;
                }

                player.sendActionBar(Component.text("Respawning in " + remaining + "s...")
                        .color(NamedTextColor.RED));
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // Respawn logic
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                player.setGameMode(GameMode.SURVIVAL);

                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!isSameTeam(player, other, finalTeamName)) {
                        player.showPlayer(plugin, other);
                    }
                }

                Location respawnLocation;
                World world = Bukkit.getWorld("lastwarGame1");
                if (teamName.equals("RED")) {
                    respawnLocation = new Location(world, -140.5, 35, 473.5, 180, 0);
                } else {
                    respawnLocation = new Location(world, -140.5, 35, 115.5, 0, 0);
                }

                player.teleport(respawnLocation);

                // Add back to team
                Team t = scoreboard.getTeam(teamName);
                if (t != null) t.addEntry(player.getName());

                player.sendActionBar(Component.text("You have respawned!")
                        .color(NamedTextColor.GREEN));
            }
        }.runTaskLater(plugin, respawnSeconds * 20L);
    }
    public static boolean isSameTeam(Player a, Player b, String teamName) {
        Team tb = b.getScoreboard().getEntryTeam(b.getName());
        return tb != null && tb.getName().equalsIgnoreCase(teamName);
    }


}
