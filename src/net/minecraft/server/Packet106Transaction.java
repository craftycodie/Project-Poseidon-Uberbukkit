package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet106Transaction extends Packet {

    public int a;
    public short b;
    public boolean c;

    public Packet106Transaction() {}

    public Packet106Transaction(int i, short short1, boolean flag) {
        this.a = i;
        this.b = short1;
        this.c = flag;
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readByte();
        this.b = datainputstream.readShort();
        this.c = datainputstream.readByte() != 0;
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeByte(this.a);
        dataoutputstream.writeShort(this.b);
        dataoutputstream.writeByte(this.c ? 1 : 0);
    }

    public int getSize(int playerPVN) {
        return 4;
    }
}
