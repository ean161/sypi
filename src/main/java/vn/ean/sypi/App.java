package vn.ean.sypi;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements Listener {
    private static App instance;

    Farm farm = new Farm();
    Balance balance = new Balance();

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(farm, this);
        Bukkit.getPluginManager().registerEvents(balance, this);

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
        // command.getName().equalsIgnoreCase

        return false;
    }
}
