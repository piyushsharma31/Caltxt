package com.jovistar.commons.threads;

public final class Semaphore {

    private int count = 0;

    public Semaphore(int count) {
        this.count = count;
    }

    public synchronized void acquire() {
        while (count == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        count--;
    }

    public synchronized void release() {
        count++;
        notify();
    }
}
