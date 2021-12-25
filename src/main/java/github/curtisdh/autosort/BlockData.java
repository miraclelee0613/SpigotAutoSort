package github.curtisdh.autosort;

import org.bukkit.Material;

public class BlockData
{
    boolean validSign;
    Material material;
    public BlockData(boolean bool, Material mat)
    {
        validSign = bool;
        material = mat;
    }
}
