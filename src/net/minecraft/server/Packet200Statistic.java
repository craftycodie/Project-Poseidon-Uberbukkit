package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet200Statistic extends Packet {

    public int a;
    public int b;

    public Packet200Statistic() {}

    public Packet200Statistic(int i, int j) {
        this.a = i;
        this.b = j;
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readInt();
        this.b = datainputstream.readByte();
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeInt(this.a);
        dataoutputstream.writeByte(this.b);
    }

    public int getSize(int playerPVN) {
        return 6;
    }
}
