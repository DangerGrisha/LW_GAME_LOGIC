package org.example.gamelogic.lastwargamelogic.privat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Set;

public class DangerZone {
    private final int minX, maxX, minY, maxY, minZ, maxZ;
    private final Set<String> allowedTeams;

    // Initializes a danger zone with two corner points and allowed team names
    public DangerZone(Location corner1, Location corner2, Set<String> allowedTeams) {
        this.minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        this.maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        this.minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        this.maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        this.minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        this.maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        this.allowedTeams = allowedTeams;
    }

    // Checks if the player is inside the danger zone and in an allowed team
    public boolean isInZone(Player player) {
        Location loc = player.getLocation();
        int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();

        if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ) return false;

        Team team = player.getScoreboard().getEntryTeam(player.getName());
        return team != null && allowedTeams.contains(team.getName().toUpperCase());
    }

    // Applies the zone's effect to the player (default is damage)
    public void applyEffect(Player player) {
        player.damage(5.0); // Deals 0.5 hearts of damage
    }
}
