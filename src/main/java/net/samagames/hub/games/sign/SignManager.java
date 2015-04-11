package net.samagames.hub.games.sign;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.samagames.hub.Hub;
import net.samagames.hub.common.JsonConfiguration;
import net.samagames.hub.common.managers.AbstractManager;
import net.samagames.hub.games.AbstractGame;
import net.samagames.tools.LocationUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class SignManager extends AbstractManager
{
    private final JsonConfiguration jsonConfig;
    private Pacman pacman;

    public SignManager(Hub hub)
    {
        super(hub);

        File config = new File(this.hub.getDataFolder(), "signs.json");

        if(!config.exists())
        {
            try
            {
                config.createNewFile();

                FileWriter fw = new FileWriter(config.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("{\"zones\":[]}");
                bw.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        this.jsonConfig = new JsonConfiguration(config);
        this.pacman = null;

        this.reloadList();
    }

    public void reloadList()
    {
        this.hub.log(this, Level.INFO, "Reloading game sign list...");

        JsonArray signZonesArray = this.jsonConfig.load().getAsJsonArray("zones");

        for(int i = 0; i < signZonesArray.size(); i++)
        {
            JsonObject signZoneObject = signZonesArray.get(i).getAsJsonObject();

            String game = signZoneObject.get("game").getAsString();

            JsonArray maps = signZoneObject.get("maps").getAsJsonArray();

            for(int j = 0; j < maps.size(); j++)
            {
                JsonObject mapObject = maps.get(j).getAsJsonObject();
                String map = mapObject.get("map").getAsString();

                JsonArray signs = mapObject.get("signs").getAsJsonArray();
                ArrayList<Location> signsLocations = new ArrayList<>();

                for(int k = 0; k < signs.size(); k++)
                {
                    String location = signs.get(k).getAsString();
                    signsLocations.add(LocationUtils.str2loc(location));
                }

                AbstractGame gameObject = this.hub.getGameManager().getGameByIdentifier(game);
                GameSignZone signZone = new GameSignZone(gameObject, map, signsLocations);

                gameObject.addSignZone(map, signZone);
                this.hub.log(this, Level.INFO, "Registered sign zone for the game '" + game + "' and the map '" + map + "'!");
            }
        }

        this.hub.log(this, Level.INFO, "Reloaded game sign list.");
    }

    public void addZone(Player player, String game, String map, ArrayList<Sign> signs)
    {
        JsonObject root = this.jsonConfig.load();
        JsonArray signZonesArray = root.getAsJsonArray("zones");

        player.sendMessage(ChatColor.GREEN + "Starting job...");

        for(int i = 0; i < signZonesArray.size(); i++)
        {
            JsonObject signZoneObject = signZonesArray.get(i).getAsJsonObject();

            if(signZoneObject.get("game").getAsString().equals(game))
            {
                player.sendMessage(ChatColor.GREEN + "Game existing.");

                JsonArray maps = signZoneObject.get("maps").getAsJsonArray();

                for(int j = 0; j < maps.size(); j++)
                {
                    JsonObject mapObject = maps.get(j).getAsJsonObject();

                    if (mapObject.get("map").getAsString().equals(map))
                    {
                        player.sendMessage(ChatColor.GREEN + "Map existing.");

                        JsonArray signsArray = mapObject.get("signs").getAsJsonArray();

                        for (Sign sign : signs)
                        {
                            signsArray.add(new JsonPrimitive(LocationUtils.loc2str(sign.getLocation())));
                            player.sendMessage(ChatColor.GREEN + "Added sign (" + LocationUtils.loc2str(sign.getLocation()) + ")");
                        }

                        mapObject.add("signs", signsArray);

                        player.sendMessage(ChatColor.GREEN + "Job finished.");
                        this.jsonConfig.save(root);
                        this.reloadList();
                        return;
                    }
                }

                JsonObject mapObject = new JsonObject();
                mapObject.addProperty("map", map);

                JsonArray signsArray = new JsonArray();

                for (Sign sign : signs)
                {
                    signsArray.add(new JsonPrimitive(LocationUtils.loc2str(sign.getLocation())));
                    player.sendMessage(ChatColor.GREEN + "Added sign (" + LocationUtils.loc2str(sign.getLocation()) + ")");
                }

                mapObject.add("signs", signsArray);
                maps.add(mapObject);

                player.sendMessage(ChatColor.GREEN + "Job finished.");
                this.jsonConfig.save(root);
                this.reloadList();
                return;
            }
        }

        player.sendMessage(ChatColor.RED + "Game don't exist!");

        JsonObject signZoneObject = new JsonObject();
        signZoneObject.addProperty("game", game);

        JsonArray maps = new JsonArray();

        JsonObject mapObject = new JsonObject();
        mapObject.addProperty("map", map);

        JsonArray signsArray = new JsonArray();

        for (Sign sign : signs)
        {
            signsArray.add(new JsonPrimitive(LocationUtils.loc2str(sign.getLocation())));
            player.sendMessage(ChatColor.GREEN + "Added sign (" + LocationUtils.loc2str(sign.getLocation()) + ")");
        }

        mapObject.add("signs", signsArray);
        maps.add(mapObject);

        signZoneObject.add("maps", maps);
        signZonesArray.add(signZoneObject);

        player.sendMessage(ChatColor.GREEN + "Job finished.");
        this.jsonConfig.save(root);
        this.reloadList();
    }

    public void startPacman(Player player)
    {
        this.pacman = new Pacman(player);
    }

    public void stopPacman()
    {
        this.pacman.stop();
        this.pacman = null;
    }

    public Pacman getPacman()
    {
        return this.pacman;
    }

    public boolean isPacmanEnabled()
    {
        return this.pacman != null;
    }

    @Override
    public String getName() { return "SignManager"; }
}
