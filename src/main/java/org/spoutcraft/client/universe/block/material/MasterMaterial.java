package org.spoutcraft.client.universe.block.material;

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;

/**
 *
 */
public class MasterMaterial extends Material {
    private final TShortObjectMap<SubMaterial> subMaterials = new TShortObjectHashMap<>();

    public MasterMaterial(short id) {
        super(id, (short) 0);
        register(this);
    }

    public SubMaterial getSubMaterial(short subID) {
        return subMaterials.get(subID);
    }

    protected void addSubMaterial(SubMaterial subMaterial) {
        subMaterials.put(subMaterial.getSubID(), subMaterial);
    }
}
