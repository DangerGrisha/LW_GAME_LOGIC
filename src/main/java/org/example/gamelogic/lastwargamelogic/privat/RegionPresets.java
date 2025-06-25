package org.example.gamelogic.lastwargamelogic.privat;

import org.bukkit.Location;
import org.bukkit.World;

public class RegionPresets {

    public static void registerRegionsForWorld(World world, ProtectionManager manager) {
        // blue team
        manager.addRegion(new ProtectedRegion(new Location(world, -137, 34, 118), new Location(world, -144, 40, 112)));
        manager.addRegion(new ProtectedRegion(new Location(world, -178, 0, 125), new Location(world, -178, 46, 117)));
        manager.addRegion(new ProtectedRegion(new Location(world, -177, 0, 128), new Location(world, -177, 46, 114)));
        manager.addRegion(new ProtectedRegion(new Location(world, -176, 0, 129), new Location(world, -176, 46, 113)));
        manager.addRegion(new ProtectedRegion(new Location(world, -175, 0, 130), new Location(world, -173, 46, 112)));
        manager.addRegion(new ProtectedRegion(new Location(world, -172, 0, 131), new Location(world, -164, 46, 111)));
        manager.addRegion(new ProtectedRegion(new Location(world, -163, 0, 130), new Location(world, -161, 46, 112)));
        manager.addRegion(new ProtectedRegion(new Location(world, -160, 0, 129), new Location(world, -160, 46, 113)));
        manager.addRegion(new ProtectedRegion(new Location(world, -159, 0, 128), new Location(world, -159, 46, 114)));
        manager.addRegion(new ProtectedRegion(new Location(world, -158, 0, 125), new Location(world, -158, 46, 117)));

        // center
        manager.addRegion(new ProtectedRegion(new Location(world, -176, 0, 290), new Location(world, -184, 44, 298)));

        // red team
        manager.addRegion(new ProtectedRegion(new Location(world, -138, 34, 470), new Location(world, -144, 41, 476)));
        manager.addRegion(new ProtectedRegion(new Location(world, -158, 0, 463), new Location(world, -158, 46, 471)));
        manager.addRegion(new ProtectedRegion(new Location(world, -159, 0, 460), new Location(world, -159, 46, 474)));
        manager.addRegion(new ProtectedRegion(new Location(world, -160, 0, 459), new Location(world, -160, 46, 475)));
        manager.addRegion(new ProtectedRegion(new Location(world, -161, 0, 458), new Location(world, -163, 46, 476)));
        manager.addRegion(new ProtectedRegion(new Location(world, -164, 0, 457), new Location(world, -172, 46, 477)));
        manager.addRegion(new ProtectedRegion(new Location(world, -173, 0, 458), new Location(world, -175, 46, 476)));
        manager.addRegion(new ProtectedRegion(new Location(world, -176, 0, 459), new Location(world, -176, 46, 475)));
        manager.addRegion(new ProtectedRegion(new Location(world, -177, 0, 460), new Location(world, -177, 46, 474)));
        manager.addRegion(new ProtectedRegion(new Location(world, -178, 0, 463), new Location(world, -178, 46, 471)));

        // from DangerZone areas
        manager.addRegion(new ProtectedRegion(new Location(world, -286, 0, 91), new Location(world, -263, 112, 499)));
        manager.addRegion(new ProtectedRegion(new Location(world, -262, 0, 91), new Location(world, -54, 112, 106)));
        manager.addRegion(new ProtectedRegion(new Location(world, -54, 0, 107), new Location(world, -70, 112, 498)));
        manager.addRegion(new ProtectedRegion(new Location(world, -71, 0, 498), new Location(world, -262, 112, 483)));

        // top
        manager.addRegion(new ProtectedRegion(new Location(world, -276, 113, 499), new Location(world, -54, 255, 92)));

        // bottom
        manager.addRegion(new ProtectedRegion(new Location(world, -276, -64, 499), new Location(world, -54, 30, 92)));
    }
}
