package cours.arar.tftp.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ERRORPacket extends TFTPPacket {
    private int errorCode;
    private String message;

    public ERRORPacket(int errorCode, String message) {
        super(ERROR);
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public byte[] generate() throws PackageGenerationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(buffer);
        try {
            stream.writeShort(code);
            stream.writeShort(errorCode);
            stream.write(message.getBytes(StandardCharsets.US_ASCII));
            stream.write(0);
        } catch (IOException e) {
            throw new PackageGenerationException("Impossible de générer le paquet", e);
        }
        return buffer.toByteArray();
    }

    @Override
    public String toString() {
        return String.format("ERROR %02d%02d%s0", code, errorCode, message);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
