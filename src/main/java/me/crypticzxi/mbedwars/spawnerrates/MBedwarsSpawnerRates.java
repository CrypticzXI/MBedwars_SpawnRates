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
	public void onLoad() {
    	Bukkit.getLogger().severe("MBedwarsSpawnerRates: onLoad. Plugin found. Ready to start.");
		super.onLoad();
	}

	@Override
    public void onEnable() {
    	Bukkit.getLogger().info("MBedwarsSpawnerRates: onEnabled. Plugin starting...");
    	super.onEnable();

    	// Load config.sys from jar if it does not exist.  Fail silently if exists.
    	this.saveDefaultConfig();
        
    	// Register all events:
        String message = registerAllEvents();
        Bukkit.getLogger().info(message);
        
        this.getCommand("spawnerrates").setExecutor(new ReloadCommand());
        Bukkit.getLogger().info("MBedwarsSpawnerRates: Command registered: spawnrantes (reload config.yml)");
        
        Bukkit.getLogger().info("MBedwarsSpawnerRates: onEnabled. Plugin loaded.");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    	
        Bukkit.getLogger().severe("MBedwarsSpawnerRates: onDisabled. Plugin Terminated.");
    }

    
    private String registerAllEvents() {
    	getServer().getPluginManager().registerEvents(new RoundStartMessage(), this);
    	getServer().getPluginManager().registerEvents(new SpawnerRates(), this);
    	getServer().getPluginManager().registerEvents(new CloneArena(), this);
    	
    	return "MBedwarsSpawnerRates: Registered all events.";
    }
    
    
    /**
     * <p>This command will reload the config.yml settings and then will reregister
     * all of the events, which will then use the updated settings.
     * </p>
     *
     */
    public class ReloadCommand implements CommandExecutor {

        // This method is called, when somebody uses our command
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            
        	boolean isPlayer = sender instanceof Player;
        	Player player = isPlayer ? (Player) sender : null;
        	
        	reloadConfig();
        	String message = "MBedwarsSpawnerRates: Reloaded config.yml settings.";
        	if ( isPlayer ) {
        		player.sendMessage( message );
        	}
        	else {
        		Bukkit.getLogger().info(message);
        	}
        	
        	String regEventseMessage = registerAllEvents();
        	if ( isPlayer ) {
        		player.sendMessage( regEventseMessage );
        	}
        	else {
        		Bukkit.getLogger().info( regEventseMessage );
        	}

            // If the player (or console) uses our command correct, we can return true
            return true;
        }
    }
}
