package org.example.gamelogic.lastwargamelogic.privat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlowGrassBreakListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> grassBreakCount = new HashMap<>();
    private final Map<UUID, BukkitRunnable> countdownTasks = new HashMap<>();
    private final int requiredBreaks = 3;
    private final int activeMapId;

    public SlowGrassBreakListener(JavaPlugin plugin, int activeMapId) {
        this.plugin = plugin;
        this.activeMapId = activeMapId;
    }

    @EventHandler
    public void onGrassBreak(BlockBreakEvent event) {
        if (activeMapId != 0) return; // only apply on map 0

        Player player = event.getPlayer();
        Material type = event.getBlock().getType();

        // Check both GRASS_BLOCK and DIRT
        if (type != Material.GRASS_BLOCK && type != Material.DIRT) return;

        UUID uuid = player.getUniqueId();
        int count = grassBreakCount.getOrDefault(uuid, 0);

        if (count < requiredBreaks) {
            event.setCancelled(true); // cancel the break
            grassBreakCount.put(uuid, count + 1);

            // reset timer
            if (countdownTasks.containsKey(uuid)) {
                countdownTasks.get(uuid).cancel();
            }

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    grassBreakCount.remove(uuid);
                    countdownTasks.remove(uuid);
                }
            };
            task.runTaskLater(plugin, 40L); // 2 seconds
            countdownTasks.put(uuid, task);

            player.sendActionBar(
                    Component.text("Breaking grass/dirt is slowed! Progress: " + (count + 1) + "/" + requiredBreaks)
                            .color(NamedTextColor.DARK_GREEN)
            );

        } else {
            // Allow break but reset counter and cancel cooldown
            grassBreakCount.remove(uuid);

            if (countdownTasks.containsKey(uuid)) {
                countdownTasks.get(uuid).cancel();
                countdownTasks.remove(uuid);
            }
        }
    }

}
