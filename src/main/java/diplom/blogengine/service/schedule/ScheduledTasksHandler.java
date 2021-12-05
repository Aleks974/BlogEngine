package diplom.blogengine.service.schedule;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ScheduledTasksHandler {
    private final ScheduledExecutorService scheduler;

    public ScheduledTasksHandler( int corePoolSize) {
        scheduler = new ScheduledThreadPoolExecutor(corePoolSize);
    }

    public void scheduleTask(Runnable runnable, int initialDelay, int delay, TimeUnit unit) {
        scheduler.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

/*    class BeeperControl {
        private final ScheduledExecutorService scheduler =
                Executors.newScheduledThreadPool(1);

        public void beepForAnHour() {
            Runnable beeper = () -> System.out.println("beep");
            ScheduledFuture<?> beeperHandle =
                    scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
            Runnable canceller = () -> beeperHandle.cancel(false);
            scheduler.schedule(canceller, 1, HOURS);
        }
    }*/
}
