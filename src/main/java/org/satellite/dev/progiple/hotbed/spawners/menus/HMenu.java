package org.satellite.dev.progiple.hotbed.spawners.menus;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.AMenu;
import org.novasparkle.lunaspring.API.menus.items.Decoration;
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

        Decoration decoration = new Decoration(menuConfig.getSection("items.decorations"), this.getInventory());
        decoration.getDecorationItems().forEach(item -> item.setLore(Tools.reLoreItem(item, spawnerConfig)));
        decoration.insert();
    }

    public boolean cancelNums(InventoryClickEvent e) {
        if (e.getClick() == ClickType.NUMBER_KEY) {
            e.setCancelled(true);
            return true;
        }
        return false;
    }
}