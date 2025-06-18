package org.example.gamelogic.lastwargamelogic.flag;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnFlagCommand implements CommandExecutor {

    private final FlagSpawner flagSpawner;

    public SpawnFlagCommand(FlagSpawner flagSpawner) {
        this.flagSpawner = flagSpawner;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can run this command.");
            return true;
        }

        if (args.length != 3) {
            player.sendMessage("Usage: /spawnflag <x> <y> <z>");
            return true;
        }

        try {
            double x = Double.parseDouble(args[0]);
            double y = Double.parseDouble(args[1]);
            double z = Double.parseDouble(args[2]);

            Location loc = new Location(player.getWorld(), x, y, z);
            flagSpawner.spawnAndReturn(loc); // <- 💥 This was missing
            player.sendMessage("§aFlag spawned at §e" + x + " " + y + " " + z);
        } catch (NumberFormatException e) {
            player.sendMessage("§cCoordinates must be numbers.");
        }

        return true;
    }
}
