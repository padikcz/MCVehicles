package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class AdminInfoCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;

    public AdminInfoCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cTento příkaz mohou použít pouze hráči.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cPoužití: /vehicleadmin info <jméno vozidla>");
            return true;
        }

        String vehicleName = args[0];
        File dataFolder = new File(vehicleConfig.getPlugin().getDataFolder(), "data/vehicles");

        if (!dataFolder.exists() || !dataFolder.isDirectory()) {
            player.sendMessage("§cSložka 'data/vehicles' neexistuje nebo není složkou.");
            return true;
        }

        File vehicleFile = findVehicleFile(dataFolder, vehicleName);
        if (vehicleFile == null) {
            player.sendMessage("§cVozidlo s názvem '" + vehicleName + "' nebylo nalezeno.");
            return true;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(vehicleFile);

        // Načíst informace o vozidle
        String type = config.getString("type", "Neznámý");
        String owner = config.getString("owner", "Neznámý");
        int fuel = config.getInt("fuel", 0);
        int damage = config.getInt("damage", 0);
        String createdAt = config.getString("created_at", "Neznámý");
        String point1 = config.getString("point1", "Neznámý");
        String point2 = config.getString("point2", "Neznámý");

        // Odeslat informace hráči
        player.sendMessage("§6Informace o vozidle: §e" + vehicleName);
        player.sendMessage("§eTyp: §7" + type);
        player.sendMessage("§eMajitel: §7" + owner);
        player.sendMessage("§ePalivo: §7" + fuel);
        player.sendMessage("§ePoškození: §7" + damage);
        player.sendMessage("§eVytvořeno: §7" + createdAt);
        player.sendMessage("§ePoint1: §7" + point1);
        player.sendMessage("§ePoint2: §7" + point2);

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
}
