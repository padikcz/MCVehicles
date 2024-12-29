package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig; // Opravený import
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TypeCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;

    public TypeCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cPoužij: /vehicle type <název vozidla>");
            return true;
        }

        String vehicleType = args[1].toLowerCase();
        String path = "vehicles." + vehicleType;
        if (!vehicleConfig.getConfig().contains(path)) {
            sender.sendMessage("§cVozidlo '" + vehicleType + "' nebylo nalezeno.");
            return true;
        }

        int minWidth = vehicleConfig.getConfig().getInt(path + ".min_size.width");
        int minLength = vehicleConfig.getConfig().getInt(path + ".min_size.length");
        int minHeight = vehicleConfig.getConfig().getInt(path + ".min_size.height");
        int maxWidth = vehicleConfig.getConfig().getInt(path + ".max_size.width");
        int maxLength = vehicleConfig.getConfig().getInt(path + ".max_size.length");
        int maxHeight = vehicleConfig.getConfig().getInt(path + ".max_size.height");
        int maxSpeed = vehicleConfig.getConfig().getInt(path + ".max_speed");
        String permission = vehicleConfig.getConfig().getString(path + ".permission");
        int fuelConsumption = vehicleConfig.getConfig().getInt(path + ".fuel_consumption");

        sender.sendMessage("§6Informace o vozidle '" + vehicleType + "':");
        sender.sendMessage("§eMinimální velikost: §7Šířka: " + minWidth + ", Délka: " + minLength + ", Výška: " + minHeight);
        sender.sendMessage("§eMaximální velikost: §7Šířka: " + maxWidth + ", Délka: " + maxLength + ", Výška: " + maxHeight);
        sender.sendMessage("§eMaximální rychlost: §7" + maxSpeed);
        sender.sendMessage("§eOprávnění: §7" + permission);
        sender.sendMessage("§eSpotřeba paliva: §7" + fuelConsumption);
        return true;
    }
}
