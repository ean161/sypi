package vn.ean.sypi;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

public class Lib {
    public static void sendMessage(Player player, String message) {
        player.sendMessage(
            ChatColor.translateAlternateColorCodes('&', String.format(
                "%s %s",
                Lib.getConfig("config", "prefix"),
                message.replace("{player}", player.getName())
            ))
        );
    
        return;
    }

    public static FileConfiguration config(String file) {
        File configFile = new File(App.getInstance().getDataFolder(), file + ".yml");

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static void setConfig(String file, String key, String value) {
        FileConfiguration config = config(file);
        config.set(key, value);
        try {
            config.save(new File(App.getInstance().getDataFolder(), file + ".yml"));
        } catch (Exception ex) {}
    }

    public static String getConfig(String file, String key) {
        FileConfiguration config = config(file);

        if (!config.contains(key))
            return "";
        return config.getString(key);
    }
}
