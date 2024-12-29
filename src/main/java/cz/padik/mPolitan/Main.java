package cz.padik.mPolitan;

import cz.padik.mPolitan.Commands.VehicleCommand; // Import příkazu VehicleCommand
import cz.padik.mPolitan.Commands.VehicleAdminCommand; // Import příkazu VehicleAdminCommand
import cz.padik.mPolitan.events.ToolVisualizer;
import cz.padik.mPolitan.vehicles.VehicleConfig; // Import VehicleConfig
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private ToolVisualizer toolVisualizer;
    private VehicleConfig vehicleConfig;

    @Override
    public void onEnable() {
        // Inicializace ToolVisualizer
        toolVisualizer = new ToolVisualizer(this);

        // Inicializace VehicleConfig
        vehicleConfig = new VehicleConfig(this);

        // Registrace eventů
        getServer().getPluginManager().registerEvents(toolVisualizer, this);

        // Registrace běžných příkazů
        getCommand("vehicle").setExecutor(new VehicleCommand(vehicleConfig, toolVisualizer));

        // Registrace admin příkazů
        getCommand("vehicleadmin").setExecutor(new VehicleAdminCommand(vehicleConfig));

        // Informace o spuštění
        logStartupMessage();
    }

    @Override
    public void onDisable() {
        // Informace o vypnutí
        getLogger().info("Plugin mPolitan byl vypnut.");
    }

    private void logStartupMessage() {
        getLogger().info("=========================================================");
        getLogger().info(" __      __  _     _      _           ");
        getLogger().info(" \\ \\    / / | |   (_)    | |          ");
        getLogger().info("  \\ \\  / /__| |__  _  ___| | ___  ___ ");
        getLogger().info("   \\ \\/ / _ \\ '_ \\| |/ __| |/ _ \\/ __|");
        getLogger().info("    \\  /  __/ | | | | (__| |  __/\\__ \\");
        getLogger().info("     \\/ \\___|_| |_|_|\\___|_|\\___||___/ created by padik");
        getLogger().info("=========================================================");
    }
}
