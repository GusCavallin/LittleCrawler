package threadpool;

import java.util.ArrayList;
import java.util.List;

class FIFOQueue<E>
{
    private Object[] queue;

    private int capacity;

    private int size;

    private int head;

    private int tail;

    public FIFOQueue(int cap)
    {
        capacity = (cap > 0) ? cap : 1; // at least 1
        queue = new Object[capacity];
        head = 0;
        tail = 0;
        size = 0;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public synchronized int getSize()
    {
        return size;
    }

    public synchronized boolean isEmpty()
    {
        return (size == 0);
    }

    public synchronized boolean isFull()
    {
        return (size == capacity);
    }

    public synchronized void add(E obj) throws InterruptedException
    {
        waitWhileFull();

        queue[head] = obj;
        head = (head + 1) % capacity;
        size++;

        notifyAll();
    }

    public synchronized void addEach(E[] list) throws InterruptedException
    {
        for (E aList : list) {
            add(aList);
        }
    }

    @SuppressWarnings({"unchecked"})
    public synchronized E remove() throws InterruptedException
    {
        waitWhileEmpty();

        Object obj = queue[tail];

        queue[tail] = null;

        tail = (tail + 1) % capacity;
        size--;

        notifyAll();

        return (E) obj;
    }

    @SuppressWarnings({"unchecked"})
    public synchronized List<E> removeAll() throws InterruptedException
    {
        List<E> list = new ArrayList<E>(size);

        for (int i = 0; i < size; i++) {
            list.add(remove());
        }

        return list;
    }

    public synchronized List<E> removeAtLeastOne() throws InterruptedException
    {
        waitWhileEmpty();
        return removeAll();
    }

    public synchronized boolean waitUntilEmpty(long msTimeout)
            throws InterruptedException
    {
        if (msTimeout == 0L) {
            waitUntilEmpty();
            return true;
        }

        long endTime = System.currentTimeMillis() + msTimeout;
        long msRemaining = msTimeout;

        while (!isEmpty() && (msRemaining > 0L)) {
            wait(msRemaining);
            msRemaining = endTime - System.currentTimeMillis();
        }

        return isEmpty();
    }

    public synchronized void waitUntilEmpty() throws InterruptedException
    {
        while (!isEmpty()) {
            wait();
        }
    }

    public synchronized void waitWhileEmpty() throws InterruptedException
    {
        while (isEmpty()) {
            wait();
        }
    }

    public synchronized void waitUntilFull() throws InterruptedException
    {
        while (!isFull()) {
            wait();
        }
    }

    public synchronized void waitWhileFull() throws InterruptedException
    {
        while (isFull()) {
            wait();
        }
    }
}