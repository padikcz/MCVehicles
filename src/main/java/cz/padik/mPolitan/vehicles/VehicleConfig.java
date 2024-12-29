package cz.padik.mPolitan.vehicles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class VehicleConfig {

    private final Plugin plugin;
    private FileConfiguration config;
    private final File configFile;

    public VehicleConfig(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "vehicles.yml");

        // Vytvoření složky pluginu, pokud neexistuje
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Načtení nebo vytvoření konfiguračního souboru
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                plugin.getLogger().info("Konfigurační soubor vehicles.yml byl vytvořen.");
                createDefaultConfig(); // Přidání výchozích hodnot
            } catch (IOException e) {
                plugin.getLogger().severe("Chyba při vytváření vehicles.yml!");
                e.printStackTrace();
            }
        }

        loadConfig();
    }

    /**
     * Načte konfiguraci ze souboru vehicles.yml.
     */
    public void loadConfig() {
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Uloží konfiguraci do souboru vehicles.yml.
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Chyba při ukládání vehicles.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Vrátí aktuální konfiguraci.
     *
     * @return FileConfiguration instance.
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Vrátí plugin přidružený k této třídě.
     *
     * @return Plugin instance.
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Vytvoří výchozí konfiguraci s ukázkovými daty.
     */
    private void createDefaultConfig() {
        config.set("vehicles.car.min_size.width", 2);
        config.set("vehicles.car.min_size.length", 3);
        config.set("vehicles.car.min_size.height", 2);
        config.set("vehicles.car.max_size.width", 5);
        config.set("vehicles.car.max_size.length", 8);
        config.set("vehicles.car.max_size.height", 4);
        config.set("vehicles.car.max_speed", 10);
        config.set("vehicles.car.fuel_consumption", 5);
        config.set("vehicles.car.permission", "vehicles.car");

        config.set("vehicles.tractor.min_size.width", 3);
        config.set("vehicles.tractor.min_size.length", 4);
        config.set("vehicles.tractor.min_size.height", 3);
        config.set("vehicles.tractor.max_size.width", 6);
        config.set("vehicles.tractor.max_size.length", 10);
        config.set("vehicles.tractor.max_size.height", 5);
        config.set("vehicles.tractor.max_speed", 8);
        config.set("vehicles.tractor.fuel_consumption", 7);
        config.set("vehicles.tractor.permission", "vehicles.tractor");

        saveConfig();
    }
}
