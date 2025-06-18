package org.example.gamelogic.lastwargamelogic.deathsystem;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.PacketType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class EnemyHider {

    private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void hideEnemy(Player viewer, Player target) {
        PacketContainer removeFromTab = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE);
        removeFromTab.getUUIDLists().write(0, Collections.singletonList(target.getUniqueId()));
        send(viewer, removeFromTab);

        PacketContainer destroyEntity = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroyEntity.getIntLists().write(0, Collections.singletonList(target.getEntityId()));
        send(viewer, destroyEntity);
    }

    public static void showEnemy(JavaPlugin plugin, Player viewer, Player target) {
        if (!viewer.isOnline() || !target.isOnline() || viewer.equals(target) || target.isDead()) return;

        ProtocolManager pm = ProtocolLibrary.getProtocolManager();

        // Step 1: re-add to tab
        PacketContainer tabPacket = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
        tabPacket.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        PlayerInfoData data = new PlayerInfoData(
                WrappedGameProfile.fromPlayer(target),
                0,
                EnumWrappers.NativeGameMode.fromBukkit(target.getGameMode()),
                WrappedChatComponent.fromText(target.getName())
        );

        tabPacket.getPlayerInfoDataLists().write(0, Collections.singletonList(data));
        sendPacket(viewer, tabPacket);

        // Step 2: re-spawn the entity
        PacketContainer spawn = pm.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawn.getIntegers().write(0, target.getEntityId());
        spawn.getUUIDs()   .write(0, target.getUniqueId());
        spawn.getDoubles().write(0, target.getLocation().getX());
        spawn.getDoubles().write(1, target.getLocation().getY());
        spawn.getDoubles().write(2, target.getLocation().getZ());
        spawn.getBytes()  .write(0, (byte)(target.getLocation().getYaw() * 256 / 360));
        spawn.getBytes()  .write(1, (byte)(target.getLocation().getPitch() * 256 / 360));
        sendPacket(viewer, spawn);
    }

    private static void sendPacket(Player p, PacketContainer packet) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private static void send(Player to, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(to, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
