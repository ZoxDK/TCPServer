package com.tdoc.spooftdocserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class TCPServer extends Activity {

    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    private TextView tvMessagesReceived;

    public static final int SERVERPORT = 6667;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpserver);
        tvMessagesReceived = (TextView) findViewById(R.id.tvMessagesReceived);

        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket;
            System.out.println("Serverthread");
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //while (!Thread.currentThread().isInterrupted()) {
            try {
                Log.d("DeviceIP", InetAddress.getLocalHost().getHostAddress());
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
                try {

                    socket = serverSocket.accept();
                    System.out.println("Socketaccept");
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            //}
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {


            while (!Thread.currentThread().isInterrupted()) {

                try {

                    String read = input.readLine();
                    String answer;
                    updateConversationHandler.post(new updateUIThread(read));
                    try {
                        PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(clientSocket.getOutputStream())),
                                true);
                        if (read.equals("Item number 1")) {
                            answer = "This would be the first item.";
                        } else if (read.equals("Item number 2")) {
                            answer = "This would be the second item.";
                        } else if (read.equals("Item number 3")) {
                            answer = "This would be the third item.";
                        } else if (read.equals("Getinge!")) {
                            answer = "This is the company!";
                        } else {
                            answer = "We'll pretend we don't know what this is.";
                        }
                        out.println(answer);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            tvMessagesReceived.setText(tvMessagesReceived.getText().toString() + "Client Says: "+ msg + "\n");
        }

    }

}

