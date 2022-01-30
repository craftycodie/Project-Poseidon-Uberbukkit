package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Packet0KeepAlive extends Packet {

    public Packet0KeepAlive() {
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) {
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) {
    }

    public int getSize(int playerPVN) {
        return 0;
    }
}
