package org.example.gamelogic.lastwargamelogic.flag;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class FlagHitListener implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> pickupBlockMap;
    private final FlagDropListener flagDropListener;

    public FlagHitListener(JavaPlugin plugin, Map<UUID, Long> pickupBlockMap, FlagDropListener flagDropListener) {
        this.plugin = plugin;
        this.pickupBlockMap = pickupBlockMap;
        this.flagDropListener = flagDropListener;
    }

    @EventHandler
    public void onFlagHit(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof ArmorStand stand)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        var container = stand.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "flag");
        if (!container.has(key, PersistentDataType.INTEGER)) return;

        long now = System.currentTimeMillis();
        if (pickupBlockMap.containsKey(player.getUniqueId())) {
            long blockUntil = pickupBlockMap.get(player.getUniqueId());
            if (now < blockUntil) {
                player.sendMessage("§cYou can't pick up the flag yet!");
                return;
            }
        }

        stand.remove();

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Flag"));
            bow.setItemMeta(meta);
        }

        player.getInventory().setItem(4, bow);

        // ✅ Set dynamic threshold
        flagDropListener.setFlagThreshold(player);

        // Give player a single arrow in slot 8 (far right)
        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        player.getInventory().setItem(17, arrow);

    }
}
