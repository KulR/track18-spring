package ru.track.prefork;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.ldap.SortKey;


/**
 * - multithreaded +
 * - atomic counter +
 * - setName() +
 * - thread -> Worker +
 * - save threads
 * - broadcast (fail-safe)
 */
public class Server {
    static Logger log = LoggerFactory.getLogger(Server.class);

    private int port;
    static int freeThreads = 5;

    synchronized static long setSynchonizedvalue(long value, int setvalue) {
        switch (setvalue) {
            case -1:
                freeThreads--;
                return freeThreads;
            case 1:
                freeThreads++;
                return freeThreads;
            case 0:
                return freeThreads;
            default:
                System.out.println("wrong argument");
                return -1;
        }
    }

    synchronized static long setFreeThreads(int setvalue) {
        return setSynchonizedvalue(freeThreads, setvalue);
    }

//    private static LinkedList<Socket> users = new LinkedList<Socket>();
//
//    synchronized static LinkedList<Socket> setUsers(Socket user, int setParams) {
//        switch (setParams) {
//            case 1:
//                if (users.contains(user)) {
//                    System.out.println("This user already in list");
//                    return users;
//                } else {
//                    users.add(user);
//                    return users;
//                }
//            case -1:
//                if (!users.contains(user)) {
//                    System.out.println("This user not in list");
//                    return users;
//                } else {
//                    users.remove(user);
//                    return users;
//                }
//            default:
////                users.remove(user);
////                LinkedList<Socket> temp = users;
////                users.add(user);
////                return temp;
//                return users;
//        }
//    }

    private static LinkedList<WorkThread> users = new LinkedList<WorkThread>();

    synchronized static LinkedList<WorkThread> setUsers(WorkThread user, int setParams) {
        switch (setParams) {
            case 1:
                if (users.contains(user)) {
                    System.out.println("This user already in list");
                    return users;
                } else {
                    users.add(user);
                    return users;
                }
            case -1:
                if (!users.contains(user)) {
                    System.out.println("This user not in list");
                    return users;
                } else {
                    users.remove(user);
                    return users;
                }
            default:
                return users;
        }
    }


    static AtomicLong atomicCounter = new AtomicLong(0);

    synchronized void StartWork(Socket socket, long id) {
        if (setFreeThreads(0) > 0) {
            WorkThread thread = new WorkThread(socket, id);
            thread.start();
        } else {
            try {
                wait();
            } catch (InterruptedException e) {
                WorkThread thread = new WorkThread(socket, id);
                thread.start();
            }
        }
    }

    public Server(int port) {
        this.port = port;
    }

    public void serve() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
        log.info("on select...");
        AdminThread admin = new AdminThread();
        admin.start();
        try {
            while (true) {

                Socket socket = serverSocket.accept();
////                StartWork(socket);
//                System.out.println("In serve " + socket.isClosed());
/*            while (true) {
                InputStream inputStream = socket.getInputStream();
                byte[] buf = new byte[1024];
                int nRead = inputStream.read(buf);
                if (nRead != -1) {
                    System.out.println(new String(buf, 0, nRead));

                    socket.getOutputStream().write(buf, 0, nRead);
                    socket.getOutputStream().flush();
                }*/
//                StartWork(socket, atomicCounter.incrementAndGet());

                WorkThread thread = new WorkThread(socket, atomicCounter.incrementAndGet());
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("exception in main server thread");
            e.printStackTrace();
        } finally {
            admin.interrupt();
        }
    }

    static class WorkThread extends Thread {
        Socket socket;
        long id;
        String name;
        ObjectInputStream in;
        ObjectOutputStream out;

        WorkThread(Socket socket, long id) {
            this.socket = socket;
            this.id = id;
            setFreeThreads(-1);
            this.setName(SetName(socket));
//            setUsers(socket, 1);
            setUsers(this, 1);
            name = SetName(socket);
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                System.out.println("initialization thread " + name);
                e.printStackTrace();
            }
        }

        String SetName(Socket socket) {
            return String.format("Client[%d]@%s:%d", id, socket.getLocalAddress().toString(), socket.getPort());
        }

        @Override
        public void run() {
            try {
//                InputStream inputStream = socket.getInputStream();
//                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//                ObjectInputStream in = new ObjectInputStream(inputStream);
//                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
//                bw.write("connected");
//                bw.flush();
                out.writeObject(new Message(1, "connected"));
                out.flush();
                System.out.println(name + ">\t" + "connected");
                while (!isInterrupted() || socket.isClosed()) {
//                    String line = br.readLine();
                    Message msg = (Message) in.readObject();
                    if (msg == null) {
                        break;
                    }

                    if(msg.GetTs() == 0 && msg.getData().equals("EXIT")){
                        break;
                    }
                    System.out.print(name);
                    System.out.print(">\t");
                    System.out.println(msg.getData());


                    if (msg.GetTs() == 1) {
                        String line = msg.getData();
                        String name = "Client" + "@" + socket.getLocalAddress().toString() +
                                ":" + socket.getPort();
                        line = name + ">\t" + line;
                        msg.PutData(line);
                        SayAll(this, msg);
                    }
                }
            } catch (Exception e) {
                System.out.println("exception in work thread" + name);
                e.printStackTrace();
            } finally {
//                LinkedList<Socket> users = setUsers(socket, 0);
//                System.out.println(setUsers(socket, 0));
//                System.out.println(socket);
//                System.out.println(users.contains(socket));
                String line = "user " + name + ">\t" + "turn off";
                Message msg = new Message(1, line);
                System.out.println(line);
                SayAll(this, msg);
                setUsers(this, -1);
                setFreeThreads(1);

                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    static void SayAll(WorkThread thread, Message msg) {
        LinkedList<WorkThread> users = setUsers(thread, 0);
        for (WorkThread user : users) {
            if (!user.equals(thread)) {
                try {
//                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(user.socket.getOutputStream()));
//                    ObjectOutputStream writer = new ObjectOutputStream(user.socket.getOutputStream());
//                    writer.write(line);
                    ObjectOutputStream writer = user.out;
                    writer.writeObject(msg);
                    writer.flush();
//                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    static class AdminThread extends Thread {
        @Override
        public void run() {
            try {
                Pattern p = Pattern.compile("^drop (\\d+)$");
                while (!isInterrupted()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String line = reader.readLine();
                    Matcher m = p.matcher(line);
                    if (line.equals("list")) {
                        System.out.println("list:");
                        LinkedList<WorkThread> users = setUsers(null, 0);
                        for (WorkThread user : users) {
                            System.out.println(user.name);
                        }
                    } else if (m.find()) {
                        LinkedList<WorkThread> users = setUsers(null, 0);
                        for (WorkThread user : users) {
                            if (user.id == Long.parseLong(m.group(1))) {
                                user.interrupt();
                                user.socket.close();
                                //or we can close user.socket
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("exception in admin thread");
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        Server server = new Server(9000);
        server.serve();
    }
}
