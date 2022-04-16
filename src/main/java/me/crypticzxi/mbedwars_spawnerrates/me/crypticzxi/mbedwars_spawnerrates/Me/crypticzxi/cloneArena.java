package me.crypticzxi.mbedwars_spawnerrates.me.crypticzxi.mbedwars_spawnerrates.Me.crypticzxi;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.exceptions.CorruptedWorldException;
import com.grinderwolf.swm.api.exceptions.NewerFormatException;
import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import com.grinderwolf.swm.api.exceptions.WorldInUseException;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.arena.RegenerationType;
import de.marcely.bedwars.api.event.arena.RoundEndEvent;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.exception.ArenaBuildException;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.world.WorldStorage;
import de.marcely.bedwars.api.world.hologram.HologramEntity;
import de.marcely.bedwars.tools.Helper;
import de.marcely.bedwars.tools.location.XYZ;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class cloneArena implements Listener {

    @EventHandler
    public void onRoundStartEvent(RoundStartEvent event) {

        Arena arena = event.getArena();
        String oldName = arena.getName();
        String[] parts = oldName.split("-");
        Integer num = Helper.get().parseInt(parts[parts.length - 1]); // Gets the Number of Arena. Pokemon-Solos-<1>
        String newName = null;

        // Example arena.getName(); == Pokemon-Solos-1, We need to change that to be Pokemon-Solos-2

        parts[parts.length - 1] = "" + (num++);
        newName = String.join("-", parts);
        Arena check_arena = GameAPI.get().getArenaByExactName(newName);

        if (check_arena == null) {

            // GREAT! We can create a new arena.

            File old_slime = new File("plugins/MBedwars/data/arenablocks/" + arena.getName() + ".slime");
            File new_slime = new File("slime_worlds/" + newName + ".slime");

            try {
                Files.copy(old_slime.toPath(), new_slime.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            SlimePlugin slimeAPI = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

            SlimeWorld slimeWorld = null;
            try {
                slimeWorld = slimeAPI.loadWorld(
                        slimeAPI.getLoader("file"),
                        new_slime.getName().replace(".slime", ""),
                        true,
                        new SlimePropertyMap());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // generate it
            slimeAPI.generateWorld(slimeWorld);


            World new_world = Bukkit.getWorld(newName);

            World oldWorld = arena.getGameWorld();
            WorldStorage newWs = BedwarsAPI.getWorldStorage(new_world);
            WorldStorage oldWs = BedwarsAPI.getWorldStorage(oldWorld);

            for (HologramEntity holo : oldWs.getHolograms()) {
                Location loc = holo.getLocation();

                loc.setWorld(new_world);
                newWs.spawnHologram(holo.getControllerType(), loc);
            }

            try {
                Arena new_arena = GameAPI.get().createArena().setRegenerationType(RegenerationType.WORLD).setWorld(new_world).setName(newName).finish();

                new_arena.setCustomNameEnabled(true);
                new_arena.setCustomName(arena.getDisplayName());
                new_arena.addAuthors(Arrays.asList(arena.getAuthors()));
                new_arena.setMinPlayers(arena.getMinPlayers());
                new_arena.setPlayersPerTeam(arena.getPlayersPerTeam());
                // new_arena.setIcon(arena.getIcon());
                new_arena.setLobbyLocation(arena.getLobbyLocation());
                arena.getEnabledTeams().forEach(t -> {
                    new_arena.setTeamEnabled(t, true);
                    new_arena.setBedLocation(t, arena.getBedLocation(t));
                    new_arena.setTeamSpawn(t, arena.getTeamSpawn(t));
                });

                for (Spawner spawner : arena.getSpawners()) {
                    new_arena.addSpawner(new XYZ(spawner.getLocation()), spawner.getDropType());
                }

                if (!new_arena.getIssues().isEmpty()) {
                    return; // we didn't set everything

                }

                new_arena.setStatus(ArenaStatus.LOBBY);
                new_arena.save();

            } catch (ArenaBuildException arenaBuildException) {
                arenaBuildException.printStackTrace();
            }

        }


    }

    @EventHandler
    public void onRoundEndEvent(RoundEndEvent event) {

        Arena arena = event.getArena();
        String Name = arena.getName();
        String[] parts = Name.split("-");
        Integer num = Helper.get().parseInt(parts[parts.length - 1]); // Gets the Number of Arena. Pokemon-Solos-<1>


        if (num != null && num != 1) {

            arena.remove();

            World slime = arena.getGameWorld();

            Bukkit.unloadWorld(slime, false);

            File slime_world = new File("slime_worlds/" + Name + ".slime");

            slime_world.delete();

        }

    }







}
