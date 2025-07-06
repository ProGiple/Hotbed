package org.satellite.dev.progiple.hotbed.configs.menuCfg;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.configuration.Configuration;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.io.File;
import java.util.Objects;

public class MenuConfig extends Configuration {
    @Getter private static final MenuConfig main;
    @Getter private static final MenuConfig container_page;
    static {
        main = new MenuConfig("main");
        container_page = new MenuConfig("container_page");
    }

    public MenuConfig(String fileName) {
        super(new File(Hotbed.getINSTANCE().getDataFolder(), String.format("menus/%s.yml", fileName)));
    }

    public String getTitle() {
        return ColorManager.color(Objects.requireNonNull(super.getString("title")));
    }

    public byte getSize() {
        return (byte) (super.getInt("rows") * 9);
    }

    public ConfigurationSection getSection(String path) {
        return super.getSection(path);
    }
}
