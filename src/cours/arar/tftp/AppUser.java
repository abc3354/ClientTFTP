package cours.arar.tftp;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class AppUser {

    private static String host;
    private static int port;

    private static void connect(String host, int port) {
        AppUser.host = host;
        AppUser.port = port;
        System.out.println("Connecté");
    }

    private static void get(String filename) {
        if (host == null) {
            System.out.println("Pas de serveur. Veuillez utiliser la commande connect.");
            return;
        }
        System.out.println("AppUser.get");
        System.out.println("filename = " + filename);
    }

    private static void put(String filename) {
        if (host == null) {
            System.out.println("Pas de serveur. Veuillez utiliser la commande connect.");
            return;
        }
        int code = TFTPClient.sendFile(host, port, filename);
        analyseCode(code);
    }

    private static void help() {
        System.out.println("Liste des commandes");
        System.out.println("help - afficher ce menu");
        System.out.println("connect <host> <port> - Donner l'adresse du serveur tftp");
        System.out.println("get <filename> - récupérer un fichier");
        System.out.println("put <filename> - envoyer un fichier");
        System.out.println("verbose - afficher plus d'informations");
        System.out.println("quit - quitter");
    }

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        String[] command;

        System.out.println("TFTP Client");

        do {
            try {
                System.out.print("> ");
                command = scanner.nextLine().split(" ");
            } catch (NoSuchElementException e) { // EOF
                break;
            }

            if (command.length == 0) {
                break;
            }

            try {
                switch (command[0]) {
                    case "help":
                        help();
                        break;
                    case "connect":
                        checkUsage(command, 2, "connect <host> <port>");
                        try {
                            int port = Integer.parseInt(command[2]);
                            connect(command[1], port);
                        } catch (NumberFormatException e) {
                            System.out.println("Erreur : le port doit être un nombre");
                            throw new InvalidUsageException("connect <host> <port>");
                        }
                        break;
                    case "get":
                        checkUsage(command, 1, "get <filename>");
                        get(command[1]);
                        break;
                    case "put":
                        checkUsage(command, 1, "put <filename>");
                        put(command[1]);
                        break;
                    case "verbose":
                        checkUsage(command, 0, "verbose");
                        boolean verbose = TFTPClient.toggleVerbosity();
                        System.out.printf("Verbosité %s\n", verbose ? "on" : "off");
                    case "quit":
                        checkUsage(command, 0, "quit");
                        break;
                    default:
                        System.out.println("Commande inconnue. Tapez help pour une liste des commandes.");
                }
            } catch (InvalidUsageException e) {
                System.out.println("Usage de la command invalide");
                System.out.print("Usage : ");
                System.out.println(e.getMessage());
            }
        } while (!command[0].equals("quit"));

        System.out.println("Bonne journée !");
    }

    private static void checkUsage(String[] command, int argNumber, String message) throws InvalidUsageException {
        if (command.length != argNumber + 1) {
            throw new InvalidUsageException(message);
        }
    }

    private static void analyseCode(int code) {
        if (code < 0) {
            System.out.println("Erreur du client :");
        } else if (code > 0) {
            System.out.println("Erreur du serveur :");
        }
        switch (code) {
            case 0:
                System.out.println("Fichier envoyé");
                break;
            case -1:
                System.out.println("Adresse du serveur incorrecte");
                break;
            case -2:
                System.out.println("Impossible de créer la connexion");
                break;
            case -3:
                System.out.println("Impossible de créer le paquet");
                break;
            case -4:
                System.out.println("Ce fichier n'existe pas");
                break;
            case -5:
                System.out.println("Le serveur ne répond pas");
                break;
            case -6:
                System.out.println("Impossible de communiquer");
                break;
            case -7:
                System.out.println("Réponse du serveur incompréhensible");
                break;
            case -8:
                System.out.println("Réponse du serveur invalide");
                break;
            case 1:
                System.out.println("Ce fichier n'existe pas");
                break;
            case 2:
                System.out.println("Impossible d'accéder à ce fichier");
                break;
            case 3:
                System.out.println("Pas de place restante");
                break;
            case 4:
                System.out.println("Opération illégale");
                break;
            case 5:
                System.out.println("TID Inconnue");
                break;
            case 6:
                System.out.println("Ce fichier existe déjà");
                break;
            case 7:
                System.out.println("Cet utilisateur n'existe pas");
                break;
            case 8:
                System.out.println("Erreur inconnue");
                break;
            default:
                System.out.println("Erreur inconnue.");
        }
        if (code != 0 && ! TFTPClient.getVerbosity()) {
            System.out.println("Le mode verbose donnera plus d'informations");
        }
    }
}

class InvalidUsageException extends Exception {
    public InvalidUsageException(String message) {
        super(message);
    }
}
