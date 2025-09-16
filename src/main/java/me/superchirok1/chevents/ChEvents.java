package me.superchirok1.chevents;

import me.superchirok1.chevents.commands.EventsCommand;
import me.superchirok1.chevents.events.EventManager;
import me.superchirok1.chevents.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ChEvents extends JavaPlugin {

    public static boolean hasVault = false;
    public static boolean hasPlayerPoints = false;

    private FileConfiguration eventsYml;
    private FileConfiguration messagesYml;

    public static Economy econ = null;
    public static PlayerPointsAPI ppAPI;

    @Override
    public void onEnable() {
        //saveDefaultConfig();
        loadConfigs();
        Utils.plugin = this;

        getServer().getPluginManager().registerEvents(new EventManager(this), this);

        getCommand("chevents").setExecutor(new EventsCommand(this));
        getCommand("chevents").setTabCompleter(new EventsCommand(this));

        pluginsCheck();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void pluginsCheck() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null
                && Bukkit.getPluginManager().getPlugin("Vault").isEnabled()) {
            getServer().getLogger().info("[chEvents] Поддержка Vault активна");
            hasVault = setupEconomy();
        }

        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null && Bukkit.getPluginManager().getPlugin("PlayerPoints").isEnabled()) {
            getServer().getLogger().info("[chEvents] Поддержка PlayerPoints активна");
            this.ppAPI = PlayerPoints.getInstance().getAPI();
            hasPlayerPoints = true;
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void loadConfigs() {
        File mobsFile = new File(getDataFolder(), "events.yml");
        if (!mobsFile.exists()) saveResource("events.yml", false);
        eventsYml = YamlConfiguration.loadConfiguration(mobsFile);

        File blocksFile = new File(getDataFolder(), "messages.yml");
        if (!blocksFile.exists()) saveResource("messages.yml", false);
        messagesYml = YamlConfiguration.loadConfiguration(blocksFile);
    }

    public FileConfiguration getEvents() {
        return eventsYml;
    }

    public FileConfiguration getMessages() {
        return messagesYml;
    }
}
