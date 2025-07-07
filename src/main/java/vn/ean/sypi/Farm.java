package vn.ean.sypi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Farm implements Listener {
    private final HashMap<Location, Float> unHarvest = new HashMap<>();

    @EventHandler
    public void onHarvest(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (
            (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN)
            && unHarvest.containsKey(block.getLocation())
        ) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!(item == null || item.getType() == Material.AIR)) {
                unHarvest.remove(block.getLocation());
                return;
            }

            float size = unHarvest.get(block.getLocation());

            ItemStack customMelon = new ItemStack(block.getType(), 1);
            ItemMeta meta = customMelon.getItemMeta();

            meta.setDisplayName(String.format(
                "§r%s §f%s §e§l%.1fkg§r",
                getSizeTag(size),
                (block.getType() == Material.MELON ? "Dưa hấu" : "Bí ngô"),
                size
            ));

            List<String> lore = new ArrayList<>();
            lore.add(String.format("§r§fCân nặng %.1fkg", size));
            lore.add("§r§7Thả xuống để bán");
            lore.add("§r§7Vật phẩm rơi ra khi thu hoạch bằng tay");
            meta.setLore(lore);

            customMelon.setItemMeta(meta);

            block.getWorld().dropItemNaturally(block.getLocation(), customMelon);

            unHarvest.remove(block.getLocation());
            event.setDropItems(false);
        }
    }

    @EventHandler
    public void onDropSell(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        int amount = item.getAmount();

        Player player = event.getPlayer();
        
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<Component> lore = item.getItemMeta().lore();
            if (lore != null) {
                double size = 0;
                for (Component line : lore) {
                    String plain = LegacyComponentSerializer.legacySection().serialize(line);
                    if (plain.contains("Cân nặng"))
                        size = Double.parseDouble(plain.split("Cân nặng ")[1].split("kg")[0]);
                }

                if (size != 0) {
                    double total = calcPrice(size) * amount;
                    Lib.getEcon().depositPlayer(player, total);
                    player.sendMessage(String.format("§r§rBán thành công %d quả §e§l%.1fkg§f ($%.1f x %d = §a§l$%.1f§r§f)", amount, size, calcPrice(size), amount, total));
                    event.getItemDrop().remove();;
                }
            }
        }
    }

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

    public String getSizeTag(float size) {
        if (size >= 10)
            return "§c§lĐỘT BIẾN§r";
        else if (size >= 5)
            return "§b§lLỚN§r";

        return "§a§lTHƯỜNG§r";
    }

    public static double calcPrice(double size) {
        double basePrice = 10;
        double exponent = 3;
        return basePrice * Math.pow(size, exponent);
    }
}
