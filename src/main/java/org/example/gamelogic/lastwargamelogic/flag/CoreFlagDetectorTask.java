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
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.example.gamelogic.lastwargamelogic.LastWarGameLogic;

public class CoreFlagDetectorTask extends BukkitRunnable {

    private final JavaPlugin plugin;

    public CoreFlagDetectorTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (World world : LastWarGameLogic.getActiveGameWorlds()) {
            runForWorld(world); // вызываем уже существующий метод
        }
    }
    private void runForWorld(World world) {
        NamespacedKey flagKey = new NamespacedKey(plugin, "flag");
        NamespacedKey coreKey = new NamespacedKey(plugin, "core");

        for (Entity entity : world.getEntities()) {
            if (isCore(entity)) {
                rotateCore((ArmorStand) entity);
            }

            if (!(entity instanceof ArmorStand flag)) continue;
            if (!flag.getPersistentDataContainer().has(flagKey, PersistentDataType.INTEGER)) continue;

            checkFlagProximity(flag, world);
        }
    }


    private void checkFlagProximity(ArmorStand flag, World world) {
        NamespacedKey coreKey = new NamespacedKey(plugin, "core");

        for (Entity nearby : world.getNearbyEntities(flag.getLocation(), 1.5, 1.5, 1.5)) {
            if (!(nearby instanceof ArmorStand core)) continue;
            if (!core.getPersistentDataContainer().has(coreKey, PersistentDataType.STRING)) continue;

            handleGoal(core, flag);
            break;
        }
    }
    public void handleExternalGoal(ArmorStand core, ArmorStand flag) {
        handleGoal(core, flag); // приватный метод ты уже сделал
    }

    private void handleGoal(ArmorStand core, ArmorStand flag) {
        NamespacedKey coreRedKey = new NamespacedKey(plugin, "core_red");
        NamespacedKey coreBlueKey = new NamespacedKey(plugin, "core_blue");

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        World world = core.getWorld();
        Objective objective = scoreboard.getObjective(world.getName());

        if (objective == null) {
            Bukkit.getLogger().warning("[LastWar] Objective for world " + world.getName() + " not found.");
            return;
        }

        if (core.getPersistentDataContainer().has(coreRedKey, PersistentDataType.INTEGER)) {
            Bukkit.broadcastMessage("§bBlue scored a goal!");
            playEffects(core.getLocation(), Color.BLUE);

            // ✅ Увеличиваем счёт BLUE
            int prev = objective.getScore("BLUE").getScore();
            objective.getScore("BLUE").setScore(prev + 1);

        } else if (core.getPersistentDataContainer().has(coreBlueKey, PersistentDataType.INTEGER)) {
            Bukkit.broadcastMessage("§cRed scored a goal!");
            playEffects(core.getLocation(), Color.RED);

            // ✅ Увеличиваем счёт RED
            int prev = objective.getScore("RED").getScore();
            objective.getScore("RED").setScore(prev + 1);
        }

        // ❌ Удаляем флаг
        flag.remove();

        // ✅ Обновляем флаг isGoalScored = 1
        objective.getScore("isGoalScored").setScore(1);
    }



    private void rotateCore(ArmorStand core) {
        Location loc = core.getLocation();
        loc.setYaw(loc.getYaw() + 5); // вращаем на 5°
        core.teleport(loc);
    }
    private boolean isCore(Entity entity) {
        if (!(entity instanceof ArmorStand)) return false;

        NamespacedKey coreKey = new NamespacedKey(plugin, "core");
        return entity.getPersistentDataContainer().has(coreKey, PersistentDataType.STRING);
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
