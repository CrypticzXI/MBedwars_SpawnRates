package me.crypticzxi.mbedwars_spawnerrates.me.crypticzxi.mbedwars_spawnerrates.Me.crypticzxi;

import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.game.spawner.SpawnerDurationModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class SpawnerRates implements Listener {

    @EventHandler
    public void onRoundStartEvent(RoundStartEvent event) {

        int ppt = event.getArena().getPlayersPerTeam();
        int teams = event.getArena().getEnabledTeams().size();

        ConfigurationSection speedRates = MBedwars_SpawnerRates.plugin.getConfig().getConfigurationSection("spawner-rates");
        Set<String> speedRateKeys = speedRates.getKeys(false);

        Arena arena = event.getArena();

        String final_name = "none";
        double final_rate = 0;

        for (String speedRateKey : speedRateKeys) {
            ConfigurationSection speedRate = speedRates.getConfigurationSection(speedRateKey);
            String name = speedRate.getString("name");
            double rate = speedRate.getDouble("spawn-rate");
            int PlayersPerTeam = speedRate.getInt("playersPerTeam");

            if (speedRate.contains("NoOfTeams")) { // is a special Gamemode.
                int NoOfTeams = speedRate.getInt("NoOfTeams");
                if (ppt == PlayersPerTeam & NoOfTeams == teams) {
                    final_name = name;
                    final_rate = rate;
                }
            } else if (ppt == PlayersPerTeam) {
                final_name = name;
                final_rate = rate;
            }
        }

        double modifier = ppt / 4D + final_rate; // solo=1, wingman=1.15, duos=1.25, trios=1.5, quads=1.75
        for (Spawner spawner : arena.getSpawners()) {
            spawner.addDropDurationModifier(final_name, MBedwars_SpawnerRates.plugin, SpawnerDurationModifier.Operation.MULTIPLY, modifier);
        }
    }


}
