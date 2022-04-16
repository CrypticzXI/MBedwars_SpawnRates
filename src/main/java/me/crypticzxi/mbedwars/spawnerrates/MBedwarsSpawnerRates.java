package me.crypticzxi.mbedwars.spawnerrates;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MBedwarsSpawnerRates extends JavaPlugin {

    private static MBedwarsSpawnerRates plugin;

    private MBedwarsSpawnerRates() {
    	super();
    }
    
    public static MBedwarsSpawnerRates getInstance() {
    	if ( plugin == null ) {
    		synchronized(MBedwarsSpawnerRates.class) {
    			if ( plugin == null ) {
    				plugin = new MBedwarsSpawnerRates();
    			}
    		}
    	}
    	
    	return plugin;
    }
    
    
    @Override
    public void onEnable() {
        // Plugin startup logic
        logSevere("&4Shitty Spawn Rates Plugin Has Loaded.");
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
        logSevere("&4Shitty Spawn Rates Plugin Has Gone To Bed.");
        saveConfig();
    }

    public class ReloadCommand implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                reloadConfig();
                log(player, "&7Config Reloaded. &3GG!");
            }

            // If the player (or console) uses our command correct, we can return true
            return true;
        }
    }
    
    private void log( Level logLevel, String message ) {
    	message = ChatColor.translateAlternateColorCodes('&', message);
    	
    	getLogger().info(message);
    }
    
    
    /**
     * <p>Log messages at info level. Translates '&' escape codes to the correct 
     * color code symbol: '§'.
     * </p>
     * 
     * @param message
     */
    public void logInfo( String message ) {
    	log( Level.INFO, message );
    }

    /**
     * <p>Log messages at warning level. Translates '&' escape codes to the correct 
     * color code symbol: '§'.
     * </p>
     * 
     * @param message
     */
    public void logWarn( String message ) {
    	log( Level.WARNING, message );
    }
    
    /**
     * <p>Log messages at severe level. Translates '&' escape codes to the correct 
     * color code symbol: '§'.
     * </p>
     * 
     * @param message
     */
    public void logSevere( String message ) {
    	log( Level.SEVERE, message );
    }
    
    public void log( Player player, String message ) {
    	message = ChatColor.translateAlternateColorCodes('&', message);
    	player.sendMessage(message);
    }
}
