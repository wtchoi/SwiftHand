package edu.berkeley.wtchoi.util;

import edu.berkeley.wtchoi.logger.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: wtchoi
 * Date: 4/11/12
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TcpChannel<Packet> {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket socket;

    private int port;
    private String ip = null; // null indicate server mode

    private int tryCount = 1;
    private int tryInterval = 1000;

    private int timeout = 120000;

    //used for asynchronous connection
    private Thread __initiator;

    private TcpChannel(){}



    public static TcpChannel getServerSide(int port){
        TcpChannel ch = new TcpChannel();
        ch.port = port;
        return ch;
    }

    public void connect(){
        if(ip == null){
            listenForClient();
        }
        else{
            connectToServer();
        }
    }

    private void listenForClient(){
        ServerSocket serverSocket;

        try {
            serverSocket = new java.net.ServerSocket(port);
        }
        catch(IOException e){return;}

        try{
            //try{Thread.sleep(100000);}catch(Exception e){}
            Logger.log("TcpChannel is Listening") ;
            socket = serverSocket.accept();
            installTimeout();
            Logger.log("TcpChannel connected") ;

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            oos = new java.io.ObjectOutputStream(os);
            oos.flush();
            os.flush();

            ois = new java.io.ObjectInputStream(is);

            Logger.log("Closing Server Socket");
            serverSocket.close();
        }
        catch(IOException e){
            e.printStackTrace();
            try{serverSocket.close();} catch(Exception ee){}
        }
    }

    private void connectToServer() {
        //Log.d("wtchoi", "connectToServer:" + ip);
        try {
            //Thread.sleep(preSleep);

            int i;
            for(i = 0 ; i < tryCount ; i++){
                try {
                    System.out.println(Integer.toString(i+1)+ "trial.");
                    socket = new Socket(ip,port);
                    installTimeout();

                    //Sleep a while to wait device to be ready
                    //Thread.sleep(postSleep);

                    //NOTE! the order of opening streams is important!
                    OutputStream os = socket.getOutputStream();
                    InputStream is = socket.getInputStream();

                    oos = new ObjectOutputStream(os);
                    oos.flush();
                    os.flush();

                    ois = new ObjectInputStream(is);
                }
                catch(UnknownHostException e){
                    e.printStackTrace();
                    throw new RuntimeException("Wrong ip address");
                }
                catch(IOException e){
                    Thread.sleep(tryInterval);
                    continue;
                }
                break;
            }
            if(i == tryCount) throw new RuntimeException("Connection timeout!");
            Logger.log("Connected!");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot connect to server");
        }
    }

    public void connectAsynchronous(){
        __initiator = new Thread(new ChannelInitiator(this));
        __initiator.start();
    }

    public void waitConnection(){
        try{
            Logger.log("Channel Initiating") ;
            __initiator.join();
            Logger.log("Channel Initiated");
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot initiate channel");
        }
    }


    public static TcpChannel getClientSide(String ip, int port){
        if(ip == null)
            throw new RuntimeException("Client side channel cannot have empty target address");

        TcpChannel ch = new TcpChannel();
        ch.ip = ip;
        ch.port = port;

        return ch;
    }


    static int sendCounter = 1;
    static int sendCounterReset = 1;
    public void sendPacket(Packet p){

        try{
            oos.writeObject(p);
            oos.flush();
            if(((++sendCounter) % sendCounterReset) == 0){
                oos.reset();
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot send an object");
        }
    }

    public Packet receivePacket() throws SocketTimeoutException{
        try{
            Object obj = ois.readObject();
            return (Packet) obj;
        }
        catch (SocketTimeoutException e){
            throw e;
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot read an object");
        }
        catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot read an object");
        }
        catch(ClassCastException e){
            e.printStackTrace();
            throw new RuntimeException("Arrived object is not a packet");
        }
    }

    public Packet receivePacketIgnoreTimeout(){
        try{
            return receivePacket();
        }
        catch(Exception e){
            return null;
        }
    }


    private static class ChannelInitiator implements Runnable {
        private TcpChannel channel;

        public ChannelInitiator(TcpChannel ch){
            channel = ch;
        }

        public void run() {
            Logger.log("Channel Initiation Start");
            channel.connect();
            Logger.log("Channel Initiation Done") ;
            return;
        }
    }

    public void setTryCount(int i){
        tryCount = i;
    }

    public void setTryInterval(int i){
        tryInterval = i;
        installTimeout();
    }

    private void installTimeout(){
        if(socket != null){
            try{
                socket.setSoTimeout(timeout);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public void setTimeout(int msec){
        timeout = msec;
        installTimeout();
    }

    public void close(){
        try{
            oos.close();
            ois.close();
            socket.close();
        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Cannot close channel");
        }
    }
}


