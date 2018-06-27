package ru.track.prefork;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    static private ServerSocket serverSocket;
    static private int freeThreads = 5;

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


    private static AtomicLong atomicCounter = new AtomicLong(0);

    synchronized void startWork(Socket socket, long id) {
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
        serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
        log.info("on select...");
        AdminThread admin = new AdminThread();
        admin.start();
        try {
            while (true) {
                Socket socket = serverSocket.accept();
//              startWork(socket, atomicCounter.incrementAndGet());
                WorkThread thread = new WorkThread(socket, atomicCounter.incrementAndGet());
                thread.start();
            }
        } catch (Exception e) {
            //System.out.println("exception in main server thread");
            e.printStackTrace();
        } finally {
            admin.interrupt();
        }
    }

    static class WorkThread extends Thread {
        public Socket socket;
        public long id;
        private String name;
        private String shortName;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        WorkThread(Socket socket, long id) {
            this.socket = socket;
            this.id = id;
            setFreeThreads(-1);
            this.setName(setName(socket));
            shortName = String.format("Client@%s:%d", socket.getLocalAddress().toString(), socket.getPort());
            setUsers(this, 1);
            name = setName(socket);
            try {
                in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                System.out.println("initialization thread " + name);
                e.printStackTrace();
            }
        }

        String setName(Socket socket) {
            return String.format("Client[%d]@%s:%d", id, socket.getLocalAddress().toString(), socket.getPort());
        }

        @Override
        public void run() {
            try {
                out.writeObject(new Message(1, "connected"));
                out.flush();
                System.out.println(name + ">\t" + "connected");
                while (!isInterrupted() || socket.isClosed()) {
                    Message msg = (Message) in.readObject();
                    if (msg == null) {
                        break;
                    }

                    if (msg.getTs() == 0 && msg.getData().equals("EXIT")) {
                        break;
                    }
                    System.out.print(name);
                    System.out.print(">\t");
                    System.out.println(msg.getData());


                    if (msg.getTs() == 1) {
                        String line = msg.getData();
                        line = shortName + ">\t" + line;
                        msg.putData(line);
                        sayAll(this, msg);
                    }
                }
            } catch (Exception e) {
                //System.out.println("exception in work thread" + name);
                e.printStackTrace();
            } finally {
                String line = "user " + shortName + ">\t" + "turn off";
                Message msg = new Message(1, line);
                System.out.println(line);
                sayAll(this, msg);
                setUsers(this, -1);
                setFreeThreads(1);

                try {
                    socket.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    static void sayAll(WorkThread thread, Message msg) {
        LinkedList<WorkThread> users = setUsers(thread, 0);
        for (WorkThread user : users) {
            if (!user.equals(thread)) {
                try {
                    ObjectOutputStream writer = user.out;
                    writer.writeObject(msg);
                    writer.flush();
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
                    } else if (line.equals("shutdown")) {
                        System.out.println("shutdown");
                        sayAll(null, new Message(1, "Admin closed server"));
                        LinkedList<WorkThread> users = setUsers(null, 0);
                        for (WorkThread user : users) {
                            user.interrupt();
                            user.socket.close();
                        }
                        serverSocket.close();
                    } else if (m.find()) {
                        LinkedList<WorkThread> users = setUsers(null, 0);
                        for (WorkThread user : users) {
                            if (user.id == Long.parseLong(m.group(1))) {
                                try {
                                    ObjectOutputStream writer = user.out;
                                    writer.writeObject(new Message(1, "You're kicked"));
                                    writer.flush();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                user.interrupt();
                                user.socket.close();
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
