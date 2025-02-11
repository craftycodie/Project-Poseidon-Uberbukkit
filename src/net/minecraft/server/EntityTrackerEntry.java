package net.minecraft.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import pl.moresteck.uberbukkit.Uberbukkit;

public class EntityTrackerEntry {

    public Entity tracker;
    // uberbukkit
    public boolean b_ = false;
    public boolean c_ = false;
    public boolean d_ = false;

    public int b;
    public int c;
    public int d;
    public int e;
    public int f;
    public int g;
    public int h;
    public double i;
    public double j;
    public double k;
    public int l = 0;
    private double o;
    private double p;
    private double q;
    private boolean r = false;
    private boolean isMoving;
    private int t = 0;
    public boolean m = false;
    public Set trackedPlayers = new HashSet();

    public EntityTrackerEntry(Entity entity, int i, int j, boolean flag) {
        this.tracker = entity;
        this.b = i;
        this.c = j;
        this.isMoving = flag;
        this.d = MathHelper.floor(entity.locX * 32.0D);
        this.e = MathHelper.floor(entity.locY * 32.0D);
        this.f = MathHelper.floor(entity.locZ * 32.0D);
        this.g = MathHelper.d(entity.yaw * 256.0F / 360.0F);
        this.h = MathHelper.d(entity.pitch * 256.0F / 360.0F);
    }

    public boolean equals(Object object) {
        return object instanceof EntityTrackerEntry ? ((EntityTrackerEntry) object).tracker.id == this.tracker.id : false;
    }

    public int hashCode() {
        return this.tracker.id;
    }

    public void track(List list) {
        this.m = false;
        if (!this.r || this.tracker.e(this.o, this.p, this.q) > 16.0D) {
            this.o = this.tracker.locX;
            this.p = this.tracker.locY;
            this.q = this.tracker.locZ;
            this.r = true;
            this.m = true;
            this.scanPlayers(list);
        }

        ++this.t;
        if (++this.l % this.c == 0) {
            // encoded means multiplied by 32
            // this is required to send it to the client, as the relative position is sent as the float multiplied by 32
            int newEncodedPosX = MathHelper.floor(this.tracker.locX * 32.0D);
            int newEncodedPosY = MathHelper.floor(this.tracker.locY * 32.0D);
            int newEncodedPosZ = MathHelper.floor(this.tracker.locZ * 32.0D);
            int newEncodedRotationYaw = MathHelper.d(this.tracker.yaw * 256.0F / 360.0F);
            int newEncodedRotationPitch = MathHelper.d(this.tracker.pitch * 256.0F / 360.0F);
            int encodedDiffX = newEncodedPosX - this.d;
            int encodedDiffY = newEncodedPosY - this.e;
            int encodedDiffZ = newEncodedPosZ - this.f;
            Object packet = null;
            // mob movement fix, credit to Oldmana#7086 from the Modification Station discord server
            // https://discordapp.com/channels/397834523028488203/397839387465089054/684637208199823377
            int movementUpdateTreshold = 1;
            int rotationUpdateTreshold = 1;
            boolean needsPositionUpdate = Math.abs(encodedDiffX) >= movementUpdateTreshold || Math.abs(encodedDiffY) >= movementUpdateTreshold || Math.abs(encodedDiffZ) >= movementUpdateTreshold
                    || tracker instanceof EntityBoat || tracker instanceof EntityMinecart;

            boolean needsRotationUpdate = Math.abs(newEncodedRotationYaw - this.g) >= rotationUpdateTreshold || Math.abs(newEncodedRotationPitch - this.h) >= rotationUpdateTreshold;

            if (encodedDiffX >= -128 && encodedDiffX < 128 && encodedDiffY >= -128 && encodedDiffY < 128 && encodedDiffZ >= -128 && encodedDiffZ < 128 && this.t <= 400) {
                // entity has moved less than 4 blocks
                if (needsPositionUpdate && needsRotationUpdate) {
                    packet = new Packet33RelEntityMoveLook(this.tracker.id, (byte) encodedDiffX, (byte) encodedDiffY, (byte) encodedDiffZ, (byte) newEncodedRotationYaw, (byte) newEncodedRotationPitch);
                } else if (needsPositionUpdate) {
                    packet = new Packet31RelEntityMove(this.tracker.id, (byte) encodedDiffX, (byte) encodedDiffY, (byte) encodedDiffZ);
                } else if (needsRotationUpdate) {
                    packet = new Packet32EntityLook(this.tracker.id, (byte) newEncodedRotationYaw, (byte) newEncodedRotationPitch);
                }
            } else {
                this.t = 0;
                // minecart clipping fix
                //this.tracker.locX = (double) i / 32.0D;
                //this.tracker.locY = (double) j / 32.0D;
                //this.tracker.locZ = (double) k / 32.0D;
                // entity has moved more than 4 blocks, send teleport
                packet = new Packet34EntityTeleport(this.tracker.id, newEncodedPosX, newEncodedPosY, newEncodedPosZ, (byte) newEncodedRotationYaw, (byte) newEncodedRotationPitch);
            }

            if (this.isMoving) {
                double d0 = this.tracker.motX - this.i;
                double d1 = this.tracker.motY - this.j;
                double d2 = this.tracker.motZ - this.k;
                double d3 = 0.02D;
                double d4 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d4 > d3 * d3 || d4 > 0.0D && this.tracker.motX == 0.0D && this.tracker.motY == 0.0D && this.tracker.motZ == 0.0D) {
                    this.i = this.tracker.motX;
                    this.j = this.tracker.motY;
                    this.k = this.tracker.motZ;
                    this.a((Packet) (new Packet28EntityVelocity(this.tracker.id, this.i, this.j, this.k)));
                }
            }

            if (packet != null) {
                this.a((Packet) packet);
            }

            // uberbukkit
            if (Uberbukkit.getProtocolHandler().canReceivePacket(40)) {
                DataWatcher datawatcher = this.tracker.aa();

                if (datawatcher.a()) {
                    this.b((Packet) (new Packet40EntityMetadata(this.tracker.id, datawatcher)));
                }
            } else {
                if (this.b_ && this.tracker.vehicle == null) {
                    this.b_ = false;
                    this.b((Packet) (new Packet18ArmAnimation(this.tracker, 101)));
                } else if (!this.b_ && this.tracker.vehicle != null) {
                    this.b_ = true;
                    this.b((Packet) (new Packet18ArmAnimation(this.tracker, 100)));
                }

                if (this.tracker instanceof EntityLiving) {
                    if (this.d_ && !this.tracker.isSneaking()) {
                        this.d_ = false;
                        this.b((Packet) (new Packet18ArmAnimation(this.tracker, 105)));
                    } else if (!this.d_ && this.tracker.isSneaking()) {
                        this.d_ = true;
                        this.b((Packet) (new Packet18ArmAnimation(this.tracker, 104)));
                    }
                }

                if (this.c_ && this.tracker.fireTicks <= 0) {
                    this.c_ = false;
                    this.b((Packet) (new Packet18ArmAnimation(this.tracker, 103)));
                } else if (!this.c_ && this.tracker.fireTicks > 0) {
                    this.c_ = true;
                    this.b((Packet) (new Packet18ArmAnimation(this.tracker, 102)));
                }
            }

            if (needsPositionUpdate) {
                this.d = newEncodedPosX;
                this.e = newEncodedPosY;
                this.f = newEncodedPosZ;
            }

            if (needsRotationUpdate) {
                this.g = newEncodedRotationYaw;
                this.h = newEncodedRotationPitch;
            }
        }

        if (this.tracker.velocityChanged) {
            // CraftBukkit start - create PlayerVelocity event
            boolean cancelled = false;

            if(this.tracker instanceof EntityPlayer) {
                org.bukkit.entity.Player player = (org.bukkit.entity.Player) this.tracker.getBukkitEntity();
                org.bukkit.util.Vector velocity = player.getVelocity();

                org.bukkit.event.player.PlayerVelocityEvent event = new org.bukkit.event.player.PlayerVelocityEvent(player, velocity);
                this.tracker.world.getServer().getPluginManager().callEvent(event);

                if(event.isCancelled()) {
                    cancelled = true;
                }
                else if(!velocity.equals(event.getVelocity())) {
                    player.setVelocity(velocity);
                }
            }

            if(!cancelled) {
                this.b((Packet) (new Packet28EntityVelocity(this.tracker)));
            }
            // CraftBukkit end
            this.tracker.velocityChanged = false;
        }
    }

    public void a(Packet packet) {
        Iterator iterator = this.trackedPlayers.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            entityplayer.netServerHandler.sendPacket(packet);
        }
    }

    public void b(Packet packet) {
        this.a(packet);
        if (this.tracker instanceof EntityPlayer) {
            ((EntityPlayer) this.tracker).netServerHandler.sendPacket(packet);
        }
    }

    public void a() {
        this.a((Packet) (new Packet29DestroyEntity(this.tracker.id)));
    }

    public void a(EntityPlayer entityplayer) {
        if (this.trackedPlayers.contains(entityplayer)) {
            this.trackedPlayers.remove(entityplayer);
        }
    }

    public void b(EntityPlayer entityplayer) {
        if (entityplayer != this.tracker) {
            double d0 = entityplayer.locX - (double) (this.d / 32);
            double d1 = entityplayer.locZ - (double) (this.f / 32);

            if (d0 >= (double) (-this.b) && d0 <= (double) this.b && d1 >= (double) (-this.b) && d1 <= (double) this.b) {
                if (!this.trackedPlayers.contains(entityplayer)) {
                    // CraftBukkit start
                    if (tracker instanceof EntityPlayer) {
                        org.bukkit.entity.Player player = (Player) ((EntityPlayer) tracker).getBukkitEntity();
                        if (!((Player) entityplayer.getBukkitEntity()).canSee(player)) {
                            return;
                        }
                    }
                    // CraftBukkit end
                    this.trackedPlayers.add(entityplayer);
                    entityplayer.netServerHandler.sendPacket(this.b());

                    // uberbukkit
                    if (!Uberbukkit.getProtocolHandler().canReceivePacket(40)) {
                        if (this.d_) {
                            entityplayer.netServerHandler.sendPacket((Packet) (new Packet18ArmAnimation(this.tracker, 104)));
                        }

                        if (this.b_) {
                            entityplayer.netServerHandler.sendPacket((Packet) (new Packet18ArmAnimation(this.tracker, 100)));
                        }

                        if (this.c_) {
                            entityplayer.netServerHandler.sendPacket((Packet) (new Packet18ArmAnimation(this.tracker, 102)));
                        }
                    }

                    if (this.isMoving) {
                        entityplayer.netServerHandler.sendPacket(new Packet28EntityVelocity(this.tracker.id, this.tracker.motX, this.tracker.motY, this.tracker.motZ));
                    }

                    ItemStack[] aitemstack = this.tracker.getEquipment();

                    if (aitemstack != null) {
                        for (int i = 0; i < aitemstack.length; ++i) {
                            entityplayer.netServerHandler.sendPacket(new Packet5EntityEquipment(this.tracker.id, i, aitemstack[i]));
                        }
                    }

                    if (this.tracker instanceof EntityHuman) {
                        EntityHuman entityhuman = (EntityHuman) this.tracker;

                        // uberbukkit
                        if (entityhuman.isSleeping() && Uberbukkit.getProtocolHandler().canReceivePacket(17)) {
                            entityplayer.netServerHandler.sendPacket(new Packet17(this.tracker, 0, MathHelper.floor(this.tracker.locX), MathHelper.floor(this.tracker.locY), MathHelper.floor(this.tracker.locZ)));
                        }
                    }
                }
            } else if (this.trackedPlayers.contains(entityplayer)) {
                this.trackedPlayers.remove(entityplayer);
                entityplayer.netServerHandler.sendPacket(new Packet29DestroyEntity(this.tracker.id));
            }
        }
    }

    public void scanPlayers(List list) {
        for (int i = 0; i < list.size(); ++i) {
            this.b((EntityPlayer) list.get(i));
        }
    }

    private Packet b() {
        if (this.tracker instanceof EntityItem) {
            EntityItem entityitem = (EntityItem) this.tracker;
            Packet21PickupSpawn packet21pickupspawn = new Packet21PickupSpawn(entityitem);

            // There's no reason to set the item's position to the compressed position
            //entityitem.locX = (double) packet21pickupspawn.b / 32.0D;
            //entityitem.locY = (double) packet21pickupspawn.c / 32.0D;
            //entityitem.locZ = (double) packet21pickupspawn.d / 32.0D;
            return packet21pickupspawn;
        } else if (this.tracker instanceof EntityPlayer) {
            // CraftBukkit start - limit name length to 16 characters
            if (((EntityHuman) this.tracker).name.length() > 16) {
                ((EntityHuman) this.tracker).name = ((EntityHuman) this.tracker).name.substring(0, 16);
            }
            // CraftBukkit end
            return new Packet20NamedEntitySpawn((EntityHuman) this.tracker);
        } else {
            if (this.tracker instanceof EntityMinecart) {
                EntityMinecart entityminecart = (EntityMinecart) this.tracker;

                if (entityminecart.type == 0) {
                    return new Packet23VehicleSpawn(this.tracker, 10);
                }

                if (entityminecart.type == 1) {
                    return new Packet23VehicleSpawn(this.tracker, 11);
                }

                if (entityminecart.type == 2) {
                    return new Packet23VehicleSpawn(this.tracker, 12);
                }
            }

            if (this.tracker instanceof EntityBoat) {
                return new Packet23VehicleSpawn(this.tracker, 1);
            } else if (this.tracker instanceof IAnimal) {
                return new Packet24MobSpawn((EntityLiving) this.tracker);
            } else if (this.tracker instanceof EntityFish) {
                return new Packet23VehicleSpawn(this.tracker, 90);
            } else if (this.tracker instanceof EntityArrow) {
                EntityLiving entityliving = ((EntityArrow) this.tracker).shooter;

                return new Packet23VehicleSpawn(this.tracker, 60, entityliving != null ? entityliving.id : this.tracker.id);
                // uberbukkit
            } else if (this.tracker instanceof EntitySnowball || (this.tracker instanceof EntityFireball && Uberbukkit.getPVN() < 12)) {
                return new Packet23VehicleSpawn(this.tracker, 61);
            } else if (this.tracker instanceof EntityFireball && Uberbukkit.getPVN() >= 12) {
                EntityFireball entityfireball = (EntityFireball) this.tracker;
                // CraftBukkit start - added check for null shooter
                int shooter = ((EntityFireball) this.tracker).shooter != null ? ((EntityFireball) this.tracker).shooter.id : 1;
                Packet23VehicleSpawn packet23vehiclespawn = new Packet23VehicleSpawn(this.tracker, 63, shooter);
                // CraftBukkit end

                packet23vehiclespawn.e = (int) (entityfireball.c * 8000.0D);
                packet23vehiclespawn.f = (int) (entityfireball.d * 8000.0D);
                packet23vehiclespawn.g = (int) (entityfireball.e * 8000.0D);
                return packet23vehiclespawn;
            } else if (this.tracker instanceof EntityEgg) {
                return new Packet23VehicleSpawn(this.tracker, 62);
            } else if (this.tracker instanceof EntityTNTPrimed) {
                return new Packet23VehicleSpawn(this.tracker, 50);
            } else {
                if (this.tracker instanceof EntityFallingSand) {
                    EntityFallingSand entityfallingsand = (EntityFallingSand) this.tracker;

                    if (entityfallingsand.a == Block.SAND.id) {
                        return new Packet23VehicleSpawn(this.tracker, 70);
                    }

                    if (entityfallingsand.a == Block.GRAVEL.id) {
                        return new Packet23VehicleSpawn(this.tracker, 71);
                    }
                }

                if (this.tracker instanceof EntityPainting) {
                    return new Packet25EntityPainting((EntityPainting) this.tracker);
                } else {
                    throw new IllegalArgumentException("Don\'t know how to add " + this.tracker.getClass() + "!");
                }
            }
        }
    }

    public void c(EntityPlayer entityplayer) {
        if (this.trackedPlayers.contains(entityplayer)) {
            this.trackedPlayers.remove(entityplayer);
            entityplayer.netServerHandler.sendPacket(new Packet29DestroyEntity(this.tracker.id));
        }
    }
}
