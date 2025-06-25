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
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathSpectatorListener implements Listener {

    private final JavaPlugin plugin;

    private static final Map<UUID, BukkitTask> pendingRespawns = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitTask> countdownTasks = new ConcurrentHashMap<>();

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

        // Get time
        Objective general = scoreboard.getObjective(player.getWorld().getName());
        if (general == null) return;
        int time = general.getScore("Timer").getScore();

        // Respawn time formula
        int respawnSeconds = 10 + (Math.max(0, deaths - 1) * 5) + (time / 50);

        // Spectator mode
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
        }.runTaskLater(plugin, 20L);

        // Countdown display
        BukkitTask countdown = new BukkitRunnable() {
            int remaining = respawnSeconds;

            @Override
            public void run() {
                if (remaining <= 0) {
                    this.cancel();
                    return;
                }
                player.sendActionBar(Component.text("Respawning in " + remaining + "s...").color(NamedTextColor.RED));
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        countdownTasks.put(player.getUniqueId(), countdown);

        // Respawn logic
        BukkitTask respawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                pendingRespawns.remove(player.getUniqueId());
                countdownTasks.remove(player.getUniqueId());

                if (!player.isOnline()) return;

                player.setGameMode(GameMode.SURVIVAL);
                for (Player other : Bukkit.getOnlinePlayers()) {
                    if (!isSameTeam(player, other, finalTeamName)) {
                        player.showPlayer(plugin, other);
                    }
                }

                Location respawnLocation = teamName.equals("RED")
                        ? new Location(player.getWorld(), -140.5, 35, 473.5, 180, 0)
                        : new Location(player.getWorld(), -140.5, 35, 115.5, 0, 0);

                player.teleport(respawnLocation);
                Team t = scoreboard.getTeam(teamName);
                if (t != null) t.addEntry(player.getName());

                player.sendActionBar(Component.text("You have respawned!").color(NamedTextColor.GREEN));
            }
        }.runTaskLater(plugin, respawnSeconds * 20L);

        pendingRespawns.put(player.getUniqueId(), respawnTask);
    }

    public static boolean isSameTeam(Player a, Player b, String teamName) {
        Team tb = b.getScoreboard().getEntryTeam(b.getName());
        return tb != null && tb.getName().equalsIgnoreCase(teamName);
    }

    public static void respawnAllDeadPlayers(String worldName, JavaPlugin plugin) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals(worldName)) continue;
            if (player.getGameMode() != GameMode.SPECTATOR) continue;

            // Сразу отменяем отложенное возрождение
            BukkitTask pending = pendingRespawns.remove(player.getUniqueId());
            if (pending != null) pending.cancel();

            // Останавливаем счётчик
            BukkitTask countdown = countdownTasks.remove(player.getUniqueId());
            if (countdown != null) countdown.cancel();

            player.setGameMode(GameMode.SURVIVAL);

            String teamName = getTeamName(scoreboard, player);
            if (teamName == null) continue;

            Location respawn = teamName.equals("RED")
                    ? new Location(world, -140.5, 35, 473.5, 180, 0)
                    : new Location(world, -140.5, 35, 115.5, 0, 0);

            player.teleport(respawn);

            // Добавляем обратно в команду, если вдруг пропал
            Team team = scoreboard.getTeam(teamName);
            if (team != null && !team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }

            for (Player other : Bukkit.getOnlinePlayers()) {
                player.showPlayer(plugin, other);
            }

            player.sendActionBar(Component.text("You have been force-respawned!").color(NamedTextColor.GREEN));
        }
    }

    private static String getTeamName(Scoreboard scoreboard, Player player) {
        Team team = scoreboard.getEntryTeam(player.getName());
        if (team != null) return team.getName().toUpperCase();
        return null;
    }

}
