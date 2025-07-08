package vn.ean.sypi;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;

public class FPlayer implements Listener {
    public static void createFarmer(Player player, String name) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, name);
        npc.data().setPersistent("type", "farmer");
        npc.spawn(player.getLocation());

        FPlayer.farmerWork(npc);
    }

    private static void farmerWork(NPC npc) {
        if (npc.getEntity() instanceof Player) {
            Player p = (Player) npc.getEntity();
            p.getInventory().setItemInMainHand(new ItemStack(Material.WOODEN_AXE));
        }

        Bukkit.getScheduler().runTaskTimer(App.getInstance(), () -> {
            if (!npc.isSpawned()) return;

            Location current = npc.getEntity().getLocation();
            Block melon = findHarvest(current, 4);

            if (melon != null) {
                npc.getNavigator().setTarget(melon.getLocation());

                Bukkit.getScheduler().runTaskLater(App.getInstance(), () -> {
                    if (npc.isSpawned() && npc.getEntity() instanceof Player) {
                        Player bot = (Player) npc.getEntity();
                        bot.setVelocity(bot.getVelocity().setY(0.42));
                    }
                }, 15L);

                Bukkit.getScheduler().runTaskLater(App.getInstance(), () -> {
                    if (npc.isSpawned() && melon.getLocation().distance(npc.getEntity().getLocation()) < 2) {
                        for (int i = 0; i < 5; i++) {
                            int delay = i * 4;
                            Bukkit.getScheduler().runTaskLater(App.getInstance(), () -> {
                                if (npc.isSpawned() && npc.getEntity() instanceof Player) {
                                    ((Player) npc.getEntity()).swingMainHand();
                                }
                            }, delay);
                        }
                        
                        Bukkit.getScheduler().runTaskLater(App.getInstance(), () -> {
                            if (!npc.isSpawned())
                                return;
                            if (melon.getLocation().distance(npc.getEntity().getLocation()) >= 2)
                                return;

                            melon.setType(Material.AIR);
                            World world = npc.getEntity().getWorld();
                            Location loc = melon.getLocation().add(0.5, 0.5, 0.5);

                            world.playSound(loc, "entity.player.attack.crit", 1f, 1f);
                            world.playSound(loc, "block.wood.break", 1.0f, 1.0f);
                        }, 24L);
                    }
                }, 40L);
            }
        }, 0L, 250L);
    }

    private static Block findHarvest(Location base, int radius) {
        World world = base.getWorld();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Block block = world.getBlockAt(base.clone().add(dx, dy, dz));
                    if (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN) {
                        return block;
                    }
                }
            }
        }
        return null;
    }


    @EventHandler
    public void onNPCSpawn(NPCSpawnEvent event) {
        NPC npc = event.getNPC();

        if (!npc.data().has("type")) return;

        String type = npc.data().get("type");
        if ("farmer".equals(type)) {
            FPlayer.farmerWork(npc);
        }
    }
}
