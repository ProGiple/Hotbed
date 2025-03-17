package org.satellite.dev.progiple.hotbed;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.novasparkle.lunaspring.Events.MenuHandler;
import org.novasparkle.lunaspring.LunaSpring;
import org.novasparkle.lunaspring.Util.Service.NBTService;
import org.novasparkle.lunaspring.Util.managers.NBTManager;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.spawners.handlers.*;

import java.util.Objects;

public final class Hotbed extends JavaPlugin {
    @Getter private static Hotbed plugin;

    @Override
    public void onEnable() {
        plugin = this;

        NBTService nbtService = new NBTService();
        LunaSpring.getServiceProvider().register(nbtService);
        NBTManager.init(nbtService);

        saveResource("menus/main.yml", false);
        saveResource("menus/container_page.yml", false);
        saveResource("mobs.yml", false);
        saveDefaultConfig();
        SpawnerConfig.load();

        this.reg(new MenuHandler());
        this.reg(new SpawnerBreakHandler());
        this.reg(new SpawnerSpawnMobsHandler());
        this.reg(new ClickOnSpawnerHandler());
        this.reg(new PlaceSpawnerHandler());
        this.reg(new DestroySpawnerHandler());

        Command command = new Command();
        Objects.requireNonNull(getCommand("hotbed")).setTabCompleter(command);
        Objects.requireNonNull(getCommand("hotbed")).setExecutor(command);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void reg(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}
