package me.crypticzxi.mbedwars.spawnerrates;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MBedwarsSpawnerRates extends JavaPlugin {

    private static MBedwarsSpawnerRates plugin;
    
    public MBedwarsSpawnerRates() {
    	super();
    	
    	plugin = this;
    }

    public static MBedwarsSpawnerRates getInstance() {
    	return plugin;
    }
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().severe("Shitty Spawn Rates Plugin Has Loaded.");
        getServer().getPluginManager().registerEvents(new RoundStartMessage(), this);
        getServer().getPluginManager().registerEvents(new SpawnerRates(), this);
        getServer().getPluginManager().registerEvents(new CloneArena(), this);
        this.getCommand("spawnerrates").setExecutor(new ReloadCommand());
        plugin = this;
        saveConfig();
        this.saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().severe("Shitty Spawn Rates Plugin Has Gone To Bed.");
        saveConfig();
    }

    public class ReloadCommand implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                reloadConfig();
                player.sendMessage("Config Reloaded. GG!");
            }

            // If the player (or console) uses our command correct, we can return true
            return true;
        }
    }
}
