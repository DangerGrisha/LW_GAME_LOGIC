package org.example.gamelogic.lastwargamelogic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LastWarGameLogic extends JavaPlugin {
    private static CoreSpawner coreSpawner;
    private CoreFlagDetectorTask coreFlagDetectorTask;
    private static final Set<World> activeWorlds = ConcurrentHashMap.newKeySet();

    public CoreFlagDetectorTask getCoreFlagDetectorTask() {
        return coreFlagDetectorTask;
    }

    public CoreSpawner getCoreSpawner() {
        return coreSpawner;
    }

    public static void addActiveGameWorld(World world) {
        activeWorlds.add(world);
    }

    public static void removeActiveGameWorld(World world) {
        activeWorlds.remove(world);
    }

    public static Set<World> getActiveGameWorlds() {
        return Collections.unmodifiableSet(activeWorlds);
    }

    @Override
    public void onEnable() {

        // Инициализируем активные миры по scoreboard'у
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        for (World world : Bukkit.getWorlds()) {
            if (scoreboard.getObjective(world.getName()) != null) {
                int value = scoreboard.getObjective(world.getName()).getScore("isGameStarted").getScore();
                if (value == 1) {
                    addActiveGameWorld(world);
                    getLogger().info("[LastWar] World " + world.getName() + " marked as active.");
                }
            }
        }


        DangerZoneManager dangerZoneManager = new DangerZoneManager(this);

        registerMapZones(dangerZoneManager);

        dangerZoneManager.startMonitoring();

        ProtectionManager protectionManager = new ProtectionManager();
        getServer().getPluginManager().registerEvents(protectionManager, this);

        for (World world : LastWarGameLogic.getActiveGameWorlds()) {
            RegionPresets.registerRegionsForWorld(world, protectionManager);
        }


        coreFlagDetectorTask = new CoreFlagDetectorTask(this);
        coreFlagDetectorTask.runTaskTimer(this, 0L, 5L);

        getServer().getPluginManager().registerEvents(new SlowGrassBreakListener(this), this);

        getServer().getPluginManager().registerEvents(new DropControlListener(), this);

        new CoreFlagDetectorTask(this).runTaskTimer(this, 0L, 1L);

        new GameTimerManager(this).startTimer();
        getServer().getPluginManager().registerEvents(new DeathSpectatorListener(this), this);

        FlagSpawner flagSpawner = new FlagSpawner(this);
        getCommand("spawnflag").setExecutor(new SpawnFlagCommand(flagSpawner));

        Map<UUID, Long> pickupBlockMap = new HashMap<>();

        getServer().getPluginManager().registerEvents(new BowChargeListener(this), this);
        FlagDropListener flagDropListener = new FlagDropListener(this, flagSpawner, pickupBlockMap);
        getServer().getPluginManager().registerEvents(flagDropListener, this);
        getServer().getPluginManager().registerEvents(new FlagHitListener(this, pickupBlockMap, flagDropListener), this);
        getServer().getPluginManager().registerEvents(new FlagBowShootListener(this, flagSpawner, pickupBlockMap), this);

        new FlagTrackerTask(this).runTaskTimer(this, 0L, 10L);

        coreSpawner = new CoreSpawner(this);
        getCommand("resetcores").setExecutor(new ResetCoresCommand(this, coreSpawner));

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
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
        activeWorlds.clear();
    }


    private void registerMapZones(DangerZoneManager manager) {
        Set<String> teams = Set.of("RED", "BLUE");
        for (World world : activeWorlds) {
                manager.addZone(new DangerZone(new Location(world, -286, 112, 91), new Location(world, -263, 0, 499), teams));
                manager.addZone(new DangerZone(new Location(world, -262, 112, 91), new Location(world, -54, 0, 106), teams));
                manager.addZone(new DangerZone(new Location(world, -54, 112, 107), new Location(world, -70, 0, 498), teams));
                manager.addZone(new DangerZone(new Location(world, -71, 112, 498), new Location(world, -262, 0, 483), teams));
                manager.addZone(new DangerZone(new Location(world, -276, 113, 499), new Location(world, -54, 255, 92), teams));
                manager.addZone(new DangerZone(new Location(world, -276, 30, 499), new Location(world, -54, -64, 92), teams));
        }
    }
}
