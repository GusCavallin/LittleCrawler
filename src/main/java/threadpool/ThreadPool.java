package threadpool;

import utils.Log;

import java.util.HashMap;
import java.util.Map;

public class ThreadPool
{
    private FIFOQueue<ThreadPoolWorker> idleWorkers;
    private ThreadPoolWorker[] workerList;
    private Log log;

    public ThreadPool(int numberOfThreads, int defaultTimeout, Log log)
    {
        this.log = log;
        log.add("Thread Pool|Initializing ThreadPool. Nr Workers: " + numberOfThreads + ", Worker Timeout: " + defaultTimeout + "s.");

        // make sure that it's at least one
        numberOfThreads = Math.max(1, numberOfThreads);

        idleWorkers = new FIFOQueue<ThreadPoolWorker>(numberOfThreads);
        workerList = new ThreadPoolWorker[numberOfThreads];

        for ( int i = 0; i < workerList.length; i++ ) {
            workerList[i] = new ThreadPoolWorker(idleWorkers, defaultTimeout, log);
        }
    }

    /**
     * NOTE: execute SHOULD NOT be called within a Worker because this will Block the Worker!!!
     * If all workers call this method, this will create a deadlock.
     */
    public void execute(Runnable target)
            throws InterruptedException
    {
        getIdleWorker().process(target);
    }

    /**
     * NOTE: execute SHOULD NOT be called within a Worker because this will Block the Worker!!!
     * If all workers call this method, this will create a deadlock.
     */
    public void execute(Runnable target, int timeout)
            throws InterruptedException
    {
        getIdleWorker().process(target, timeout);
    }

    /**
     * Block (forever) until a worker is available
     */
    private ThreadPoolWorker getIdleWorker()
            throws InterruptedException
    {
        return idleWorkers.remove();
    }

    public void stopRequestIdleWorkers()
    {
        log.add("Thread Pool|Stop request has been issued to IDLE Workers");
        try {
            for (ThreadPoolWorker idleWorker : idleWorkers.removeAll()) {
                idleWorker.stopRequest();
            }
        } catch ( InterruptedException x ) {
            Thread.currentThread().interrupt(); // re-assert
        }
    }

    public void stopRequestAllWorkers()
    {
        // Stop the idle one's first
        // productive.
        stopRequestIdleWorkers();

        // give the idle workers a quick chance to die
        try { Thread.sleep(500); } // Default was 250
        catch ( InterruptedException x )
        {
            log.add(x.getMessage(), x);
        } // TODO

        log.add("Thread Pool|Stop request has been issued to ALL Workers.");
        
        // Step through the list of ALL workers.
        for (ThreadPoolWorker aWorkerList : workerList) {
            if (aWorkerList.isAlive()) {
                aWorkerList.stopRequest();
            }
        }
    }

    public int getNumberOfLiveWorkers()
    {
        int counter = 0;
        for (ThreadPoolWorker aWorkerList : workerList) {
            if (aWorkerList.isAlive()) {
                counter++;
            }
        }
        return counter;
    }

    public int getNumberOfBusyWorkers()
    {
        int counter = 0;
        for (ThreadPoolWorker aWorkerList : workerList) {
            if (aWorkerList.isBusy()) {
                counter++;
            }
        }
        return counter;
    }

    public Map<Thread.State, Integer> getWorkerStates()
    {
        Map<Thread.State, Integer> states = new HashMap<Thread.State, Integer>();

        for (ThreadPoolWorker aWorker : workerList) {
            Thread.State ts = aWorker.getThreadState();
            if (states.containsKey(ts)) {
                states.put(ts, states.get(ts) + 1);
            } else {
                states.put(ts, 1);
            }
        }
        return states;
    }

    public boolean isBusy()
    {
        return getNumberOfBusyWorkers() > 0;
    }

    public String getStatus()
    {
        // Write Status String
        StringBuilder sb = new StringBuilder();
        sb.append("Thread Pool -> Total workers: ");
        int totalWorkers = workerList.length; 
        sb.append(totalWorkers);
        sb.append(", Idle Workers: ");
        int numberOfIdleWorkers = idleWorkers.getSize();
        sb.append(numberOfIdleWorkers);
        sb.append(", Live Workers: ");
        sb.append(getNumberOfLiveWorkers());
        sb.append(", Busy Workers: ");
        sb.append(getNumberOfBusyWorkers());
        sb.append("\nThread Pool -> Worker States: ");
        sb.append(getWorkerStates());
//        sb.append("\n");
//        sb.append(", Utilization: ");
//        sb.append((totalWorkers - numberOfIdleWorkers)/(float)totalWorkers*100);

        String str = sb.toString();
        log.add(str);
        // Flush log
        log.flush();

        return str;
    }
}