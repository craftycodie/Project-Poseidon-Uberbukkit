package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet53BlockChange extends Packet {

    public int a;
    public int b;
    public int c;
    public int material;
    public int data;

    public Packet53BlockChange() {
        this.k = true;
    }

    public Packet53BlockChange(int i, int j, int k, int material, int data) {
        this.k = true;
        this.a = i;
        this.b = j;
        this.c = k;
        this.material = material;
        this.data = data;
    }

    public Packet53BlockChange(int i, int j, int k, World world) {
        this.k = true;
        this.a = i;
        this.b = j;
        this.c = k;
        this.material = world.getTypeId(i, j, k);
        this.data = world.getData(i, j, k);
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        this.a = datainputstream.readInt();
        this.b = datainputstream.read();
        this.c = datainputstream.readInt();
        this.material = datainputstream.read();
        this.data = datainputstream.read();
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        dataoutputstream.writeInt(this.a);
        dataoutputstream.write(this.b);
        dataoutputstream.writeInt(this.c);
        dataoutputstream.write(this.material);
        dataoutputstream.write(this.data);
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int getSize(int playerPVN) {
        return 11;
    }
}
