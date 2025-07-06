package vn.ean.sypi;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class App extends JavaPlugin implements Listener {
    private static App instance;

    Farm farm = new Farm();
    Sicbo sicbo = new Sicbo();

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Lib.linkEcon();
        sicbo.init();
        Bukkit.getPluginManager().registerEvents(this, this);

        Bukkit.getPluginManager().registerEvents(farm, this);
        Bukkit.getPluginManager().registerEvents(sicbo, this);

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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("tx") && sender instanceof Player) {
            if (args[0].equalsIgnoreCase("soi")) {
                sicbo.getHistory((Player) sender);
                return true;
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("tai") || args[0].equalsIgnoreCase("xiu")) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch (Exception ex) {
                        sender.sendMessage("Số tiền cược là số nguyên dương và tối thiểu là $1000");
                        return false;
                    }

                    sicbo.bet((Player) sender, args[0].toLowerCase(), amount);
                    return true;
                }
            }
            sender.sendMessage("Cách cược tài xỉu: /tx <tai/xiu/soi> [số tiền cược]");
        }

        return false;
    }
}
