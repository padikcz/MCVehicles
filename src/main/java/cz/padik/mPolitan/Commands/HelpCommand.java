package cz.padik.mPolitan.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§6===== §eVehicle Plugin Help §6=====");
        sender.sendMessage("§e/vehicle help §7- Zobrazí tuto nápovědu.");
        sender.sendMessage("§e/vehicle list §7- Zobrazí seznam dostupných vozidel.");
        sender.sendMessage("§e/vehicle type <název> §7- Zobrazí informace o konkrétním vozidle.");
        sender.sendMessage("§e/vehicle create <typ> <jméno> §7- Vytvoří nové vozidlo.");
        sender.sendMessage("§e/vehicle info §7- Zobrazí informace o vozidle, ve kterém se nacházíš.");
        return true;
    }
}
