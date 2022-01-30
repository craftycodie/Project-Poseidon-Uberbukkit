package net.minecraft.server;

import java.io.*;

public class Packet2Handshake extends Packet {

    public String a;
    public boolean notchianString;

    public Packet2Handshake() {}

    public Packet2Handshake(String s, boolean notchianString) {
        this.a = s;
        this.notchianString = notchianString;
    }

    public void readPacket(DataInputStream datainputstream, int playerPVN) throws IOException {
        short length = datainputstream.readShort();
        byte[] bytes = new byte[length];
        datainputstream.read(bytes);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(length + 2);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeShort(length);
        dos.write(bytes);
        byte[] payload = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(payload);
        DataInputStream dis = new DataInputStream(bais);
        String utfString = dis.readUTF();

        for (char character : utfString.toCharArray()) {
            if (FontAllowedCharacters.allowedCharacters.indexOf(character) < 0) {
                // if the string is invalid read it notchian
                byte[] notchBytes = new byte[length];
                datainputstream.read(notchBytes);

                baos = new ByteArrayOutputStream(length + 2);
                dos = new DataOutputStream(baos);
                dos.writeShort(length);
                dos.write(bytes);
                dos.write(notchBytes);
                payload = baos.toByteArray();

                bais = new ByteArrayInputStream(payload);
                dis = new DataInputStream(bais);
                String notchString = readString(dis, 32);

                this.a = notchString;
                notchianString = true;

                return;
            }
        }

        this.a = utfString;
    }

    public void writePacket(DataOutputStream dataoutputstream, int playerPVN) throws IOException {
        // uberbukkit
        if (notchianString) {
            a(this.a, dataoutputstream);
        } else {
            dataoutputstream.writeUTF(this.a);
        }
    }

    public void a(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int getSize(int playerPVN) {
        return 4 + this.a.length() + 4;
    }
}
