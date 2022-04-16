package me.crypticzxi.mbedwars_spawnerrates.me.crypticzxi.mbedwars_spawnerrates.Me.crypticzxi;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.game.spawner.SpawnerDurationModifier;
import de.marcely.bedwars.tools.Helper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import me.crypticzxi.mbedwars_spawnerrates.me.crypticzxi.mbedwars_spawnerrates.Me.crypticzxi.RoundStartMessage;
import me.crypticzxi.mbedwars_spawnerrates.me.crypticzxi.mbedwars_spawnerrates.Me.crypticzxi.CloneArena;

import java.io.File;
import java.util.*;

public final class MBedwars_SpawnerRates extends JavaPlugin {

    public static JavaPlugin plugin;

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
