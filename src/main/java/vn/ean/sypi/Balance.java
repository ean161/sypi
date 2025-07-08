package vn.ean.sypi;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.lone.itemsadder.api.CustomStack;

public class Balance implements Listener {
    public static void createCard(Player player) {
        CustomStack customStack = CustomStack.getInstance("eanmc:atm");
        ItemStack item = customStack.getItemStack();
        ItemMeta meta = item.getItemMeta();

        int cardNum = (int) Lib.rand(111111111, 999999999);
        if (meta != null) {
            meta.setDisplayName("§r§fThẻ §bngân hàng");
            meta.setLore(Arrays.asList(
                "§r§fChủ thẻ: " + player.getName(),
                "§r§fSố thẻ: " + cardNum
            ));
            item.setItemMeta(meta);
        }

        Lib.setConfig("balance", String.format("%d.author", cardNum), player.getName());
        Lib.setConfig("balance", String.format("%d.amount", cardNum), "0");

        player.getInventory().addItem(item);
    }
}
