package org.example.gamelogic.lastwargamelogic.commands;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.example.gamelogic.lastwargamelogic.privat.CoreSpawner;
import org.bukkit.plugin.java.JavaPlugin;

public class ResetCoresCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final CoreSpawner coreSpawner;

    public ResetCoresCommand(JavaPlugin plugin, CoreSpawner coreSpawner) {
        this.plugin = plugin;
        this.coreSpawner = coreSpawner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.isOp()) {
            sender.sendMessage("§cOnly OP players can use this command.");
            return true;
        }
        World world = ((Player) sender).getWorld();
        coreSpawner.spawnCoresForce(world);
        sender.sendMessage("§aCores have been reset.");
        return true;
    }
}
