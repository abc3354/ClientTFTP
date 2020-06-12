package cours.arar.tftp.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RRQPacket extends TFTPPacket {
    private String filename;
    private String mode;

    public RRQPacket(String filename) {
        super(RRQ);
        this.filename = filename;
        this.mode = "octet";
    }

    @Override
    public byte[] generate() throws PackageGenerationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buffer);
        try {
            stream.writeShort(code);
            stream.write(filename.getBytes(StandardCharsets.US_ASCII));
            stream.write(0);
            stream.write(mode.getBytes(StandardCharsets.US_ASCII));
            stream.write(0);
        } catch (IOException e) {
            throw new PackageGenerationException("Impossible de générer le paquet", e);
        }
         return buffer.toByteArray();
    }

    @Override
    public String toString() {
        return String.format("RRQ %02d%s0%s0", code, filename, mode);
    }
}
