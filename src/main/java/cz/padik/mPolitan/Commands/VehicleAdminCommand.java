package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class VehicleAdminCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;

    public VehicleAdminCommand(VehicleConfig vehicleConfig) {
        this.vehicleConfig = vehicleConfig;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§ePoužij /vehicleadmin help pro nápovědu.");
            return true;
        }

        if (!sender.hasPermission("vehicle.admin")) {
            sender.sendMessage("§cNemáš oprávnění pro použití admin příkazů.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                new AdminHelpCommand().onCommand(sender, command, label, args);
                break;
            case "info":
                new AdminInfoCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            case "input":
                new AdminInputCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            case "ouput":
                new AdminOutputCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            default:
                sender.sendMessage("§cNeznámý Admin příkaz. Použij /vehicleadmin help pro více informací.");
                break;
        }
        return true;
    }
}
