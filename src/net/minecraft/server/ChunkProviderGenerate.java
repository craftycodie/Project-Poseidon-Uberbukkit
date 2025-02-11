package net.minecraft.server;

import java.util.Calendar;
import java.util.Random;

import com.legacyminecraft.poseidon.PoseidonConfig;

public class ChunkProviderGenerate implements IChunkProvider {

    private Random j;
    private NoiseGeneratorOctaves k;
    private NoiseGeneratorOctaves l;
    private NoiseGeneratorOctaves m;
    private NoiseGeneratorOctaves n;
    private NoiseGeneratorOctaves o;
    public NoiseGeneratorOctaves a;
    public NoiseGeneratorOctaves b;
    public NoiseGeneratorOctaves c;
    private World p;
    private double[] q;
    private double[] r = new double[256];
    private double[] s = new double[256];
    private double[] t = new double[256];
    private MapGenBase u = new MapGenCaves();
    private BiomeBase[] v;
    double[] d;
    double[] e;
    double[] f;
    double[] g;
    double[] h;
    int[][] i = new int[32][32];
    private double[] w;

    public ChunkProviderGenerate(World world, long i) {
        this.p = world;
        this.j = new Random(i);
        this.k = new NoiseGeneratorOctaves(this.j, 16);
        this.l = new NoiseGeneratorOctaves(this.j, 16);
        this.m = new NoiseGeneratorOctaves(this.j, 8);
        this.n = new NoiseGeneratorOctaves(this.j, 4);
        this.o = new NoiseGeneratorOctaves(this.j, 4);
        this.a = new NoiseGeneratorOctaves(this.j, 10);
        this.b = new NoiseGeneratorOctaves(this.j, 16);
        this.c = new NoiseGeneratorOctaves(this.j, 8);
    }

    public void a(int i, int j, byte[] abyte, BiomeBase[] abiomebase, double[] adouble) {
        byte b0 = 4;
        byte b1 = 64;
        int k = b0 + 1;
        byte b2 = 17;
        int l = b0 + 1;

        this.q = this.a(this.q, i * b0, 0, j * b0, k, b2, l);

        for (int i1 = 0; i1 < b0; ++i1) {
            for (int j1 = 0; j1 < b0; ++j1) {
                for (int k1 = 0; k1 < 16; ++k1) {
                    double d0 = 0.125D;
                    double d1 = this.q[((i1 + 0) * l + j1 + 0) * b2 + k1 + 0];
                    double d2 = this.q[((i1 + 0) * l + j1 + 1) * b2 + k1 + 0];
                    double d3 = this.q[((i1 + 1) * l + j1 + 0) * b2 + k1 + 0];
                    double d4 = this.q[((i1 + 1) * l + j1 + 1) * b2 + k1 + 0];
                    double d5 = (this.q[((i1 + 0) * l + j1 + 0) * b2 + k1 + 1] - d1) * d0;
                    double d6 = (this.q[((i1 + 0) * l + j1 + 1) * b2 + k1 + 1] - d2) * d0;
                    double d7 = (this.q[((i1 + 1) * l + j1 + 0) * b2 + k1 + 1] - d3) * d0;
                    double d8 = (this.q[((i1 + 1) * l + j1 + 1) * b2 + k1 + 1] - d4) * d0;

                    for (int l1 = 0; l1 < 8; ++l1) {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i2 = 0; i2 < 4; ++i2) {
                            int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k1 * 8 + l1;
                            short short1 = 128;
                            double d14 = 0.25D;
                            double d15 = d10;
                            double d16 = (d11 - d10) * d14;

                            for (int k2 = 0; k2 < 4; ++k2) {
                                double d17 = adouble[(i1 * 4 + i2) * 16 + j1 * 4 + k2];
                                int l2 = 0;

                                if (k1 * 8 + l1 < b1) {
                                    if (d17 < 0.5D && k1 * 8 + l1 >= b1 - 1) {
                                        l2 = Block.ICE.id;
                                    } else {
                                        l2 = Block.STATIONARY_WATER.id;
                                    }
                                }

                                if (d15 > 0.0D) {
                                    l2 = Block.STONE.id;
                                }

                                abyte[j2] = (byte) l2;
                                j2 += short1;
                                d15 += d16;
                            }

                            d10 += d12;
                            d11 += d13;
                        }

                        d1 += d5;
                        d2 += d6;
                        d3 += d7;
                        d4 += d8;
                    }
                }
            }
        }
    }

    public void a(int i, int j, byte[] abyte, BiomeBase[] abiomebase) {
        byte b0 = 64;
        double d0 = 0.03125D;

        this.r = this.n.a(this.r, (double) (i * 16), (double) (j * 16), 0.0D, 16, 16, 1, d0, d0, 1.0D);
        this.s = this.n.a(this.s, (double) (i * 16), 109.0134D, (double) (j * 16), 16, 1, 16, d0, 1.0D, d0);
        this.t = this.o.a(this.t, (double) (i * 16), (double) (j * 16), 0.0D, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeBase biomebase = abiomebase[k + l * 16];
                boolean flag = this.r[k + l * 16] + this.j.nextDouble() * 0.2D > 0.0D;
                boolean flag1 = this.s[k + l * 16] + this.j.nextDouble() * 0.2D > 3.0D;
                int i1 = (int) (this.t[k + l * 16] / 3.0D + 3.0D + this.j.nextDouble() * 0.25D);
                int j1 = -1;
                byte b1 = biomebase.p;
                byte b2 = biomebase.q;

                for (int k1 = 127; k1 >= 0; --k1) {
                    int l1 = (l * 16 + k) * 128 + k1;

                    if (k1 <= 0 + this.j.nextInt(5)) {
                        abyte[l1] = (byte) Block.BEDROCK.id;
                    } else {
                        byte b3 = abyte[l1];

                        if (b3 == 0) {
                            j1 = -1;
                        } else if (b3 == Block.STONE.id) {
                            if (j1 == -1) {
                                if (i1 <= 0) {
                                    b1 = 0;
                                    b2 = (byte) Block.STONE.id;
                                } else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
                                    b1 = biomebase.p;
                                    b2 = biomebase.q;
                                    if (flag1) {
                                        b1 = 0;
                                    }

                                    if (flag1) {
                                        b2 = (byte) Block.GRAVEL.id;
                                    }

                                    if (flag) {
                                        b1 = (byte) Block.SAND.id;
                                    }

                                    if (flag) {
                                        b2 = (byte) Block.SAND.id;
                                    }
                                }

                                if (k1 < b0 && b1 == 0) {
                                    b1 = (byte) Block.STATIONARY_WATER.id;
                                }

                                j1 = i1;
                                if (k1 >= b0 - 1) {
                                    abyte[l1] = b1;
                                } else {
                                    abyte[l1] = b2;
                                }
                            } else if (j1 > 0) {
                                --j1;
                                abyte[l1] = b2;
                                // uberbukkit
                                if (PoseidonConfig.getInstance().getBoolean("version.worldgen.generate_sandstone", true) && j1 == 0 && b2 == Block.SAND.id) {
                                    j1 = this.j.nextInt(4);
                                    b2 = (byte) Block.SANDSTONE.id;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Chunk getChunkAt(int i, int j) {
        return this.getOrCreateChunk(i, j);
    }

    public Chunk getOrCreateChunk(int i, int j) {
        this.j.setSeed((long) i * 341873128712L + (long) j * 132897987541L);
        byte[] abyte = new byte['\u8000'];
        Chunk chunk = new Chunk(this.p, abyte, i, j);

        this.v = this.p.getWorldChunkManager().a(this.v, i * 16, j * 16, 16, 16);
        double[] adouble = this.p.getWorldChunkManager().temperature;

        this.a(i, j, abyte, this.v, adouble);
        this.a(i, j, abyte, this.v);
        this.u.a(this, this.p, i, j, abyte);
        chunk.initLighting();
        return chunk;
    }

    private double[] a(double[] adouble, int i, int j, int k, int l, int i1, int j1) {
        if (adouble == null) {
            adouble = new double[l * i1 * j1];
        }

        double d0 = 684.412D;
        double d1 = 684.412D;
        double[] adouble1 = this.p.getWorldChunkManager().temperature;
        double[] adouble2 = this.p.getWorldChunkManager().rain;

        this.g = this.a.a(this.g, i, k, l, j1, 1.121D, 1.121D, 0.5D);
        this.h = this.b.a(this.h, i, k, l, j1, 200.0D, 200.0D, 0.5D);
        this.d = this.m.a(this.d, (double) i, (double) j, (double) k, l, i1, j1, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
        this.e = this.k.a(this.e, (double) i, (double) j, (double) k, l, i1, j1, d0, d1, d0);
        this.f = this.l.a(this.f, (double) i, (double) j, (double) k, l, i1, j1, d0, d1, d0);
        int k1 = 0;
        int l1 = 0;
        int i2 = 16 / l;

        for (int j2 = 0; j2 < l; ++j2) {
            int k2 = j2 * i2 + i2 / 2;

            for (int l2 = 0; l2 < j1; ++l2) {
                int i3 = l2 * i2 + i2 / 2;
                double d2 = adouble1[k2 * 16 + i3];
                double d3 = adouble2[k2 * 16 + i3] * d2;
                double d4 = 1.0D - d3;

                d4 *= d4;
                d4 *= d4;
                d4 = 1.0D - d4;
                double d5 = (this.g[l1] + 256.0D) / 512.0D;

                d5 *= d4;
                if (d5 > 1.0D) {
                    d5 = 1.0D;
                }

                double d6 = this.h[l1] / 8000.0D;

                if (d6 < 0.0D) {
                    d6 = -d6 * 0.3D;
                }

                d6 = d6 * 3.0D - 2.0D;
                if (d6 < 0.0D) {
                    d6 /= 2.0D;
                    if (d6 < -1.0D) {
                        d6 = -1.0D;
                    }

                    d6 /= 1.4D;
                    d6 /= 2.0D;
                    d5 = 0.0D;
                } else {
                    if (d6 > 1.0D) {
                        d6 = 1.0D;
                    }

                    d6 /= 8.0D;
                }

                if (d5 < 0.0D) {
                    d5 = 0.0D;
                }

                d5 += 0.5D;
                d6 = d6 * (double) i1 / 16.0D;
                double d7 = (double) i1 / 2.0D + d6 * 4.0D;

                ++l1;

                for (int j3 = 0; j3 < i1; ++j3) {
                    double d8 = 0.0D;
                    double d9 = ((double) j3 - d7) * 12.0D / d5;

                    if (d9 < 0.0D) {
                        d9 *= 4.0D;
                    }

                    double d10 = this.e[k1] / 512.0D;
                    double d11 = this.f[k1] / 512.0D;
                    double d12 = (this.d[k1] / 10.0D + 1.0D) / 2.0D;

                    if (d12 < 0.0D) {
                        d8 = d10;
                    } else if (d12 > 1.0D) {
                        d8 = d11;
                    } else {
                        d8 = d10 + (d11 - d10) * d12;
                    }

                    d8 -= d9;
                    if (j3 > i1 - 4) {
                        double d13 = (double) ((float) (j3 - (i1 - 4)) / 3.0F);

                        d8 = d8 * (1.0D - d13) + -10.0D * d13;
                    }

                    adouble[k1] = d8;
                    ++k1;
                }
            }
        }

        return adouble;
    }

    public boolean isChunkLoaded(int i, int j) {
        return true;
    }

    public void getChunkAt(IChunkProvider ichunkprovider, int i, int j) {
        BlockSand.instaFall = true;
        int k = i * 16;
        int l = j * 16;
        BiomeBase biomebase = this.p.getWorldChunkManager().getBiome(k + 16, l + 16);

        this.j.setSeed(this.p.getSeed());
        long i1 = this.j.nextLong() / 2L * 2L + 1L;
        long j1 = this.j.nextLong() / 2L * 2L + 1L;

        this.j.setSeed((long) i * i1 + (long) j * j1 ^ this.p.getSeed());
        double d0 = 0.25D;
        int k1;
        int l1;
        int i2;

        if (this.j.nextInt(4) == 0) {
            k1 = k + this.j.nextInt(16) + 8;
            l1 = this.j.nextInt(128);
            i2 = l + this.j.nextInt(16) + 8;
            (new WorldGenLakes(Block.STATIONARY_WATER.id)).a(this.p, this.j, k1, l1, i2);
        }

        if (this.j.nextInt(8) == 0) {
            k1 = k + this.j.nextInt(16) + 8;
            l1 = this.j.nextInt(this.j.nextInt(120) + 8);
            i2 = l + this.j.nextInt(16) + 8;
            if (l1 < 64 || this.j.nextInt(10) == 0) {
                (new WorldGenLakes(Block.STATIONARY_LAVA.id)).a(this.p, this.j, k1, l1, i2);
            }
        }

        int j2;

        for (k1 = 0; k1 < 8; ++k1) {
            l1 = k + this.j.nextInt(16) + 8;
            i2 = this.j.nextInt(128);
            j2 = l + this.j.nextInt(16) + 8;
            (new WorldGenDungeons()).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 10; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(128);
            j2 = l + this.j.nextInt(16);
            (new WorldGenClay(32)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 20; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(128);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.DIRT.id, 32)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 10; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(128);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.GRAVEL.id, 32)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 20; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(128);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.COAL_ORE.id, 16)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 20; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(64);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.IRON_ORE.id, 8)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 2; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(32);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.GOLD_ORE.id, 8)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 8; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(16);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.REDSTONE_ORE.id, 7)).a(this.p, this.j, l1, i2, j2);
        }

        for (k1 = 0; k1 < 1; ++k1) {
            l1 = k + this.j.nextInt(16);
            i2 = this.j.nextInt(16);
            j2 = l + this.j.nextInt(16);
            (new WorldGenMinable(Block.DIAMOND_ORE.id, 7)).a(this.p, this.j, l1, i2, j2);
        }

        // uberbukkit
        if (PoseidonConfig.getInstance().getBoolean("version.worldgen.generate_lapis_ores", true)) {
            for (k1 = 0; k1 < 1; ++k1) {
                l1 = k + this.j.nextInt(16);
                i2 = this.j.nextInt(16) + this.j.nextInt(16);
                j2 = l + this.j.nextInt(16);
                (new WorldGenMinable(Block.LAPIS_ORE.id, 6)).a(this.p, this.j, l1, i2, j2);
            }
        }

        d0 = 0.5D;
        k1 = (int) ((this.c.a((double) k * d0, (double) l * d0) / 8.0D + this.j.nextDouble() * 4.0D + 4.0D) / 3.0D);
        l1 = 0;
        if (this.j.nextInt(10) == 0) {
            ++l1;
        }

        if (biomebase == BiomeBase.FOREST) {
            l1 += k1 + 5;
        }

        if (biomebase == BiomeBase.RAINFOREST) {
            l1 += k1 + 5;
        }

        if (biomebase == BiomeBase.SEASONAL_FOREST) {
            l1 += k1 + 2;
        }

        if (biomebase == BiomeBase.TAIGA) {
            l1 += k1 + 5;
        }

        if (biomebase == BiomeBase.DESERT) {
            l1 -= 20;
        }

        if (biomebase == BiomeBase.TUNDRA) {
            l1 -= 20;
        }

        if (biomebase == BiomeBase.PLAINS) {
            l1 -= 20;
        }

        // uberbukkit
        Object object = biomebase.a(this.j);
        if (PoseidonConfig.getInstance().getBoolean("version.worldgen.pre_b1_2_ore_generation", false)) {
            object = new WorldGenTrees();
            if (this.j.nextInt(10) == 0) {
                object = new WorldGenBigTree();
            }

            if (biomebase == BiomeBase.RAINFOREST && this.j.nextInt(3) == 0) {
                object = new WorldGenBigTree();
            }
        }

        int k2;

        for (i2 = 0; i2 < l1; ++i2) {
            j2 = k + this.j.nextInt(16) + 8;
            k2 = l + this.j.nextInt(16) + 8;
            // uberbukkit
            WorldGenerator worldgenerator = (WorldGenerator) object;

            worldgenerator.a(1.0D, 1.0D, 1.0D);
            worldgenerator.a(this.p, this.j, j2, this.p.getHighestBlockYAt(j2, k2), k2);
        }

        byte b0 = 0;

        if (biomebase == BiomeBase.FOREST) {
            b0 = 2;
        }

        if (biomebase == BiomeBase.SEASONAL_FOREST) {
            b0 = 4;
        }

        if (biomebase == BiomeBase.TAIGA) {
            b0 = 2;
        }

        if (biomebase == BiomeBase.PLAINS) {
            b0 = 3;
        }

        int l2;
        int i3;

        for (j2 = 0; j2 < b0; ++j2) {
            k2 = k + this.j.nextInt(16) + 8;
            i3 = this.j.nextInt(128);
            l2 = l + this.j.nextInt(16) + 8;
            (new WorldGenFlowers(Block.YELLOW_FLOWER.id)).a(this.p, this.j, k2, i3, l2);
        }

        byte b1 = 0;

        if (biomebase == BiomeBase.FOREST) {
            b1 = 2;
        }

        if (biomebase == BiomeBase.RAINFOREST) {
            b1 = 10;
        }

        if (biomebase == BiomeBase.SEASONAL_FOREST) {
            b1 = 2;
        }

        if (biomebase == BiomeBase.TAIGA) {
            b1 = 1;
        }

        if (biomebase == BiomeBase.PLAINS) {
            b1 = 10;
        }

        int j3;
        int k3;

        // uberbukkit
        if (PoseidonConfig.getInstance().getBoolean("version.worldgen.generate_tallgrass", true)) {
            for (k2 = 0; k2 < b1; ++k2) {
                byte b2 = 1;

                if (biomebase == BiomeBase.RAINFOREST && this.j.nextInt(3) != 0) {
                    b2 = 2;
                }

                l2 = k + this.j.nextInt(16) + 8;
                k3 = this.j.nextInt(128);
                j3 = l + this.j.nextInt(16) + 8;
                (new WorldGenGrass(Block.LONG_GRASS.id, b2)).a(this.p, this.j, l2, k3, j3);
            }
        }

        b1 = 0;
        if (biomebase == BiomeBase.DESERT) {
            b1 = 2;
        }

        // uberbukkit
        if (PoseidonConfig.getInstance().getBoolean("version.worldgen.generate_tallgrass", true)) {
            for (k2 = 0; k2 < b1; ++k2) {
                i3 = k + this.j.nextInt(16) + 8;
                l2 = this.j.nextInt(128);
                k3 = l + this.j.nextInt(16) + 8;
                (new WorldGenDeadBush(Block.DEAD_BUSH.id)).a(this.p, this.j, i3, l2, k3);
            }
        }

        if (this.j.nextInt(2) == 0) {
            k2 = k + this.j.nextInt(16) + 8;
            i3 = this.j.nextInt(128);
            l2 = l + this.j.nextInt(16) + 8;
            (new WorldGenFlowers(Block.RED_ROSE.id)).a(this.p, this.j, k2, i3, l2);
        }

        if (this.j.nextInt(4) == 0) {
            k2 = k + this.j.nextInt(16) + 8;
            i3 = this.j.nextInt(128);
            l2 = l + this.j.nextInt(16) + 8;
            (new WorldGenFlowers(Block.BROWN_MUSHROOM.id)).a(this.p, this.j, k2, i3, l2);
        }

        if (this.j.nextInt(8) == 0) {
            k2 = k + this.j.nextInt(16) + 8;
            i3 = this.j.nextInt(128);
            l2 = l + this.j.nextInt(16) + 8;
            (new WorldGenFlowers(Block.RED_MUSHROOM.id)).a(this.p, this.j, k2, i3, l2);
        }

        for (k2 = 0; k2 < 10; ++k2) {
            i3 = k + this.j.nextInt(16) + 8;
            l2 = this.j.nextInt(128);
            k3 = l + this.j.nextInt(16) + 8;
            (new WorldGenReed()).a(this.p, this.j, i3, l2, k3);
        }

        if (this.j.nextInt(32) == 0) {
            k2 = k + this.j.nextInt(16) + 8;
            i3 = this.j.nextInt(128);
            l2 = l + this.j.nextInt(16) + 8;
            (new WorldGenPumpkin()).a(this.p, this.j, k2, i3, l2);
        }

        k2 = 0;
        if (biomebase == BiomeBase.DESERT) {
            k2 += 10;
        }

        for (i3 = 0; i3 < k2; ++i3) {
            l2 = k + this.j.nextInt(16) + 8;
            k3 = this.j.nextInt(128);
            j3 = l + this.j.nextInt(16) + 8;
            (new WorldGenCactus()).a(this.p, this.j, l2, k3, j3);
        }

        for (i3 = 0; i3 < 50; ++i3) {
            l2 = k + this.j.nextInt(16) + 8;
            k3 = this.j.nextInt(this.j.nextInt(120) + 8);
            j3 = l + this.j.nextInt(16) + 8;
            (new WorldGenLiquids(Block.WATER.id)).a(this.p, this.j, l2, k3, j3);
        }

        for (i3 = 0; i3 < 20; ++i3) {
            l2 = k + this.j.nextInt(16) + 8;
            k3 = this.j.nextInt(this.j.nextInt(this.j.nextInt(112) + 8) + 8);
            j3 = l + this.j.nextInt(16) + 8;
            (new WorldGenLiquids(Block.LAVA.id)).a(this.p, this.j, l2, k3, j3);
        }

        this.w = this.p.getWorldChunkManager().a(this.w, k + 8, l + 8, 16, 16);

        for (i3 = k + 8; i3 < k + 8 + 16; ++i3) {
            for (l2 = l + 8; l2 < l + 8 + 16; ++l2) {
                k3 = i3 - (k + 8);
                j3 = l2 - (l + 8);
                int l3 = this.p.e(i3, l2);
                double d1 = this.w[k3 * 16 + j3] - (double) (l3 - 64) / 64.0D * 0.3D;

                if (d1 < 0.5D && l3 > 0 && l3 < 128 && this.p.isEmpty(i3, l3, l2) && this.p.getMaterial(i3, l3 - 1, l2).isSolid() && this.p.getMaterial(i3, l3 - 1, l2) != Material.ICE) {
                    this.p.setTypeId(i3, l3, l2, Block.SNOW.id);
                }
            }
        }

        // uberbukkit
        if (PoseidonConfig.getInstance().getBoolean("version.worldgen.generate_steveco_chests", false)) {
            k2 = k + this.j.nextInt(16) + 8;
            l2 = this.j.nextInt(128);
            i3 = l + this.j.nextInt(16) + 8;
            if (this.p.getTypeId(k2, l2, i3) == 0 && this.p.p(k2, l2 - 1, i3)) {
                System.out.println("added a chest!!");
                this.p.setTypeId(k2, l2, i3, Block.LOCKED_CHEST.id);
            }
        }

        BlockSand.instaFall = false;
    }

    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
        return true;
    }

    public boolean unloadChunks() {
        return false;
    }

    public boolean canSave() {
        return true;
    }
}
