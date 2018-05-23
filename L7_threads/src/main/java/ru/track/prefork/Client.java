package ru.track.prefork;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    static Logger log = LoggerFactory.getLogger(Client.class);

    private int port;
    private String host;

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void loop(){
        try {
            Socket socket = new Socket(host, port);
            WriteThread wt = new WriteThread(socket);
            ListenThread rt = new ListenThread(socket);
            wt.start();
            rt.start();
        } catch (IOException e) {
            System.out.print("exception in main client thread");
            e.printStackTrace();
        }
    }


    static class WriteThread extends Thread {
        Socket socket;
        ObjectOutputStream out;

        WriteThread(Socket socket) {
            this.socket = socket;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                System.out.println("initialization write thread");
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(System.in);
                while (!isInterrupted()) {
                    String line = scanner.nextLine();
                    if ("EXIT".equals(line)) {
                        Message msg = new Message(0, line);
                        out.writeObject(msg);
                        out.flush();
                        socket.close();
                        break;
                    }
                    Message msg = new Message(1, line);
                    out.writeObject(msg);
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("exception");
                e.printStackTrace();
            }
        }
    }

    static class ListenThread extends Thread {
        Socket socket;
        ObjectInputStream in;

        ListenThread(Socket socket) {
            this.socket = socket;
            try {
                in = new ObjectInputStream(socket.getInputStream());
            } catch (Exception e) {
                System.out.println("initialization listen thread");
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    Message msg = (Message) in.readObject();
                    if (msg == null) {
                        break;
                    }
                    System.out.println(msg.getData());
                }
            } catch (Exception e) {
                System.out.println("exception");
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
