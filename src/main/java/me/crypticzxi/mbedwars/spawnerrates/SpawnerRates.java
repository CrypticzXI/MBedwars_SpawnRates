package me.crypticzxi.mbedwars.spawnerrates;

import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.game.spawner.SpawnerDurationModifier;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class SpawnerRates implements Listener {

    @EventHandler
    public void onRoundStartEvent(RoundStartEvent event) {

        int ppt = event.getArena().getPlayersPerTeam();
        int teams = event.getArena().getEnabledTeams().size();

        ConfigurationSection speedRates = MBedwarsSpawnerRates.getInstance().getConfig().getConfigurationSection("spawner-rates");
        Set<String> speedRateKeys = speedRates.getKeys(false);

        Arena arena = event.getArena();

        String finalName = "none";
        double finalRate = 0;

        for (String speedRateKey : speedRateKeys) {
            ConfigurationSection speedRate = speedRates.getConfigurationSection(speedRateKey);
            String name = speedRate.getString("name");
            double rate = speedRate.getDouble("spawn-rate");
            int PlayersPerTeam = speedRate.getInt("playersPerTeam");

            if (speedRate.contains("NoOfTeams")) { // is a special Gamemode.
                int NoOfTeams = speedRate.getInt("NoOfTeams");
                if (ppt == PlayersPerTeam & NoOfTeams == teams) {
                    finalName = name;
                    finalRate = rate;
                }
            } else if (ppt == PlayersPerTeam) {
                finalName = name;
                finalRate = rate;
            }
        }

        double modifier = ppt / 4D + finalRate; // solo=1, wingman=1.15, duos=1.25, trios=1.5, quads=1.75
        for (Spawner spawner : arena.getSpawners()) {
            spawner.addDropDurationModifier(finalName, MBedwarsSpawnerRates.getInstance(), SpawnerDurationModifier.Operation.MULTIPLY, modifier);
        }

        Bukkit.getLogger().info("Set spawner rates for " + arena.getName() + " : " + finalName + "-" + finalRate);
    }


}
