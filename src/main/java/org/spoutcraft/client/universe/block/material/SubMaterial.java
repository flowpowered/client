package org.spoutcraft.client.universe.block.material;

/**
 *
 */
public class SubMaterial extends Material {
    private final Material master;

    public SubMaterial(MasterMaterial master, short subID) {
        super(master.getID(), subID);
        if (master == null) {
            throw new IllegalArgumentException("Master material cannot be null");
        }
        if (subID == 0) {
            throw new IllegalArgumentException("Sub ID 0 is reserved for the master material");
        }
        this.master = master;
        master.addSubMaterial(this);
    }

    public Material getMaster() {
        return master;
    }
}
