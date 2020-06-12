package cours.arar.tftp.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DATAPacket extends TFTPPacket {
    private int number;
    private byte[] data;
    private int length;

    public DATAPacket(int number, byte[] data, int length) {
        super(DATA);
        this.number = number;
        this.data = data;
        this.length = length;
    }

    public int getNumber() {
        return number;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] generate() throws PackageGenerationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buffer);
        try {
            stream.writeShort(code);
            stream.writeShort(number);
            stream.write(data, 0, length);
        } catch (IOException e) {
            throw new PackageGenerationException("Impossible de générer le paquet", e);
        }

        return buffer.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(String.format("DATA %02d%02d", code, number));
        for (int i = 0; i < (Math.min(length, 10)); ++i) {
            result.append(String.format("%02x", data[i]));
        }
        result.append(String.format("(%d)", length));

        return result.toString();
    }
}
