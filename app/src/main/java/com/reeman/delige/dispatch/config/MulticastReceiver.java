package com.reeman.delige.dispatch.config;

import org.greenrobot.eventbus.EventBus;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class MulticastReceiver {
    private MsgSender msgSender;
    private static final int port = 9999;
    private volatile boolean stopped = false;

    public void start() {
        try {
            InetAddress group;
            group = InetAddress.getByName("230.11.10.10");
            MulticastSocket ms = new MulticastSocket(port);
            ms.setSoTimeout(3000);
            ms.joinGroup(group);
            MsgReceiver msgReceiver = new MsgReceiver(ms);
            new Thread(msgReceiver, "msg-reader").start();
            msgSender = new MsgSender(ms);
            new Thread(msgSender, "msg-sender").start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (msgSender != null) {
            synchronized (msgSender.queue) {
                stopped = true;
                msgSender.queue.notifyAll();
            }
        }
    }

    public void send(String msg) {
        msgSender.send(msg);
    }

    private class MsgReceiver implements Runnable {

        private final MulticastSocket ms;
        private final DatagramPacket dp;

        public MsgReceiver(MulticastSocket ms) {
            this.ms = ms;
            byte[] buff = new byte[1024];
            dp = new DatagramPacket(buff, buff.length);
        }

        @Override
        public void run() {
            while (true) {
                if (stopped) break;
                try {
                    ms.receive(dp);
                    byte[] data = dp.getData();
                    String msg = new String(data, dp.getOffset(), dp.getLength(), StandardCharsets.UTF_8);
                    if (msg.length() >= 17 && msg.split(":").length == 6) {
                        event.msg = msg.replace("\"", "");
                        EventBus.getDefault().post(event);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MsgSender implements Runnable {
        private final MulticastSocket ms;
        private final LinkedList<String> queue;

        public void send(String msg) {
            synchronized (queue) {
                queue.addLast(msg);
                queue.notifyAll();
            }
        }

        public MsgSender(MulticastSocket ms) {
            this.ms = ms;
            queue = new LinkedList<>();
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queue) {
                    try {
                        if (queue.isEmpty()) queue.wait();
                        if (stopped) break;
                        String msg = queue.getFirst();
                        byte[] bytes = msg.getBytes();
                        InetAddress group;
                        group = InetAddress.getByName("230.11.10.10");
                        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, group, port);
                        ms.send(datagramPacket);
                        queue.removeFirst();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static final MacAddressEvent event = new MacAddressEvent();

    public static class MacAddressEvent {
        public String msg;
    }
}
