// FlagSpawner.java
package org.example.gamelogic.lastwargamelogic.flag;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;

public class FlagSpawner {

    private final JavaPlugin plugin;

    public FlagSpawner(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ArmorStand spawnAndReturn(Location location) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        stand.setInvisible(true);
        stand.setInvulnerable(false);
        stand.setGravity(true);
        stand.setSmall(false);
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setCustomNameVisible(false);
        stand.setCanPickupItems(false);
        stand.setMarker(false);
        stand.setRightArmPose(new EulerAngle(0, 0, 0));
        stand.setLeftArmPose(new EulerAngle(0, 0, 0));

        ItemStack dye = new ItemStack(Material.RED_DYE);
        ItemMeta meta = dye.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("FlagInFly"));
            dye.setItemMeta(meta);
        }

        stand.getEquipment().setItem(EquipmentSlot.HAND, dye);
        stand.addEquipmentLock(EquipmentSlot.HAND, ArmorStand.LockType.REMOVING_OR_CHANGING);

        stand.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "flag"),
                PersistentDataType.INTEGER,
                1
        );

        return stand;
    }
}
