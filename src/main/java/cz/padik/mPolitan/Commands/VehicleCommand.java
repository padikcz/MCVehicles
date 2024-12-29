package cz.padik.mPolitan.Commands;

import cz.padik.mPolitan.vehicles.VehicleConfig;
import cz.padik.mPolitan.events.ToolVisualizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class VehicleCommand implements CommandExecutor {

    private final VehicleConfig vehicleConfig;
    private final ToolVisualizer toolVisualizer;

    public VehicleCommand(VehicleConfig vehicleConfig, ToolVisualizer toolVisualizer) {
        this.vehicleConfig = vehicleConfig;
        this.toolVisualizer = toolVisualizer;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6[§3Vehicle§6] §ePoužij §6/vehicle help §epro nápovědu.");
            return true;
        }

        if (!sender.hasPermission("vehicle.user")) {
            sender.sendMessage("§6[§3Vehicle§6] §cNemáš oprávnění pro použití tohoto příkazu.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                new HelpCommand().onCommand(sender, command, label, args);
                break;
            case "list":
                new ListCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            case "type":
                new TypeCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            case "create":
                new CreateCommand(vehicleConfig, toolVisualizer).onCommand(sender, command, label, args);
                break;
            case "info":
                new InfoCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            case "steering":
                new SteeringCommand(vehicleConfig).onCommand(sender, command, label, args);
                break;
            default:
                sender.sendMessage("§6[§3Vehicle§6] §eNeznámý příkaz. Použij §6/vehicle help §epro více informací.");
                break;
        }
        return true;
    }
}
