package me.crypticzxi.mbedwars.spawnerrates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.arena.RegenerationType;
import de.marcely.bedwars.api.event.arena.ArenaIssuesCheckEvent.Issue;
import de.marcely.bedwars.api.event.arena.RoundEndEvent;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.exception.ArenaBuildException;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.world.WorldStorage;
import de.marcely.bedwars.api.world.hologram.HologramEntity;
import de.marcely.bedwars.tools.Helper;
import de.marcely.bedwars.tools.location.XYZ;

public class CloneArena implements Listener {

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
        Arena checkArena = GameAPI.get().getArenaByExactName(newName);

        if (checkArena == null) {

            // GREAT! We can create a new arena.

            File oldSlime = new File("plugins/MBedwars/data/arenablocks/" + arena.getName() + ".slime");
            File newSlime = new File("slime_worlds/" + newName + ".slime");

            // Create directories if they do not exist:
            oldSlime.mkdirs();
            newSlime.mkdirs();
            
            if ( !oldSlime.exists() ) {
            	MBedwarsSpawnerRates.getInstance().logSevere(
            			String.format(
            					"&4Error onRoundStartEvent: &3Slime world source does not exist: &7%s", 
            					oldSlime.getAbsoluteFile() ));
            	return;
            }
            
            if ( newSlime.exists() ) {
            	MBedwarsSpawnerRates.getInstance().logSevere(
            			String.format(
            					"&4Error onRoundStartEvent: &3Slime world destination already exists "
            					+ "and will be replaced: &7%s", 
            					newSlime.getAbsoluteFile() ));
            	
            }
            
            try {
                Files.copy(oldSlime.toPath(), newSlime.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
            	MBedwarsSpawnerRates.getInstance().logSevere(
            			String.format(
            					"&4Error onRoundStartEvent: &3Slime world copy failure. " +
            					"Source: &7%s &3Target: &7%s  &3Error: &7[&4&7]",
            							oldSlime.getAbsoluteFile(),
            							newSlime.getAbsoluteFile(),
            							e.getMessage() ));
                e.printStackTrace();
            }

            SlimePlugin slimeAPI = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

            SlimeWorld slimeWorld = null;
            try {
                slimeWorld = slimeAPI.loadWorld(
                        slimeAPI.getLoader("file"),
                        newSlime.getName().replace(".slime", ""),
                        true,
                        new SlimePropertyMap());
            } catch (Exception e) {
            	MBedwarsSpawnerRates.getInstance().logSevere(
            			String.format(
            					"&4Error onRoundStartEvent: &3Slime world load failure. " +
            							"&3Slime world: &7%s  &3Error: &7[&4&7]",
            							newSlime.getAbsoluteFile(),
            							e.getMessage() ));
                e.printStackTrace();
            }

            // generate it
            slimeAPI.generateWorld(slimeWorld);


            World newWorld = Bukkit.getWorld(newName);

            World oldWorld = arena.getGameWorld();
            WorldStorage newWs = BedwarsAPI.getWorldStorage(newWorld);
            WorldStorage oldWs = BedwarsAPI.getWorldStorage(oldWorld);

            for (HologramEntity holo : oldWs.getHolograms()) {
                Location loc = holo.getLocation();

                loc.setWorld(newWorld);
                newWs.spawnHologram(holo.getControllerType(), loc);
            }

            try {
                Arena newArena = GameAPI.get().createArena().setRegenerationType(RegenerationType.WORLD).setWorld(newWorld).setName(newName).finish();

                newArena.setCustomNameEnabled(true);
                newArena.setCustomName(arena.getDisplayName());
                newArena.addAuthors(Arrays.asList(arena.getAuthors()));
                newArena.setMinPlayers(arena.getMinPlayers());
                newArena.setPlayersPerTeam(arena.getPlayersPerTeam());
                newArena.setIcon(arena.getIcon());
                newArena.setLobbyLocation(arena.getLobbyLocation());
                arena.getEnabledTeams().forEach(t -> {
                    newArena.setTeamEnabled(t, true);
                    newArena.setBedLocation(t, arena.getBedLocation(t));
                    newArena.setTeamSpawn(t, arena.getTeamSpawn(t));
                });

                for (Spawner spawner : arena.getSpawners()) {
                    newArena.addSpawner(new XYZ(spawner.getLocation()), spawner.getDropType());
                }

                if (!newArena.getIssues().isEmpty()) {
                	
                	MBedwarsSpawnerRates.getInstance().logSevere(
                			String.format(
                					"&4Error onRoundStartEvent: &3Slime world cloning failure. " +
                							"&3Arena: &7%s",
                							newArena.getName() ));
                	
                	for (Issue issue : newArena.getIssues() ) {
                		MBedwarsSpawnerRates.getInstance().logSevere(
                				String.format(
                						"&4Error onRoundStartEvent: &3Issue Type: &7%s &3Detail: &7%s." +
                								"&3Arena: &7%s",
                								issue.getType(),
                								issue.getDetail() ));
                                                                                   		
					}
                	
                    return; // we didn't set everything
                }

                newArena.setStatus(ArenaStatus.LOBBY);

            } catch (ArenaBuildException arenaBuildException) {
                arenaBuildException.printStackTrace();
            }

        }


    }

    @EventHandler
    public void onRoundEndEvent(RoundEndEvent event) {

        Arena arena = event.getArena();
        String name = arena.getName();
        String[] parts = name.split("-");
        Integer num = Helper.get().parseInt(parts[parts.length - 1]); // Gets the Number of Arena. Pokemon-Solos-<1>


        if (num != null && num != 1) {

            arena.remove();

            World slime = arena.getGameWorld();

            Bukkit.unloadWorld(slime, false);

            File slimeWorld = new File("slime_worlds/" + name + ".slime");

            if ( slimeWorld.exists() ) {
            	
            	slimeWorld.delete();

            	MBedwarsSpawnerRates.getInstance().logInfo(
            			String.format(
            					"&onRoundEndEvent: &3slimeWorld deleted: &7%s", 
            					slimeWorld.getAbsoluteFile() ));
            }
            else {
            	MBedwarsSpawnerRates.getInstance().logInfo(
            			String.format(
            					"&4Error onRoundEndEvent: &3slimeWorld cannot be deleted since it not exist: &7%s", 
            					slimeWorld.getAbsoluteFile() ));
            }

        }

    }







}
