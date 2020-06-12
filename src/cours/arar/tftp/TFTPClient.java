package cours.arar.tftp;

import cours.arar.tftp.packets.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;

public class TFTPClient {
    private static boolean verbosity = false;

    private final static int TIMEOUT_RETRY_COUNT = 3;
    private final static int TIMEOUT_DELAY = 10; // secondes

    public static boolean toggleVerbosity() {
        verbosity = !verbosity;
        return verbosity;
    }

    public static boolean getVerbosity() {
        return verbosity;
    }
    
    public static int receiveFile(String host, int port, String filename) {
    	int returnCode = 0;
    	try {
    		FileOutputStream file = null;
    		
    		InetAddress address = InetAddress.getByName(host);
    		DatagramSocket socket = new DatagramSocket();
            DatagramPacket envoi = new DatagramPacket(new byte[516], 516, address, port);
            DatagramPacket req = new DatagramPacket(new byte[516], 516);
            
            socket.setSoTimeout(TIMEOUT_DELAY * 1000);

            TFTPPacket packet = null;
            SocketAddress serverAdress = null;
            
            int packetCounter = 0;
            boolean communicationEnd = false;
            boolean transmitAgain = false, receiveAgain = false;
            
            while(true) {
            	if (!transmitAgain && !receiveAgain) {
            		if (packetCounter == 0) {
            			packet = new RRQPacket(filename.replace('\0', ' '));
            		} else {
            			packet = new ACKPacket(packetCounter);
            		}
            		envoi.setData(packet.generate());
            	}
            	
            	socket.send(envoi);
            	log("OUT " + packet);

            	if (communicationEnd) {
            	    try {
            	        log("Attente d'une erreur de transmission");
            	        socket.receive(req);
                    } catch (SocketTimeoutException e) {
            	        break; // Le programme peut quitter
                    }
                } else {
                    receiveNTimes(socket, req, TIMEOUT_RETRY_COUNT);
                }
            	
            	packet = TFTPPacket.parse(req);
            	log("IN " + packet);
            	
            	checkPacket(packet, DATAPacket.class);
                DATAPacket data = (DATAPacket) packet;
            	
            	if (packetCounter == 0) {
            		serverAdress = req.getSocketAddress();
            		envoi.setSocketAddress(serverAdress);
            	} else if (!req.getSocketAddress().equals(serverAdress) ) {
                    packet = new ERRORPacket(5, "Unknown transfer ID");
                    envoi.setData(packet.generate());
                    envoi.setSocketAddress(req.getSocketAddress()); // On renvoie le paquet d'erreur au bon serveur
                    receiveAgain = true;
                    continue; // On ne veut pas interpréter le paquet
                } else {
                	envoi.setSocketAddress(serverAdress);
                	receiveAgain = false;
                }
            	
            	transmitAgain = false;
            	if (data.getNumber() == (packetCounter + 1)) {
            	    if (packetCounter == 0) { // Pour ne pas créer de fichier vide si le fichier n'existe pas sur le serveur
                        file = new FileOutputStream(filename);
                    }
            		++packetCounter;
            		file.write(data.getData(), 0, data.getLength());
            		if (data.getLength() < 512) {
            			communicationEnd = true;
            		}
            	} else if (data.getNumber() == packetCounter) {
            		transmitAgain = true;
            	} else {
                    throw new BadResponseException("Mauvais numéro ACK");
                }
            }
            file.close();
        } catch (UnknownHostException e) { // Serveur inconnu
            returnCode = -1;
            log(e);
        } catch (SocketException e) { // Impossible de créer la socket
            returnCode = -2;
            log(e);
        } catch (PackageGenerationException e) {
            returnCode = -3;
            log(e);
        } catch (FileNotFoundException e) {
            returnCode = -4;
            log(e);
        } catch (SocketTimeoutException e) {
            returnCode = -5;
            log(e);
        } catch (IOException e) { // socket.send || socket.receive
            returnCode = -6;
            log(e);
        } catch (PackageParsingException e) {
            returnCode = -7;
            log(e);
        } catch (BadResponseException e) {
            returnCode = -8;
            log(e);
        } catch (TFTPErrorException e) {
            returnCode = e.getCode();
            if (e.getCode() == 0) {
                returnCode = 8; // on ne peut pas renvoyer 0
            }
            log(e);
        }
        return returnCode;
    }

    public static int sendFile(String host, int port, String filename) {
        int returnCode = 0;

        try {
            FileInputStream file = new FileInputStream(filename);

            InetAddress address = InetAddress.getByName(host);
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket envoi = new DatagramPacket(new byte[516], 516, address, port);
            DatagramPacket req = new DatagramPacket(new byte[516], 516);

            socket.setSoTimeout(TIMEOUT_DELAY * 1000);

            TFTPPacket packet = null;
            SocketAddress serverAdress = null;

            int packetCounter = 0, dataSize = 512;
            byte[] buffer = new byte[512];
            boolean communicationEnd = false;
            boolean transmitAgain = false, receiveAgain = false;

            while ( ! communicationEnd) {

                if ( ! transmitAgain && ! receiveAgain) {
                    if (packetCounter == 0) {
                        packet = new WRQPacket(filename.replace('\0', ' '));
                    } else {
                        // Il y a une double copie : fichier => buffer => ByteArrayOutputStream
                        dataSize = file.read(buffer);
                        packet = new DATAPacket(packetCounter, buffer, dataSize);
                    }
                    envoi.setData(packet.generate());
                }

                socket.send(envoi);
                log("OUT " + packet);

                receiveNTimes(socket, req, TIMEOUT_RETRY_COUNT);
                packet = TFTPPacket.parse(req);
                log("IN " + packet);

                checkPacket(packet, ACKPacket.class);
                ACKPacket ack = (ACKPacket)packet;

                if (packetCounter == 0) {
                    serverAdress = req.getSocketAddress();
                    envoi.setSocketAddress(serverAdress); // On se connecte au TID donné par le serveur
                } else if ( ! req.getSocketAddress().equals(serverAdress) ) {
                    packet = new ERRORPacket(5, "Unknown transfer ID");
                    envoi.setData(packet.generate());
                    envoi.setSocketAddress(req.getSocketAddress()); // On renvoie le paquet d'erreur au bon serveur
                    receiveAgain = true;
                    continue; // On ne veut pas interpréter le paquet
                } else {
                    envoi.setSocketAddress(serverAdress); // On recommence à envoyer au serveur après avoir envoyer l'erreur
                    receiveAgain = false;
                }

                transmitAgain = false;
                if (ack.getNumber() == packetCounter) {
                    ++packetCounter;
                    if (dataSize < 512) {
                        communicationEnd = true;
                    }
                } else if (ack.getNumber() == packetCounter - 1) {
                    transmitAgain = true;
                } else {
                    throw new BadResponseException("Mauvais numéro ACK");
                }
            }
            file.close();
        } catch (UnknownHostException e) { // Serveur inconnu
            returnCode = -1;
            log(e);
        } catch (SocketException e) { // Impossible de créer la socket
            returnCode = -2;
            log(e);
        } catch (PackageGenerationException e) {
            returnCode = -3;
            log(e);
        } catch (FileNotFoundException e) {
            returnCode = -4;
            log(e);
        } catch (SocketTimeoutException e) {
            returnCode = -5;
            log(e);
        } catch (IOException e) { // socket.send || socket.receive
            returnCode = -6;
            log(e);
        } catch (PackageParsingException e) {
            returnCode = -7;
            log(e);
        } catch (BadResponseException e) {
            returnCode = -8;
            log(e);
        } catch (TFTPErrorException e) {
            returnCode = e.getCode();
            if (e.getCode() == 0) {
                returnCode = 8; // on ne peut pas renvoyer 0
            }
            log(e);
        }

        return returnCode;
    }

    static void receiveNTimes(DatagramSocket socket, DatagramPacket packet, int times) throws IOException {
        for (int i = 0; i < times; ++i) {
            try {
                socket.receive(packet);
                return;
            } catch (SocketTimeoutException e) {
                log("timeout " + (i+1));
                if (i == times - 1) {
                    throw e;
                }
            }
        }
    }

    static void checkPacket(TFTPPacket packet, Class<? extends TFTPPacket> packetType) throws TFTPErrorException, BadResponseException {
        if (packet instanceof ERRORPacket) {
            ERRORPacket errorPacket = (ERRORPacket)packet;
            throw new TFTPErrorException(errorPacket.getErrorCode(), errorPacket.getMessage());
        }
        if (!packetType.isInstance(packet)) {
            throw new BadResponseException(packetType.getName() + " demandé, " + packet.getClass().getName() + " reçu");
        }
    }

    static void log(String s) {
        if (verbosity) {
            System.out.println(s);
        }
    }

    static void log(Exception e) {
        if (verbosity) {
            e.printStackTrace();
        }
    }
}

class TFTPErrorException extends Exception {
    private int code;

    public TFTPErrorException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

class BadResponseException extends Exception {
    public BadResponseException(String message) {
        super(message);
    }
}