package threadpool;

import utils.Log;

/**
 * ThreadPoolTest (former ThreadPoolMain)
 */
public class ThreadPoolTest
{

    public static TestRunnable makeRunnable(final String name, final long firstDelay, final Log log)
    {
        return new TestRunnable(name, firstDelay, log);
    }

    public static void main(String[] args)
    {
        try {
            Log log = new Log("thread_pool_test", true);
            ThreadPool pool = new ThreadPool(6, 5, log);
            log.add(pool.getStatus());

            TestRunnable ra = makeRunnable("Runnable A", 3000, log);
            pool.execute(ra);
            log.add(pool.getStatus());

            TestRunnable rb = makeRunnable("Runnable B", 1000, log);
            pool.execute(rb);
            log.add(pool.getStatus());

            TestRunnable rc = makeRunnable("Runnable C", 2000, log);
            pool.execute(rc);
            log.add(pool.getStatus());

            TestRunnable rd = makeRunnable("Runnable D", 60000, log);
            pool.execute(rd);
            log.add(pool.getStatus());
            log.add("+++++" + rb + " has finished: " + rb.hasFinished);

            TestRunnable re = makeRunnable("Runnable E", 1500, log);
            pool.execute(re);
            log.add(pool.getStatus());

            TestRunnable rf = makeRunnable("Runnable F", 60000, log);
            pool.execute(rf);
            log.add(pool.getStatus());
            log.add("+++++" + rb + " has finished: " + rb.hasFinished);

            TestRunnable rg = makeRunnable("Runnable G", 20000, log);
            pool.execute(rg);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            log.add("+++++" + rb + " has finished: " + rb.hasFinished);
            log.add("+++++" + rf + " has finished: " + rf.hasFinished);

            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            log.add("+++++" + rc + " has finished: " + rc.hasFinished);
            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());
            Thread.sleep(500);
            log.add(pool.getStatus());

//            pool.stopRequestIdleWorkers();
            log.add(pool.getStatus());
            Thread.sleep(15000);
            log.add(pool.getStatus());

            TestRunnable ra2 = makeRunnable("Runnable A-2", 3000, log);
            pool.execute(ra2);
            log.add(pool.getStatus());

            TestRunnable rb2 = makeRunnable("Runnable B-2", 1000, log);
            pool.execute(rb2);
            log.add(pool.getStatus());

            TestRunnable rc2 = makeRunnable("Runnable C-2", 2000, log);
            pool.execute(rc2);
            log.add(pool.getStatus());
            pool.stopRequestIdleWorkers();
            log.add(pool.getStatus());

            TestRunnable rd2 = makeRunnable("Runnable D-2", 60000, log);
            pool.execute(rd2);
            log.add(pool.getStatus());

            TestRunnable re2 = makeRunnable("Runnable E-2", 1500, log);
            pool.execute(re2);
            log.add(pool.getStatus());

            TestRunnable rf2 = makeRunnable("Runnable F-2", 60000, log);
            pool.execute(rf2);
            log.add(pool.getStatus());

            TestRunnable rg2 = makeRunnable("Runnable G-2", 20000, log);
            pool.execute(rg2);
            log.add(pool.getStatus());

            Thread.sleep(50000);
            log.add(pool.getStatus());
            pool.stopRequestAllWorkers();
            log.add(pool.getStatus());

            log.close();
        } catch ( InterruptedException ix ) {
            ix.printStackTrace();
        }
    }

    static class TestRunnable
            implements Runnable
    {
        public boolean hasFinished = false;
        private Log log;
        private String name;
        private long firstDelay;

        TestRunnable(String name, long firstDelay, Log log)
        {
            this.name = name;
            this.firstDelay = firstDelay;
            this.log = log;
        }

        public void run()
        {
            try {
                log.add("***" + name + ": starting up (next step in " + firstDelay + " ms)");
                Thread.sleep(firstDelay);
                log.add("***" + name + ": doing some stuff");
                Thread.sleep(2000);
                log.add("***" + name + ": leaving");
            } catch ( InterruptedException ix ) {
                log.add("***" + name + ": got interrupted!");
                // return; // Unnecessary
            } catch ( Exception x ) {
                x.printStackTrace();
            } finally {
                hasFinished = true;
            }
        }

        public String toString()
        {
            return name;
        }
    }
}
 