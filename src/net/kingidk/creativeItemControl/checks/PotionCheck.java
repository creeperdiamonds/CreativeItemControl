package net.kingidk.creativeItemControl.checks;

import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class PotionCheck implements ItemCheck {
   private final CreativeItemControl plugin;

   public PotionCheck(CreativeItemControl plugin) {
      this.plugin = plugin;
   }

   @Override
   public void check(ItemCheckContext ctx) {
      if (!ctx.isCancelled()) {
         if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.POTIONS_ENABLED)) {
            boolean playerNotNull = ctx.player != null;
            if (!playerNotNull || !ctx.player.hasPermission("cic.bypass.potions")) {
               if (ctx.meta instanceof BundleMeta bm) {
                  for (ItemStack item : bm.getItems()) {
                     if (item.getItemMeta() instanceof PotionMeta pm && pm.hasCustomEffects()) {
                        ctx.cancel();
                        if (playerNotNull && this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_PLAYER_ALERTS)) {
                           this.plugin.getMessageManager().sendAlert(ctx.player, ConfigManager.ConfigKeys.ALERTS_POTIONS);
                        }

                        return;
                     }
                  }
               } else {
                  switch (ctx.item.getType()) {
                     case POTION:
                     case LINGERING_POTION:
                     case SPLASH_POTION:
                        PotionMeta potionMeta = (PotionMeta)ctx.item.getItemMeta();
                        if (potionMeta.hasCustomEffects()) {
                           if (playerNotNull && this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_PLAYER_ALERTS)) {
                              this.plugin.getMessageManager().sendAlert(ctx.player, ConfigManager.ConfigKeys.ALERTS_POTIONS);
                           }

                           ctx.cancel();
                        }

                        return;
                  }
               }
            }
         }
      }
   }
}
