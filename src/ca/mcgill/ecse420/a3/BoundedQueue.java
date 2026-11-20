package ca.mcgill.ecse420.a3;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue<T> {
    ReentrantLock enqLock, deqLock;
    Condition notEmptyCondition, notFullCondition;
    AtomicInteger size;
    T[] items;
    int head; // next slot to dequeue
    int  tail; //  next slot to enqueue
    int capacity;

    public BoundedQueue(int _capacity) {
        capacity = _capacity;
        items = (T[]) new Object[capacity];
        head = 0;
        tail = 0;
        size = new AtomicInteger(0);
        enqLock = new ReentrantLock();
        notFullCondition = enqLock.newCondition();
        deqLock = new ReentrantLock();
        notEmptyCondition = deqLock.newCondition();
    }

    public void enq(T x) throws InterruptedException {
        boolean mustWakeDequeuers = false;
        enqLock.lock();
        try {
            while (size.get() == capacity)
                notFullCondition.await();
            items[tail] = x;
            tail = (tail + 1) % capacity;
            if (size.getAndIncrement() == 0)
                mustWakeDequeuers = true;
        }
        finally {
            enqLock.unlock();
        }
        if (mustWakeDequeuers) {
            deqLock.lock();
            try {
                notEmptyCondition.signalAll();
            }
            finally {
                deqLock.unlock();
            }
        }
    }

    public T deq() throws InterruptedException {
        T result;
        boolean mustWakeEnqueuers = false;
        deqLock.lock();
        try {
            while (size.get() == 0)
                notEmptyCondition.await();
            result = items[head];
            items[head] = null;
            head = (head + 1) % capacity; // head is the next element
            if (size.getAndDecrement() == capacity) {
                mustWakeEnqueuers = true;
            }
        }
        finally {
            deqLock.unlock();
        }
        if (mustWakeEnqueuers) {
            enqLock.lock();
            try {
                notFullCondition.signalAll();
            }
            finally {
                enqLock.unlock();
            }
        }
        return result;
    }
}