package net.kingidk.creativeItemControl.listeners;

import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.checks.AttributeCheck;
import net.kingidk.creativeItemControl.checks.ComponentCheck;
import net.kingidk.creativeItemControl.checks.EnchantmentCheck;
import net.kingidk.creativeItemControl.checks.ItemCheckContext;
import net.kingidk.creativeItemControl.checks.PotionCheck;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryListener implements Listener {
   private final CreativeItemControl plugin;
   private final AttributeCheck attributeCheck;
   private final ComponentCheck componentCheck;
   private final EnchantmentCheck enchantmentCheck;
   private final PotionCheck potionCheck;

   public InventoryListener(CreativeItemControl plugin) {
      this.plugin = plugin;
      this.attributeCheck = new AttributeCheck(plugin);
      this.componentCheck = new ComponentCheck(plugin);
      this.enchantmentCheck = new EnchantmentCheck(plugin);
      this.potionCheck = new PotionCheck(plugin);
   }

   @EventHandler
   public void onCreativeInventory(InventoryCreativeEvent e) {
      if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_ENABLED)) {
         if (!this.plugin.getConfigManager().isWorldDenied(e.getWhoClicked().getWorld())) {
            if (!e.getWhoClicked().hasPermission("cic.bypass")) {
               boolean isDrop = e.getSlot() < 0;
               ItemStack item = e.getCursor();
               if (!item.getType().isAir()) {
                  ItemMeta meta = item.getItemMeta();
                  if (meta != null) {
                     ItemMeta originalMeta = meta.clone();
                     Player p = (Player)e.getWhoClicked();
                     if (!meta.equals(Bukkit.getItemFactory().getItemMeta(item.getType()))) {
                        if (!this.plugin.getExcludedItemManager().isExcluded(item)) {
                           ItemCheckContext ctx = new ItemCheckContext(p, item, e.getSlot());
                           this.attributeCheck.check(ctx);
                           this.componentCheck.check(ctx);
                           this.enchantmentCheck.check(ctx);
                           this.potionCheck.check(ctx);
                           boolean wasModified = !ctx.meta.equals(originalMeta);
                           if (ctx.isCancelled()) {
                              e.setCancelled(true);
                           } else if (isDrop && wasModified) {
                              e.setCancelled(true);
                           } else {
                              item.setItemMeta(ctx.getNewMeta());
                              if (!isDrop && e.getSlot() < p.getInventory().getSize()) {
                                 p.getInventory().setItem(e.getSlot(), item);
                              }

                              if (wasModified) {
                                 e.setCancelled(true);
                              }
                           }

                           if (ctx.isCancelled()) {
                              e.setCancelled(true);
                           } else if (wasModified) {
                              item.setItemMeta(ctx.getNewMeta());
                              if (!isDrop && e.getSlot() < p.getInventory().getStorageContents().length) {
                                 p.getInventory().setItem(e.getSlot(), item);
                              }

                              if (isDrop) {
                                 e.setCancelled(true);
                              } else if (e.getSlot() == 40) {
                                 p.getInventory().setItemInOffHand(item);
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

   @EventHandler
   public void onInventorySlotChange(PlayerInventorySlotChangeEvent e) {
      if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_ENABLED)) {
         if (e.getSlot() >= 0) {
            if (e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
               if (!this.plugin.getConfigManager().isWorldDenied(e.getPlayer().getWorld())) {
                  if (!e.getPlayer().hasPermission("cic.bypass")) {
                     ItemStack item = e.getNewItemStack();
                     if (!item.getType().isAir()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                           Player p = e.getPlayer();
                           if (!meta.equals(Bukkit.getItemFactory().getItemMeta(item.getType()))) {
                              if (!this.plugin.getExcludedItemManager().isExcluded(item)) {
                                 ItemCheckContext ctx = new ItemCheckContext(p, item, e.getSlot());
                                 this.attributeCheck.check(ctx);
                                 this.componentCheck.check(ctx);
                                 this.enchantmentCheck.check(ctx);
                                 this.potionCheck.check(ctx);
                                 boolean isOffHand = e.getSlot() == 40 || item.equals(p.getInventory().getItemInOffHand());
                                 if (ctx.isCancelled()) {
                                    if (isOffHand) {
                                       p.getInventory().setItemInOffHand(null);
                                    } else {
                                       p.getInventory().setItem(e.getSlot(), null);
                                    }

                                    p.updateInventory();
                                 } else {
                                    item.setItemMeta(ctx.getNewMeta());
                                    if (isOffHand) {
                                       p.getInventory().setItemInOffHand(item);
                                    } else {
                                       p.getInventory().setItem(e.getSlot(), item);
                                    }

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
      }
   }
}
