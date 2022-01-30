package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.moresteck.uberbukkit.Uberbukkit;

public class Packet255KickDisconnect extends Packet {

    public String a;

    public Packet255KickDisconnect() {}

    public Packet255KickDisconnect(String s) {
        this.a = s;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        // uberbukkit
        if (playerPVN >= 11) {
            this.a = readString(datainputstream, 100);
        } else {
            this.a = datainputstream.readUTF();
        }
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        // uberbukkit
        if (playerPVN >= 11) {
            a(this.a, dataoutputstream);
        } else {
            dataoutputstream.writeUTF(this.a);
        }
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int getSize(int playerPVN) {
        return this.a.length();
    }
}
