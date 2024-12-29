package cz.padik.mPolitan.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminHelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§6===== §eAdmin příkazy §6=====");
        sender.sendMessage("§e/vehicleadmin info <jméno vozidla> §7- Zobrazí informace o vozidle.");
        sender.sendMessage("§e/vehicleadmin tp <jméno vozidla> §7- tereportujetě k vozidlu");
        sender.sendMessage("§e/vehicleadmin give <jméno vozidla> <jmeno hrače> §7- dá vozidlo grači ");
        sender.sendMessage("§e/vehicleadmin input <jméno vozidla> §7- ulozi aktualni stav vozidla");
        sender.sendMessage("§e/vehicleadmin export <jméno vozidla> §7- načte ulozeni stav vozidla");
        sender.sendMessage("§e/vehicleadmin delete <jméno vozidla> §7- smaze vozidlo");

        return true;
    }
}
