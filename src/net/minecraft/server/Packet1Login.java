package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet1Login extends Packet {

    public int a;
    public String name;
    public long c;
    public byte d;

    public Packet1Login() {}

    public Packet1Login(String s, int i, long j, byte b0) {
        this.name = s;
        this.a = i;
        this.c = j;
        this.d = b0;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readInt();
        // uberbukkit
        if (a >= 11) {
            this.name = readString(datainputstream, 16);
        } else {
            this.name = datainputstream.readUTF();
            datainputstream.readUTF();
        }

        this.c = datainputstream.readLong();
        this.d = datainputstream.readByte();
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeInt(this.a);
        // uberbukkit
        if (playerPVN >= 11) {
            a(this.name, dataoutputstream);
        } else {
            dataoutputstream.writeUTF(this.name);
            dataoutputstream.writeUTF("");
        }

        dataoutputstream.writeLong(this.c);
        dataoutputstream.writeByte(this.d);
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int getSize(int playerPVN) {
        return 4 + this.name.length() + 4 + 5;
    }
}