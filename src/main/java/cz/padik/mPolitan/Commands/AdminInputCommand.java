package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminInputCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;

    public AdminInputCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTento příkaz mohou použít pouze hráči.");
            return true;
        }

        if (args.length < 3) { // Očekáváme minimálně 3 argumenty: admin, input, jméno vozidla
            player.sendMessage("§cPoužití: /vehicle admin input <jméno vozidla>");
            return true;
        }

        String vehicleName = args[2]; // Jméno vozidla je na indexu 2

        // Najít soubor vozidla
        File vehicleFile = new File(vehicleConfig.getPlugin().getDataFolder(), "data/vehicles/" + vehicleName + ".yml");
        if (!vehicleFile.exists()) {
            player.sendMessage("§cSoubor pro vozidlo \"" + vehicleName + "\" neexistuje.");
            return true;
        }

        // Načíst body point1 a point2 ze souboru
        YamlConfiguration config = YamlConfiguration.loadConfiguration(vehicleFile);
        Location point1 = deserializeLocation(config.getString("point1"), player);
        Location point2 = deserializeLocation(config.getString("point2"), player);

        if (point1 == null || point2 == null) {
            player.sendMessage("§cNelze najít body \"point1\" nebo \"point2\" ve vozidle \"" + vehicleName + "\".");
            return true;
        }

        // Sestavení struktury
        Map<String, String> structure = new HashMap<>();
        int minX = Math.min(point1.getBlockX(), point2.getBlockX());
        int minY = Math.min(point1.getBlockY(), point2.getBlockY());
        int minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());

        int maxX = Math.max(point1.getBlockX(), point2.getBlockX());
        int maxY = Math.max(point1.getBlockY(), point2.getBlockY());
        int maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(point1.getWorld(), x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType() != Material.AIR) {
                        String relativeCoords = (x - minX) + "," + (y - minY) + "," + (z - minZ);
                        structure.put(relativeCoords, block.getType().toString().toLowerCase());
                    }
                }
            }
        }

        config.createSection("structure", structure);

        try {
            config.save(vehicleFile);
            player.sendMessage("§aStruktura vozidla \"" + vehicleName + "\" byla úspěšně uložena.");
        } catch (IOException e) {
            player.sendMessage("§cNepodařilo se uložit strukturu vozidla.");
            e.printStackTrace();
        }

        return true;
    }

    private Location deserializeLocation(String serialized, Player player) {
        if (serialized == null || serialized.isEmpty()) return null;

        try {
            String[] parts = serialized.split(";");
            return new Location(
                    player.getServer().getWorld(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
            );
        } catch (Exception e) {
            player.sendMessage("§cChyba při načítání umístění: " + serialized);
            e.printStackTrace();
            return null;
        }
    }
}
