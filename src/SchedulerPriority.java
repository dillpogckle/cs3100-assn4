import java.util.Comparator;
import java.util.PriorityQueue;

public class SchedulerPriority extends SchedulerBase implements Scheduler{

    private PriorityQueue<Process> ready = new PriorityQueue<>(Comparator.comparingInt(Process::getPriority));
    private Logger logger;

    public SchedulerPriority(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void notifyNewProcess(Process p) {
        ready.add(p);
    }

    @Override
    public Process update(Process cpu) {

        // if cpu idle schedule next
        if (cpu == null) {
            Process next = ready.poll();
            if (next != null) {
                contextSwitches++;
                logger.log("CPU 0 > Scheduled " + next.getName());
            }
            return next;
        }

        // reduce calls
        boolean burstDone = cpu.isBurstComplete();
        boolean execDone  = cpu.isExecutionComplete();

        // burst done and maybe execution done, new process needs to be scheduled
        if (burstDone || execDone) {

            // log burst done
            if (burstDone) {
                logger.log("CPU 0 > Process " + cpu.getName() + " burst complete");
            }

            // log execution done
            if (execDone) {
                logger.log("CPU 0 > Process " + cpu.getName() + " execution complete");
            }

            // requeue if not done
            if (!execDone) {
                ready.add(cpu);
            }

            // switch for removing process
            contextSwitches++;

            // run next process
            Process next = ready.poll();
            if (next != null) {
                contextSwitches++;
                logger.log("CPU 0 > Scheduled " + next.getName());
            }
            return next;
        }

        // preemption check
        Process best = ready.peek();
        if (best != null && best.getPriority() < cpu.getPriority()) {

            // preempt
            contextSwitches++;

            logger.log("CPU 0 > Preemptively removed: " + cpu.getName());

            // put current process back in ready queue
            ready.add(cpu);

            // run the better one
            Process next = ready.poll();
            if (next != null) {
                contextSwitches++;
                logger.log("CPU 0 > Scheduled " + next.getName());
            }
            return next;
        }

        // continue
        return cpu;
    }
}