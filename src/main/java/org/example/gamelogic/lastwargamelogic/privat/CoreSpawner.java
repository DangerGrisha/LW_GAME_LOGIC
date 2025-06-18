package org.example.gamelogic.lastwargamelogic.privat;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.NamespacedKey;

public class CoreSpawner {

    private final JavaPlugin plugin;

    public CoreSpawner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void spawnCoresIfNeeded() {
        int mapId = getMapId();
        if (mapId != 0) return;

        World world = Bukkit.getWorld("lastwarGame1");
        if (world == null) return;

        clearAllArmorStands(world);

        spawnCore(world, new Location(world, -167.5, 37.5, 121.5), DyeColor.BLUE, "ICE CORE", "blue");
        spawnCore(world, new Location(world, -167.5, 37.5, 467.5), DyeColor.RED, "HELL CORE", "red");
    }


    private int getMapId() {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var objective = scoreboard.getObjective("GENERAL_VARIABLES");
        if (objective == null) return -1;

        return objective.getScore("map").getScore();
    }

    private void spawnCore(World world, Location location, DyeColor color, String displayName, String sideTag) {
        NamespacedKey genericCoreKey = new NamespacedKey(plugin, "core");
        NamespacedKey specificCoreKey = new NamespacedKey(plugin, "core_" + sideTag.toLowerCase());



        // Spawn the core armor stand
        ArmorStand stand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setSmall(false);
        stand.setArms(false);
        stand.setBasePlate(false);
        stand.setCustomNameVisible(false);
        stand.setCanPickupItems(false);
        stand.setMarker(false);
        stand.setRightArmPose(new EulerAngle(0, 0, 0));
        stand.setLeftArmPose(new EulerAngle(0, 0, 0));

        Material dyeMaterial = (color == DyeColor.RED) ? Material.RED_DYE : Material.BLUE_DYE;

        ItemStack dye = new ItemStack(dyeMaterial);
        ItemMeta meta = dye.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(displayName));
            dye.setItemMeta(meta);
        }

        stand.getEquipment().setItem(EquipmentSlot.HAND, dye);
        stand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

        // Tags for detection
        stand.getPersistentDataContainer().set(genericCoreKey, PersistentDataType.STRING, displayName);
        stand.getPersistentDataContainer().set(specificCoreKey, PersistentDataType.INTEGER, 1);
    }
    public void spawnCoresForce() {
        World world = Bukkit.getWorld("lastwarGame1");
        if (world == null) return;

        clearAllArmorStands(world);

        spawnCore(world, new Location(world, -167.5, 37.5, 121.5), DyeColor.BLUE, "ICE CORE", "blue");
        spawnCore(world, new Location(world, -167.5, 37.5, 467.5), DyeColor.RED, "HELL CORE", "red");
    }


    private void clearAllArmorStands(World world) {
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand) {
                entity.remove();
            }
        }
    }


}
