package net.kingidk.creativeItemControl.checks;

import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import java.util.Objects;
import net.kingidk.creativeItemControl.CreativeItemControl;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class ComponentCheck implements ItemCheck {
   private final CreativeItemControl plugin;

   public ComponentCheck(CreativeItemControl plugin) {
      this.plugin = plugin;
   }

   @Override
   public void check(ItemCheckContext ctx) {
      if (!ctx.isCancelled()) {
         if (this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.COMPONENTS_ENABLED)) {
            boolean playerNotNull = ctx.player != null;
            if (!playerNotNull || !ctx.player.hasPermission("cic.bypass.components")) {
               for (DataComponentType type : this.plugin.getConfigManager().getResolvedComponents()) {
                  if (ctx.item.hasData(type)) {
                     ItemStack defaultItem = this.plugin.getConfigManager().getDefaultItem(ctx.item.getType());
                     if (!(type instanceof Valued<?> valued ? Objects.equals(defaultItem.getData(valued), ctx.item.getData(valued)) : defaultItem.hasData(type))
                        )
                      {
                        ctx.cancel();
                     }
                  }
               }

               if (!ctx.isCancelled() && ctx.meta instanceof BundleMeta bm) {
                  for (ItemStack item : bm.getItems()) {
                     for (DataComponentType type : this.plugin.getConfigManager().getResolvedComponents()) {
                        ItemStack defaultItem = this.plugin.getConfigManager().getDefaultItem(item.getType());
                        if (!item.hasData(type)
                           || !(
                              type instanceof Valued<?> valued ? Objects.equals(defaultItem.getData(valued), item.getData(valued)) : defaultItem.hasData(type)
                           )) {
                           ctx.cancel();
                        }
                     }
                  }
               }

               if (playerNotNull && ctx.isCancelled() && this.plugin.getConfigManager().getBoolean(ConfigManager.ConfigKeys.MAIN_PLAYER_ALERTS)) {
                  this.plugin.getMessageManager().sendAlert(ctx.player, ConfigManager.ConfigKeys.ALERTS_COMPONENTS);
               }
            }
         }
      }
   }
}
