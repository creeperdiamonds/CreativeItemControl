package net.kingidk.creativeItemControl.listeners;

import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.checks.AttributeCheck;
import net.kingidk.creativeItemControl.checks.ComponentCheck;
import net.kingidk.creativeItemControl.checks.EnchantmentCheck;
import net.kingidk.creativeItemControl.checks.ItemCheckContext;
import net.kingidk.creativeItemControl.checks.PotionCheck;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DispenserListener implements Listener {
   private final CreativeItemControl plugin;
   private final AttributeCheck attributeCheck;
   private final ComponentCheck componentCheck;
   private final EnchantmentCheck enchantmentCheck;
   private final PotionCheck potionCheck;

   public DispenserListener(CreativeItemControl plugin) {
      this.plugin = plugin;
      this.attributeCheck = new AttributeCheck(plugin);
      this.componentCheck = new ComponentCheck(plugin);
      this.enchantmentCheck = new EnchantmentCheck(plugin);
      this.potionCheck = new PotionCheck(plugin);
   }

   @EventHandler
   public void onDispense(BlockDispenseEvent e) {
      if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_DISPENSERS_ENABLED)) {
         if (!this.plugin.getConfigManager().isWorldDenied(e.getBlock().getWorld())) {
            ItemStack item = e.getItem();
            if (!item.getType().isAir()) {
               ItemMeta meta = item.getItemMeta();
               if (meta != null) {
                  ItemMeta originalMeta = meta.clone();
                  if (!meta.equals(this.plugin.getConfigManager().getDefaultItem(item.getType()))) {
                     if (!this.plugin.getExcludedItemManager().isExcluded(item)) {
                        ItemCheckContext ctx = new ItemCheckContext(null, item, 0);
                        this.attributeCheck.check(ctx);
                        this.componentCheck.check(ctx);
                        this.enchantmentCheck.check(ctx);
                        this.potionCheck.check(ctx);
                        boolean wasModified = !ctx.meta.equals(originalMeta);
                        if (wasModified || ctx.isCancelled()) {
                           e.setCancelled(true);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
