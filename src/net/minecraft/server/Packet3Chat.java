package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet3Chat extends Packet {

    public String message;

    public Packet3Chat() {}

    public Packet3Chat(String s) {
        /* CraftBukkit start - handle this later
        if (s.length() > 119) {
            s = s.substring(0, 119);
        }
        // CraftBukkit end */

        this.message = s;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException { // CraftBukkit
        // uberbukkit
        if (playerPVN >= 11) {
            this.message = readString(datainputstream, 119);
        } else {
            this.message = datainputstream.readUTF();
        }
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException { // CraftBukkit
        // uberbukkit
        if (playerPVN >= 11) {
            a(this.message, dataoutputstream);
        } else {
            dataoutputstream.writeUTF(this.message);
        }
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int getSize(int playerPVN) {
        return this.message.length();
    }
}
