import java.util.LinkedList;
import java.util.Queue;

public class SchedulerFCFS extends SchedulerBase implements Scheduler {

    private Queue<Process> ready = new LinkedList<>();
    private Logger logger;

    public SchedulerFCFS(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void notifyNewProcess(Process p) {
        ready.add(p);
    }

    @Override
    public Process update(Process cpu) {

        // if CPU idle schedule next
        if (cpu == null) {
            Process next = ready.poll();
            if (next != null) {
                contextSwitches++;
                logger.log("CPU 0 > Scheduled " + next.getName());
            }
            return next;
        }

        // burst complete
        if (cpu.isBurstComplete() || cpu.isExecutionComplete()) {

            // log events
            if (cpu.isBurstComplete()) {
                logger.log("CPU 0 > Process " + cpu.getName() + " burst complete");
            }

            if (cpu.isExecutionComplete()) {
                logger.log("CPU 0 > Process " + cpu.getName() + " execution complete");
            }

            // if burst complete but not execution complete, requeue
            if (!cpu.isExecutionComplete()) {
                ready.add(cpu);
            }

            contextSwitches++;

            Process next = ready.poll();
            if (next != null) {
                contextSwitches++;
                logger.log("CPU 0 > Scheduled " + next.getName());
            }

            return next;
        }

        // Continue current process
        return cpu;
    }
}