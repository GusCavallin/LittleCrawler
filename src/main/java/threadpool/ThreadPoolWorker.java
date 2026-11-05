package threadpool;

import utils.Log;

import java.util.Timer;
import java.util.TimerTask;

public class ThreadPoolWorker
{
    private static int nextWorkerID = 1; // First worker is worker 1.

    private FIFOQueue<ThreadPoolWorker> idleWorkers;
    private int workerID;
    private FIFOQueue<Runnable> handoffBox;
    private Log log;

    private Thread internalThread;
    private volatile boolean noStopRequested;
    private boolean busy; // Flag with better precision than the Idle Workers list.
    private String currentRunnableName;
    private int defaultTimeout;
    private int threadTimeout;

    public ThreadPoolWorker(FIFOQueue<ThreadPoolWorker> idleWorkers, final int defaultTimeout, final Log log)
    {
        this.idleWorkers = idleWorkers;
        this.log = log;
        this.defaultTimeout = defaultTimeout;

        workerID = getNextWorkerID();
        handoffBox = new FIFOQueue<Runnable>(1); // only one slot

        // just before returning, the thread should be created.
        noStopRequested = true;

        Runnable r = new Runnable() {
            public void run() {
                try {
                    runWork();
                } catch ( Exception x ) {
                    // in case ANY exception slips through
                    addLog("ERROR", x);
                }
            }
        };

        internalThread = new Thread(r);
        internalThread.setName("Worker_" + workerID);
        internalThread.start();
    }

    private TimerTask createTimerTask(final int timeout)
    {
        return new TimerTask(){
            public void run(){
                addLog("ERROR: Thread has timed out after " + timeout + " seconds");
                cancelRequest();
            }
        };
    }

    public static synchronized int getNextWorkerID()
    {
        // notice: sync'd at the class level to ensure uniqueness
        int id = nextWorkerID;
        nextWorkerID++;
        return id;
    }

    public void process(Runnable target) throws InterruptedException
    {
        process(target, defaultTimeout);
    }

    public void process(Runnable target, int timeout) throws InterruptedException
    {
        threadTimeout = timeout;
        handoffBox.add(target);
    }

    private void runWork()
    {
        while ( noStopRequested ) {
            try {
                addLog("READY for work!");
                // Worker is ready for work. This will never block
                // because the idleWorker FIFO queue has
                // enough capacity for all the workers.
                idleWorkers.add(this);
                // wait here until the server adds a request
                Runnable r = handoffBox.remove();

                busy = true;
                runIt(r); // catches all exceptions
                busy = false;
            } catch ( InterruptedException x ) {
                addLog("Thread interrupted. Re-asserting interruption");  // TODO
                Thread.currentThread().interrupt(); // re-assert
            } finally {
                if ( noStopRequested ) {
                    boolean interrupted = Thread.interrupted();
                    addLog("Worker has finished. Interrupted flag was CLEARED (Thread.interrupted: " + interrupted + "; internalThread.interrupted: " + internalThread.isInterrupted() + "; busy: " + busy + ")");  // TODO
                }
            }
        }
    }

    private void runIt(Runnable r)
    {
        Timer timer = null;
        try {
            currentRunnableName = r.toString();
            addLog("STARTING execution of new Runnable");
            if (threadTimeout > 0) {
                timer = new Timer(); // A timer cannot be reused. A new one needs to be created for every new execution.
                timer.schedule(createTimerTask(threadTimeout), threadTimeout * 1000);
            }
            r.run();
        } catch ( Exception runex ) {
            // catch any and all exceptions
            addLog("ERROR: Uncaught exception fell through from run()", runex);
        } finally {
            // Clear the interrupted flag (in case it comes back
            // set) so that if the loop goes again, the
            // handoffBox.remove() does not mistakenly
            // throw an InterruptedException.          TODO: GUS: ???
            boolean interrupted = Thread.interrupted();
            if (timer != null) {
                timer.cancel();
                int removedTasks = timer.purge(); // TODO: Absolutely remove previous cancel operations.
                addLog("Timer has been cancelled. Removed Tasks: " + removedTasks);
            }
            currentRunnableName = null;
            // addLog("Worker has finished. Interrupted flag was cleared (interrupted: " + interrupted + ")");  // Extra debug.
        }
    }

    private void addLog(String msg, Throwable t)
    {
        log.add(getDetailedLogMsg(msg), t);
    }

    private void addLog(String msg)
    {
        log.add(getDetailedLogMsg(msg));
    }

    private String getDetailedLogMsg(String msg) 
    {
        String logMessage = "WorkerID=" + workerID + "|" + msg + ".";
        if (currentRunnableName != null) {
            logMessage += " Thread name: " + currentRunnableName;
        }

        return logMessage;
    }

    public void cancelRequest()
    {
        boolean wasInterrupted = internalThread.isInterrupted();
        internalThread.interrupt();
        addLog("*** cancelRequest() received. internalThread wasInterrupted: " + wasInterrupted  + ", isInterrupted: " + internalThread.isInterrupted() + ", busy: " + busy);
    }

    public void stopRequest()
    {
        boolean wasInterrupted = internalThread.isInterrupted();
        noStopRequested = false;
        internalThread.interrupt();
        addLog("--- stopRequest() received. internalThread wasInterrupted: " + wasInterrupted  + ", isInterrupted: " + internalThread.isInterrupted() + ", busy: " + busy);
    }

    public boolean isAlive()
    {
        return internalThread.isAlive();
    }

    public Thread.State getThreadState()
    {
        return internalThread.getState();
    }

    public boolean isBusy()
    {
        return busy;
    }
}