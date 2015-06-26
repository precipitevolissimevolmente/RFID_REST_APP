/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main.payword.Servers;

import main.payword.MessagePacket.Info;
import main.payword.MessagePacket.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author G
 */
public class PublicTransportServer extends JFrame implements ActionListener {

    private int port = 5050, trdCnt = 0;/**/

    ServerSocket serverSocket = null;
    Socket socket = null;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private GridBagConstraints c;
    private javax.swing.JTextArea display;
    private javax.swing.JButton startJB, stopJB, exit;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JTextField ipAddressTF;
    private javax.swing.JTextField portTF;
    private javax.swing.JLabel jIpLabel;
    private javax.swing.JLabel jPortLabel;
    private boolean debug = false, loopCTL = true, loopCTL2 = true;/**/

    private Thread thrd[];/**/

    public ArrayList<Message> listOfClients = new ArrayList<Message>();
    private BigInteger R; //blinded keyshare

    /**
     * **************************************************************
     * This is the tcpServer constructor
     * **************************************************************
     */
    public PublicTransportServer() {
        super("PublicTransportServer");

        setup();

        setupThreads();

        ServerRun();
    }

    /**
     * **************************************************************
     * The setThreadcount() method resets the thread count.
     * **************************************************************
     */
    public void setThreadcount(int a) {
        trdCnt = a;
    }

    /**
     * **************************************************************
     * The setUp() method does the intialization for the application. The logic
     * is 1- Get the content pane 2- Define a Gregorian Calendar to enable us to
     * get the current time. 3- Define an exit push button. 4- Define a panel to
     * contain the exit push button. 5- Add the exit push button to the button
     * panel. 6- Using the BorderLayout manager add the buttonPanel to bottom of
     * the content pane. 7- Add an ActionListener to the exit push button. 8-
     * Define a text area to display mesaages. 9- Add the text area to the
     * center of the content pane. 10- Set the size of the main frame. 11- Set
     * the initial location of the main frame on the terminal. 12- Make the main
     * frame visible on the main frame.
     * <p>
     * **************************************************************
     */
    public void setup() {
        //c = getContentPane();

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH; // components grow in both dimensions
        c.insets = new Insets(5, 5, 5, 5); // 5-pixel margins on all sides


        buttonPanel = new javax.swing.JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        jIpLabel = new javax.swing.JLabel();
        jIpLabel.setText("Ip Address");
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(jIpLabel, c);

        ipAddressTF = new javax.swing.JTextField("127.0.0.1");
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(ipAddressTF, c);

        jPortLabel = new javax.swing.JLabel();
        jPortLabel.setText("Port");
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(jPortLabel, c);

        portTF = new javax.swing.JTextField();
        portTF.setText(Integer.toString(port));
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(portTF, c);

        startJB = new javax.swing.JButton("Start");
        startJB.setEnabled(false);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(startJB, c);

        stopJB = new javax.swing.JButton("Stop");
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(stopJB, c);


        display = new javax.swing.JTextArea();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.gridheight = 2;
        c.weightx = c.weighty = 1.0;
        buttonPanel.add(display, c);

        exit = new javax.swing.JButton("Exit");
        exit.setBackground(Color.red);
        exit.setForeground(Color.white);
        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = c.weighty = 0.0;
        buttonPanel.add(exit, c);

        exit.addActionListener(this);

        getContentPane().add(buttonPanel);
        setSize(400, 400);
        setLocation(10, 20);
        setVisible(true);
    }

    /**
     * ******************************************************************
     * The setupThreads() method
     * *******************************************************************
     */
    public void setupThreads() {

        thrd = new Thread[15];
    }

    /**
     * ******************************************************************
     * The ServerRun() method in the server reads and writes data to the client.
     * the logic for the ServerRun() method is 1- Create a ServerSocket object
     * 2- Create messages and display them in the text area. 3- Loop while
     * waiting for Client connections. 4- Call ServerSocket accept() method and
     * listen. 5- Create an InputStreamReader based on the
     * socket.getInputStream() object 6- Create a BufferedReader based on the
     * InputStreamReader object 7- Create a new MyThread object 8- Start the new
     * MyThread object
     * *******************************************************************
     */
    public void ServerRun() {
        try {

            try {
                serverSocket = new ServerSocket(port);
            } catch (BindException e) {
                port++;
                portTF.setText(Integer.toString(port));
                try {
                    serverSocket = new ServerSocket(port);
                } catch (BindException e1) {
                    port++;
                    portTF.setText(Integer.toString(port));
                    serverSocket = new ServerSocket(port);
                }
            }


            display.setText("This is a multithreded server\n");
            display.append("Server waiting for client on port "
                    + serverSocket.getLocalPort() + "\n");


            while (loopCTL) {
                socket = serverSocket.accept();
//                SocketChannel sChannel = ssChannel.accept();
//                sysPrint("New connection accepted "
//                        + sChannel.getLocalAddress());
//                display.append("New connection accepted " + sChannel.getLocalAddress());
                display.append("New connection accepted " + socket.getLocalPort());


                // Construct handler to process the Client request message.
                try {
                    MyThread request =
                            new MyThread(this, socket, trdCnt);
                    thrd[trdCnt] = request;

                    // Start the thread.
                    thrd[trdCnt].start();
                    trdCnt++;
                } catch (Exception e) {
                    sysPrint("" + e);
                }
            }   // End of while loop*/

        } catch (IOException e) {
            display.append("\n" + e);
        }
    }

    public void srvListen(ServerSocket srvSok) {
        try {
            while (loopCTL) {
                socket = serverSocket.accept();
//                SocketChannel sChannel = ssChannel.accept();

//             Socket socket = server_socket.accept();
                sysPrint("\nNew connection accepted "
                        + socket.getLocalAddress());

                input = new ObjectInputStream(socket.getInputStream());
//                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Construct handler to process the Client request message.
                try {
                    MyThread request =
                            new MyThread(this, socket, trdCnt);
                    thrd[trdCnt] = request;

                    // Start the thread.
                    thrd[trdCnt].start();
                    trdCnt++;
                } catch (Exception e) {
                    sysPrint("" + e);
                }
            }   // End of while loop*/
        } catch (IOException e) {
            display.append("\n" + e);
        }
    }

    /**
     * ******************************************************************
     * This method responds to the exit button being pressed on the tcpServer
     * frame.
     * ********************************************************************
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exit) {
            sysExit(0);
        }
    }

    /**
     * ****************************************************************
     * This method closes the socket connect to the server.
     * *****************************************************************
     */
    private void closeConnection() {
        sysPrint("There are " + trdCnt + " currently threads running.");
        for (int ii = 0; ii < trdCnt; ii++) {
            thrd[ii] = null;
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            display.append("\n" + e);
        } catch (NullPointerException e) {
            System.out.println("Inca nu a fost deschis socket-ul");
        }

    }

    /**
     * ***********************************************************
     * The sysExit() method is called in response to a close application event.
     * ************************************************************
     */
    public void sysExit(int ext) {
        System.out.println("Iesire");
        loopCTL = false;
        loopCTL2 = false;
        closeConnection();
        System.exit(ext);
    }

    /**
     * ***********************************************************
     * The sysPrint method prints out debugging messages.
     * ***********************************************************
     */
    public void sysPrint(String str) {
        if (debug) {
            System.out.println("" + str);
        }
    }

    /**
     * ***********************************************************
     * The main() is called by Java when the tcpServer program is loaded.
     * ***********************************************************
     */
    public static void main(String args[]) {
        final PublicTransportServer server = new PublicTransportServer();
        server.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        server.sysExit(0);
                    }
                } // End of WindowAdapter()
        );  // End of addWindowListener
    }

    /**
     * ***********************************************************
     * The purpose of the MyThread class is to create a thread of execution to
     * respond to client requests. A thread is a thread of execution in a
     * program. The Java Virtual Machine allows an application to have multiple
     * threads of execution running concurrently.
     * ************************************************************
     */
    public class MyThread extends Thread {

        Socket socket;
        private ObjectInputStream input2;
        private ObjectOutputStream output2;
        private ObjectInputStream br;
        private ObjectOutputStream outp;
        private int trdCnt;
        private PublicTransportServer tcpS;

        /**
         * *********************************************************
         * The purpose of the MyThread() constructor is to used the passed
         * parameters to initialize MyThread class level variables.
         * ************************************************************
         */
        public MyThread(PublicTransportServer tps, Socket socket, int trd_Cnt) throws Exception {
            trdCnt = trd_Cnt;
            tcpS = tps;
            this.socket = socket;

            this.br = new ObjectInputStream(socket.getInputStream());


            this.output2 = new ObjectOutputStream(socket.getOutputStream());
            this.output2.flush();

            System.out.println("thread initializat");
        }

        /**
         * *********************************************************
         * The run() method responds to the client's request.
         * ************************************************************
         */
        public void run() {
            sysPrint("Thread run() 1: running Thread" + (trdCnt + 1));
            display.append("\nThread run() 1: running Thread" + (trdCnt + 1));

            try {

                while (loopCTL2) {
                    System.out.println("astept mesajele------");
                    Message messageToClient = new Message();
                    Message messagePacket = (Message) this.br.readObject();
                    System.out.println(messagePacket.getMessage());

                    if (messagePacket.getMessage().equals("getCertificateAndCharge")) {
                        listOfClients.add(messagePacket);
                        System.out.println("enrollClient ok!");

                        System.out.println(messagePacket.getPublicKey());
                        RSA rsa = new RSA();

                        /*C(U)=(B,U,IPU,K,EXP,INFO, SIG(THIS))*/

                        messageToClient.setMessage("enrollClient ok!");
                        messageToClient.setIdBank("RATP");
                        messageToClient.setCardNumber(messagePacket.getCardNumber()); //U
                        messageToClient.setIPU(this.socket.getRemoteSocketAddress().toString()); //IPU
                        messageToClient.setPublicKey(messagePacket.getPublicKey()); //K
                        Info info = new Info();
                        info.setAddress(messagePacket.getInfo().getAddress());
                        info.setSum(messagePacket.getInfo().getSum() + messagePacket.getInfo().getCurrentAmount());
                        messageToClient.setInfo(info);//Info

                        Date expDate = new Date();
                        Calendar c = Calendar.getInstance();
                        c.setTime(expDate);
                        c.add(Calendar.DATE, 1);
                        expDate = c.getTime();
                        messageToClient.setExpDate(expDate);//EXP

                        String dataToSign = messageToClient.toString();

                        String encriptedData = rsa.encriptLongData(dataToSign);
                        messagePacket.setEncryptedData(encriptedData);
                        listOfClients.add(messagePacket);
                        messageToClient.setEncryptedData(encriptedData);
                        messageToClient.setPublicKeyRSA(rsa.getPublicKeyRSA());
                        System.out.println(encriptedData);

                        this.output2.writeObject(messageToClient);
                        sysPrint("processRequest() 2:" + messagePacket.getMessage());

                        display.append("\nThread" + (trdCnt + 1) + " " + messagePacket.getMessage());

                        System.out.println(rsa.decryptLongData(encriptedData));
                    } else if (messagePacket.getMessage().toUpperCase().equals("QUIT")) {
                        messageToClient.setMessage("QUIT");
                        System.out.println(listOfClients.size());
                        this.output2.writeObject(messageToClient);
//                        this.br.close();
//                        this.output2.close();
//                        this.socket.close();
                        loopCTL2 = true;
                    }
                }
            } catch (Exception e) {
                //System.out.println(e);
            }
        }
    }
}
