package cz.padik.mPolitan.events;

import cz.padik.mPolitan.vehicles.VehicleConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class Steering {

    private final VehicleConfig vehicleConfig;
    private final Player player;
    private final YamlConfiguration config;
    private final File vehicleFile;
    private final Plugin plugin;

    private int speed;
    private final int maxSpeed;
    private int fuel;
    private int damage;
    private boolean isActive;
    private Location point1;
    private Location point2;

    public Steering(VehicleConfig vehicleConfig, Player player, YamlConfiguration config, File vehicleFile, Plugin plugin) {
        this.vehicleConfig = vehicleConfig;
        this.player = player;
        this.config = config;
        this.vehicleFile = vehicleFile;
        this.plugin = plugin;

        this.speed = 1;
        this.maxSpeed = config.getInt("max_speed", 10);
        this.fuel = config.getInt("fuel", 100);
        this.damage = config.getInt("damage", 0);
        this.isActive = true;

        this.point1 = safeDeserializeLocation(config.getString("point1"), player.getWorld().getSpawnLocation());
        this.point2 = safeDeserializeLocation(config.getString("point2"), player.getWorld().getSpawnLocation());
    }

    public void start() {
        player.sendMessage("§6[Steering] Řízení bylo aktivováno!");

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive || !player.isOnline()) {
                    cancel();
                    return;
                }
                updateActionBar();
            }
        }.runTaskTimer(plugin, 0L, 5L);

        plugin.getServer().getPluginManager().registerEvents(new PlayerInputHandler(), plugin);
    }

    private void moveVehicle(String direction) {
        int dx = 0, dz = 0;

        // Směr pohybu
        switch (direction) {
            case "N": dz = speed; break;
            case "S": dz = -speed; break;
            case "E": dx = -speed; break;
            case "W": dx = speed; break;
        }

        Location newPoint1 = point1.clone().add(dx, 0, dz);
        Location newPoint2 = point2.clone().add(dx, 0, dz);

        try {
            copyAndMoveRegion(point1, point2, newPoint1);
        } catch (Exception e) {
            player.sendMessage("§6[Steering] Došlo k chybě při přesunu vozidla.");
            e.printStackTrace();
            return;
        }

        point1 = newPoint1;
        point2 = newPoint2;

        Location playerLocation = player.getLocation();
        Location newPlayerLocation = playerLocation.clone().add(dx, 0, dz);
        player.teleport(newPlayerLocation);

        config.set("point1", serializeLocation(point1));
        config.set("point2", serializeLocation(point2));
        saveVehicleData();

        player.sendMessage("§6[Steering] Vozidlo se posunulo směrem " + direction + " o " + speed + " bloky.");
    }

    private void copyAndMoveRegion(Location from1, Location from2, Location to1) {
        int minX = Math.min(from1.getBlockX(), from2.getBlockX());
        int maxX = Math.max(from1.getBlockX(), from2.getBlockX());
        int minY = Math.min(from1.getBlockY(), from2.getBlockY());
        int maxY = Math.max(from1.getBlockY(), from2.getBlockY());
        int minZ = Math.min(from1.getBlockZ(), from2.getBlockZ());
        int maxZ = Math.max(from1.getBlockZ(), from2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location oldLocation = new Location(from1.getWorld(), x, y, z);
                    Location newLocation = oldLocation.clone().add(to1.getBlockX() - from1.getBlockX(), 0, to1.getBlockZ() - from1.getBlockZ());

                    Block oldBlock = oldLocation.getBlock();
                    Block newBlock = newLocation.getBlock();

                    newBlock.setType(oldBlock.getType());
                    newBlock.setBlockData(oldBlock.getBlockData());
                    oldBlock.setType(Material.AIR);
                }
            }
        }
    }

    private void updateActionBar() {
        String actionBarMessage = String.format("§a【%d/100】 §e【%db/h】 §c【%d/100】", damage, speed, fuel);
        player.sendActionBar(actionBarMessage);
    }

    private void saveVehicleData() {
        config.set("fuel", fuel);
        config.set("damage", damage);
        try {
            config.save(vehicleFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PlayerInputHandler implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onPlayerItemHeld(org.bukkit.event.player.PlayerItemHeldEvent event) {
            if (!event.getPlayer().equals(player) || !isActive) return;

            int delta = event.getNewSlot() - event.getPreviousSlot();
            if (delta > 0) {
                if (speed < maxSpeed) {
                    speed++;
                }
            } else if (delta < 0) {
                if (speed > 1) {
                    speed--;
                }
            }

            updateActionBar();
        }

        @org.bukkit.event.EventHandler
        public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
            if (!event.getPlayer().equals(player) || !isActive) return;

            if (event.getAction().toString().contains("LEFT_CLICK")) {
                String direction = getFacingDirection();
                moveVehicle(direction);
            }
        }

        private String getFacingDirection() {
            float yaw = player.getLocation().getYaw();
            if (yaw < 0) yaw += 360;

            if (yaw >= 45 && yaw < 135) return "E";
            else if (yaw >= 135 && yaw < 225) return "S";
            else if (yaw >= 225 && yaw < 315) return "W";
            else return "N";
        }

        @org.bukkit.event.EventHandler
        public void onPlayerSneak(org.bukkit.event.player.PlayerToggleSneakEvent event) {
            if (!event.getPlayer().equals(player) || !isActive) return;

            if (event.isSneaking()) {
                player.sendMessage("§6[Steering] Řízení bylo ukončeno.");
                isActive = false;
                saveVehicleData();
            }
        }
    }

    private Location safeDeserializeLocation(String serialized, Location fallback) {
        try {
            if (serialized == null || serialized.isEmpty()) return fallback;

            String[] parts = serialized.split(";");
            return new Location(
                    plugin.getServer().getWorld(parts[0]),
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2]),
                    Double.parseDouble(parts[3])
            );
        } catch (Exception e) {
            Bukkit.getLogger().warning("Chyba při deserializaci umístění: " + serialized);
            e.printStackTrace();
            return fallback;
        }
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + ";" + location.getBlockX() + ";" + location.getBlockY() + ";" + location.getBlockZ();
    }
}
