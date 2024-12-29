package cz.padik.mPolitan.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class ToolVisualizer implements Listener {

    private final HashMap<UUID, Arena> userArenas = new HashMap<>();
    private final Plugin plugin;

    public ToolVisualizer(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // Kontrola, zda hráč drží zlatou motyku
        if (player.getInventory().getItemInMainHand().getType() == Material.GOLDEN_HOE) {
            // Zákaz rozbíjení bloků nebo orání půdy
            event.setCancelled(true);

            // Zpracování kliknutí
            if (clickedBlock != null && (event.getAction().toString().contains("LEFT_CLICK") || event.getAction().toString().contains("RIGHT_CLICK"))) {
                Location blockLocation = clickedBlock.getLocation();

                // Aktualizace levého nebo pravého bodu
                Arena arena = userArenas.getOrDefault(player.getUniqueId(), new Arena(player, null, null));
                if (event.getAction().toString().contains("LEFT_CLICK")) {
                    arena.setPoint1(blockLocation);
                    player.sendMessage("§6[Workshop] §aLevý bod nastaven na: " + formatLocation(blockLocation));
                } else {
                    arena.setPoint2(blockLocation);
                    player.sendMessage("§6[Workshop] §aPravý bod nastaven na: " + formatLocation(blockLocation));
                }

                // Uložení arény
                userArenas.put(player.getUniqueId(), arena);

                // Zkontroluj, zda jsou oba body nastaveny
                if (arena.isComplete()) {
                    player.sendMessage("§6[Workshop] §eDílna byla vytvořena.");
                    arena.startVisualization();
                }
            } else {
                player.sendMessage("§6[Workshop] §cNemůžeš používat zlatou motyku pro jiné akce.");
            }
        }
    }

    private String formatLocation(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }

    public boolean hasPlayerSelection(Player player) {
        return userArenas.containsKey(player.getUniqueId()) && userArenas.get(player.getUniqueId()).isComplete();
    }

    public Location[] getPlayerSelection(Player player) {
        if (!userArenas.containsKey(player.getUniqueId())) {
            return null;
        }
        Arena arena = userArenas.get(player.getUniqueId());
        return new Location[]{arena.getPoint1(), arena.getPoint2()};
    }

    private class Arena {
        private final Player player;
        private Location point1;
        private Location point2;
        private BukkitRunnable particleTask;
        private BukkitRunnable expirationTask;

        public Arena(Player player, Location point1, Location point2) {
            this.player = player;
            this.point1 = point1;
            this.point2 = point2;
        }

        public Location getPoint1() {
            return point1;
        }

        public Location getPoint2() {
            return point2;
        }

        public void setPoint1(Location point1) {
            this.point1 = point1;
            restartExpiration();
        }

        public void setPoint2(Location point2) {
            this.point2 = point2;
            restartExpiration();
        }

        public boolean isComplete() {
            return point1 != null && point2 != null;
        }

        public void startVisualization() {
            if (particleTask != null) {
                particleTask.cancel();
            }
            if (expirationTask != null) {
                expirationTask.cancel();
            }

            // Zobrazení částic každou sekundu
            particleTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || player.getLocation().distance(point1.clone().add(point2.clone()).multiply(0.5)) > 15) {
                        player.sendMessage("§6[Workshop] §cOdešel/a jsi od Dílny moc daleko.");
                        cancel();
                        userArenas.remove(player.getUniqueId());
                        return;
                    }
                    visualizeArea();
                }
            };
            particleTask.runTaskTimer(plugin, 0, 20); // Každých 1 sekundu

            // Nastavení časovače na automatické zrušení po 2 minutách
            restartExpiration();
        }

        private void restartExpiration() {
            if (expirationTask != null) {
                expirationTask.cancel();
            }
            expirationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage("§6[Workshop] §cDílna byla zrušena po 2 minutách neaktivity.");
                    userArenas.remove(player.getUniqueId());
                    if (particleTask != null) {
                        particleTask.cancel();
                    }
                }
            };
            expirationTask.runTaskLater(plugin, 2400); // 2 minuty (2400 ticků)
        }

        private void visualizeArea() {
            // Určení minimálních a maximálních souřadnic
            int minX = Math.min(point1.getBlockX(), point2.getBlockX());
            int maxX = Math.max(point1.getBlockX(), point2.getBlockX()) + 1; // Rozšíření na East
            int minY = Math.min(point1.getBlockY(), point2.getBlockY());
            int maxY = Math.max(point1.getBlockY(), point2.getBlockY()) + 1; // Rozšíření nahoru
            int minZ = Math.min(point1.getBlockZ(), point2.getBlockZ());
            int maxZ = Math.max(point1.getBlockZ(), point2.getBlockZ()) + 1; // Rozšíření na South

            // Spodní čáry
            drawEdge(minX, minY, minZ, maxX, minY, minZ); // Spodní čára - X (North)
            drawEdge(minX, minY, maxZ, maxX, minY, maxZ); // Spodní čára - X (South)

            // Horní čáry
            drawEdge(minX, maxY, minZ, maxX, maxY, minZ); // Horní čára - X (North)
            drawEdge(minX, maxY, maxZ, maxX, maxY, maxZ); // Horní čára - X (South)

            // Levá a pravá svislá čára
            drawEdge(minX, minY, minZ, minX, maxY, minZ); // Levá svislá čára (West)
            drawEdge(minX, minY, maxZ, minX, maxY, maxZ); // Levá svislá čára (West)
            drawEdge(maxX, minY, minZ, maxX, maxY, minZ); // Pravá svislá čára (East)
            drawEdge(maxX, minY, maxZ, maxX, maxY, maxZ); // Pravá svislá čára (East)

            // Boční čáry na osách Z
            drawEdge(minX, minY, minZ, minX, minY, maxZ); // Levá čára (West)
            drawEdge(maxX, minY, minZ, maxX, minY, maxZ); // Pravá čára (East)
            drawEdge(minX, maxY, minZ, minX, maxY, maxZ); // Horní levá čára (West)
            drawEdge(maxX, maxY, minZ, maxX, maxY, maxZ); // Horní pravá čára (East)
        }

        private void drawEdge(int x1, int y1, int z1, int x2, int y2, int z2) {
            // Výpočet směru a délky čáry
            double dx = (x2 - x1) / 100.0; // Rozdělení čáry na 100 částic
            double dy = (y2 - y1) / 100.0;
            double dz = (z2 - z1) / 100.0;

            for (int i = 0; i <= 100; i++) {
                double x = x1 + (dx * i);
                double y = y1 + (dy * i);
                double z = z1 + (dz * i);

                Location particleLocation = new Location(point1.getWorld(), x, y, z);
                point1.getWorld().spawnParticle(Particle.REVERSE_PORTAL, particleLocation, 1, 0, 0, 0, 0);
            }
        }

    }
}
