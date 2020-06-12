package cours.arar.tftp.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

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
                    int ackNumber = stream.readShort() & 0xFF;
                    return new ACKPacket(ackNumber);
                case DATA:
                	int dataNumber = stream.readShort() & 0xFF;
                    byte[] buffer = new byte[512];
                    int length = stream.read(buffer);
                	return new DATAPacket(dataNumber, buffer, length);
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