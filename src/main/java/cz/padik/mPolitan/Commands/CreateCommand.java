package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig;
import cz.padik.mPolitan.events.ToolVisualizer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CreateCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;
    private final ToolVisualizer toolVisualizer;

    public CreateCommand(VehicleConfig vehicleConfig, ToolVisualizer toolVisualizer) {
        this.vehicleConfig = vehicleConfig;
        this.toolVisualizer = toolVisualizer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§6[Workshop] §cTento příkaz mohou použít pouze hráči.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("§6[Workshop] §cPoužij: /vehicle create <typ vozidla> <jméno>");
            return true;
        }

        String vehicleType = args[1];
        String vehicleName = args[2];

        // Kontrola, zda má hráč označenou oblast
        if (!toolVisualizer.hasPlayerSelection(player)) {
            player.sendMessage("§6[Workshop] §cMusíš nejprve označit oblast pomocí nástroje zlatá motyka.");
            return true;
        }

        Location[] selection = toolVisualizer.getPlayerSelection(player);
        Location point1 = selection[0];
        Location point2 = selection[1];

        // Výpočet rozměrů oblasti
        int width = Math.abs(point1.getBlockX() - point2.getBlockX()) + 1;
        int length = Math.abs(point1.getBlockZ() - point2.getBlockZ()) + 1;
        int height = Math.abs(point1.getBlockY() - point2.getBlockY()) + 1;

        // Kontrola rozměrů oblasti
        String path = "vehicles." + vehicleType;
        if (!vehicleConfig.getConfig().contains(path)) {
            player.sendMessage("§cTyp vozidla '" + vehicleType + "' nebyl nalezen.");
            return true;
        }

        int minWidth = vehicleConfig.getConfig().getInt(path + ".min_size.width");
        int minLength = vehicleConfig.getConfig().getInt(path + ".min_size.length");
        int minHeight = vehicleConfig.getConfig().getInt(path + ".min_size.height");
        int maxWidth = vehicleConfig.getConfig().getInt(path + ".max_size.width");
        int maxLength = vehicleConfig.getConfig().getInt(path + ".max_size.length");
        int maxHeight = vehicleConfig.getConfig().getInt(path + ".max_size.height");

        if (width < minWidth || length < minLength || height < minHeight) {
            player.sendMessage("§6[Workshop] §cOznačená oblast je příliš malá. Minimální rozměry jsou:");
            player.sendMessage("§cŠířka: " + minWidth + ", Délka: " + minLength + ", Výška: " + minHeight);
            return true;
        }

        if (width > maxWidth || length > maxLength || height > maxHeight) {
            player.sendMessage("§6[Workshop] §cOznačená oblast je příliš velká. Maximální rozměry jsou:");
            player.sendMessage("§cŠířka: " + maxWidth + ", Délka: " + maxLength + ", Výška: " + maxHeight);
            return true;
        }

        // Vytvoření souboru pro nové vozidlo
        File vehicleFile = new File(vehicleConfig.getPlugin().getDataFolder() + "/data/vehicles/" + vehicleType, vehicleName + ".yml");
        if (vehicleFile.exists()) {
            player.sendMessage("§6[Workshop] §cVozidlo s názvem '" + vehicleName + "' již existuje.");
            return true;
        }

        // Uložení informací do souboru
        try {
            if (vehicleFile.getParentFile().mkdirs() || vehicleFile.createNewFile()) {
                YamlConfiguration vehicleData = YamlConfiguration.loadConfiguration(vehicleFile);

                vehicleData.set("type", vehicleType);
                vehicleData.set("owner", player.getUniqueId().toString());
                vehicleData.set("point1", serializeLocation(point1));
                vehicleData.set("point2", serializeLocation(point2));
                vehicleData.set("fuel", 100); // Výchozí hodnota
                vehicleData.set("damage", 0); // Výchozí hodnota
                vehicleData.set("created_at", System.currentTimeMillis()); // Datum vytvoření
                vehicleData.set("creator", player.getName()); // Jméno hráče

                vehicleData.save(vehicleFile);

                player.sendMessage("§6[Workshop] §aVozidlo '" + vehicleName + "' bylo úspěšně vytvořeno!");
            } else {
                player.sendMessage("§6[Workshop] §cNepodařilo se vytvořit soubor pro vozidlo.");
            }
        } catch (IOException e) {
            player.sendMessage("§6[Workshop] §cDošlo k chybě při vytváření vozidla.");
            e.printStackTrace();
        }

        return true;
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }
}
