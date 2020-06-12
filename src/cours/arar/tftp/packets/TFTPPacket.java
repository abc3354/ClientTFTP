package cours.arar.tftp.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

public abstract class TFTPPacket {
    protected int code;
    protected static final int RRQ = 1;
    protected static final int WRQ = 2;
    protected static final int DATA = 3;
    protected static final int ACK = 4;
    protected static final int ERROR = 5;

    public abstract byte[] generate() throws PackageGenerationException;
    public abstract String toString();

    public TFTPPacket(int code) {
        this.code = code;
    }

    public static TFTPPacket parse(DatagramPacket udpPacket) throws PackageParsingException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(udpPacket.getData()));
        try {
            if (stream.read() != 0) {
                throw new PackageParsingException("Le packet ne commence pas par 0");
            }
            switch (stream.read() & 0xFF) {
                case ACK:
                    int ackNumber = stream.readShort() & 0xFFFF;
                    return new ACKPacket(ackNumber);
                case DATA:
                	int dataNumber = stream.readShort() & 0xFFFF;
                    byte[] buffer = new byte[512];
                    int length = stream.read(buffer, 0, udpPacket.getLength() - 4);
                	return new DATAPacket(dataNumber, buffer, length);
                case ERROR:
                    int errorCode = stream.readShort() & 0xFFFF;
                    byte[] messageBuffer = new byte[udpPacket.getLength() - 5];
                    stream.read(messageBuffer);
                    String message = new String(messageBuffer, StandardCharsets.US_ASCII);
                    return new ERRORPacket(errorCode, message);
                default:
                    throw new PackageParsingException("Code inconnu");
            }
        } catch (IOException e) {
            throw new PackageParsingException("Impossible de lire le paquet correctement");
        }
    }

    public int getCode() {
        return code;
    }
}