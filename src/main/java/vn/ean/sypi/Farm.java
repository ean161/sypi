package vn.ean.sypi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Farm implements Listener {
    private final HashMap<Location, Float> unHarvest = new HashMap<>();

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

    }

    @EventHandler
    public void onHarvest(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        Player player = event.getPlayer();
        double radius = 1.5;
        int beeCount = 0;

        if ((block.getType() == Material.MELON || block.getType() == Material.PUMPKIN) && unHarvest.containsKey(block.getLocation())) {
            for (Entity entity : block.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                if (entity instanceof Bee) {
                    Bee bee = (Bee) entity;

                    boolean isFlying = !bee.isOnGround();
                    boolean notLeashed = !bee.isLeashed();

                    if (isFlying && notLeashed) {
                        beeCount++;
                        bee.setHealth(Math.max(0.0, bee.getHealth() - 1.0));
                    }
                }
            }

            float size = unHarvest.get(block.getLocation()) * (beeCount > 0 && (Lib.rand(1, 5) < 2) ? (beeCount + 1) : 1);
            
            ItemStack customMelon = new ItemStack(block.getType(), 1);
            ItemMeta meta = customMelon.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add(String.format("§r§fCân nặng %.1fkg", size));
            meta.setLore(lore);

            customMelon.setItemMeta(meta);

            block.getWorld().dropItemNaturally(block.getLocation(), customMelon);

            unHarvest.remove(block.getLocation());
            event.setDropItems(false);
        }
    }

    // @EventHandler
    // public void onDropSell(PlayerDropItemEvent event) {
    //     ItemStack item = event.getItemDrop().getItemStack();
    //     int amount = item.getAmount();

    //     Player player = event.getPlayer();
        
    //     if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
    //         List<Component> lore = item.getItemMeta().lore();
    //         if (lore != null) {
    //             double size = 0;
    //             for (Component line : lore) {
    //                 String plain = LegacyComponentSerializer.legacySection().serialize(line);
    //                 if (plain.contains("Cân nặng"))
    //                     size = Double.parseDouble(plain.split("Cân nặng ")[1].split("kg")[0]);
    //             }

    //             if (size != 0) {
    //                 double total = calcPrice(size) * amount;
    //                 Lib.getEcon().depositPlayer(player, total);
    //                 player.sendMessage(String.format("§r§rBán thành công %d quả §e§l%.1fkg§f ($%.1f x %d = §a§l$%.1f§r§f)", amount, size, calcPrice(size), amount, total));
    //                 event.getItemDrop().remove();;
    //             }
    //         }
    //     }
    // }

    @EventHandler
    public void onGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        
        if (event.getNewState().getType() == Material.MELON || event.getNewState().getType() == Material.PUMPKIN) {
            float size = getRandSize();
            unHarvest.put(block.getLocation(), size);
        }
    }

    public void onExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN)
                unHarvest.remove(block.getLocation());
        }
    }

    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            if (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN)
                unHarvest.remove(block.getLocation());
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN)
                unHarvest.remove(block.getLocation());
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN)
                unHarvest.remove(block.getLocation());
        }
    }

    public float getRandSize() {
        Random random = new Random();
        float rand = random.nextFloat() * 100000;

        if (rand == 1610) 
            return Lib.rand(10, 50);
        else if (rand < 50)
            return Lib.rand(5, 9.9f);
        else if (rand < 10000)
            return Lib.rand(0.1f, 4.9f);
        
        return Lib.rand(0.1f, 1f);
    }

    public static double calcPrice(double size) {
        double basePrice = 10;
        double exponent = 3;
        return basePrice * Math.pow(size, exponent);
    }
}
