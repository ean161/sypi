package vn.ean.sypi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class Sicbo implements Listener {
    private Thread thread;
    private final AtomicInteger seconds = new AtomicInteger(120);

    HashMap<Player, HashMap<String, Integer>> bets = new HashMap<>();
    ArrayList<String> history = new ArrayList<>();

    public void init() {
        if (thread != null && thread.isAlive())
            thread.interrupt();

        thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(1000);
                    int current = seconds.decrementAndGet();

                    for (Player player : bets.keySet()) {
                        HashMap<String, Integer> bet = bets.get(player);
                        String type = bet.keySet().iterator().next();
                        int amount = bet.get(type);

                        player.sendActionBar(Component.text(String.format("§f$§e§l%d§r§f (cho %s), mở bát sau %d giây", amount, convertTypeReadable(type), current)));
                    }

                    if (current == 5)
                        Bukkit.broadcast(Component.text("§fPhiên cược đã đóng, mở bát sau §a5§f giây nữa"));
                    else if (current == 35)
                        Bukkit.broadcast(Component.text("§fNgừng nhận cược tài xỉu sau §a30§f giây nữa"));

                    if (current == 0) {
                        int d1 = (int) Lib.rand(1, 7);
                        int d2 = (int) Lib.rand(1, 7);
                        int d3 = (int) Lib.rand(1, 7);
                        int total = d1 + d2 + d3;
                        String result = (total <= 10 ? "xiu" : "tai");

                        Bukkit.broadcast(Component.text(String.format("§fKết quả xúc xắc 1 là §a§l%d§r§f §7(§f%d §7- ? §7- ?)", d1, d1)));
                        Thread.sleep(5000);
                        Bukkit.broadcast(Component.text(String.format("§fKết quả xúc xắc 2 là §a§l%d§r§f §7(§f%d §7- §f%d §7- ?)", d2, d1, d2)));
                        Thread.sleep(7000);
                        Bukkit.broadcast(Component.text(String.format("§fKết quả xúc xắc 3 là §a§l%d§r§f §7(§f%d §7- §f%d §7- §f%d§7)", d3, d1, d2, d3)));
                        Thread.sleep(1000);
                        Bukkit.broadcast(Component.text(String.format("§fKết quả là §e§l%s§r§f (%d nút)", (result.equals("tai") ? "TÀI" : "XỈU"), total)));

                        int maxAmount = 0;
                        String bigWinner = "";
                        for (Player player : bets.keySet()) {
                            HashMap<String, Integer> bet = bets.get(player);
                            String type = bet.keySet().iterator().next();
                            int amount = bet.get(type);

                            if (!type.equals(result))
                                continue;

                            if (amount > maxAmount) {
                                maxAmount = amount;
                                bigWinner = player.getName();
                            }
                            
                            winnerPrac(player);

                            Lib.getEcon().depositPlayer(player, (amount * 2) - (amount * 2 * 0.1));
                            player.sendMessage(String.format("Chúc mừng bạn đã thắng §e§l%d§r§f từ phiên tài xỉu", amount));
                        }

                        if (maxAmount > 0)
                            Bukkit.broadcast(Component.text(String.format("§fChúc mừng đại gia §a§l%s§r§f thắng lớn với số tiền $§e§l%d", bigWinner, maxAmount)));

                        bets.clear();
                        history.add(result.substring(0, 1).toUpperCase());

                        Thread.sleep(500000);
                        seconds.set(120);
                        Bukkit.broadcast(Component.text("Phiên cược mới §abắt đầu§f, dùng lệnh §7/tx <tai/xiu/soi> [money]§f để cược/soi cầu"));
                    }
                }
            } catch (InterruptedException e) {}
        });
        thread.start();
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
    }

    public void bet(Player player, String type, int amount) {
        if (amount < 1000) {
            player.sendMessage("Tiền cược tối thiểu từ $1000");
            return;
        } else if (Lib.getEcon().getBalance(player) < amount) {
            player.sendMessage("Bạn không đủ tiền để cược");
            return;
        } else if (seconds.get() <= 5) {
            player.sendMessage("Phiên này đã ngừng nhận cược");
            return;
        } else if (bets.containsKey(player)) {
            player.sendMessage("Bạn không thể cược 1 phiên 2 lần");
            return;
        }

        Lib.getEcon().withdrawPlayer(player, amount);
        player.sendMessage(String.format("Cược thành công $§a%d§f cho %s", amount, convertTypeReadable(type)));
        Bukkit.broadcast(Component.text(String.format("§a%s§r§f đã tham gia phiên cược %s ($%d)", player.getName(), convertTypeReadable(type), amount)));
        bets.put(player, new HashMap<>());
        bets.get(player).put(type, amount);
    }

    public String convertTypeReadable(String type) {
        return (type.equals("tai") ? "TÀI" : "XỈU");
    }

    public void getHistory(Player player) {
        String his = "";
        List<String> last15 = history.subList(Math.max(0, history.size() - 15), history.size());
        
        for (String res : last15) {
            his += String.format("§%s%s ", (res.equals("T") ? "c" : "f"), res);
        }

        player.sendMessage(String.format("Soi cầu tài xỉu: %s", (his.isEmpty() ? "§7Chưa có kết quả để soi" : his)));
    }

    public void winnerPrac(Player player) {
        Bukkit.getScheduler().runTask(App.getInstance(), () -> {
            Firework firework = player.getWorld().spawn(player.getLocation(), Firework.class);

            FireworkMeta meta = firework.getFireworkMeta();
            meta.addEffect(FireworkEffect.builder()
                .withColor(Color.LIME)
                .withFade(Color.YELLOW)
                .with(Type.BALL_LARGE)
                .flicker(true)
                .trail(true)
                .build());
            meta.setPower(0);
            firework.setFireworkMeta(meta);

            Bukkit.getScheduler().runTaskLater(App.getInstance(), firework::detonate, 1L);
        });
    }
} 
