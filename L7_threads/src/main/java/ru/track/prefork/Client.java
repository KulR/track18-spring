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

    public void loop() /*throws Exception*/ {
        try /*(Socket socket = new Socket(host, port))*/ {
            Socket socket = new Socket(host, port);
/*            final OutputStream out = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);
            final InputStream in = socket.getInputStream();*/

            WriteThread wt = new WriteThread(socket);
            ListenThread rt = new ListenThread(socket);
            wt.start();
            rt.start();
            /*while (true) {
                String line = scanner.nextLine();
                if ("EXIT".equals(line)) {
                    socket.close();
                    break;
                }
                out.write(line.getBytes());
                out.flush();


                byte[] buf = new byte[1024];
                int nRead = in.read(buf);
                if(nRead != -1) {
                    System.out.println(new String(buf, 0, nRead));
                }
                else{
                    break;
                }
            }*/
        } catch (IOException e) {
            //System.out.println("exception");
            System.out.print("exception in main client thread");
            e.printStackTrace();
        }
        return;
//        WorkThread thread = new WorkThread(port, host, socket);
//        thread.start();
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
//                final OutputStream out = socket.getOutputStream();
                Scanner scanner = new Scanner(System.in);
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
//                        socket.getOutputStream()));
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());


                while (!isInterrupted()) {
                    String line = scanner.nextLine();
                    if ("EXIT".equals(line)) {
                        Message msg = new Message(0, line);
                        out.writeObject(msg);
                        out.flush();
                        socket.close();
                        break;
                    }

//                    line += "\n";
                    Message msg = new Message(1, line);
                    out.writeObject(msg);
                    out.flush();

                    /*bw.write(line);
                    bw.flush();*/
//                    out.write(line.getBytes());
//                    out.flush();
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
//                InputStream in = socket.getInputStream();
//                InputStream inputStream = socket.getInputStream();
//                ObjectInputStream in = new ObjectInputStream(inputStream);

                while (!isInterrupted()) {
//                    byte[] buf = new byte[1024];
//                    int nRead = in.read(buf);
//                    if (nRead != -1) {
//                        System.out.println(new String(buf, 0, nRead));
//                    } else {
//                        break;
//                    }
//                    Object obj = in.readObject();
//                    System.out.println(obj);
//                    Message msg = (Message) obj;
//                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//                    Object msg = in.readObject();
                    Message msg = (Message) in.readObject();
                    if (msg == null) {
                        break;
                    }
//                    System.out.println(msg.getData());
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