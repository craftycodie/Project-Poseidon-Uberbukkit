package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet105CraftProgressBar extends Packet {

    public int a;
    public int b;
    public int c;

    public Packet105CraftProgressBar() {}

    public Packet105CraftProgressBar(int i, int j, int k) {
        this.a = i;
        this.b = j;
        this.c = k;
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readByte();
        this.b = datainputstream.readShort();
        this.c = datainputstream.readShort();
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeByte(this.a);
        dataoutputstream.writeShort(this.b);
        dataoutputstream.writeShort(this.c);
    }

    public int getSize(int playerPVN) {
        return 5;
    }
}
