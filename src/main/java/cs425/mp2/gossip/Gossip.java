package cs425.mp2.gossip;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by ayush on 12/11/15.
 */
public class Gossip {

    private MembershipList group;
    private String my_ip;

    // Default parameters
    private int my_port=3128;

    private int T_GOSSIP = 1000;
    private int T_SUSPECT = 3000;
    private int T_FAIL = 6000;
    private int K = 3;
    private String[] introducers = {"fa15-cs425-g50-01.cs.illinois.edu", "fa15-cs425-g50-02.cs.illinois.edu", "fa15-cs425-g50-03.cs.illinois.edu","fa15-cs425-g50-04.cs.illinois.edu"};

    public Gossip(){

        //Read parameters from config file
        Properties prop = new Properties();
        InputStream input = null;

        try {

            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("config.properties").getFile());

            input = new FileInputStream(file);

            prop.load(input);

            this.my_port = Integer.parseInt(prop.getProperty("port"));
            this.T_FAIL = Integer.parseInt(prop.getProperty("T_FAIL"));
            this.T_GOSSIP = Integer.parseInt(prop.getProperty("T_GOSSIP"));
            this.T_SUSPECT = Integer.parseInt(prop.getProperty("T_SUSPECT"));
            this.K = Integer.parseInt(prop.getProperty("K"));
            this.introducers = prop.getProperty("introducers").split(",");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            this.my_ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("My Address: " + my_ip);
        } catch (UnknownHostException e) {
            this.my_ip = "localhost";
        }

        this.group = new MembershipList(my_ip, my_port);


        for (String introducer_ip: introducers){
            Integer introducer_port = my_port;
            String jsonToSend = group.getJSON().toString();
            try {
                Socket socket = new Socket(introducer_ip, introducer_port);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(jsonToSend);
                out.close();
                socket.close();
            } catch (Exception e) {

            }
        }

    }

    public void start_gossip(){

        Thread send_gossip = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    List<String> keys = new ArrayList<>();
                    final boolean b = keys.addAll(group.getProcesses());
                    for (String node : keys){
                        long currTime = System.currentTimeMillis();
                        if (group.hasNode(node) && currTime - group.get(node).getHeartbeatTime() >= T_FAIL){
                            group.get(node).setStatus(NodeStatus.FAILED);
                            group.remove(node);
                        }
                        else if (group.hasNode(node) && currTime - group.get(node).getHeartbeatTime() >= T_SUSPECT){
                            group.get(node).setStatus(NodeStatus.SUSPECT);
                        }
                    }



                    java.util.Collections.shuffle(keys);
                    int myHeartbeat = group.getHeartbeatFor(my_ip, my_port);
                    group.setHeartbeatFor(my_ip, my_port, myHeartbeat + 1);
                    int i = 0;
                    for (String node : keys) {
                        if (i == K) {
                            break;
                        }
                        if (!group.hasNode(node)){
                            continue;
                        }
                        if (group.hasNode(node) && group.get(node).getStatus() != NodeStatus.ALIVE) {
                            continue;
                        }
                        if (group.hasNode(node) && group.get(node).getAddr() == my_ip && group.get(node).getPort() == my_port) {
                            continue;
                        }
                        String addr = group.get(node).getAddr();
                        int port = group.get(node).getPort();
                        String jsonToSend = group.getJSON().toString();
                        try {
                            Socket socket = new Socket(addr, port);
                            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(jsonToSend);
                            out.close();
                            socket.close();
                            i++;
                        } catch (IOException e) {

                        }
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(T_GOSSIP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int portNumber = my_port;

                try {
                        ServerSocket serverSocket =
                                new ServerSocket(portNumber);
                        while (true) {
                            Socket clientSocket = serverSocket.accept();
                            BufferedReader in = new BufferedReader(
                                    new InputStreamReader(clientSocket.getInputStream()));

                            String inputLine;
                            while ((inputLine = in.readLine()) != null) {
                                group.merge(inputLine);
                            }
                        }
                } catch (IOException e) {
                    System.out.println("Exception caught when trying to listen on port "
                            + portNumber + " or listening for a connection");
                    System.out.println(e.getMessage());
                }
            }
        });
        t2.start();
        send_gossip.start();

        BufferedReader stdIn =
                new BufferedReader(
                        new InputStreamReader(System.in));
        String userInput;
        try {
            System.out.println("Type 'p' to print the nodes in the membership table");
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.equals("p")){
                    System.out.println(group.getProcesses());
                }
                System.out.println("Type 'p' to print the nodes in the membership table");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void main(String args[]){
        Gossip me = new Gossip();
        me.start_gossip();
    }
}

