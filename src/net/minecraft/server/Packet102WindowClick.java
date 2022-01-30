
package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import pl.moresteck.uberbukkit.Uberbukkit;

public class Packet102WindowClick extends Packet {

    public int a;
    public int b;
    public int c;
    public short d;
    public ItemStack e;
    public boolean f;

    public Packet102WindowClick() {}

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readByte();
        this.b = datainputstream.readShort();
        this.c = datainputstream.readByte();
        this.d = datainputstream.readShort();
        // uberbukkit
        if (playerPVN >= 11) {
            this.f = datainputstream.readBoolean();
        } else {
            this.f = false;
        }

        short short1 = datainputstream.readShort();

        if (short1 >= 0) {
            byte b0 = datainputstream.readByte();
            short short2 = 0;
            // uberbukkit
            if (playerPVN >= 8) {
                short2 = datainputstream.readShort();
            } else {
                short2 = datainputstream.readByte();
            }

            this.e = new ItemStack(short1, b0, short2);
        } else {
            this.e = null;
        }
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeByte(this.a);
        dataoutputstream.writeShort(this.b);
        dataoutputstream.writeByte(this.c);
        dataoutputstream.writeShort(this.d);
        // uberbukkit
        if (playerPVN >= 11) {
            dataoutputstream.writeBoolean(this.f);
        }

        if (this.e == null) {
            dataoutputstream.writeShort(-1);
        } else {
            dataoutputstream.writeShort(this.e.id);
            dataoutputstream.writeByte(this.e.count);
            // uberbukkit
            if (playerPVN >= 8) {
                dataoutputstream.writeShort(this.e.getData());
            } else {
                dataoutputstream.writeByte(this.e.getData());
            }
        }
    }

    public int getSize(int playerPVN) {
        // uberbukkit
        return playerPVN >= 8 ? 11 : 10;
    }
}
