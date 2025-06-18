package org.example.gamelogic.lastwargamelogic.flag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlagDropListener implements Listener {

    private final JavaPlugin plugin;
    private final FlagSpawner flagSpawner;
    private final Map<UUID, Long> pickupBlockMap;
    private final Map<UUID, Double> flagHealthThresholds = new HashMap<>();


    public FlagDropListener(JavaPlugin plugin, FlagSpawner flagSpawner, Map<UUID, Long> pickupBlockMap) {
        this.plugin = plugin;
        this.flagSpawner = flagSpawner;
        this.pickupBlockMap = pickupBlockMap;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        UUID id = player.getUniqueId();

        if (!flagHealthThresholds.containsKey(id)) return;

        double current = player.getHealth();
        double newHealth = current - event.getFinalDamage();
        double oldThreshold = flagHealthThresholds.get(id);


        if (current > oldThreshold + 10.0) {
            flagHealthThresholds.put(id, current - 10.0);
            return; // пока он в норме, не дропаем
        }


        if (newHealth <= oldThreshold) {
            dropFlag(player);
            flagHealthThresholds.remove(id);
        }
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        dropFlag(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        dropFlag(event.getPlayer());
    }

    private void dropFlag(Player player) {
        // Check if player has the flag bow
        for (ItemStack item : player.getInventory().getContents()) {
            if (isFlagBow(item)) {
                // Remove banner helmet if any
                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && helmet.getType().toString().endsWith("_BANNER")) {
                    player.getInventory().setHelmet(null);
                }

                // Remove item
                player.getInventory().remove(item);
                giveBannerSlotItem(player,4);

                // Drop flag in the world
                Location dropLocation = player.getLocation().clone().add(0, 0.1, 0);
                flagSpawner.spawnAndReturn(dropLocation);

                // Sound
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);

                // Prevent pickup for 3s
                UUID playerId = player.getUniqueId();
                long unblockTime = System.currentTimeMillis() + 3000;
                pickupBlockMap.put(playerId, unblockTime);

                // Countdown display
                new BukkitRunnable() {
                    int secondsLeft = 3;
                    @Override
                    public void run() {
                        if (!player.isOnline()) {
                            cancel();
                            return;
                        }
                        if (secondsLeft <= 0) {
                            player.sendActionBar(Component.text("§a✔ You can now pick up the flag."));
                            pickupBlockMap.remove(playerId);
                            cancel();
                            return;
                        }
                        player.sendActionBar(Component.text("§ePicking flag allowed in: §c" + secondsLeft + "s"));
                        secondsLeft--;
                    }
                }.runTaskTimer(plugin, 0L, 20L);


                break;
            }

        }

        // Remove 1 arrow from slot 8 if it's exactly 1 arrow
        ItemStack item = player.getInventory().getItem(17);
        if (item != null && item.getType() == Material.ARROW && item.getAmount() == 1) {
            player.getInventory().setItem(17, null);
        }
    }

    private boolean isFlagBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() &&
                Component.text("Flag").equals(meta.displayName());
    }
    public void setFlagThreshold(Player player) {
        double currentHealth = player.getHealth();
        flagHealthThresholds.put(player.getUniqueId(), currentHealth - 10.0);
    }
    public void giveBannerSlotItem(Player player, int slot) {
        // Create RED_DYE with display name "Banner Slot"
        ItemStack dye = new ItemStack(Material.RED_DYE);
        ItemMeta meta = dye.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Banner Slot"));
            dye.setItemMeta(meta);
        }

        // Set item in specified inventory slot
        player.getInventory().setItem(slot, dye);
    }
}
