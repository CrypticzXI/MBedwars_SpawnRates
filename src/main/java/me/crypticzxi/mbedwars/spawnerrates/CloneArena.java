package me.crypticzxi.mbedwars.spawnerrates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import com.grinderwolf.swm.api.exceptions.UnknownWorldException;
import de.marcely.bedwars.api.world.hologram.HologramControllerType;
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
import de.marcely.bedwars.api.event.arena.RoundEndEvent;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.event.arena.ArenaIssuesCheckEvent.Issue;
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

        parts[parts.length - 1] = "" + (++num);
        newName = String.join("-", parts);
        Arena checkArena = GameAPI.get().getArenaByExactName(newName);

        Bukkit.getLogger().info("Set new arena name: " + newName + " Number ==== " + num);

        if (checkArena == null) {

            // GREAT! We can create a new arena.

            File oldSlime = new File("plugins/MBedwars/data/arenablocks/" + arena.getName() + ".slime");
            File newSlime = new File("slime_worlds/" + newName + ".slime");
            
            if ( !oldSlime.exists() ) {
            	Bukkit.getLogger().severe(
            			String.format(
            					"Error onRoundStartEvent: Slime world source does not exist: %s", 
            					oldSlime.getAbsoluteFile() ));
            	return;
            }
            
            if ( newSlime.exists() ) {
            	Bukkit.getLogger().severe(
            			String.format(
            					"Error onRoundStartEvent: Slime world destination already exists "
            					+ "and will be replaced: %s", 
            					newSlime.getAbsoluteFile() ));
            }
            
            
            try {
                Files.copy(oldSlime.toPath(), newSlime.toPath());
                
                Bukkit.getLogger().info("copied files from arenablocks to SlimeWorldManager.");
            } catch (IOException e) {
            	Bukkit.getLogger().severe(
            			String.format(
            					"Error onRoundStartEvent: Slime world copy failure. " +
            					"Source: %s Target: %s  Error: [%s]",
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
                Bukkit.getLogger().info("Loaded Slimeworld.");
            } catch (Exception e) {
            	Bukkit.getLogger().severe(
            			String.format(
            					"Error onRoundStartEvent: Slime world load failure. " +
            							"Slime world: %s  Error: [%s]",
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

                if(holo.getControllerType() == HologramControllerType.DEALER) {

                    loc.setWorld(newWorld);
                    newWs.spawnHologram(holo.getControllerType(), loc);

                }
                else if (holo.getControllerType() == HologramControllerType.UPGRADE_DEALER) {
                    loc.setWorld(newWorld);
                    newWs.spawnHologram(holo.getControllerType(), loc);
                }

            }
            Bukkit.getLogger().info("Spawned Holograms in new arena.");

            try {
                Arena newArena = GameAPI.get().createArena().setRegenerationType(RegenerationType.WORLD).setWorld(newWorld).setName(newName).finish();

                Bukkit.getLogger().info("Creating new arena via MBedwars.");

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

                Bukkit.getLogger().info("Added Details to Arena.");

                for (Spawner spawner : arena.getSpawners()) {
                    newArena.addSpawner(new XYZ(spawner.getLocation()), spawner.getDropType());
                }
                Bukkit.getLogger().info("Set Spawner Locations.");

                if (!newArena.getIssues().isEmpty()) {
                	
                	Bukkit.getLogger().severe(
                			String.format(
                					"Error onRoundStartEvent: Slime world cloning failure. " +
                							"New Arena: %s",
                							newArena.getName() ));
                	
                	for (Issue issue : newArena.getIssues() ) {
                		Bukkit.getLogger().severe(
                				String.format(
                						"Error onRoundStartEvent: Issue Type: %s Detail: %s.",
                								issue.getType(),
                								issue.getDetail() ));
                                                                                   		
					}
                	
                    return; // we didn't set everything
                }

                newArena.setStatus(ArenaStatus.LOBBY);
                Bukkit.getLogger().info("Set Arena Status to Lobby.");
                newArena.save();
                Bukkit.getLogger().info("Saved Arena.");

            } catch (ArenaBuildException arenaBuildException) {
            	Bukkit.getLogger().severe("Error onRoundStartEvent: Something went extremely wrong.");
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

        arena.remove();
        Bukkit.getLogger().info("Removed Arena.");

        SlimePlugin slimeAPI = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");

        File newSlime = new File("slime_worlds/" + arena.getName() + ".slime");

        SlimeWorld slimeWorld = null;

        try {
            slimeWorld = slimeAPI.loadWorld(
                    slimeAPI.getLoader("file"),
                    newSlime.getName().replace(".slime", ""),
                    true,
                    new SlimePropertyMap());
            Bukkit.getLogger().info("Loaded Slimeworld.");
        } catch (Exception e) {
            Bukkit.getLogger().severe(
                    String.format(
                            "Error onRoundStartEvent: Slime world load failure. " +
                                    "Slime world: %s  Error: [%s]",
                            newSlime.getAbsoluteFile(),
                            e.getMessage() ));
            e.printStackTrace();
        }

        try {
            slimeWorld.getLoader().deleteWorld("file");
            Bukkit.getLogger().info("Slimeworld Deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().info("Slimeworld Failed to Delete.");
        }




    }







}
