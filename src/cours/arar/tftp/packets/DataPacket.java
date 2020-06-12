package cours.arar.tftp.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataPacket extends TFTPPacket {
    int number;

    public DataPacket(int number) {
        super(DATA);
        this.number = number;
    }

    @Override
    public byte[] generate() throws PackageGenerationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buffer);
        try {
            stream.writeShort(code);
            stream.writeShort(number);
        } catch (IOException e) {
            throw new PackageGenerationException("Impossible de générer le paquet", e);
        }
        return buffer.toByteArray();
    }

    @Override
    public String toString() {
        return String.format("DATA %02d%02d", code, number);
    }

    public int getNumber() {
        return number;
    }
}
