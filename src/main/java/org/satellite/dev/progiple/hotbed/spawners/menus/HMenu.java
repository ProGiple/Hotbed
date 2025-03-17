package org.satellite.dev.progiple.hotbed.spawners.menus;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.Menus.AMenu;
import org.novasparkle.lunaspring.Menus.Items.Decoration;
import org.satellite.dev.progiple.hotbed.Tools;
import org.satellite.dev.progiple.hotbed.configs.SpawnerConfig;
import org.satellite.dev.progiple.hotbed.configs.menuCfg.MenuConfig;
import org.satellite.dev.progiple.hotbed.spawners.menus.buttons.Button;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class HMenu extends AMenu {
    private final SpawnerConfig spawnerConfig;
    private final MenuConfig menuConfig;
    private final List<Button> buttons = new ArrayList<>();
    public HMenu(Player player, MenuConfig menuConfig, SpawnerConfig spawnerConfig) {
        super(player, menuConfig.getTitle(), menuConfig.getSize());
        this.spawnerConfig = spawnerConfig;
        this.menuConfig = menuConfig;

        Decoration decoration = new Decoration(menuConfig.getSection("items.decorations"));
        decoration.getDecorationItems().forEach(item -> {
            item.setLore(Tools.reLoreItem(item, spawnerConfig));
        });
        decoration.insert(this);
    }
}
