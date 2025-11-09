import java.util.LinkedList;
import java.util.Queue;

public class SchedulerRR extends SchedulerBase implements Scheduler{

    private Queue<Process> ready = new LinkedList<>();
    private Logger logger;
    private int quantum;
    // portion of quantum elapsed
    private int sliceElapsed = 0;

    public SchedulerRR(Logger logger, int quantum) {
        this.logger = logger;
        this.quantum = quantum;
    }

    @Override
    public void notifyNewProcess(Process p) {
        ready.add(p);
    }

    public Process update(Process cpu) {

        // if cpu idle schedule next
        if (cpu == null) {
            Process next = ready.poll();
            // reset slice time elapsed
            sliceElapsed = 0;
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

            // requeue if still work to do
            if (!execDone) {
                ready.add(cpu);
            }

            // switch for removing process
            contextSwitches++;

            // queue next process
            Process next = ready.poll();
            // reset slice time elapsed
            sliceElapsed = 0;
            if (next != null) {
                contextSwitches++;
                logger.log("CPU 0 > Scheduled " + next.getName());
            }
            return next;
        }
        // preemptive switch on quantum
        sliceElapsed++;
        if (sliceElapsed >= quantum) {
            // time quantum complete, requeue current process
            contextSwitches++;
            logger.log("CPU 0 > Time quantum complete for process " + cpu.getName());

            ready.add(cpu);

            // queue next process
            Process next = ready.poll();
            // reset slice time elapsed
            sliceElapsed = 0;
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