package org.spoutcraft.client.universe.block.material;

import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;

/**
 *
 */
public abstract class Material {
    private static final TShortObjectMap<MasterMaterial> MATERIALS_BY_ID = new TShortObjectHashMap<>();
    private final short id;
    private final short subID;

    protected Material(short id, short subID) {
        this.id = id;
        this.subID = subID;
    }

    public short getID() {
        return id;
    }

    public short getSubID() {
        return subID;
    }

    protected static void register(MasterMaterial material) {
        final MasterMaterial previous = MATERIALS_BY_ID.put(material.getID(), material);
        if (previous != null) {
            System.out.println("New material has conflicting ID, previous material was overwritten: " + previous + " => " + material);
        }
    }

    public static Material get(short id) {
        return get(id, (short) 0);
    }

    public static Material getPacked(int packed) {
        return get((short) (packed >> 16), (short) packed);
    }

    public static Material get(short id, short subID) {
        final MasterMaterial master = MATERIALS_BY_ID.get(id);
        if (master == null || subID == 0) {
            return master;
        }
        return master.getSubMaterial(subID);
    }

    @Override
    public String toString() {
        return "Material{" +
                "id=" + id +
                ", subID=" + subID +
                '}';
    }
}
