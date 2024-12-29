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
        if (!sender.hasPermission("vehicle.admin")) {
            sender.sendMessage("§cNemáš oprávnění pro použití admin příkazů.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§ePoužij /vehicleadmin help pro nápovědu.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                if (args.length < 2) {
                    sender.sendMessage("§cPoužij /vehicleadmin info <jméno vozidla>.");
                    return true;
                }
                String vehicleNameInfo = args[1];
                new AdminInfoCommand(vehicleConfig).onCommand(sender, command, label, new String[]{vehicleNameInfo});
                break;
            case "input":
                if (args.length < 2) {
                    sender.sendMessage("§cPoužij /vehicleadmin input <jméno vozidla>.");
                    return true;
                }
                String vehicleNameInput = args[1];
                new AdminInputCommand(vehicleConfig).onCommand(sender, command, label, new String[]{vehicleNameInput});
                break;
            case "help":
                new AdminHelpCommand().onCommand(sender, command, label, args);
                break;
            default:
                sender.sendMessage("§cNeznámý Admin příkaz. Použij /vehicleadmin help pro více informací.");
                break;
        }
        return true;
    }
}
