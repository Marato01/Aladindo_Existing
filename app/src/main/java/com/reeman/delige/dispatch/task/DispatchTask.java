package com.reeman.delige.dispatch.task;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;

public class DispatchTask implements Runnable {

    private final ConcurrentLinkedDeque<String> messages;
    private volatile boolean finished = false;


    public void setFinished(boolean finished) {
        this.finished = finished;
        messages.clear();
    }

    public DispatchTask(ConcurrentLinkedDeque<String> messages) {
        this.messages = messages;
    }

    @Override
    public void run() {
        while (!finished) {
            try {
                if (messages.isEmpty()) LockSupport.park();
                if (finished) break;
                String msg = messages.poll();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
