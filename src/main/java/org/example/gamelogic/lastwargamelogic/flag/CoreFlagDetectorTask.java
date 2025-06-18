package org.example.gamelogic.lastwargamelogic.flag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class CoreFlagDetectorTask extends BukkitRunnable {

    private final JavaPlugin plugin;

    public CoreFlagDetectorTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        World world = Bukkit.getWorld("lastwarGame1");
        if (world == null) return;

        NamespacedKey flagKey = new NamespacedKey(plugin, "flag");
        NamespacedKey coreKey = new NamespacedKey(plugin, "core");
        NamespacedKey coreRedKey = new NamespacedKey(plugin, "core_red");
        NamespacedKey coreBlueKey = new NamespacedKey(plugin, "core_blue");

        for (Entity entity : world.getEntities()) {
            // ROTATE cores
            if (entity instanceof ArmorStand core &&
                    core.getPersistentDataContainer().has(coreKey, PersistentDataType.STRING)) {

                Location loc = core.getLocation();
                loc.setYaw(loc.getYaw() + 5); // rotate 5 degrees every tick
                core.teleport(loc);
            }

            // Check if it's a flag
            if (!(entity instanceof ArmorStand stand)) continue;
            var container = stand.getPersistentDataContainer();

            if (!container.has(flagKey, PersistentDataType.INTEGER)) continue;

            // Check if flag is close to a core
            for (Entity nearby : world.getNearbyEntities(stand.getLocation(), 1.5, 1.5, 1.5)) {
                if (!(nearby instanceof ArmorStand core)) continue;
                var coreData = core.getPersistentDataContainer();

                if (!coreData.has(coreKey, PersistentDataType.STRING)) continue;

                if (coreData.has(coreRedKey, PersistentDataType.INTEGER)) {
                    Bukkit.broadcastMessage("§b Blue scored a goal!");
                    playEffects(core.getLocation(), Color.BLUE);
                } else if (coreData.has(coreBlueKey, PersistentDataType.INTEGER)) {
                    Bukkit.broadcastMessage("§c Red scored a goal!");
                    playEffects(core.getLocation(), Color.RED);
                }

                stand.remove(); // remove the flag
                break;
            }
        }
    }



    private void playEffects(Location center, Color color) {
        World world = center.getWorld();
        if (world == null) return;

        // Play dragon death sound for all players
        world.playSound(center, Sound.ENTITY_ENDER_DRAGON_DEATH, 10f, 1f);

        // Launch fireworks in a small radius around the core
        for (int i = 0; i < 8; i++) {
            Location fireworkLoc = center.clone().add(Math.cos(i * Math.PI / 4) * 1.2, 0.1, Math.sin(i * Math.PI / 4) * 1.2);
            Firework fw = world.spawn(fireworkLoc, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                    .withColor(color)
                    .withFade(Color.WHITE)
                    .with(FireworkEffect.Type.BURST)
                    .trail(true)
                    .flicker(true)
                    .build());
            meta.setPower(0); // So they explode almost instantly
            fw.setFireworkMeta(meta);
            fw.setVelocity(new Vector(0, 0.1, 0)); // Slight lift
            fw.detonate(); // Instant explosion
        }
    }
}
