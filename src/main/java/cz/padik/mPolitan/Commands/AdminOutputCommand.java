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
import java.util.Map;

public class AdminOutputCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;

    public AdminOutputCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTento příkaz mohou použít pouze hráči.");
            return true;
        }

        if (args.length < 2) { // Očekáváme minimálně 2 argumenty: output, jméno vozidla
            sender.sendMessage("§cPoužití: /vehicleadmin output <jméno vozidla>");
            return true;
        }

        String vehicleName = args[1]; // Jméno vozidla je na indexu 1

        // Najít soubor vozidla ve všech podadresářích
        File dataFolder = new File(vehicleConfig.getPlugin().getDataFolder(), "data/vehicles");
        File vehicleFile = findVehicleFile(dataFolder, vehicleName);
        if (vehicleFile == null) {
            player.sendMessage("§cSoubor pro vozidlo \"" + vehicleName + "\" neexistuje.");
            return true;
        }

        // Načíst strukturu vozidla
        YamlConfiguration config = YamlConfiguration.loadConfiguration(vehicleFile);
        Map<String, Object> structure = config.getConfigurationSection("structure").getValues(false);
        if (structure == null || structure.isEmpty()) {
            player.sendMessage("§cStruktura vozidla \"" + vehicleName + "\" nebyla nalezena.");
            return true;
        }

        // Načíst souřadnice point1 a point2
        Location point1 = deserializeLocation(config.getString("point1"), player);
        Location point2 = deserializeLocation(config.getString("point2"), player);

        if (point1 == null || point2 == null) {
            player.sendMessage("§cBod \"point1\" nebo \"point2\" není správně definován ve vozidle \"" + vehicleName + "\".");
            return true;
        }

        // Vypočítat minimální bod
        int minX = Math.min(point1.getBlockX(), point2.getBlockX());
        int minY = Math.min(point1.getBlockY(), point2.getBlockY());
        int minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());

        // Nastavit základní bod na minimální bod
        Location adjustedBaseLocation = new Location(point1.getWorld(), minX, minY, minZ);

        try {
            for (Map.Entry<String, Object> entry : structure.entrySet()) {
                String[] relativeCoords = entry.getKey().split(",");
                int relX = Integer.parseInt(relativeCoords[0]);
                int relY = Integer.parseInt(relativeCoords[1]);
                int relZ = Integer.parseInt(relativeCoords[2]);

                String blockDataString = (String) entry.getValue();
                int indexOfAttributes = blockDataString.indexOf("[");
                Material material;
                String blockData = null;

                if (indexOfAttributes != -1) {
                    material = Material.valueOf(blockDataString.substring(0, indexOfAttributes).toUpperCase());
                    blockData = blockDataString.substring(indexOfAttributes);
                } else {
                    material = Material.valueOf(blockDataString.toUpperCase());
                }

                Location blockLocation = adjustedBaseLocation.clone().add(relX, relY, relZ);
                Block block = blockLocation.getBlock();
                block.setType(material);

                // Nastavení orientace bloku, pokud existuje
                if (blockData != null) {
                    block.setBlockData(org.bukkit.Bukkit.createBlockData(material, blockData));
                }
            }

            player.sendMessage("§aVozidlo \"" + vehicleName + "\" bylo úspěšně postaveno na minimálním bodu.");
        } catch (Exception e) {
            player.sendMessage("§cDošlo k chybě při stavbě vozidla \"" + vehicleName + "\".");
            e.printStackTrace();
        }

        return true;
    }

    private File findVehicleFile(File dataFolder, String vehicleName) {
        File[] subFolders = dataFolder.listFiles(File::isDirectory);
        if (subFolders == null) return null;

        for (File subFolder : subFolders) {
            File vehicleFile = new File(subFolder, vehicleName + ".yml");
            if (vehicleFile.exists()) {
                return vehicleFile;
            }
        }

        return null; // Pokud soubor nebyl nalezen
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
