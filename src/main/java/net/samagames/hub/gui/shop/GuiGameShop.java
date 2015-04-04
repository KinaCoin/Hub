package net.samagames.hub.gui.shop;

import net.samagames.hub.Hub;
import net.samagames.hub.games.IGame;
import net.samagames.hub.games.shop.ShopIcon;
import net.samagames.hub.gui.AbstractGui;
import net.samagames.hub.utils.GuiUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class GuiGameShop extends AbstractGui
{
    private final IGame game;

    public GuiGameShop(IGame game)
    {
        this.game = game;
    }

    @Override
    public void display(Player player)
    {
        this.inventory = Bukkit.createInventory(null, 9, "Boutique de " + this.game.getName());

        this.update(player);

        player.openInventory(this.inventory);
    }

    @Override
    public void update(Player player)
    {
        int slot = 0;

        for(ShopIcon item : this.game.getShopConfiguration().getShopItems())
        {
            this.setSlotData(item.getFormattedIcon(player), slot, "item_" + item.getActionName());
            slot++;
        }

        this.setSlotData(GuiUtils.getBackItem(), this.inventory.getSize() - 1, "back");
    }

    @Override
    public void onClick(Player player, ItemStack stack, String action, ClickType clickType)
    {
        if(action.equals("back"))
        {
            Hub.getInstance().getGuiManager().openGui(player, new GuiShop());
        }
        else
        {
            String item = action.split("_")[1];
            this.game.getShopConfiguration().getShopItemByName(item).execute(player, clickType);
        }
    }
}
