package net.kingidk.creativeItemControl;

import net.kingidk.bukkit.Metrics;
import net.kingidk.creativeItemControl.command.CICCommand;
import net.kingidk.creativeItemControl.listeners.DispenserListener;
import net.kingidk.creativeItemControl.listeners.InventoryListener;
import net.kingidk.creativeItemControl.managers.ConfigManager;
import net.kingidk.creativeItemControl.managers.ExcludedItemManager;
import net.kingidk.creativeItemControl.managers.MessageManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class CreativeItemControl extends JavaPlugin {
   private ConfigManager configManager;
   private MessageManager messageManager;
   private ExcludedItemManager excludedItemManager;

   public void onEnable() {
      int PLUGINID = 31021;
      new Metrics(this, 31021);
      this.saveDefaultConfig();
      this.configManager = new ConfigManager(this);
      this.configManager.migrate();
      this.configManager.load();
      this.messageManager = new MessageManager(this);
      this.excludedItemManager = new ExcludedItemManager(this);
      this.excludedItemManager.loadAll();
      this.getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
      this.getServer().getPluginManager().registerEvents(new DispenserListener(this), this);
      PluginCommand cmd = this.getCommand("cic");
      if (cmd != null) {
         CICCommand cicCommand = new CICCommand(this);
         cmd.setExecutor(cicCommand);
         cmd.setTabCompleter(cicCommand);
      }
   }

   public void onDisable() {
      HandlerList.unregisterAll(this);
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }

   public MessageManager getMessageManager() {
      return this.messageManager;
   }

   public ExcludedItemManager getExcludedItemManager() {
      return this.excludedItemManager;
   }
}
