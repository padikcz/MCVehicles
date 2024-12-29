package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.events.Steering;
import cz.padik.mPolitan.vehicles.VehicleConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


import java.io.File;

public class SteeringCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;

    public SteeringCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTento příkaz mohou použít pouze hráči.");
            return true;
        }

        Location playerLoc = player.getLocation();
        File dataFolder = new File(vehicleConfig.getPlugin().getDataFolder(), "data/vehicles");
        if (!dataFolder.exists()) {
            player.sendMessage("§cŽádná vozidla nebyla nalezena.");
            return true;
        }

        for (File typeFolder : dataFolder.listFiles()) {
            if (typeFolder.isDirectory()) {
                for (File vehicleFile : typeFolder.listFiles()) {
                    if (vehicleFile.isFile() && vehicleFile.getName().endsWith(".yml")) {
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(vehicleFile);

                        Location point1 = deserializeLocation(config.getString("point1"));
                        Location point2 = deserializeLocation(config.getString("point2"));

                        if (isInside(playerLoc, point1, point2)) {
                            // Spuštění události Steering
                            new Steering(vehicleConfig, player, config, vehicleFile, vehicleConfig.getPlugin()).start();
                            player.sendMessage("§6Řízení vozidla bylo aktivováno.");
                            return true;
                        }
                    }
                }
            }
        }

        player.sendMessage("§cNenacházíš se v žádném vozidle.");
        return true;
    }

    private boolean isInside(Location loc, Location point1, Location point2) {
        int minX = Math.min(point1.getBlockX(), point2.getBlockX());
        int maxX = Math.max(point1.getBlockX(), point2.getBlockX());
        int minY = Math.min(point1.getBlockY(), point2.getBlockY());
        int maxY = Math.max(point1.getBlockY(), point2.getBlockY());
        int minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());
        int maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ());

        return loc.getBlockX() >= minX && loc.getBlockX() <= maxX &&
                loc.getBlockY() >= minY && loc.getBlockY() <= maxY &&
                loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ;
    }

    private Location deserializeLocation(String serialized) {
        if (serialized == null || serialized.isEmpty()) return null;

        String[] parts = serialized.split(";");
        if (parts.length != 4) return null;

        String worldName = parts[0];
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
}
