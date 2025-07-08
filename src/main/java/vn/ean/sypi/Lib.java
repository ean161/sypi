package vn.ean.sypi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;

public class Lib {
    private static Economy econ;

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

    public static boolean hasLicense() {
        try {
            URL url = new URL("https://ean.vn/models/project/sypi");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
            );
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString().equals("true");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void linkEcon() {
        if (econ != null) return;

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
    }

    public static Economy getEcon() {
        return econ;
    }

    public static float rand(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    public static String formatNum(String input) {
        try {
            long number = Long.parseLong(input);
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');

            DecimalFormat formatter = new DecimalFormat("#,###", symbols);
            return formatter.format(number);
        } catch (NumberFormatException e) {
            return input;
        }
    }

    public static int parseSafeInt(String input) {
        if (input == null || input.trim().isEmpty())
            return 0;

        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
