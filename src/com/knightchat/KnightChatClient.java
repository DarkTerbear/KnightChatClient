package com.knightchat;

import org.jline.reader.Buffer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by tyli on 8/25/17.
 */
public class KnightChatClient {
    private static final int PORT = 1337;

    private String mHandle;

    Socket socket;
    BufferedReader socketIn;
    PrintWriter socketOut;
    static Terminal terminal;
    static LineReader reader;
    Robot robot;

    public KnightChatClient() {
        System.out.println("INFO: KnightChat client initializing...");
    }

    private String getServerAddress() {
        String ip;

        while (true) {
            System.out.print("Please enter the IP address of the KnightChat server: ");
            ip = reader.readLine();
            if (!isValidIP(ip)) System.out.println("\nInvalid IP input!");
            else break;
        }
        return ip;
    }

    private String getName() {
        String name;

        while (true) {
            System.out.print("Please enter your handle: ");
            name = reader.readLine();
            if (name == null || name.isEmpty()) System.out.println("\nInvalid name input!");
            else break;
        }
        return name;
    }


    private void run() {
        // Make connection and initialize streams
        String serverAddress = getServerAddress();
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        try {
            socket = new Socket(serverAddress, PORT);
            socketIn = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            socketOut = new PrintWriter(socket.getOutputStream(), true);

            // Process all messages from server, according to the protocol.
            while (true) {
                if (!socket.isClosed() && socketIn.ready()) {
                    String line = socketIn.readLine();

                    if (line == null) {
                        System.out.println("WARN: The server has shut down.");
                        break;
                    }
                    if (line.startsWith("SUBMITNAME")) {
                        mHandle = getName();
                        socketOut.println(mHandle);
                    } else if (line.startsWith("NAMEACCEPTED")) {
                        System.out.println("You have successfully joined KnightChat at " + serverAddress + "!");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                readInput();
                            }
                        }).start();
                    } else if (line.startsWith("MESSAGE")) {
                        if (!line.substring(8).startsWith(mHandle + ":")) printAndMoveCursor((line.substring(8) + "\n"));
                    } else if (line.startsWith("INFO")) {
                        printAndMoveCursor((line.substring(5) + "\n"));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: Failed to connect to KnightChat server");
            e.printStackTrace();
        }
    }

    private void printAndMoveCursor(String string) {
        String buffer = reader.getBuffer().copy().toString();
        reader.getTerminal().puts(InfoCmp.Capability.carriage_return);
        reader.getTerminal().writer().print(string);
        reader.callWidget(LineReader.REDRAW_LINE);
        reader.callWidget(LineReader.REDISPLAY);
        reader.getTerminal().writer().flush();
        writeString(buffer);
    }

    private void writeString(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                robot.keyPress(KeyEvent.VK_SHIFT);
            }
            robot.keyPress(Character.toUpperCase(c));
            robot.keyRelease(Character.toUpperCase(c));

            if (Character.isUpperCase(c)) {
                robot.keyRelease(KeyEvent.VK_SHIFT);
            }
        }
        robot.delay(5);
    }

    private void stop() {
        try {
            socket.close();
            socketIn.close();
            socketOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("INFO: You have disconnected from the server.");
        run();
    }

    public static void main(String[] args) {

        try {
            terminal = TerminalBuilder.builder().name("KnightChat Client v0.1").build();
            reader = LineReaderBuilder.builder().terminal(terminal).build();
            KnightChatClient client = new KnightChatClient();
            client.run();
        } catch (Exception e) {
            System.out.println("FATAL: KnightChat client initialization failed");
            e.printStackTrace();
        }
    }

    public void readInput() {
        String input = reader.readLine();
        if (!input.isEmpty()) {
            if (input.startsWith("/")) {
                runCommand(input.substring(1));
            } else socketOut.println(input);

        }
        readInput();
    }

    public void runCommand(String command) {
        String[] clauses = command.split(" ");

        String primaryCommand = clauses[0];

        switch (primaryCommand) {

            /**
             * /dc or /disconnect disconnects client from server
             */
            case "dc":
                stop();
                break;
            case "disconnect":
                stop();
                break;

            /**
             * /users lists all the users connected
             */
            case "users":
                socketOut.println("/users");
                break;

            /**
             * /mute stops the client from displaying new messages
             */
            case "mute":

                break;

            /**
             * /unmute is self-explanatory
             */
            case "unmute":

                break;
            default:
                System.out.println("Unrecognized command");
                break;
        }
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
