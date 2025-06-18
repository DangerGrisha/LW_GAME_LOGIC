// FlagBowShootListener.java
package org.example.gamelogic.lastwargamelogic.flag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class FlagBowShootListener implements Listener {

    private final JavaPlugin plugin;
    private final FlagSpawner flagSpawner;
    private static final double FORCE_MULTIPLIER = 1.6;

    private final Map<UUID, Long> pickupBlockMap;


    public FlagBowShootListener(JavaPlugin plugin, FlagSpawner flagSpawner, Map<UUID,Long> pickupBlockMap) {
        this.plugin = plugin;
        this.flagSpawner = flagSpawner;
        this.pickupBlockMap = pickupBlockMap;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onFlagBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack bow = event.getBow();
        if (!isFlagBow(bow)) return;

        // Cancel the default arrow shooting behavior
        event.setCancelled(true);

        // Play shooting sound manually
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);

        // Get charge level from XP bar
        double power = player.getExp();
        player.setExp(0);

        if (power < 0.1) return;

        // Stronger forward offset to avoid hitting the player at steep angles
        Location start = player.getEyeLocation().add(
                player.getLocation().getDirection().normalize().multiply(1.0)//far from face spawn arrow
        );

        Arrow arrow = player.getWorld().spawnArrow(
                start,
                player.getLocation().getDirection(),
                (float)(power * FORCE_MULTIPLIER),
                0f
        );
        arrow.setShooter(player);
        arrow.setPickupStatus(Arrow.PickupStatus.DISALLOWED);

        // Remove one arrow from inventory
        player.getInventory().removeItem(new ItemStack(Material.ARROW, 1));

        // Remove the flag bow from player's hand
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand != null && isFlagBow(itemInHand)) {
            player.getInventory().setItemInMainHand(null);
            giveBannerSlotItem(player,4);
        }

        // Spawn and track armor stand
        var stand = flagSpawner.spawnAndReturn(player.getLocation().add(0, 0.5, 0));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || arrow.isDead()) {
                    this.cancel();
                    return;
                }
                // Check if the arrow is on the ground (landed)
                if (arrow.isOnGround() || stand.isDead()) {
                    arrow.remove(); // Clean up the arrow
                    this.cancel();
                    return;
                }

                // Follow slightly behind the arrow to avoid collisions
                Vector direction = arrow.getVelocity().normalize();
                Location flagLocation = arrow.getLocation().subtract(direction.multiply(0.7)).add(0, 0.2, 0);
                stand.teleport(flagLocation);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Prevent pickup for 2s
        UUID playerId = player.getUniqueId();
        long unblockTime = System.currentTimeMillis() + 3000;
        pickupBlockMap.put(playerId, unblockTime);

        // Countdown display
        new BukkitRunnable() {
            int secondsLeft = 2;
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

    }



    private boolean isFlagBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() &&
                Component.text("Flag").equals(meta.displayName());
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
