package org.example.gamelogic.lastwargamelogic.privat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.gamelogic.lastwargamelogic.LastWarGameLogic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SlowGrassBreakListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> grassBreakCount = new HashMap<>();
    private final Map<UUID, BukkitRunnable> countdownTasks = new HashMap<>();
    private final int requiredBreaks = 3;

    public SlowGrassBreakListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGrassBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // ❗ Прерываем, если игрок не в активном игровом мире
        if (!LastWarGameLogic.getActiveGameWorlds().contains(player.getWorld())) return;

        Material type = event.getBlock().getType();
        if (type != Material.GRASS_BLOCK && type != Material.DIRT) return;

        UUID uuid = player.getUniqueId();
        int count = grassBreakCount.getOrDefault(uuid, 0);

        if (count < requiredBreaks) {
            event.setCancelled(true);
            grassBreakCount.put(uuid, count + 1);

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
            task.runTaskLater(plugin, 40L);
            countdownTasks.put(uuid, task);

            player.sendActionBar(
                    Component.text("Breaking grass/dirt is slowed! Progress: " + (count + 1) + "/" + requiredBreaks)
                            .color(NamedTextColor.DARK_GREEN)
            );

        } else {
            grassBreakCount.remove(uuid);

            if (countdownTasks.containsKey(uuid)) {
                countdownTasks.get(uuid).cancel();
                countdownTasks.remove(uuid);
            }
        }
    }
}
