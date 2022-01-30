package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet100OpenWindow extends Packet {

    public int a;
    public int b;
    public String c;
    public int d;

    public Packet100OpenWindow() {}

    public Packet100OpenWindow(int i, int j, String s, int k) {
        this.a = i;
        this.b = j;
        this.c = s;
        this.d = k;
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readByte();
        this.b = datainputstream.readByte();
        this.c = datainputstream.readUTF();
        this.d = datainputstream.readByte();
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeByte(this.a);
        dataoutputstream.writeByte(this.b);
        dataoutputstream.writeUTF(this.c);
        dataoutputstream.writeByte(this.d);
    }

    public int getSize(int playerPVN) {
        return 3 + this.c.length();
    }
}
