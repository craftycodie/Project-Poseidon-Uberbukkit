package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet32EntityLook extends Packet30Entity {

    public Packet32EntityLook() {
        this.g = true;
    }

    public Packet32EntityLook(int i, byte b0, byte b1) {
        super(i);
        this.e = b0;
        this.f = b1;
        this.g = true;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        super.readPacket(datainputstream, playerPVN);
        this.e = datainputstream.readByte();
        this.f = datainputstream.readByte();
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        super.writePacket(dataoutputstream, playerPVN);
        dataoutputstream.writeByte(this.e);
        dataoutputstream.writeByte(this.f);
    }

    public int getSize(int playerPVN) {
        return 6;
    }
}
