package me.superchirok1.chevents.commands;

import me.superchirok1.chevents.ChEvents;
import me.superchirok1.chevents.actions.ActionManager;
import me.superchirok1.chevents.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EventsCommand implements CommandExecutor, TabCompleter {

    private final ChEvents plugin;

    public EventsCommand(ChEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        ConfigurationSection msgs = plugin.getMessages().getConfigurationSection("messages");
        ConfigurationSection events = plugin.getEvents().getConfigurationSection("events");

        if (!sender.hasPermission("chevents.admin")) {
            Utils.send(sender, msgs.getString("no-perms", "Нет прав"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {

            Utils.send(sender, msgs.getString("list-title"));
            for (String event : events.getKeys(false)) {
                String event_name = events.getString(event + ".event", msgs.getString("placeholders.no-listener", "Не слушатель"));
                Utils.send(sender, msgs.getString("list")
                        .replace("{name}", event)
                        .replace("{event}", event_name)
                );
            }
            return true;

        }

        if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
            Utils.send(sender, """
                    &#FF7C00🔥 &#FF7F06Д&#FF8109о&#FF820Cс&#FF840Fт&#FF8512у&#FF8715п&#FF8818н&#FF8A1Bы&#FF8B1Eе &#FF8E24к&#FF9027о&#FF922Aм&#FF932Dа&#FF9530н&#FF9633д&#FF9836ы &#FF9B3Cc&#FF9C3Fh&#FF9E42E&#FF9F45v&#FFA148e&#FFA24Bn&#FFA44Et&#FFA551s
                    &8&m                  &f
                    &#6ffc03/chevents reload &f- Перезагружает плагин
                    &#6ffc03/chevents execute &f- Выполнить действие
                    &#6ffc03/chevents list &f- Список слушателей
                    """);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("execute")) {
            if (args.length == 1) {
                Utils.send(sender, msgs.getString("usage", "/chev execute <ивент> <игрок>")
                        .replace("{cmd}", "/chev execute <ивент> <игрок>"));
                return true;
            }

            String eventName = args[1];

            Player target;
            if (args.length == 2) {
                if (!(sender instanceof Player)) {
                    Utils.send(sender, msgs.getString("player-only", "Только игроки могут использовать команду"));
                    return true;
                }
                target = (Player) sender;
            } else {
                target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    Utils.send(sender, msgs.getString("player-not-found", "Игрок не найден"));
                    return true;
                }
            }

            if (events != null && events.contains(eventName)) {
                ConfigurationSection section = events.getConfigurationSection(eventName);
                runActions(target, section.getStringList("actions"));
                Utils.send(sender, msgs.getString("event-executed", "Событие выполнено для " + target.getName())
                        .replace("{target}", target.getName()));
            } else {
                Utils.send(sender, msgs.getString("dont-have", "Такого события нет"));
            }
            return true;
        }

        reload(sender, Objects.requireNonNull(msgs));
        return true;
    }

    private void runActions(Player p, List<String> actions) {
        for (String action : actions) {
            ActionManager.execute(p, action);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (!sender.hasPermission("chevents.admin")) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("reload", "execute", "list", "help");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("execute")) {
            ConfigurationSection events = plugin.getEvents().getConfigurationSection("events");
            if (events == null) return Collections.emptyList();
            return new ArrayList<>(events.getKeys(false));
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("execute")) {
            return plugin.getServer().getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }

        return List.of();
    }

    private void reload(CommandSender sender, ConfigurationSection msgs) {
        plugin.loadConfigs();
        Utils.send(sender, msgs.getString("reloaded", "Конфиг перезагружен"));
    }
}
