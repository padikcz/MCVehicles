package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig; // Opravený import
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Set;

public class ListCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig; // Použití VehicleConfig ze správného balíčku

    public ListCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        var section = vehicleConfig.getConfig().getConfigurationSection("vehicles");
        if (section == null) {
            sender.sendMessage("§cŽádná vozidla nebyla nalezena.");
            return true;
        }

        Set<String> vehicleTypes = section.getKeys(false);
        sender.sendMessage("§6Dostupná vozidla:");
        for (String vehicle : vehicleTypes) {
            sender.sendMessage("§e- " + vehicle);
        }
        return true;
    }
}
