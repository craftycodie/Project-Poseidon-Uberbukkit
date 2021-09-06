package net.minecraft.server;

import pl.moresteck.uberbukkit.Uberbukkit;

public enum EnumToolMaterial {

	// TODO old tool durability etc
	// TODO old ore generation, pre-b1.2
    WOOD("WOOD", 0, 0, 59, 2.0F, 0), STONE("STONE", 1, 1, 131, 4.0F, 1), IRON("IRON", 2, 2, 250, 6.0F, 2), DIAMOND("EMERALD", 3, 3, 1561, 8.0F, 3), GOLD("GOLD", Uberbukkit.getPVN() >= 8 ? 4 : 1, 0, 32, 12.0F, 0);
    private final int f;
    private final int g;
    private final float h;
    private final int i;

    private static final EnumToolMaterial[] j = new EnumToolMaterial[] { WOOD, STONE, IRON, DIAMOND, GOLD};

    private EnumToolMaterial(String s, int i, int j, int k, float f, int l) {
        this.f = j;
        this.g = k;
        this.h = f;
        this.i = l;
    }

    public int a() {
        return this.g;
    }

    public float b() {
        return this.h;
    }

    public int c() {
        return this.i;
    }

    public int d() {
        return this.f;
    }
}
