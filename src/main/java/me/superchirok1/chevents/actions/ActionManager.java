package me.superchirok1.chevents.actions;

import me.superchirok1.chevents.ChEvents;
import me.superchirok1.chevents.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Optional;

import static me.superchirok1.chevents.ChEvents.econ;
import static me.superchirok1.chevents.ChEvents.ppAPI;

public class ActionManager {

    public static void execute(Player player, String raw) {

        raw = placeholders(player, raw);

        if (raw.startsWith("[msg]")) {
            player.sendMessage(Utils.format(player, raw.replace("[msg] ", "").trim()));
        } else if (raw.startsWith("[broadcast]")) {
            Bukkit.broadcastMessage(Utils.format(raw.replace("[broadcast] ", "")));
        } else if (raw.startsWith("[console]")) {
            String cmd = raw.replace("[console] ", "");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else if (raw.startsWith("[player]")) {
            String cmd = raw.replace("[player] ", "").trim();
            player.performCommand(cmd);
        } else if (raw.startsWith("[sound]")) {
            String argsPart = raw.replace("[sound] ", "").trim();
            String[] parts = argsPart.split("\\|");

            if (parts.length >= 3) {
                String soundName = parts[0].toUpperCase().replace(".","_");
                float volume = Float.parseFloat(parts[1]);
                float pitch = Float.parseFloat(parts[2]);

                player.playSound(player.getLocation(), Sound.valueOf(soundName), volume, pitch);
            }
        } else if (raw.startsWith("[title]")) {
            String argsPart = raw.replace("[title] ", "").trim();
            String[] parts = argsPart.split("\\|");

            if (parts.length >= 5) {
                String title = Utils.format(player, parts[0]);
                String subtitle = Utils.format(player, parts[1]);
                int fadein = Integer.parseInt(parts[2]);
                int stay = Integer.parseInt(parts[3]);
                int fadeout = Integer.parseInt(parts[4]);

                player.sendTitle(title, subtitle, fadein, stay, fadeout);
            }
        } else if (raw.startsWith("[actionbar]")) {
            String msg = raw.replace("[actionbar]", "").trim();
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(Utils.format(player, msg)));
        } else if (raw.startsWith("[tp]")) {
            String tp = raw.replace("[tp] ", "").trim();
            String[] parts = tp.split("\\|");

            if (parts.length >= 3) {
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);
                double z = Double.parseDouble(parts[2]);

                World world = player.getLocation().getWorld();
                Location location = new Location(world, x, y, z);
                player.teleport(location);
            }
        } else if (raw.startsWith("[kick]")) {

            String msg = raw.replace("[kick]", "").trim();
            String value = Utils.format(player, msg);
            player.kickPlayer(value);

        } else if (raw.startsWith("[effect]")) {
            String msg = raw.replace("[effect]", "").trim();
            String[] parts = msg.split("\\|");

            try {
                if (parts.length >= 3) {
                    String effectName = parts[0].trim().toUpperCase();
                    int amplifier = Integer.parseInt(parts[1].trim());
                    int duration = Integer.parseInt(parts[2].trim());

                    PotionEffectType type = PotionEffectType.getByName(effectName);
                    if (type != null) {
                        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                    } else {
                        Bukkit.getLogger().warning("Неизвестный эффект: " + effectName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (raw.startsWith("[velocity]")) {

            String msg = raw.replace("[velocity]", "").trim();
            String[] parts = msg.split("\\|");

            try {
                if (parts.length >= 3) {
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    double z = Double.parseDouble(parts[2]);


                    player.setVelocity(player.getVelocity().setZ(z).setY(y).setX(x));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (raw.startsWith("[give]")) {

            String value = raw.replace("[give]", "").trim();
            String[] parts = value.split("\\|");

            try {
                if (parts.length >= 2) {
                    String material = parts[0].toUpperCase();
                    int amount = Integer.parseInt(parts[1]);

                    player.getInventory().addItem(new ItemStack(Material.valueOf(material), amount));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (raw.startsWith("[take]")) {

            String value = raw.replace("[take]", "").trim();
            String[] parts = value.split("\\|");

            try {
                if (parts.length >= 2) {
                    String material = parts[0];
                    int amount = Integer.parseInt(parts[1]);

                    player.getInventory().removeItem(new ItemStack(Material.valueOf(material), amount));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        } else if (raw.startsWith("[vault_give]")) {

            String value = raw.replace("[vault_give]", "").trim();
            EconomyResponse r = econ.depositPlayer(player, Double.parseDouble(value));

        } else if (raw.startsWith("[vault_take]")) {

            String value = raw.replace("[vault_take]", "").trim();
            EconomyResponse r = econ.withdrawPlayer(player, Double.parseDouble(value));

        } else if (raw.startsWith("[playerp_give]")) {

            String value = raw.replace("[playerp_give]", "").trim();
            ppAPI.give(player.getUniqueId(), Integer.parseInt(value));

        } else if (raw.startsWith("[playerp_take]")) {

            String value = raw.replace("[playerp_take]", "").trim();
            ppAPI.take(player.getUniqueId(), Integer.parseInt(value));

        } else if (raw.startsWith("[particle]")) {
            String value = raw.replace("[particle]", "").trim();
            String[] parts = value.split("\\|");

            try {
                if (parts.length >= 9) {
                    double x = Double.parseDouble(parts[0]);
                    double y = Double.parseDouble(parts[1]);
                    double z = Double.parseDouble(parts[2]);

                    String id = parts[3];
                    int count = Integer.parseInt(parts[4]);

                    double offsetX = Double.parseDouble(parts[5]);
                    double offsetY = Double.parseDouble(parts[6]);
                    double offsetZ = Double.parseDouble(parts[7]);
                    double extra = Double.parseDouble(parts[8]);

                    World world = player.getWorld();

                    world.spawnParticle(
                            Particle.valueOf(id),
                            x, y, z,
                            count,
                            offsetX, offsetY, offsetZ,
                            extra
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (raw.startsWith("[gamemode]")) {
            String value = raw.replace("[gamemode]", "").trim();

            try {
                player.setGameMode(GameMode.valueOf(value.toUpperCase()));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else if (raw.startsWith("[heal]")) {
            player.setHealth(player.getMaxHealth());
        } else if (raw.startsWith("[feed]")) {
            player.setFoodLevel(20);
            player.setSaturation(20f);
        } else if (raw.startsWith("[sethealth]")) {
            String value = raw.replace("[sethealth]", "").trim();

            try {
                player.setHealth(Integer.parseInt(value));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else if (raw.startsWith("[setfood]")) {
            String value = raw.replace("[setfood]", "").trim();

            try {
                player.setFoodLevel(Integer.parseInt(value));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        } else if (raw.startsWith("[xp_give]")) {
            String value = raw.replace("[xp_give]", "").trim();

            try {
                player.giveExp(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (raw.startsWith("[xp_take]")) {
            String value = raw.replace("[xp_take]", "").trim();

            try {
                player.giveExp(-Integer.parseInt(value));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (raw.startsWith("[level_give]")) {
            String value = raw.replace("[level_give]", "").trim();

            try {
                player.giveExpLevels(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (raw.startsWith("[level_take]")) {
            String value = raw.replace("[level_take]", "").trim();

            try {
                player.giveExpLevels(-Integer.parseInt(value));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    private static String placeholders(Player player, String text) {
        return text
                .replace("%name%", player.getName())
                .replace("%x%", Double.toString(player.getLocation().getX()))
                .replace("%y%", Double.toString(player.getLocation().getY()))
                .replace("%z%", Double.toString(player.getLocation().getZ()))
                .replace("%world%", player.getWorld().getName())
                .replace("%health%", String.format("%.1f", player.getHealth()))
                .replace("%max_health%", String.format("%.1f", player.getMaxHealth()))
                .replace("%uuid%", String.valueOf(player.getUniqueId()));
    }


}
