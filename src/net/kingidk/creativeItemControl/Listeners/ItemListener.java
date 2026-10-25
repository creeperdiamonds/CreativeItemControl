package net.kingidk.creativeItemControl.Listeners;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.Handlers.AttributeHandler;
import net.kingidk.creativeItemControl.Handlers.ComponentHandler;
import net.kingidk.creativeItemControl.Handlers.EnchantmentHandler;
import net.kingidk.creativeItemControl.Handlers.PotionHandler;
import net.kingidk.creativeItemControl.Util.ItemCheckContext;
import net.kingidk.creativeItemControl.Util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemListener implements Listener {
   private final CreativeItemControl plugin;
   private final AttributeHandler attributeHandler;
   private final PotionHandler potionHandler;
   private final EnchantmentHandler enchantmentHandler;
   private final ComponentHandler componentHandler;

   public ItemListener(CreativeItemControl plugin, MessageUtil messageUtil) {
      this.plugin = plugin;
      this.attributeHandler = new AttributeHandler(plugin, messageUtil);
      this.potionHandler = new PotionHandler(plugin, messageUtil);
      this.enchantmentHandler = new EnchantmentHandler(plugin, messageUtil);
      this.componentHandler = new ComponentHandler(plugin, messageUtil);
   }

   @EventHandler
   public void onCreativeInventory(InventoryCreativeEvent e) {
      boolean inList = this.plugin.config.worlds.contains(e.getWhoClicked().getWorld().getName());
      if (this.plugin.config.worldsBlacklist != inList) {
         if (!e.getWhoClicked().hasPermission("cic.bypass")) {
            boolean isDrop = e.getSlot() < 0;
            ItemStack item = e.getCursor();
            if (!item.getType().isAir()) {
               ItemMeta meta = item.getItemMeta();
               if (meta != null) {
                  ItemMeta originalMeta = meta.clone();
                  Player p = (Player)e.getWhoClicked();
                  if (!meta.equals(this.plugin.getDefaultMeta(item.getType()))) {
                     if (!this.plugin.isExcluded(item)) {
                        ItemCheckContext ctx = new ItemCheckContext(p, item, meta, e.getSlot());
                        this.attributeHandler.check(ctx);
                        this.potionHandler.check(ctx);
                        this.enchantmentHandler.check(ctx);
                        this.componentHandler.check(ctx);
                        boolean wasModified = !ctx.meta.equals(originalMeta);
                        if (ctx.isCancelled()) {
                           e.setCancelled(true);
                        } else if (isDrop && wasModified) {
                           e.setCancelled(true);
                        } else {
                           item.setItemMeta(ctx.newItemMeta());
                           if (!isDrop && e.getSlot() < p.getInventory().getSize()) {
                              p.getInventory().setItem(e.getSlot(), item);
                           }

                           if (wasModified) {
                              p.updateInventory();
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onInventorySlotChange(PlayerInventorySlotChangeEvent e) {
      if (this.plugin.config.masterEnabled) {
         if (e.getSlot() >= 0) {
            if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
               boolean inList = this.plugin.config.worlds.contains(e.getPlayer().getWorld().getName());
               if (this.plugin.config.worldsBlacklist != inList) {
                  if (!e.getPlayer().hasPermission("cic.bypass")) {
                     ItemStack item = e.getNewItemStack();
                     if (!item.getType().isAir()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                           Player p = e.getPlayer();
                           if (!meta.equals(this.plugin.getDefaultMeta(item.getType()))) {
                              if (!this.plugin.isExcluded(item)) {
                                 ItemCheckContext ctx = new ItemCheckContext(p, item, meta, e.getSlot());
                                 this.attributeHandler.check(ctx);
                                 this.potionHandler.check(ctx);
                                 this.enchantmentHandler.check(ctx);
                                 this.componentHandler.check(ctx);
                                 if (ctx.isCancelled()) {
                                    p.getInventory().setItem(e.getSlot(), null);
                                 } else {
                                    item.setItemMeta(ctx.newItemMeta());
                                    p.getInventory().setItem(e.getSlot(), item);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
