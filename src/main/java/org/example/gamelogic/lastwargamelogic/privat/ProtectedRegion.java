package org.example.gamelogic.lastwargamelogic.privat;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class ProtectedRegion {
    private final int minX, maxX, minY, maxY, minZ, maxZ;
    private final String world;

    public ProtectedRegion(Location corner1, Location corner2) {
        this.minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        this.maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        this.minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        this.maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        this.minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        this.maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        this.world = corner1.getWorld().getName();
    }

    public boolean contains(Block block) {
        Location loc = block.getLocation();
        return loc.getWorld().getName().equals(world) &&
                loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }
}
