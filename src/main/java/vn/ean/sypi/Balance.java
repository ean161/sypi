package vn.ean.sypi;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Balance implements Listener {
    public static void createCard(Player player) {
        CustomStack customStack = CustomStack.getInstance("eanmc:atm");
        ItemStack item = customStack.getItemStack();
        ItemMeta meta = item.getItemMeta();
        
        String cardNum = String.format("%d%c", (int) Lib.rand(111111, 999999), (char) ('A' + (int) Lib.rand(1, 27)));
        if (meta != null) {
            meta.setDisplayName("§r§fThẻ §bngân hàng");
            meta.setLore(Arrays.asList(
                "§r§fChủ thẻ: " + player.getName(),
                "§r§fSố thẻ: " + cardNum
            ));
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            item.setItemMeta(meta);
        }

        Lib.setConfig("balance", String.format("%s.author", cardNum), player.getName());
        Lib.setConfig("balance", String.format("%s.amount", cardNum), "0");

        player.getInventory().addItem(item);
    }

    @EventHandler
    public void onCheckBalance(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return;

        String cardNum = "";
        List<Component> lore = item.getItemMeta().lore();
        if (lore != null) {
            for (Component line : lore) {
                String plain = LegacyComponentSerializer.legacySection().serialize(line);
                if (!plain.contains(": "))
                    continue;

                cardNum = plain.split(": ")[1];
            }
        }

        player.sendMessage("Số dư hiện tại của thẻ là §e" + Balance.getBalance(cardNum, true));
    }

    public static String getBalance(String cardNum, boolean hasFormat) {
        String amount = Lib.getConfig("balance", cardNum + ".amount");
        if (amount.isEmpty())
            amount = "0";

        if (hasFormat)
            return Lib.formatNum(amount);
        return amount;
    }
}
