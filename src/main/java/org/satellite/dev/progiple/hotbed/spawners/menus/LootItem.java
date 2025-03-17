package org.satellite.dev.progiple.hotbed.spawners.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.Menus.Items.Item;
import org.satellite.dev.progiple.hotbed.configs.Config;

import java.util.ArrayList;
import java.util.List;

public class LootItem extends Item {
    public LootItem(Material material, int amount) {
        super(material, amount);

        List<String> lore = new ArrayList<>(Config.getStrList("settings.loot_item_lore"));
        this.setLore(lore);
    }

    public void collect(Player player) {
        ItemStack loot = new ItemStack(this.getMaterial(), this.getAmount());

        Inventory inventory = player.getInventory();
        boolean itemCollected = false;

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= 36) break;

            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                inventory.addItem(loot);
                itemCollected = true;
                break;
            }

            if (item.getType() == loot.getType() && item.getAmount() < 64) {
                int spaceLeft = 64 - item.getAmount();
                if (loot.getAmount() <= spaceLeft) {
                    item.setAmount(item.getAmount() + loot.getAmount());
                    itemCollected = true;
                    break;
                } else {
                    item.setAmount(64);
                    loot.setAmount(loot.getAmount() - spaceLeft);
                }
            }
        }

        if (!itemCollected) {
            player.getWorld().dropItem(player.getLocation(), loot);
        }
    }
}
