package org.satellite.dev.progiple.hotbed.spawners.menus.buttons;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.Menus.Items.Item;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;

@Getter
public abstract class Button extends Item implements IButton {
    private final SpawnerConfig spawnerConfig;
    public Button(ConfigurationSection section, SpawnerConfig spawnerConfig) {
        super(section, section.getInt("slot"));
        this.spawnerConfig = spawnerConfig;
        this.setLore(Tools.reLoreItem(this, this.spawnerConfig));
    }
}
