package me.superchirok1.chevents.events;

import me.clip.placeholderapi.PlaceholderAPI;
import me.superchirok1.chevents.ChEvents;
import me.superchirok1.chevents.actions.ActionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.List;
import java.util.Random;

import static me.superchirok1.chevents.ChEvents.econ;
import static me.superchirok1.chevents.ChEvents.ppAPI;

public class EventManager implements Listener {

    private final ChEvents plugin;

    public EventManager(ChEvents plugin) {
        this.plugin = plugin;
    }

    private final Random random = new Random();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        handleEvent(e.getPlayer(), "join", e);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        handleEvent(e.getPlayer(), "quit", e);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            handleEvent((Player) e.getEntity(), "damage", e);
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent e) {
        handleEvent(e.getPlayer(), "bed_enter", e);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent e) {
        handleEvent(e.getPlayer(), "bed_leave", e);
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent e) {
        handleEvent(e.getPlayer(), "level_change", e);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        handleEvent(e.getPlayer(), "move", e);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        handleEvent(e.getEntity(), "death", e);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        handleChatEvent(e.getPlayer(), "chat_message", e.getMessage(), e);
    }

    private void handleEvent(Player player, String eventName, Event event) {
        ConfigurationSection events = plugin.getEvents().getConfigurationSection("events");
        if (events == null) return;

        for (String key : events.getKeys(false)) {
            ConfigurationSection section = events.getConfigurationSection(key);
            if (section == null) continue;

            if (!section.getBoolean("enabled", true)) continue;
            if (!eventName.equalsIgnoreCase(section.getString("event"))) continue;

            if (section.getBoolean("full-cancel-event", false) && event instanceof Cancellable) {
                ((Cancellable) event).setCancelled(true);
                return;
            }

            double chance = section.getDouble("conditions.chance", 1.0);
            boolean chancePassed = random.nextDouble() <= chance;
            boolean conditionsPassed = checkConditions(player, section);

            String target = section.getString("actions-target", "EVENT_PLAYER");

            if (chancePassed && conditionsPassed) {
                if (target.isEmpty() || target.equalsIgnoreCase("EVENT_PLAYER")) {
                    runActions(player, section.getStringList("actions"));
                } else {
                    for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                        runActions(target_player, section.getStringList("actions"));
                    }
                }

                if (section.getBoolean("cancel-event", false) && event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                    return;
                }
            } else {
                List<String> denyActions = section.getStringList("deny-actions");
                if (denyActions != null && !denyActions.isEmpty()) {
                    if (target.isEmpty() || target.equalsIgnoreCase("EVENT_PLAYER")) {
                        runActions(player, denyActions);
                    } else {
                        for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                            runActions(target_player, denyActions);
                        }
                    }
                }
            }
        }
    }


    private void handleChatEvent(Player player, String eventName, String message, Event event) {
        ConfigurationSection events = plugin.getEvents().getConfigurationSection("events");
        if (events == null) return;

        for (String key : events.getKeys(false)) {
            ConfigurationSection section = events.getConfigurationSection(key);
            if (section == null) continue;

            if (!section.getBoolean("enabled", true)) continue;
            if (!eventName.equalsIgnoreCase(section.getString("event"))) continue;

            if (section.getBoolean("full-cancel-event", false) && event instanceof Cancellable) {
                ((Cancellable) event).setCancelled(true);
                return;
            }

            double chance = section.getDouble("conditions.chance", 1.0);
            boolean chancePassed = random.nextDouble() <= chance;
            boolean conditionsPassed = checkConditionsChat(player, section, message);

            String target = section.getString("actions-target", "EVENT_PLAYER");

            if (chancePassed && conditionsPassed) {
                if (target.isEmpty() || target.equalsIgnoreCase("EVENT_PLAYER")) {
                    runActions(player, section.getStringList("actions"));
                } else {
                    for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                        runActions(target_player, section.getStringList("actions"));
                    }
                }

                if (section.getBoolean("cancel-event", false) && event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                    return;
                }
            } else {
                List<String> denyActions = section.getStringList("deny-actions");
                if (denyActions != null && !denyActions.isEmpty()) {
                    if (target.isEmpty() || target.equalsIgnoreCase("EVENT_PLAYER")) {
                        runActions(player, denyActions);
                    } else {
                        for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                            runActions(target_player, denyActions);
                        }
                    }
                }
            }
        }
    }


    private void runActions(Player p, List<String> actions) {
        for (String action : actions) {
            ActionManager.execute(p, action);
        }
    }

    private boolean checkConditions(Player player, ConfigurationSection section) {
        if (!section.getBoolean("enabled", true)) return false;

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


    private boolean checkConditionsChat(Player player, ConfigurationSection section, String message) {

        if (!(section.getString("conditions.message") == null)) {
            if (!section.getString("conditions.message").equalsIgnoreCase(message)) {
                return false;
            }
        }

        if (!checkConditions(player, section)) {
            return false;
        }

        return true;
    }


}
