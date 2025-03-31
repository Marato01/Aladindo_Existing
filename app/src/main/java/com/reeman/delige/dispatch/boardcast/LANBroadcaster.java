package com.reeman.delige.dispatch.boardcast;

import android.app.Application;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.google.gson.Gson;
import com.reeman.delige.dispatch.model.RobotInfo;
import com.reeman.delige.dispatch.util.DispatchUtil;
import com.reeman.ros.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LANBroadcaster {
    private static LANBroadcaster instance;
    private MulticastSocket socket;
    private InetAddress broadcastAddress;
    private int port;
    private boolean isReceiving;
    private static boolean isStart = false;
    private Thread receivingThread;

    private Thread messageThread;
    private final Gson gson = new Gson();
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    public static boolean isStart() {
        return isStart;
    }

    private LANBroadcaster(Context context) {
        try {
//            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//            int broadcast = (dhcpInfo.ipAddress & dhcpInfo.netmask) | ~dhcpInfo.netmask;
//            byte[] broadcastBytes = new byte[4];
//            for (int i = 0; i < 4; i++) {
//                broadcastBytes[i] = (byte) ((broadcast >> i * 8) & 0xFF);
//            }
            broadcastAddress = InetAddress.getByName("224.0.0.1");
            port = 4446;
            socket = new MulticastSocket(port);
            socket.joinGroup(broadcastAddress);
//            socket.setBroadcast(true);
            startReceiving();
            isStart = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized LANBroadcaster getInstance(Context context) {
        if (instance == null) {
            instance = new LANBroadcaster(context);
        }
        return instance;
    }

    public void sendBroadcast(RobotInfo robotInfo) {
        try {
            String message = gson.toJson(robotInfo);
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startReceiving() {
        isReceiving = true;
        receivingThread = new Thread(() -> {
            byte[] receiveData = new byte[1024]; // 调整为适当的大小
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            Log.w("开启线程接收消息:" + isReceiving);
            while (isReceiving) {
                try {
                    socket.receive(receivePacket);
                    String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    messageQueue.offer(receivedMessage);
//                    DispatchUtil.Companion.updateRobotList(gson.fromJson(receivedMessage, RobotInfo.class));
//                    listener.onMessageReceived(receivedMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receivingThread.start();
        messageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isReceiving) {
                    if (!messageQueue.isEmpty()) {
                        String poll = messageQueue.poll();
                        DispatchUtil.Companion.updateRobotList(gson.fromJson(poll, RobotInfo.class));
                    }
                }
            }
        });
        messageThread.start();

    }

    public void stopReceiving() {
        isReceiving = false;
        if (receivingThread != null) {
            try {
                receivingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (messageThread != null) {
            try {
                messageThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        isStart = false;
        stopReceiving();
        if (socket != null && !socket.isClosed()) {
            try {
                socket.leaveGroup(broadcastAddress);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
