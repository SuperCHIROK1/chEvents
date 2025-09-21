package me.superchirok1.chevents.conditions;

import me.clip.placeholderapi.PlaceholderAPI;
import me.superchirok1.chevents.ChEvents;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import static me.superchirok1.chevents.ChEvents.econ;
import static me.superchirok1.chevents.ChEvents.ppAPI;

public class Conditions {

    public static boolean check(Player player, ConfigurationSection section, String message, ChEvents plugin) {

        ConfigurationSection conditions = section.getConfigurationSection("conditions");
        if (conditions == null) return true;

        String world = conditions.getString("world", "");
        if (!world.isEmpty() && !player.getWorld().getName().equalsIgnoreCase(world)) return false;

        String permission = conditions.getString("permission", "");
        if (!permission.isEmpty()) {
            if (permission.startsWith("!")) {
                if (player.hasPermission(permission.substring(1))) return false;
            } else {
                if (!player.hasPermission(permission)) return false;
            }
        }

        String name = conditions.getString("name", "");
        if (!name.isEmpty() && !name.equalsIgnoreCase(player.getName())) return false;

        if (message != null) {
            String msgEq = conditions.getString("message");
            if (msgEq != null && !msgEq.equalsIgnoreCase(message)) return false;

            String msgContains = conditions.getString("message-contains");
            if (msgContains != null && !message.contains(msgContains)) return false;
        }

        ConfigurationSection has = conditions.getConfigurationSection("has");
        if (has != null) {
            if (has.contains("vault") && econ.getBalance(player) < has.getDouble("vault", 0)) return false;

            if (has.contains("playerpoints") && ppAPI.look(player.getUniqueId()) < has.getInt("playerpoints", 0)) return false;

            if (has.contains("item")) {
                String materialName = has.getString("item", "");
                if (!materialName.isEmpty() && !player.getInventory().contains(Material.matchMaterial(materialName))) return false;
            }
        }

        ConfigurationSection varsSection = conditions.getConfigurationSection("vars");
        if (varsSection != null) {
            for (String key : varsSection.getKeys(false)) {
                ConfigurationSection var = varsSection.getConfigurationSection(key);
                if (var == null) continue;

                Object input = PlaceholderAPI.setPlaceholders(player, var.get("input").toString());
                Object output = var.get("output").toString();
                if (!input.equals(output)) return false;
            }
        }

        String time = conditions.getString("time", "");
        if (!time.isEmpty()) {
            try {
                String[] parts = time.split("-");
                if (parts.length == 2) {
                    int min = Integer.parseInt(parts[0]);
                    int max = Integer.parseInt(parts[1]);
                    long worldTime = player.getWorld().getTime();
                    if (worldTime < min || worldTime > max) return false;
                }
            } catch (NumberFormatException ex) {
                plugin.getLogger().warning("Неверный формат времени: " + time);
            }
        }

        return true;
    }

}
