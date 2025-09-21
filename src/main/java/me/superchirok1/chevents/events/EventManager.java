package me.superchirok1.chevents.events;

import me.clip.placeholderapi.PlaceholderAPI;
import me.superchirok1.chevents.ChEvents;
import me.superchirok1.chevents.actions.ActionManager;
import me.superchirok1.chevents.conditions.Conditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        handle(e.getPlayer(), "join", e);
    }
    @EventHandler public void onQuit(PlayerQuitEvent e) {
        handle(e.getPlayer(), "quit", e);
    }
    @EventHandler public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            handle((Player) e.getEntity(), "damage", e);
        }
    }
    @EventHandler public void onBedEnter(PlayerBedEnterEvent e) {
        handle(e.getPlayer(), "bed_enter", e);
    }
    @EventHandler public void onBedLeave(PlayerBedLeaveEvent e) {
        handle(e.getPlayer(), "bed_leave", e);
    }
    @EventHandler public void onLevelChange(PlayerLevelChangeEvent e) {
        handle(e.getPlayer(), "level_change", e);
    }
    @EventHandler public void onMove(PlayerMoveEvent e) {
        handle(e.getPlayer(), "move", e);
    }
    @EventHandler public void onDeath(PlayerDeathEvent e) {
        handle(e.getEntity(), "death", e);
    }
    @EventHandler public void onChat(AsyncPlayerChatEvent e) {handle(e.getPlayer(), "chat_message", e, e.getMessage());}
    @EventHandler public void onBreak(BlockBreakEvent e) {handle(e.getPlayer(), "block_break", e);}
    @EventHandler public void onPlace(BlockPlaceEvent e) {handle(e.getPlayer(), "block_place", e);}
    @EventHandler public void onKill(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer != null) {
            handle(killer, "player_kill_player", e);
            handle(victim, "player_death_by_player", e);
        }
    }


    private void handle(Player player, String eventName, Event event) {
        handle(player, eventName, event, null);
    }

    private void handle(Player player, String eventName, Event event, String message) {
        ConfigurationSection events = plugin.getEvents().getConfigurationSection("events");
        if (events == null) return;

        for (String key : events.getKeys(false)) {
            ConfigurationSection section = events.getConfigurationSection(key);
            if (section == null) continue;

            if (!section.getBoolean("enabled", true)) continue;
            if (!eventName.equalsIgnoreCase(section.getString("listen"))) continue;

            if (section.getBoolean("full-cancel-event", false) && event instanceof Cancellable) {
                ((Cancellable) event).setCancelled(true);
                return;
            }

            double chance = section.getDouble("conditions.chance", 1.0);
            boolean chancePassed = random.nextDouble() <= chance;
            boolean conditionsPassed = Conditions.check(player, section, message, plugin);

            String target = section.getString("actions-target", "EVENT_PLAYER");
            List<String> always_actions = section.getStringList("always-actions");
            if (!always_actions.isEmpty()) {
                run(target, player, always_actions);
            }

            if (chancePassed && conditionsPassed) {

                run(target, player, section.getStringList("actions"));

                if (section.getBoolean("cancel-event", false) && event instanceof Cancellable) {
                    ((Cancellable) event).setCancelled(true);
                    return;
                }
            } else {
                List<String> denyActions = section.getStringList("deny-actions");
                if (denyActions != null && !denyActions.isEmpty()) {
                    run(target, player, denyActions);
                }
            }
        }
    }


    private void runActions(Player p, List<String> actions) {
        for (String action : actions) {
            ActionManager.execute(p, action);
        }
    }

    private void run(String target, Player player, List<String> actions) {
        if (actions != null && !actions.isEmpty()) {
            if (target.isEmpty() || target.equalsIgnoreCase("EVENT_PLAYER")) {
                runActions(player, actions);
            } else if (target.equalsIgnoreCase("ONLINE_PLAYERS")) {
                for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                    runActions(target_player, actions);
                }
            } else if (target.equalsIgnoreCase("ALL_EXCEPT")) {
                for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                    if (!(target_player.getName().equalsIgnoreCase(player.getName()))) {
                        runActions(target_player, actions);
                    }
                }
            } else if (target.startsWith("NAME_")) {
                String value = target.replace("NAME_", "");

                Player target_player = Bukkit.getPlayer(value);
                if (target_player != null) {
                    runActions(target_player, actions);
                }
            } else if (target.startsWith("UUID_")) {
                String value = target.replace("UUID_", "");

                Player target_player = Bukkit.getPlayer(value);
                if (target_player != null) {
                    runActions(target_player, actions);
                }
            } else if (target.startsWith("PERMISSION_")) {
                String value = target.replace("UUID_", "");

                for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                    if (target_player.hasPermission(value)) {
                        runActions(target_player, actions);
                    }
                }
            } else if (target.startsWith("WORLD_")) {
                String value = target.replace("WORLD_", "");

                for (Player target_player : Bukkit.getServer().getOnlinePlayers()) {
                    if (target_player.getWorld().getName().equalsIgnoreCase(value)) {
                        runActions(target_player, actions);
                    }
                }
            }
        }
    }

}
