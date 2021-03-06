package ru.empireprojekt.empireitems.ItemManager.menusystem.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.empireprojekt.empireitems.EmpireItems;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PaginatedMenu;
import ru.empireprojekt.empireitems.ItemManager.menusystem.PlayerMenuUtility;

public class EmpireCategoryMenu extends PaginatedMenu {
    private EmpireItems plugin;
    private int slot;
    private int maxPage;

    EmpireCategoryMenu(PlayerMenuUtility playerMenuUtility, EmpireItems plugin, int slot) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.slot = slot;
        maxPage=getMaxPages();
    }
    EmpireCategoryMenu(PlayerMenuUtility playerMenuUtility, EmpireItems plugin, int slot, int page) {
        super(playerMenuUtility);
        this.plugin = plugin;
        this.slot = slot;
        this.page = page;
        maxPage=getMaxPages();
    }
    public String getMenuName() {
        return plugin.CONSTANTS.HEXPattern(plugin.menuItems.get(slot).categoryTitle);
    }

    public int getSlots() {
        return 54;
    }

    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() != null) {
            if (e.getSlot() == 49) {
                e.getWhoClicked().closeInventory();
                new EmpireCategoriesMenu(playerMenuUtility, plugin).open();
            } else if (e.getSlot() == 53) {
                if (this.page >= maxPage) {
                    playerMenuUtility.getPlayer().sendMessage(ChatColor.YELLOW + "Вы на последней странице");
                    return;
                }
                inventory.clear();
                this.page += 1;
                setMenuItems();
            } else if (e.getSlot() == 45) {
                if (this.page == 0) {
                    playerMenuUtility.getPlayer().sendMessage(ChatColor.YELLOW + "Вы на первой странице");
                    return;
                }
                this.page -= 1;
                inventory.clear();
                setMenuItems();
            }else
                new EmpireCraftMenu(playerMenuUtility,slot,page,plugin,plugin.menuItems.get(slot).categoryItems.get(page*maxItemsPerPage+e.getSlot())).open();

        }

    }

    private int getMaxPages() {
        int size = plugin.menuItems.get(slot).categoryItems.size();
        int mP = size / maxItemsPerPage;
        mP += (size % maxItemsPerPage > 0) ? 1 : 0;
        return mP-1;
    }

    public void setMenuItems() {
        addManageButtons(plugin);

        for (int i = 0; i < super.maxItemsPerPage; ++i) {
            index = super.maxItemsPerPage * page + i;
            if (index < plugin.menuItems.get(slot).categoryItems.size() && plugin.menuItems.get(slot).categoryItems.get(index) != null) {
                String item = plugin.menuItems.get(slot).categoryItems.get(index);
                ItemStack itemStack;
                if (Material.getMaterial(item) != null)
                    itemStack = new ItemStack(Material.getMaterial(item));
                else if (plugin.items.containsKey(item))
                    itemStack = plugin.items.get(item);
                else {
                    System.out.println(ChatColor.AQUA+"[EmpireItems]"+ChatColor.YELLOW + "Предмет не найден:" + item);
                    itemStack = new ItemStack(Material.STONE);
                }
                inventory.setItem(i, itemStack);
            }
        }
    }
}
