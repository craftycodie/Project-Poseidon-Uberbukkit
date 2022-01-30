package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet12PlayerLook extends Packet10Flying {

    public Packet12PlayerLook() {
        this.hasLook = true;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.yaw = datainputstream.readFloat();
        this.pitch = datainputstream.readFloat();
        super.readPacket(datainputstream, playerPVN);
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeFloat(this.yaw);
        dataoutputstream.writeFloat(this.pitch);
        super.writePacket(dataoutputstream, playerPVN);
    }

    public int getSize(int playerPVN) {
        return 9;
    }
}
