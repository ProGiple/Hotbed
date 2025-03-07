package org.satellite.dev.progiple.hotbed.configs.menuCfg;

import org.novasparkle.lunaspring.Configuration.Configuration;
import org.novasparkle.lunaspring.Util.Utils;
import org.satellite.dev.progiple.hotbed.Hotbed;

import java.io.File;
import java.util.Objects;

public class IMenuConfig extends Configuration {
    public IMenuConfig(String fileName) {
        super(new File(Hotbed.getPlugin().getDataFolder(), String.format("menus/%s", fileName)));
        Hotbed.getPlugin().saveResource(String.format("menus/%s", fileName), false);
    }

    public String getTitle() {
        return Utils.color(Objects.requireNonNull(this.config.getString("title")));
    }

    public byte getSize() {
        return (byte) (this.config.getInt("rows") * 9);
    }
}
