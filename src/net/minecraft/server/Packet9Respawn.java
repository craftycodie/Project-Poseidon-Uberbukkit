package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.moresteck.uberbukkit.Uberbukkit;

public class Packet9Respawn extends Packet {

    public byte a;

    public Packet9Respawn() {}

    public Packet9Respawn(byte b0) {
        this.a = b0;
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        // uberbukkit
        if (playerPVN >= 12) {
            this.a = datainputstream.readByte();
        } else {
            this.a = 0;
        }
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        // uberbukkit
        if (playerPVN >= 12) {
            dataoutputstream.writeByte(this.a);
        }
    }

    public int getSize(int playerPVN) {
        // uberbukkit
        return playerPVN >= 12 ? 1 : 0;
    }
}
