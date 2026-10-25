package net.kingidk.creativeItemControl.checks;

import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class AttributeCheck implements ItemCheck {
   private final CreativeItemControl plugin;

   public AttributeCheck(CreativeItemControl plugin) {
      this.plugin = plugin;
   }

   @Override
   public void check(ItemCheckContext ctx) {
      if (!ctx.isCancelled()) {
         if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.ATTRIBUTES_ENABLED)) {
            boolean playerNotNull = ctx.player != null;
            if (!playerNotNull || !ctx.player.hasPermission("cic.bypass.attributes")) {
               boolean attributeIssue = ctx.meta.getAttributeModifiers() != null;
               if (!attributeIssue && ctx.meta instanceof BundleMeta bm) {
                  for (ItemStack item : bm.getItems()) {
                     ItemMeta meta = item.getItemMeta();
                     if (meta.getAttributeModifiers() != null) {
                        ctx.cancel();
                        attributeIssue = true;
                        break;
                     }
                  }
               }

               if (attributeIssue) {
                  if (this.plugin.getConfigManager().getString(ConfigManager.ConfigKeys.ATTRIBUTES_ACTION).equalsIgnoreCase("REMOVE")) {
                     ctx.meta.setAttributeModifiers(null);
                  } else {
                     ctx.cancel();
                  }

                  if (playerNotNull && this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_PLAYER_ALERTS)) {
                     this.plugin.getMessageManager().sendAlert(ctx.player, ConfigManager.ConfigKeys.ALERTS_ATTRIBUTES);
                  }
               }
            }
         }
      }
   }
}
