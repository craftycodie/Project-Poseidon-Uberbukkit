package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet13PlayerLookMove extends Packet10Flying {

    public Packet13PlayerLookMove() {
        this.hasLook = true;
        this.h = true;
    }

    public Packet13PlayerLookMove(double d0, double d1, double d2, double d3, float f, float f1, boolean flag) {
        this.x = d0;
        this.y = d1;
        this.stance = d2;
        this.z = d3;
        this.yaw = f;
        this.pitch = f1;
        this.g = flag;
        this.hasLook = true;
        this.h = true;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.x = datainputstream.readDouble();
        this.y = datainputstream.readDouble();
        this.stance = datainputstream.readDouble();
        this.z = datainputstream.readDouble();
        this.yaw = datainputstream.readFloat();
        this.pitch = datainputstream.readFloat();
        super.readPacket(datainputstream, playerPVN);
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeDouble(this.x);
        dataoutputstream.writeDouble(this.y);
        dataoutputstream.writeDouble(this.stance);
        dataoutputstream.writeDouble(this.z);
        dataoutputstream.writeFloat(this.yaw);
        dataoutputstream.writeFloat(this.pitch);
        super.writePacket(dataoutputstream, playerPVN);
    }

    public int getSize(int playerPVN) {
        return 41;
    }
}
