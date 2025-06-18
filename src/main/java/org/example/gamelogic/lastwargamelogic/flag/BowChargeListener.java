package org.example.gamelogic.lastwargamelogic.flag;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BowChargeListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> lastLevel = new HashMap<>();
    private final Map<UUID, BukkitTask> activeCharging = new HashMap<>();

    public BowChargeListener(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBowUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() == Material.BOW && event.getAction().toString().contains("RIGHT_CLICK")) {
            UUID uuid = player.getUniqueId();

            if (!activeCharging.containsKey(uuid)) {
                startCharging(player);
            }
        }
    }

    private void startCharging(Player player) {
        UUID uuid = player.getUniqueId();
        lastLevel.put(uuid, player.getLevel());

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    stopCharging(uuid);
                    return;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                if (item == null || item.getType() != Material.BOW || !player.isHandRaised()) {
                    stopCharging(uuid);
                    return;
                }


                player.giveExp(1);


                int currentLevel = player.getLevel();
                int previousLevel = lastLevel.getOrDefault(uuid, currentLevel);

                if (currentLevel > previousLevel) {
                    player.setLevel(currentLevel - 1);
                    lastLevel.put(uuid, currentLevel - 1);
                } else {
                    lastLevel.put(uuid, currentLevel);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);

        activeCharging.put(uuid, task);
    }

    private void stopCharging(UUID uuid) {
        BukkitTask task = activeCharging.remove(uuid);
        if (task != null) {
            task.cancel();
        }

        // Reset XP bar when charging ends
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.setExp(0.0f); // Clears charge bar but keeps level
        }
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopCharging(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        stopCharging(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        stopCharging(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent event) {
        stopCharging(event.getPlayer().getUniqueId());
    }
}
