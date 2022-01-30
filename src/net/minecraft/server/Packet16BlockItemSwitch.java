package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.moresteck.uberbukkit.Uberbukkit;

public class Packet16BlockItemSwitch extends Packet {

    public int itemInHandIndex;
    public int itemId;
    public int itemDamage;

    public Packet16BlockItemSwitch() {}

    // pvn <= 6
    public Packet16BlockItemSwitch(int i, int j) {
        this.itemId = i;
        this.itemDamage = j;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        if (playerPVN >= 7) {
            this.itemInHandIndex = datainputstream.readShort();
        } else {
            this.itemDamage = datainputstream.readInt();
            this.itemId = datainputstream.readShort();
        }
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        if (playerPVN >= 7) {
            dataoutputstream.writeShort(this.itemInHandIndex);
        } else {
            dataoutputstream.writeInt(this.itemDamage);
            dataoutputstream.writeShort(this.itemId);
        }
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int getSize(int playerPVN) {
        return playerPVN >= 7 ? 2 : 6;
    }
}
