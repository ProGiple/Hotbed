package org.satellite.dev.progiple.hotbed.spawners.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.satellite.dev.progiple.hotbed.configs.Config;

import java.util.ArrayList;
import java.util.List;

public class LootItem extends Item {
    public LootItem(Material material, int amount) {
        super(material, amount);
        this.setDisplayName("");

        List<String> lore = new ArrayList<>(Config.getStrList("settings.loot_item_lore"));
        this.setLore(lore);
    }

    public void collect(Player player) {
        ItemStack loot = new ItemStack(this.getMaterial(), this.getAmount());
        Utils.Items.give(player, false, loot);
    }
}
