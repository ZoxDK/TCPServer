package com.tdoc.spooftdocserver;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * The class extends the Thread class so we can receive and send messages at the same time
 */
public class TCPServer extends Activity implements Runnable {

    public static final int SERVERPORT = 6667;
    private boolean running = false;
    private PrintWriter mOut;
    private OnMessageReceived messageListener;
    private TextView tvMessagesReceived;
    private ServerSocket serverSocket;
    private String clntMessage = "";

    public TCPServer(){}

    /**
     * Constructor of the class
     * @param messageListener listens for the messages
     */
    public TCPServer(OnMessageReceived messageListener) {
        Looper.prepare();
        this.messageListener = messageListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpserver);

        tvMessagesReceived = (TextView) findViewById(R.id.tvMessagesReceived);

        try {
            //creates the object OnMessageReceived asked by the TCPServer constructor
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object... arg0) {
                    try {
                        TCPServer server = new TCPServer(new TCPServer.OnMessageReceived() {

                            @Override
                            //this method declared in the interface from TCPServer class is implemented here
                            //this method is actually a callback method, because it will run every time when it will be called from
                            //TCPServer class (at while)
                            public void messageReceived(String message) {
                                clntMessage = message;
                                sendMessage("TDOC got: " + message);
                                if (message.equals("Item number 1")) {
                                    sendMessage("This would be the first item.");
                                } else if (message.equals("Item number 2")) {
                                    sendMessage("This would be the second item.");
                                } else if (message.equals("Item number 3")) {
                                    sendMessage("This would be the third item.");
                                } else if (message.equals("Getinge!")) {
                                    sendMessage("This is the company!");
                                } else {
                                    sendMessage("We'll pretend we don't know what this is.");
                                }
                            }
                        });
                        server.run();
                        return clntMessage;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return e;
                    }
                }

                @Override
                protected void onPostExecute(Object message) {
                    tvMessagesReceived.append("\n " + message);
                }
            }.execute();
        } catch (Exception e){
            e.printStackTrace();
            tvMessagesReceived.setText("Der skete en fejl:\n" + e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();

    }


    /**
     * Method to send the messages from server to client
     * @param message the message sent by the server
     */
    public void sendMessage(String message){
        if (mOut != null && !mOut.checkError()) {
            mOut.println(message);
            mOut.flush();
        }
    }
    public void stopServer(){
        running = false;
    }

    @Override
    public void run() {

        running = true;

        try {
            System.out.println("S: Connecting...");

            //create a server socket. A server socket waits for requests to come in over the network.
            serverSocket = new ServerSocket(SERVERPORT);

            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
            Socket client = serverSocket.accept();
            System.out.println("S: Receiving...");
            System.out.println(client.getInetAddress().getHostAddress());

            try {

                //sends the message to the client
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);

                //read the message received from client
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                //in this while we wait to receive messages from client (it's an infinite loop)
                //this while it's like a listener for messages
                while (running) {
                    String message = in.readLine();

                    if (message != null && messageListener != null) {
                        //call the method messageReceived from ServerBoard class
                        messageListener.messageReceived(message);
                    }
                }

            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            } finally {
                client.close();
                System.out.println("S: Done.");
            }

        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the ServerBoard
    //class at on startServer button click
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
