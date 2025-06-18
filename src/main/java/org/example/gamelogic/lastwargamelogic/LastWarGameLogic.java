package org.example.gamelogic.lastwargamelogic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.example.gamelogic.lastwargamelogic.commands.ResetCoresCommand;
import org.example.gamelogic.lastwargamelogic.deathsystem.DeathSpectatorListener;
import org.example.gamelogic.lastwargamelogic.flag.BowChargeListener;
import org.example.gamelogic.lastwargamelogic.flag.CoreFlagDetectorTask;
import org.example.gamelogic.lastwargamelogic.flag.FlagBowShootListener;
import org.example.gamelogic.lastwargamelogic.flag.FlagDropListener;
import org.example.gamelogic.lastwargamelogic.flag.FlagHitListener;
import org.example.gamelogic.lastwargamelogic.flag.FlagSpawner;
import org.example.gamelogic.lastwargamelogic.flag.FlagTrackerTask;
import org.example.gamelogic.lastwargamelogic.flag.SpawnFlagCommand;
import org.example.gamelogic.lastwargamelogic.privat.CoreSpawner;
import org.example.gamelogic.lastwargamelogic.privat.DangerZone;
import org.example.gamelogic.lastwargamelogic.privat.DangerZoneManager;
import org.example.gamelogic.lastwargamelogic.privat.DropControlListener;
import org.example.gamelogic.lastwargamelogic.privat.ProtectionManager;
import org.example.gamelogic.lastwargamelogic.privat.RegionPresets;
import org.example.gamelogic.lastwargamelogic.privat.SlowGrassBreakListener;
import org.example.gamelogic.lastwargamelogic.timer.GameTimerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LastWarGameLogic extends JavaPlugin {

    @Override
    public void onEnable() {
        //Boarder manager = Danger Zones
        DangerZoneManager dangerZoneManager = new DangerZoneManager(this);

        int mapId = getCurrentMapId();
        registerMapZones(mapId, dangerZoneManager);

        dangerZoneManager.startMonitoring();


        //Protection region , cant place, explode, break
        ProtectionManager protectionManager = new ProtectionManager();
        getServer().getPluginManager().registerEvents(protectionManager, this);

        RegionPresets.registerMapRegions(mapId, protectionManager);

        //Listeners

        getServer().getPluginManager().registerEvents(new SlowGrassBreakListener(this, getCurrentMapId()), this);
        getServer().getPluginManager().registerEvents(new DropControlListener(), this);

        //CORE SPAWNER
        new CoreSpawner(this).spawnCoresIfNeeded();
        new CoreFlagDetectorTask(this).runTaskTimer(this, 0L, 1L);

        //Time manager
        new GameTimerManager(this).startTimer();
        //DeathManager
        getServer().getPluginManager().registerEvents(new DeathSpectatorListener(this), this);

        //Flag system
        // Flag system initialization
        FlagSpawner flagSpawner = new FlagSpawner(this); // ✅ Create once

        // Register command with spawner
        getCommand("spawnflag").setExecutor(new SpawnFlagCommand(flagSpawner));

        // Shared map for pickup delay
        Map<UUID, Long> pickupBlockMap = new HashMap<>();

        // Register listeners
        getServer().getPluginManager().registerEvents(new BowChargeListener(this), this);
        FlagDropListener flagDropListener = new FlagDropListener(this, flagSpawner, pickupBlockMap);
        getServer().getPluginManager().registerEvents(flagDropListener, this);
        getServer().getPluginManager().registerEvents(new FlagHitListener(this, pickupBlockMap, flagDropListener), this);
        getServer().getPluginManager().registerEvents(new FlagBowShootListener(this, flagSpawner, pickupBlockMap), this);

        // Start tracker task (e.g. helmet banner logic)
        new FlagTrackerTask(this).runTaskTimer(this, 0L, 10L); // every 10 ticks (0.5 sec)

        CoreSpawner coreSpawner = new CoreSpawner(this);
        getCommand("resetcores").setExecutor(new ResetCoresCommand(this, coreSpawner));



        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team red = scoreboard.getTeam("RED");
        if (red == null) red = scoreboard.registerNewTeam("RED");
        red.setCanSeeFriendlyInvisibles(true);
        red.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        red.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        red.setColor(ChatColor.RED);

        Team blue = scoreboard.getTeam("BLUE");
        if (blue == null) blue = scoreboard.registerNewTeam("BLUE");
        blue.setCanSeeFriendlyInvisibles(true);
        blue.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
        blue.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OWN_TEAM);
        blue.setColor(ChatColor.BLUE);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private int getCurrentMapId() {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var generalVars = scoreboard.getObjective("GENERAL_VARIABLES");

        if (generalVars == null) return -1;

        return generalVars.getScore("map").getScore();
    }

    private void registerMapZones(int mapId, DangerZoneManager manager) {
        if (mapId == 0) {

            var world = Bukkit.getWorld("lastwarGame1");
            Set<String> teams = Set.of("RED", "BLUE");

            manager.addZone(new DangerZone(new Location(world, -286, 112, 91), new Location(world, -263, 0, 499), teams));
            manager.addZone(new DangerZone(new Location(world, -262, 112, 91), new Location(world, -54, 0, 106), teams));
            manager.addZone(new DangerZone(new Location(world, -54, 112, 107), new Location(world, -70, 0, 498), teams));
            manager.addZone(new DangerZone(new Location(world, -71, 112, 498), new Location(world, -262, 0, 483), teams));
            manager.addZone(new DangerZone(new Location(world, -276, 113, 499), new Location(world, -54, 255, 92), teams));   // top
            manager.addZone(new DangerZone(new Location(world, -276, 30, 499), new Location(world, -54, -64, 92), teams));     // bottom

        }
    }


}
