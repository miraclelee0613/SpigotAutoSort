package github.curtisdh.autosort;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class onSignRightClick implements Listener
{
    private Map<String,BlockData> blockDataMap;
    @EventHandler
    public void SignRightClick(PlayerInteractEvent event)
    {
        if(IsSignValid(event.getClickedBlock().getType()))
        {
            AutoSort.PrintWithClassName(this, "Valid Sign");
        }

    }
    public void SetBlockDataMap(Map<String,BlockData> map)
    {
        blockDataMap = map;
    }
    private boolean IsSignValid(Material sign)
    {
        for (Map.Entry<String, BlockData> validSignEntries : blockDataMap.entrySet())
        {
            if(!validSignEntries.getValue().validSign)
            {
                return false;
            }
            if(validSignEntries.getValue().material == sign)
            {
                return true;
            }
        }
        return false;

//        switch (sign) //Loop over a config
//        {
//            case OAK_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "OAK_SIGN");
//                return true;
//            case SPRUCE_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "SPRUCE_SIGN");
//                return true;
//            case BIRCH_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "BIRCH_SIGN");
//                return true;
//            case JUNGLE_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "JUNGLE_SIGN");
//                return true;
//            case ACACIA_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "ACACIA_SIGN");
//                return true;
//            case DARK_OAK_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "DARK_OAK_SIGN");
//                return true;
//            case CRIMSON_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "CRIMSON_SIGN");
//                return true;
//            case WARPED_WALL_SIGN:
//                AutoSort.PrintWithClassName(this, "WARPED_SIGN");
//                return true;
//            default:
//                return false;
//        }
    }
    /*
    Inspiration: https://www.spigotmc.org/resources/mondochest.22109/
    Okay so the way this is gonna work is we create a sign that has to have a fixed sentence on it
    [chestMaster], this is where all the items the player wants to sort/dump will be put into.
    Other chests with the relevant items will be assigned with [chestSlave]. When the [chestMaster] sign is right
    clicked, all the relevant items will be moved from the master chest to the slave chest (if it contains said items).

    How do we associate chests with players? -- Actually I dont think we need to. If the sign has attached chests
    then it may already be able to contain all the data for us.
     */
}
