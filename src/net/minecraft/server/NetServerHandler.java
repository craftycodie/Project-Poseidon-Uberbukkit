package net.minecraft.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandException;
import org.bukkit.craftbukkit.ChunkCompressionThread;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.TextWrapper;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.packet.PacketReceivedEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.event.PlayerSendPacketEvent;
import com.projectposeidon.ConnectionType;

import pl.moresteck.uberbukkit.Uberbukkit;
import pl.moresteck.uberbukkit.protocol.Protocol;

// CraftBukkit start
// CraftBukkit end

public class NetServerHandler extends NetHandler implements ICommandListener {

    public static Logger a = Logger.getLogger("Minecraft");
    public NetworkManager networkManager;
    public boolean disconnected = false;
    private MinecraftServer minecraftServer;
    public EntityPlayer player; // CraftBukkit - private -> public
    private int f;
    private int g;
    private int h;
    private boolean i;
    private double x;
    private double y;
    private double z;
    private boolean checkMovement = true;
    private Map n = new HashMap();
    private boolean usingReleaseToBeta = false; //Project Poseidon - Create Variable
    private ConnectionType connectionType = ConnectionType.NORMAL; //Project Poseidon - Create Variable
    private int rawConnectionType = 0; //Project Poseidon - Create Variable
    private boolean receivedKeepAlive = false;
    private boolean firePacketEvents;

    public boolean isReceivedKeepAlive() {
        return receivedKeepAlive;
    }

    public void setReceivedKeepAlive(boolean receivedKeepAlive) {
        this.receivedKeepAlive = receivedKeepAlive;
    }

    public NetServerHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
        networkmanager.a((NetHandler) this);
        this.player = entityplayer;
        entityplayer.netServerHandler = this;

        // CraftBukkit start
        this.server = minecraftserver.server;
        this.firePacketEvents = PoseidonConfig.getInstance().getBoolean("settings.packet-events.enabled", false); //Poseidon
    }

    //Project Poseidon - Start
    public boolean isUsingReleaseToBeta() {
        return usingReleaseToBeta;
    }

    public void setUsingReleaseToBeta(boolean usingReleaseToBeta) {
        this.usingReleaseToBeta = usingReleaseToBeta;
    }

    public ConnectionType getConnectionType() {
        return this.connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public void setRawConnectionType(int rawConnectionType) {
        this.rawConnectionType = rawConnectionType;
    }

    public int getRawConnectionType() {
        return this.rawConnectionType;
    }

    //Project Poseidon - End

    private final CraftServer server;
    private int lastTick = MinecraftServer.currentTick;
    private int lastDropTick = MinecraftServer.currentTick;
    private int dropCount = 0;
    private static final int PLACE_DISTANCE_SQUARED = 6 * 6;

    // Get position of last block hit for BlockDamageLevel.STOPPED
    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;

    // For the packet15 hack :(
    Long lastPacket;

    // Store the last block right clicked and what type it was
    private int lastMaterial;

    public CraftPlayer getPlayer() {
        return (this.player == null) ? null : (CraftPlayer) this.player.getBukkitEntity();
    }
    // CraftBukkit end

    // uberbukkit
    public Integer lastDigX = null;
    public Integer lastDigY = null;
    public Integer lastDigZ = null;
    
    public void a() {
        this.i = false;
        this.networkManager.b();
        if (this.f - this.g > 20) {
            this.sendPacket(new Packet0KeepAlive());
        }
    }

    public void disconnect(String s) {
        // CraftBukkit start
        String leaveMessage = "\u00A7e" + this.player.name + " left the game.";

        PlayerKickEvent event = new PlayerKickEvent(this.server.getPlayer(this.player), s, leaveMessage);
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            // Do not kick the player
            return;
        }
        // Send the possibly modified leave message
        s = event.getReason();
        // CraftBukkit end

        this.player.B();
        this.sendPacket(new Packet255KickDisconnect(s));
        this.networkManager.d();

        // CraftBukkit start
        leaveMessage = event.getLeaveMessage();
        if (leaveMessage != null) {
            this.minecraftServer.serverConfigurationManager.sendAll(new Packet3Chat(leaveMessage));
        }
        // CraftBukkit end

        this.minecraftServer.serverConfigurationManager.disconnect(this.player);
        this.disconnected = true;
    }

    // uberbukkit
    public void a(Packet5EntityEquipment packet5) {
        if (Uberbukkit.getPVN() > 6) return;

        System.out.println("PACKET 5 received");
        this.player.packet5.process(packet5);
    }

    public void a(Packet27 packet27) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet27);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        this.player.a(packet27.c(), packet27.e(), packet27.g(), packet27.h(), packet27.d(), packet27.f());
    }

    public void a(Packet10Flying packet10flying) {
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet10flying);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        this.i = true;
        double d0;

        if (!this.checkMovement) {
            d0 = packet10flying.y - this.y;
            if (packet10flying.x == this.x && d0 * d0 < 0.01D && packet10flying.z == this.z) {
                this.checkMovement = true;
            }
        }

        // CraftBukkit start
        Player player = this.getPlayer();
        Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get the Players previous Event location.
        Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

        // If the packet contains movement information then we update the To location with the correct XYZ.
        if (packet10flying.h && !(packet10flying.h && packet10flying.y == -999.0D && packet10flying.stance == -999.0D)) {
            to.setX(packet10flying.x);
            to.setY(packet10flying.y);
            to.setZ(packet10flying.z);
        }

        // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
        if (packet10flying.hasLook) {
            to.setYaw(packet10flying.yaw);
            to.setPitch(packet10flying.pitch);
        }

        // Prevent 40 event-calls for less than a single pixel of movement >.>
        double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
        float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

        if ((delta > 1f / 256 || deltaAngle > 10f) && (this.checkMovement && !this.player.dead)) {
            this.player.isInWorkbench = false; // uberbukkit - cancel workbench status when player moves
            this.lastPosX = to.getX();
            this.lastPosY = to.getY();
            this.lastPosZ = to.getZ();
            this.lastYaw = to.getYaw();
            this.lastPitch = to.getPitch();

            // Skip the first time we do this
            if (from.getX() != Double.MAX_VALUE) {
                PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
                this.server.getPluginManager().callEvent(event);

                // If the event is cancelled we move the player back to their old location.
                if (event.isCancelled()) {
                    this.player.netServerHandler.sendPacket(new Packet13PlayerLookMove(from.getX(), from.getY() + 1.6200000047683716D, from.getY(), from.getZ(), from.getYaw(), from.getPitch(), false));
                    return;
                }

                /* If a Plugin has changed the To destination then we teleport the Player
                   there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                   We only do this if the Event was not cancelled. */
                if (!to.equals(event.getTo()) && !event.isCancelled()) {
                    this.player.getBukkitEntity().teleport(event.getTo());
                    return;
                }

                /* Check to see if the Players Location has some how changed during the call of the event.
                   This can happen due to a plugin teleporting the player instead of using .setTo() */
                if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
                    this.justTeleported = false;
                    return;
                }
            }
        }

        if (Double.isNaN(packet10flying.x) || Double.isNaN(packet10flying.y) || Double.isNaN(packet10flying.z) || Double.isNaN(packet10flying.stance)) {
            player.teleport(player.getWorld().getSpawnLocation());
            System.err.println(player.getName() + " was caught trying to crash the server with an invalid position.");
            player.kickPlayer("Nope!");
            return;
        }

        if (this.checkMovement && !this.player.dead) {
            // CraftBukkit end
            double d1;
            double d2;
            double d3;
            double d4;

            if (this.player.vehicle != null) {
                float f = this.player.yaw;
                float f1 = this.player.pitch;

                this.player.vehicle.f();
                d1 = this.player.locX;
                d2 = this.player.locY;
                d3 = this.player.locZ;
                double d5 = 0.0D;

                d4 = 0.0D;
                if (packet10flying.hasLook) {
                    f = packet10flying.yaw;
                    f1 = packet10flying.pitch;
                }

                if (packet10flying.h && packet10flying.y == -999.0D && packet10flying.stance == -999.0D) {
                    d5 = packet10flying.x;
                    d4 = packet10flying.z;
                }

                this.player.onGround = packet10flying.g;
                this.player.a(true);
                this.player.move(d5, 0.0D, d4);
                this.player.setLocation(d1, d2, d3, f, f1);
                this.player.motX = d5;
                this.player.motZ = d4;
                if (this.player.vehicle != null) {
                    worldserver.vehicleEnteredWorld(this.player.vehicle, true);
                }

                if (this.player.vehicle != null) {
                    this.player.vehicle.f();
                }

                this.minecraftServer.serverConfigurationManager.d(this.player);
                this.x = this.player.locX;
                this.y = this.player.locY;
                this.z = this.player.locZ;
                worldserver.playerJoinedWorld(this.player);
                return;
            }

            if (this.player.isSleeping()) {
                this.player.a(true);
                this.player.setLocation(this.x, this.y, this.z, this.player.yaw, this.player.pitch);
                worldserver.playerJoinedWorld(this.player);
                return;
            }

            d0 = this.player.locY;
            this.x = this.player.locX;
            this.y = this.player.locY;
            this.z = this.player.locZ;
            d1 = this.player.locX;
            d2 = this.player.locY;
            d3 = this.player.locZ;
            float f2 = this.player.yaw;
            float f3 = this.player.pitch;

            if (packet10flying.h && packet10flying.y == -999.0D && packet10flying.stance == -999.0D) {
                packet10flying.h = false;
            }

            if (packet10flying.h) {
                d1 = packet10flying.x;
                d2 = packet10flying.y;
                d3 = packet10flying.z;
                d4 = packet10flying.stance - packet10flying.y;
                if (!this.player.isSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
                    this.disconnect("Illegal stance");
                    a.warning(this.player.name + " had an illegal stance: " + d4);
                    return;
                }

                if (Math.abs(packet10flying.x) > 3.2E7D || Math.abs(packet10flying.z) > 3.2E7D) {
                    this.disconnect("Illegal position");
                    return;
                }
            }

            if (packet10flying.hasLook) {
                f2 = packet10flying.yaw;
                f3 = packet10flying.pitch;
            }

            this.player.a(true);
            this.player.br = 0.0F;
            this.player.setLocation(this.x, this.y, this.z, f2, f3);
            if (!this.checkMovement) {
                return;
            }

            d4 = d1 - this.player.locX;
            double d6 = d2 - this.player.locY;
            double d7 = d3 - this.player.locZ;
            double d8 = d4 * d4 + d6 * d6 + d7 * d7;

            if (d8 > 200.0D && this.checkMovement) { // CraftBukkit - Added this.checkMovement condition to solve this check being triggered by teleports
                a.warning(this.player.name + " moved too quickly!");
                this.disconnect("You moved too quickly :( (Hacking?)");
                return;
            }

            float f4 = 0.0625F;
            boolean flag = worldserver.getEntities(this.player, this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4)).size() == 0;

            this.player.move(d4, d6, d7);
            d4 = d1 - this.player.locX;
            d6 = d2 - this.player.locY;
            if (d6 > -0.5D || d6 < 0.5D) {
                d6 = 0.0D;
            }

            d7 = d3 - this.player.locZ;
            d8 = d4 * d4 + d6 * d6 + d7 * d7;
            boolean flag1 = false;

            if (d8 > 0.0625D && !this.player.isSleeping()) {
                flag1 = true;
                a.warning(this.player.name + " moved wrongly!");
                System.out.println("Got position " + d1 + ", " + d2 + ", " + d3);
                System.out.println("Expected " + this.player.locX + ", " + this.player.locY + ", " + this.player.locZ);
            }

            this.player.setLocation(d1, d2, d3, f2, f3);
            boolean flag2 = worldserver.getEntities(this.player, this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4)).size() == 0;

            if (flag && (flag1 || !flag2) && !this.player.isSleeping()) {
                this.a(this.x, this.y, this.z, f2, f3);
                return;
            }

            AxisAlignedBB axisalignedbb = this.player.boundingBox.clone().b((double) f4, (double) f4, (double) f4).a(0.0D, -0.55D, 0.0D);

            if (!this.minecraftServer.allowFlight && !worldserver.b(axisalignedbb)) {
                if (d6 >= -0.03125D) {
                    ++this.h;
                    if (this.h > 80) {
                        a.warning(this.player.name + " was kicked for floating too long!");
                        this.disconnect("Flying is not enabled on this server");
                        return;
                    }
                }
            } else {
                this.h = 0;
            }

            this.player.onGround = packet10flying.g;
            this.minecraftServer.serverConfigurationManager.d(this.player);
            this.player.b(this.player.locY - d0, packet10flying.g);
        }
    }

    public void a(double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit start - Delegate to teleport(Location)
        Player player = this.getPlayer();
        Location from = player.getLocation();
        Location to = new Location(this.getPlayer().getWorld(), d0, d1, d2, f, f1);
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);
        this.server.getPluginManager().callEvent(event);

        from = event.getFrom();
        to = event.isCancelled() ? from : event.getTo();

        this.teleport(to);
    }

    public void teleport(Location dest) {
        double d0, d1, d2;
        float f, f1;

        d0 = dest.getX();
        d1 = dest.getY();
        d2 = dest.getZ();
        f = dest.getYaw();
        f1 = dest.getPitch();

        // TODO: make sure this is the best way to address this.
        if (Float.isNaN(f)) {
            f = 0;
        }

        if (Float.isNaN(f1)) {
            f1 = 0;
        }

        this.lastPosX = d0;
        this.lastPosY = d1;
        this.lastPosZ = d2;
        this.lastYaw = f;
        this.lastPitch = f1;
        this.justTeleported = true;
        // CraftBukkit end

        this.checkMovement = false;
        this.x = d0;
        this.y = d1;
        this.z = d2;
        this.player.setLocation(d0, d1, d2, f, f1);
        this.player.netServerHandler.sendPacket(new Packet13PlayerLookMove(d0, d1 + 1.6200000047683716D, d1, d2, f, f1, false));
    }

    // uberbukkit
    public void a(Packet21PickupSpawn packet21) {
        // copy from craftbukkit
        if (this.lastDropTick != MinecraftServer.currentTick) {
            this.dropCount = 0;
            this.lastDropTick = MinecraftServer.currentTick;
        } else {
            // Else we increment the drop count and check the amount.
            this.dropCount++;
            if (this.dropCount >= 20) {
                a.warning(this.player.name + " dropped their items too quickly!");
                this.disconnect("You dropped your items too quickly (Hacking?)");
            }
        }
        // drop itemstack
        ItemStack hand = this.player.inventory.items[this.player.inventory.itemInHandIndex];
        ItemStack todrop = null;

        if (hand != null && hand.id == packet21.h && hand.count >= packet21.i && packet21.i == 1) {
            todrop = hand.cloneItemStack();
            todrop.count = packet21.i;
            hand.count -= packet21.i;
        } else {
            ArrayList<ItemStack> list = this.player.packet5.queue.getQueue();
            for (ItemStack stack : list) {
                if (stack.id == packet21.h && stack.count >= packet21.i) {
                    todrop = stack.cloneItemStack();
                    todrop.count = packet21.i;
                    this.player.packet5.queue.removeStackFromQueue(todrop);
                    break;
                }
            }
        }
        this.player.a(todrop, false);
        //this.player.F();
    }

    public long lastDigTick = -1;
    public void a(Packet14BlockDig packet14blockdig) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet14blockdig);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (this.player.dead) return; // CraftBukkit
//        if (packet14blockdig.e == 0 || packet14blockdig.e == 2 || packet14blockdig.e == 3) {
//            System.out.println("PACKET14: " + packet14blockdig.e + " (" + (System.nanoTime() - lastDigTick) + ")");
//            System.out.println(packet14blockdig.a + ", " + packet14blockdig.b + ", " + packet14blockdig.c);
//            lastDigTick = System.nanoTime();
//        }

        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        if (packet14blockdig.e == 4) {
            // CraftBukkit start
            // If the ticks aren't the same then the count starts from 0 and we update the lastDropTick.
            if (this.lastDropTick != MinecraftServer.currentTick) {
                this.dropCount = 0;
                this.lastDropTick = MinecraftServer.currentTick;
            } else {
                // Else we increment the drop count and check the amount.
                this.dropCount++;
                if (this.dropCount >= 20) {
                    a.warning(this.player.name + " dropped their items too quickly!");
                    this.disconnect("You dropped your items too quickly (Hacking?)");
                }
            }
            // CraftBukkit end
            this.player.F();
        } else {
            boolean flag = worldserver.weirdIsOpCache = worldserver.dimension != 0 || this.minecraftServer.serverConfigurationManager.isOp(this.player.name); // CraftBukkit
            boolean flag1 = false;

            // uberbukkit

            // Pre-b1.3 block handling
            // e == 2 is stop digging (holds no block coordinate data)
            // e == 3 is expected block break from client
            // e == 1 is digging
            // e == 0 is start digging, or digging every 5 packets (notch is weird)

            // Post-b1.2_01 block handling
            // e == 2 is stop digging
            // e == 0 is start digging
            Integer i = packet14blockdig.a;
            Integer j = packet14blockdig.b;
            Integer k = packet14blockdig.c;

            if (packet14blockdig.e == 0) {
                flag1 = true;
            }

            if (packet14blockdig.e == 1 && Uberbukkit.getPVN() <= 8) {
                flag1 = true;
            }

            if (packet14blockdig.e == 2 && Uberbukkit.getPVN() >= 9) {
                flag1 = true;
            }

            if (flag1) {
                double d0 = this.player.locX - ((double) i + 0.5D);
                double d1 = this.player.locY - ((double) j + 0.5D);
                double d2 = this.player.locZ - ((double) k + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > 36.0D) {
                    return;
                }
            }

            ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
            int l = (int) MathHelper.abs((float) (i - chunkcoordinates.x));
            int i1 = (int) MathHelper.abs((float) (k - chunkcoordinates.z));

            if (l > i1) {
                i1 = l;
            }

            if (Uberbukkit.getPVN() <= 8) {
                // CraftBukkit start
                CraftPlayer player = getPlayer();
                CraftBlock block = (CraftBlock) player.getWorld().getBlockAt(i, j, k);
                int blockId = block.getTypeId();
                float damage = 0;
                if (Block.byId[blockId] != null) {
                    damage = Block.byId[blockId].getDamage(player.getHandle()); //Get amount of damage going to block
                }
                // CraftBukkit end

                if (packet14blockdig.e == 0) {
                    // CraftBukkit start
                    if (i1 > this.server.getSpawnRadius() || flag) {
                        if (blockId > 0) {
                            BlockDamageEvent breakEvent;
                            // If the amount of damage that the player is going to do to the block
                            // is >= 1, then the block is going to break (eg, flowers, torches)
                            if (damage >= 1.0F) {
                                // if we are destroying either a redstone wire with a current greater than 0 or
                                // a redstone torch that is on, then we should notify plugins that this block has
                                // returned to a current value of 0 (since it will once the redstone is destroyed)
                                if ((blockId == Block.REDSTONE_WIRE.id && block.getData() > 0) || blockId == Block.REDSTONE_TORCH_ON.id) {
                                    server.getPluginManager().callEvent( new BlockRedstoneEvent(block, (blockId == Block.REDSTONE_WIRE.id ? block.getData() : 15), 0));
                                }
                                breakEvent = new BlockDamageEvent(player, block, player.getItemInHand(), true);
                            } else {
                                breakEvent = new BlockDamageEvent(player, block, player.getItemInHand(), false);
                            }
                            server.getPluginManager().callEvent(breakEvent);
                            if (!breakEvent.isCancelled()) {
                                this.player.itemInWorldManager.oldClick(i, j, k, packet14blockdig.face);
                            }
                        }
                    }
                    // CraftBukkit end
                } else if (packet14blockdig.e == 2) {
                    // CraftBukkit start - Get last block that the player hit
                    // Otherwise the block is a Bedrock @(0,0,0)
                    block = (CraftBlock) player.getWorld().getBlockAt(lastDigX, lastDigY, lastDigZ);
                    BlockDamageEvent breakEvent = new BlockDamageEvent(player, block, player.getItemInHand(), damage >= 1.0F);
                    server.getPluginManager().callEvent(breakEvent);
                    if (!breakEvent.isCancelled()) {
                        this.player.itemInWorldManager.oldHaltBreak();
                    }
                    // CraftBukkit end
                } else if (packet14blockdig.e == 1) {
                    // CraftBukkit start
                    if (i1 > this.server.getSpawnRadius() || flag) {
                        BlockDamageEvent breakEvent;
                        // If the amount of damage going to the block plus the current amount
                        // of damage is greater than 1, the block is going to break.
                        if (this.player.itemInWorldManager.damageDealt + damage  >= 1.0F) {
                            // if we are destroying either a redstone wire with a current greater than 0 or
                            // a redstone torch that is on, then we should notify plugins that this block has
                            // returned to a current value of 0 (since it will once the redstone is destroyed)
                            if ((blockId == Block.REDSTONE_WIRE.id && block.getData() > 0) || blockId == Block.REDSTONE_TORCH_ON.id) {
                                server.getPluginManager().callEvent( new BlockRedstoneEvent(block, (blockId == Block.REDSTONE_WIRE.id ? block.getData() : 15), 0));
                            }
                            breakEvent = new BlockDamageEvent(player, block, player.getItemInHand(), damage >= 1.0F);
                        } else {
                            breakEvent = new BlockDamageEvent(player, block, player.getItemInHand(), damage >= 1.0F);
                        }
                        server.getPluginManager().callEvent(breakEvent);
                        if (!breakEvent.isCancelled()) {
                            this.player.itemInWorldManager.oldDig(i, j, k, l);
                        } else {
                            this.player.itemInWorldManager.damageDealt = 0; // Reset the amount of damage if stopping break.
                        }
                    }
                    // CraftBukkit end
                } else if (packet14blockdig.e == 3) {
                    double d5 = this.player.locX - ((double) i + 0.5D);
                    double d6 = this.player.locY - ((double) j + 0.5D);
                    double d7 = this.player.locZ - ((double) k + 0.5D);
                    double d8 = d5 * d5 + d6 * d6 + d7 * d7;

                    if (d8 < 256.0D) {
                        this.player.netServerHandler.sendPacket((Packet) (new Packet53BlockChange(i, j, k, this.player.world))); // Craftbukkit
                    }
                }
            } else {
                if (packet14blockdig.e == 0) {
                    // CraftBukkit
                    if (i1 < this.server.getSpawnRadius() && !flag) {
                        this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
                    } else {
                        // CraftBukkit - add face argument
                        this.player.itemInWorldManager.dig(i, j, k, packet14blockdig.face);
                    }
                } else if (packet14blockdig.e == 2) {
                    // uberbukkit - swapped i,j,k for lastDigX,lastDigY,lastDigZ
                    this.player.itemInWorldManager.a(lastDigX, lastDigY, lastDigZ);
                    if (worldserver.getTypeId(lastDigX, lastDigY, lastDigZ) != 0) {
                        this.player.netServerHandler.sendPacket(new Packet53BlockChange(lastDigX, lastDigY, lastDigZ, worldserver));
                    }
                } else if (packet14blockdig.e == 3) {
                    double d4 = this.player.locX - ((double) i + 0.5D);
                    double d5 = this.player.locY - ((double) j + 0.5D);
                    double d6 = this.player.locZ - ((double) k + 0.5D);
                    double d7 = d4 * d4 + d5 * d5 + d6 * d6;

                    if (d7 < 256.0D) {
                        this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
                    }
                }
            }

            // uberbukkit - reset last positions when stops digging
            lastDigX = i;
            lastDigY = j;
            lastDigZ = k;

            worldserver.weirdIsOpCache = false;
        }
    }

    public void a(Packet15Place packet15place) {
//        System.out.println("Packet15 received");
//        System.out.println("a: " + packet15place.a);
//        System.out.println("b: " + packet15place.b);
//        System.out.println("c: " + packet15place.c);
//        System.out.println("face: " + packet15place.face);
//        System.out.println("data: " + packet15place.data);
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet15place);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        // CraftBukkit start
        if (this.player.dead) return;

        // uberbukkit: noptch what the fuck have you done
        if (Uberbukkit.getPVN() == 7) {
            if (packet15place.itemstack != null && packet15place.a != -1 && packet15place.b != 255 && packet15place.c != -1 && (packet15place.itemstack.id == Item.BUCKET.id || packet15place.itemstack.id == Item.WATER_BUCKET.id || packet15place.itemstack.id == Item.LAVA_BUCKET.id || packet15place.itemstack.id == Item.MILK_BUCKET.id)) {
                return;
            }
        }

        // This is a horrible hack needed because the client sends 2 packets on 'right mouse click'
        // aimed at a block. We shouldn't need to get the second packet if the data is handled
        // but we cannot know what the client will do, so we might still get it
        //
        // If the time between packets is small enough, and the 'signature' similar, we discard the
        // second one. This sadly has to remain until Mojang makes their packets saner. :(
        //  -- Grum

        if (packet15place.face == 255) {
            if (packet15place.itemstack != null && packet15place.itemstack.id == this.lastMaterial && this.lastPacket != null && packet15place.timestamp - this.lastPacket < 100) {
                this.lastPacket = null;
                return;
            }
        } else {
            this.lastMaterial = packet15place.itemstack == null ? -1 : packet15place.itemstack.id;
            this.lastPacket = packet15place.timestamp;
        }

        // CraftBukkit - if rightclick decremented the item, always send the update packet.
        // this is not here for CraftBukkit's own functionality; rather it is to fix
        // a notch bug where the item doesn't update correctly.
        boolean always = false;

        // CraftBukkit end

        ItemStack itemstack = this.player.inventory.getItemInHand();
        boolean flag = worldserver.weirdIsOpCache = worldserver.dimension != 0 || this.minecraftServer.serverConfigurationManager.isOp(this.player.name); // CraftBukkit

        if (packet15place.face == 255) {
            if (itemstack == null) {
                return;
            }

            // CraftBukkit start
            int itemstackAmount = itemstack.count;
            PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack);
            if (event.useItemInHand() != Event.Result.DENY) {
                this.player.itemInWorldManager.useItem(this.player, this.player.world, itemstack);
            }

            // CraftBukkit - notch decrements the counter by 1 in the above method with food,
            // snowballs and so forth, but he does it in a place that doesn't cause the
            // inventory update packet to get sent
            always = (itemstack.count != itemstackAmount);
            // CraftBukkit end
        } else {
            int i = packet15place.a;
            int j = packet15place.b;
            int k = packet15place.c;
            int l = packet15place.face;
            ChunkCoordinates chunkcoordinates = worldserver.getSpawn();
            int i1 = (int) MathHelper.abs((float) (i - chunkcoordinates.x));
            int j1 = (int) MathHelper.abs((float) (k - chunkcoordinates.z));

            if (i1 > j1) {
                j1 = i1;
            }

            // CraftBukkit start - Check if we can actually do something over this large a distance
            Location eyeLoc = this.getPlayer().getEyeLocation();
            if (Math.pow(eyeLoc.getX() - i, 2) + Math.pow(eyeLoc.getY() - j, 2) + Math.pow(eyeLoc.getZ() - k, 2) > PLACE_DISTANCE_SQUARED) {
                return;
            }
            flag = true; // spawn protection moved to ItemBlock!!!
            // CraftBukkit end

            if (j1 > 16 || flag) {
                this.player.itemInWorldManager.interact(this.player, worldserver, itemstack, i, j, k, l);
            }

            this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
            if (l == 0) {
                --j;
            }

            if (l == 1) {
                ++j;
            }

            if (l == 2) {
                --k;
            }

            if (l == 3) {
                ++k;
            }

            if (l == 4) {
                --i;
            }

            if (l == 5) {
                ++i;
            }

            this.player.netServerHandler.sendPacket(new Packet53BlockChange(i, j, k, worldserver));
        }

        itemstack = this.player.inventory.getItemInHand();
        if (itemstack != null && itemstack.count == 0) {
            this.player.inventory.items[this.player.inventory.itemInHandIndex] = null;
        }

        this.player.h = true;
        this.player.inventory.items[this.player.inventory.itemInHandIndex] = ItemStack.b(this.player.inventory.items[this.player.inventory.itemInHandIndex]);
        Slot slot = this.player.activeContainer.a(this.player.inventory, this.player.inventory.itemInHandIndex);

        this.player.activeContainer.a();
        this.player.h = false;
        // CraftBukkit
        if (!ItemStack.equals(this.player.inventory.getItemInHand(), packet15place.itemstack) || always) {
            if (Uberbukkit.getPVN() <= 6) {
                this.refreshInventory();
            } else {
                this.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, slot.a, this.player.inventory.getItemInHand()));
            }
        }

        worldserver.weirdIsOpCache = false;
    }

    // uberbukkit
    public void refreshInventory() {
        this.sendPacket(new Packet5EntityEquipment(-1, this.player.inventory.items));
        this.sendPacket(new Packet5EntityEquipment(-2, this.player.inventory.craft));
        this.sendPacket(new Packet5EntityEquipment(-3, this.player.inventory.armor));
    }

    public void a(String s, Object[] aobject) {
        if (this.disconnected) return; // CraftBukkit - rarely it would send a disconnect line twice


        if (!(boolean) PoseidonConfig.getInstance().getConfigOption("settings.remove-join-leave-debug", true) || !s.equals("disconnect.quitting")) {
            a.info(this.player.name + " lost connection: " + s);
        }

        a.info(this.player.name + " has left the game.");
        // CraftBukkit start - we need to handle custom quit messages
        String quitMessage = this.minecraftServer.serverConfigurationManager.disconnect(this.player);
        if (quitMessage != null) {
            this.minecraftServer.serverConfigurationManager.sendAll(new Packet3Chat(quitMessage));
        }
        // CraftBukkit end
        this.disconnected = true;
    }

    public void a(Packet packet) {
        a.warning(this.getClass() + " wasn\'t prepared to deal with a " + packet.getClass());
        this.disconnect("Protocol error, unexpected packet");
    }

    public void sendPacket(Packet packet) {
        //Poseidon Start - Send Packet Event
        if (firePacketEvents) {
            PlayerSendPacketEvent event = new PlayerSendPacketEvent(this.player.name, packet);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            packet = event.getPacket(); //In case a plugin replaces the entire packet
        }
        //Poseidon End


        // uberbukkit
        Protocol protocol = Uberbukkit.getProtocolHandler();
        if (!protocol.canReceivePacket(packet.b())) {
            this.g = this.f;
            return;
        }

        // try to disallow for incompatible blocks
        if (packet instanceof Packet5EntityEquipment) {
            Packet5EntityEquipment packet5 = (Packet5EntityEquipment) packet;
            if (packet5.c > 0 && !protocol.canReceiveBlockItem(packet5.c)) {
                packet5.c = -1;
                packet5.d = 0;
            }
        } else if (packet instanceof Packet53BlockChange) {
            Packet53BlockChange packet53 = (Packet53BlockChange) packet;
            if (!protocol.canReceiveBlockItem(packet53.material)) {
                packet53.material = 1;
            }
        }
        // CraftBukkit start
        else if (packet instanceof Packet6SpawnPosition) {
            Packet6SpawnPosition packet6 = (Packet6SpawnPosition) packet;
            this.player.compassTarget = new Location(this.getPlayer().getWorld(), packet6.x, packet6.y, packet6.z);
        } else if (packet instanceof Packet3Chat) {
            String message = ((Packet3Chat) packet).message;
            // uberbukkit
            String[] wrapped = null;
            if (Uberbukkit.getPVN() >= 9) { // TODO check compatibility
                wrapped = TextWrapper.wrapText(message);
            } else {
                wrapped = TextWrapper.wrapTextLegacy(message);
            }

            for (final String line : wrapped) {
                this.networkManager.queue(new Packet3Chat(line));
            }
            packet = null;
        } else if (packet.k == true) {
            // Reroute all low-priority packets through to compression thread.
            ChunkCompressionThread.sendPacket(this.player, packet);
            packet = null;
        }
        if (packet != null) this.networkManager.queue(packet);
        // CraftBukkit end

        this.g = this.f;
    }

    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet16blockitemswitch);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        if (this.player.dead) return; // CraftBukkit

        if (Uberbukkit.getPVN() >= 7) {
            if (packet16blockitemswitch.itemInHandIndex >= 0 && packet16blockitemswitch.itemInHandIndex <= InventoryPlayer.e()) {
                // CraftBukkit start
                PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), this.player.inventory.itemInHandIndex, packet16blockitemswitch.itemInHandIndex);
                this.server.getPluginManager().callEvent(event);
                // CraftBukkit end

                this.player.inventory.itemInHandIndex = packet16blockitemswitch.itemInHandIndex;
            } else {
                a.warning(this.player.name + " tried to set an invalid carried item");
            }
        } else {

            for (int i = 0; i < 9; i++) {
                ItemStack stack = this.player.inventory.items[i];

                if ((stack != null && stack.id == packet16blockitemswitch.itemId) ||
                        (stack == null && packet16blockitemswitch.itemId == 0)) {
                    this.player.inventory.itemInHandIndex = i;
                }
            }
        }
    }

    public void a(Packet3Chat packet3chat) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet3chat);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        String s = packet3chat.message;

        if (s.length() > 100) {
            this.disconnect("Chat message too long");
        } else {
            s = s.trim();

            for (int i = 0; i < s.length(); ++i) {
                if (FontAllowedCharacters.allowedCharacters.indexOf(s.charAt(i)) < 0) {
                    this.disconnect("Illegal characters in chat");
                    return;
                }
            }

            // CraftBukkit start
            this.chat(s);
        }
    }

    public boolean chat(String s) {
        if (!this.player.dead) {
            if (s.startsWith("/")) {
                this.handleCommand(s);
                return true;
            } else {
                Player player = this.getPlayer();
                PlayerChatEvent event = new PlayerChatEvent(player, s);
                this.server.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return true;
                }

                s = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
                minecraftServer.console.sendMessage(s);
                for (Player recipient : event.getRecipients()) {
                    recipient.sendMessage(s);
                }
            }
        }

        return false;
        // CraftBukkit end
    }

    private void handleCommand(String s) {
        // CraftBukkit start
        CraftPlayer player = this.getPlayer();

        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s);
        this.server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        try {
            if (this.server.dispatchCommand(player, s.substring(1))) {
                //Project Poseidon Start
                //Basic XAuth & Authme Firewall
                if (s.toLowerCase().startsWith("/register") || s.toLowerCase().startsWith("/login") || s.toLowerCase().startsWith("/l ") || s.toLowerCase().startsWith("/changepw") || s.toLowerCase().startsWith("/changepassword") || s.toLowerCase().startsWith("/unregister")) {
                    a.info(player.getName() + " issued server command: COMMAND REDACTED");
                } else {
                    a.info(player.getName() + " issued server command: " + s);
                }

                //Project Poseidon End
                return;
            }
        } catch (CommandException ex) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
            Logger.getLogger(NetServerHandler.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            return;
        }
        // CraftBukkit end

        /* CraftBukkit start - No longer neaded av we have already handled it server.dispatchCommand above.
        if (s.toLowerCase().startsWith("/me ")) {
            s = "* " + this.player.name + " " + s.substring(s.indexOf(" ")).trim();
            a.info(s);
            this.minecraftServer.serverConfigurationManager.sendAll(new Packet3Chat(s));
        } else if (s.toLowerCase().startsWith("/kill")) {
            this.player.damageEntity(this.player, 1000); // CraftBukkit - replace null entity with player entity; TODO: decide if we want damage with a null source to fire an event.
        } else if (s.toLowerCase().startsWith("/tell ")) {
            String[] astring = s.split(" ");

            if (astring.length >= 3) {
                s = s.substring(s.indexOf(" ")).trim();
                s = s.substring(s.indexOf(" ")).trim();
                s = "\u00A77" + this.player.name + " whispers " + s;
                a.info(s + " to " + astring[1]);
                if (!this.minecraftServer.serverConfigurationManager.a(astring[1], (Packet) (new Packet3Chat(s)))) {
                    this.sendPacket(new Packet3Chat("\u00A7cThere\'s no player by that name online."));
                }
            }
        } else {
            String s1;

            if (this.minecraftServer.serverConfigurationManager.isOp(this.player.name)) {
                s1 = s.substring(1);
                a.info(this.player.name + " issued server command: " + s1);
                this.minecraftServer.issueCommand(s1, this);
            } else {
                s1 = s.substring(1);
                a.info(this.player.name + " tried command: " + s1);
            }
        }
        // CraftBukkit end */
    }

    public void a(Packet18ArmAnimation packet18armanimation) {
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet18armanimation);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        // CraftBukkit start
        if (this.player.dead) return;

        if (packet18armanimation.b == 104 || packet18armanimation.b == 105) {
            PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packet18armanimation.b == 104);
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
        }
        // CraftBukkit end

        if (packet18armanimation.b == 1) {
            // CraftBukkit start - raytrace to look for 'rogue armswings'
            float f = 1.0F;
            float f1 = this.player.lastPitch + (this.player.pitch - this.player.lastPitch) * f;
            float f2 = this.player.lastYaw + (this.player.yaw - this.player.lastYaw) * f;
            double d0 = this.player.lastX + (this.player.locX - this.player.lastX) * (double) f;
            double d1 = this.player.lastY + (this.player.locY - this.player.lastY) * (double) f + 1.62D - (double) this.player.height;
            double d2 = this.player.lastZ + (this.player.locZ - this.player.lastZ) * (double) f;
            Vec3D vec3d = Vec3D.create(d0, d1, d2);

            float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = 5.0D;
            Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, true);

            if (movingobjectposition == null || movingobjectposition.type != EnumMovingObjectType.TILE) {
                CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getItemInHand());
            }

            // Arm swing animation
            PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled()) return;
            // CraftBukkit end

            this.player.w();
            // uberbukkit
        } else if (packet18armanimation.b == 104) {
            this.player.setSneak(true);
        } else if (packet18armanimation.b == 105) {
            this.player.setSneak(false);
        }
    }

    public void a(Packet19EntityAction packet19entityaction) {
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet19entityaction);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        // CraftBukkit start
        if (this.player.dead) return;

        if (packet19entityaction.animation == 1 || packet19entityaction.animation == 2) {
            PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(), packet19entityaction.animation == 1);
            this.server.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }
        }
        // CraftBukkit end

        if (packet19entityaction.animation == 1) {
            this.player.setSneak(true);
        } else if (packet19entityaction.animation == 2) {
            this.player.setSneak(false);
        } else if (packet19entityaction.animation == 3) {
            this.player.a(false, true, true);
            this.checkMovement = false;
        }
    }

    public void a(Packet0KeepAlive packet0KeepAlive) {
        this.receivedKeepAlive = true;
    }

    public void a(Packet255KickDisconnect packet255kickdisconnect) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet255kickdisconnect);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        // uberbukkit - drop item queue on disconnect
        if (Uberbukkit.getPVN() <= 6) {
            ArrayList<ItemStack> queue = this.player.packet5.queue.dropAllQueue();
            Player bukkitEntity = (Player) this.player.getBukkitEntity();
            for (ItemStack item : queue) {
                System.out.println("Drop queue id: " + item.id + ", dmg: " + item.damage + ", cnt: " + item.count);
                HashMap<Integer, org.bukkit.inventory.ItemStack> map = bukkitEntity.getInventory().addItem(new CraftItemStack(item));
                // drop what couldn't fit in the inventory
                for (org.bukkit.inventory.ItemStack stack : map.values()) {
                    bukkitEntity.getWorld().dropItemNaturally(bukkitEntity.getLocation(), stack);
                }
            }
        }
        this.networkManager.a("disconnect.quitting", new Object[0]);
    }

    public int b() {
        return this.networkManager.e();
    }

    public void sendMessage(String s) {
        this.sendPacket(new Packet3Chat("\u00A77" + s));
    }

    public String getName() {
        return this.player.name;
    }

    public void a(Packet7UseEntity packet7useentity) {
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet7useentity);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        if (this.player.dead) return; // CraftBukkit

        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        Entity entity = worldserver.getEntity(packet7useentity.target);
        ItemStack itemInHand = this.player.inventory.getItemInHand();

        if (entity != null && this.player.e(entity) && this.player.g(entity) < 36.0D) {
            if (packet7useentity.c == 0) {
                Player player = (Player) this.getPlayer();
                org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
                // CraftBukkit start
                //Project Poseidon Start - Fixes a Minecart dupe glitch
                if (player.isInsideVehicle() && bukkitEntity instanceof StorageMinecart) {
                    return;
                }
                //Project Poseidon End
                PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(player, bukkitEntity);
                this.server.getPluginManager().callEvent(event);

                if (event.isCancelled()) {
                    return;
                }
                // CraftBukkit end
                this.player.c(entity);
                // CraftBukkit start - update the client if the item is an infinite one
                if (itemInHand != null && itemInHand.count <= -1) {
                    this.player.updateInventory(this.player.activeContainer);
                }
                // CraftBukkit end
            } else if (packet7useentity.c == 1) {
                this.player.d(entity);
                // CraftBukkit start - update the client if the item is an infinite one
                if (itemInHand != null && itemInHand.count <= -1) {
                    this.player.updateInventory(this.player.activeContainer);
                }
                // CraftBukkit end
            }
        }
    }

    public void a(Packet9Respawn packet9respawn) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet9respawn);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (this.player.health <= 0) {
            this.player = this.minecraftServer.serverConfigurationManager.moveToWorld(this.player, 0);

            this.getPlayer().setHandle(this.player); // CraftBukkit
        }
    }

    public void a(Packet101CloseWindow packet101closewindow) {
        if (this.player.dead) return; // CraftBukkit

        this.player.A();
    }

    public void a(Packet102WindowClick packet102windowclick) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet102windowclick);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (this.player.dead) return; // CraftBukkit

        if (this.player.activeContainer.windowId == packet102windowclick.a && this.player.activeContainer.c(this.player)) {
            ItemStack itemstack = this.player.activeContainer.a(packet102windowclick.b, packet102windowclick.c, packet102windowclick.f, this.player);

            if (ItemStack.equals(packet102windowclick.e, itemstack)) {
                this.player.netServerHandler.sendPacket(new Packet106Transaction(packet102windowclick.a, packet102windowclick.d, true));
                this.player.h = true;
                this.player.activeContainer.a();
                this.player.z();
                this.player.h = false;
            } else {
                this.n.put(Integer.valueOf(this.player.activeContainer.windowId), Short.valueOf(packet102windowclick.d));
                this.player.netServerHandler.sendPacket(new Packet106Transaction(packet102windowclick.a, packet102windowclick.d, false));
                this.player.activeContainer.a(this.player, false);
                ArrayList arraylist = new ArrayList();

                for (int i = 0; i < this.player.activeContainer.e.size(); ++i) {
                    arraylist.add(((Slot) this.player.activeContainer.e.get(i)).getItem());
                }

                this.player.a(this.player.activeContainer, arraylist);
            }
        }
    }

    public void a(Packet106Transaction packet106transaction) {
        // poseidon
        PacketReceivedEvent event = new PacketReceivedEvent(server.getPlayer(player), packet106transaction);
        server.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (this.player.dead) return; // CraftBukkit

        Short oshort = (Short) this.n.get(Integer.valueOf(this.player.activeContainer.windowId));

        if (oshort != null && packet106transaction.b == oshort.shortValue() && this.player.activeContainer.windowId == packet106transaction.a && !this.player.activeContainer.c(this.player)) {
            this.player.activeContainer.a(this.player, true);
        }
    }

    public void a(Packet130UpdateSign packet130updatesign) {
        // poseidon
        PacketReceivedEvent pevent = new PacketReceivedEvent(server.getPlayer(player), packet130updatesign);
        server.getPluginManager().callEvent(pevent);
        if (pevent.isCancelled())
            return;

        if (this.player.dead) return; // CraftBukkit

        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        if (worldserver.isLoaded(packet130updatesign.x, packet130updatesign.y, packet130updatesign.z)) {
            TileEntity tileentity = worldserver.getTileEntity(packet130updatesign.x, packet130updatesign.y, packet130updatesign.z);

            if (tileentity instanceof TileEntitySign) {
                TileEntitySign tileentitysign = (TileEntitySign) tileentity;

                if (!tileentitysign.a()) {
                    this.minecraftServer.c("Player " + this.player.name + " just tried to change non-editable sign");
                    // CraftBukkit
                    this.sendPacket(new Packet130UpdateSign(packet130updatesign.x, packet130updatesign.y, packet130updatesign.z, tileentitysign.lines));
                    return;
                }
            }

            int i;
            int j;

            for (j = 0; j < 4; ++j) {
                boolean flag = true;

                if (packet130updatesign.lines[j].length() > 15) {
                    flag = false;
                } else {
                    for (i = 0; i < packet130updatesign.lines[j].length(); ++i) {
                        if (FontAllowedCharacters.allowedCharacters.indexOf(packet130updatesign.lines[j].charAt(i)) < 0) {
                            flag = false;
                        }
                    }
                }

                if (!flag) {
                    packet130updatesign.lines[j] = "!?";
                }
            }

            if (tileentity instanceof TileEntitySign) {
                j = packet130updatesign.x;
                int k = packet130updatesign.y;

                i = packet130updatesign.z;
                TileEntitySign tileentitysign1 = (TileEntitySign) tileentity;

                // CraftBukkit start
                Player player = this.server.getPlayer(this.player);
                SignChangeEvent event = new SignChangeEvent((CraftBlock) player.getWorld().getBlockAt(j, k, i), this.server.getPlayer(this.player), packet130updatesign.lines);
                this.server.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    for (int l = 0; l < 4; ++l) {
                        tileentitysign1.lines[l] = event.getLine(l);
                    }
                    tileentitysign1.a(false);
                }
                // CraftBukkit end

                tileentitysign1.update();
                worldserver.notify(j, k, i);
            }
        }
    }

    public boolean c() {
        return true;
    }
}
