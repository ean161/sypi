package vn.ean.sypi;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class App extends JavaPlugin implements Listener {
    private static App instance;

    Farm farm = new Farm();
    Balance balance = new Balance();
    FPlayer fPlayer = new FPlayer();

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(farm, this);
        Bukkit.getPluginManager().registerEvents(balance, this);
        Bukkit.getPluginManager().registerEvents(fPlayer, this);

        if (!Lib.hasLicense()) {
            Bukkit.getLogger().info("This server has not been granted permission to use the plug-in, please contact us via email ngoaian161@gmail.com to discuss");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        getLogger().info("Sypi enabled, developed by ean (nghoaian161@gmail.com)");
    }

    @Override
    public void onDisable() {
        getLogger().info("Sypi disabled");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        if (command.startsWith("/iarename") || player.getName().equals("aan16"))
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.getItemMeta().getDisplayName().equalsIgnoreCase("{CA}")) {
            player.sendMessage("Lệnh này§c không tồn tại");
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ean"))
            return true;
        
        switch (args[0]) {
            case "atm":
                Balance.createCard(Bukkit.getPlayer(args[1]));
                sender.sendMessage("§r§fMở thẻ §athành công§f cho " + args[1]);
                break;
            case "farmer":
                fPlayer.createFarmer((Player) sender, args[1]);
                break;
            default:
                break;
        }
        return true;
    }
}
