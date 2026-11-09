package com.yungnickyoung.minecraft.betterdeserttemples.entity;

import net.minecraft.world.phys.Vec3;

public interface IPharaohData {
    void setOriginalSpawnPos(Vec3 pos);
    Vec3 getOriginalSpawnPos();

    /** Marks this husk as a pharaoh for its whole life, independent of the head it wears (saved as bdtPharaoh). */
    void setMarkedPharaoh(boolean isPharaoh);
    boolean isMarkedPharaoh();
}
