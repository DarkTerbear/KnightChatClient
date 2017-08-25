package com.knightchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by tyli on 8/25/17.
 */
public class KnightChatClient {
    private static final int PORT = 1337;

    private String mHandle;

    static Scanner scanner;
    BufferedReader in;
    PrintWriter out;

    public KnightChatClient() {
        System.out.println("INFO: KnightChat client initializing...");
    }

    private String getServerAddress() {
        String ip;

        while (true) {
            System.out.print("Please enter the IP address of the KnightChat server: ");
            ip = scanner.nextLine();
            if (!isValidIP(ip)) System.out.println("\nInvalid IP input!");
            else break;
        }
        return ip;
    }

    private String getName() {
        String name;

        while (true) {
            System.out.print("Please enter your handle: ");
            name = scanner.nextLine();
            if (name == null || name.isEmpty()) System.out.println("\nInvalid name input!");
            else break;
        }
        return name;
    }


    private void run() {
        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        try {
            Socket socket = new Socket(serverAddress, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Process all messages from server, according to the protocol.
            while (true) {
                String line = in.readLine();

                if (line == null) {
                    System.out.println("WARN: The server has shut down.");
                    break;
                }
                if (line.startsWith("SUBMITNAME")) {
                    mHandle = getName();
                    out.println(mHandle);
                } else if (line.startsWith("NAMEACCEPTED")) {
                    System.out.println("You have successfully joined KnightChat at "+ serverAddress + "!");
                    System.out.print("> ");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readInput();
                        }
                    }).start();
                } else if (line.startsWith("MESSAGE")) {
                    if (!line.substring(8).startsWith(mHandle + ":")) System.out.print((line.substring(8) + "\n> "));
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: Failed to connect to KnightChat server");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        KnightChatClient client = new KnightChatClient();
        try {
            client.run();
        } catch (Exception e) {
            System.out.println("FATAL: KnightChat client initialization failed");
            e.printStackTrace();
        }
    }

    public void readInput() {
        String input = scanner.nextLine();
        out.println(input);
        System.out.print("> ");
        readInput();
    }

    public static boolean isValidIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            if (ip.equals("localhost")) return true;

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
