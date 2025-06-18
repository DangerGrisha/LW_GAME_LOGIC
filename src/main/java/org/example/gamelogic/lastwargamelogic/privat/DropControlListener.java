package org.example.gamelogic.lastwargamelogic.privat;

import org.bukkit.block.Block;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;

public class DropControlListener implements Listener {

    // Only red and blue wool are allowed to drop
    private boolean isAllowed(Material type) {
        return type == Material.RED_WOOL || type == Material.BLUE_WOOL;
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isAllowed(event.getBlock().getType())) {
            event.setDropItems(false);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Allow the explosion, but remove drops from unwanted blocks
        event.blockList().forEach(block -> {
            if (!isAllowed(block.getType())) {
                block.setType(Material.AIR); // destroy block without drop
            } else {
                block.breakNaturally(); // allow drop for red/blue wool
            }
        });

        event.setYield(0); // disable default explosion drop physics
    }




    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        // Let the explosion happen visually and physically
        event.blockList().forEach(block -> {
            if (!isAllowed(block.getType())) {
                block.setType(Material.AIR); // manually destroy without drop
            } else {
                block.breakNaturally(); // drop for allowed wool
            }
        });

        event.setYield(0); // disable default drops from explosion
    }

}
