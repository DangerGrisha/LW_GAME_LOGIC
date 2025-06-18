package org.example.gamelogic.lastwargamelogic.flag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class FlagTrackerTask extends BukkitRunnable {

    private final JavaPlugin plugin;

    public FlagTrackerTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean hasFlagBow = false;

            // Check if the player has the Flag bow anywhere in inventory
            for (ItemStack item : player.getInventory().getContents()) {
                if (isFlagBow(item)) {
                    hasFlagBow = true;
                    break;
                }
            }

            // If player doesn't have a flag bow at all — skip
            if (!hasFlagBow) continue;

            // Check what player is holding in main hand
            ItemStack hand = player.getInventory().getItemInMainHand();

            if (isFlagBow(hand)) {
                // Holding the correct flag bow — remove banner if exists
                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && helmet.getType().toString().endsWith("_BANNER")) {
                    player.getInventory().setHelmet(null);
                }
            } else {
                // Not holding the flag bow — give banner on head
                ItemStack banner = new ItemStack(Material.GREEN_BANNER); // Base color white
                BannerMeta meta = (BannerMeta) banner.getItemMeta();

                meta.addPattern(new Pattern(DyeColor.YELLOW, PatternType.TRIANGLE_BOTTOM)); // "bt"
                meta.addPattern(new Pattern(DyeColor.YELLOW, PatternType.TRIANGLE_TOP));    // "tt"
                meta.addPattern(new Pattern(DyeColor.GREEN, PatternType.RHOMBUS_MIDDLE));   // "mr"
                meta.addPattern(new Pattern(DyeColor.BLACK, PatternType.FLOWER));           // "flo"

                banner.setItemMeta(meta);
                player.getInventory().setHelmet(banner);

            }
        }
    }

    private boolean isFlagBow(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasDisplayName() &&
                Component.text("Flag").equals(meta.displayName());
    }
}
