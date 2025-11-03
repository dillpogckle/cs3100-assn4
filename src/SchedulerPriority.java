import java.util.LinkedList;
import java.util.Queue;

public class SchedulerPriority extends SchedulerBase implements Scheduler{

    private Queue<Process> ready = new LinkedList<>();
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

        if (cpu == null) {
            Process next = ready.poll();
            if (next != null) contextSwitches++;
            return next;
        }

        if (cpu.isBurstComplete() || cpu.isExecutionComplete()) {
            contextSwitches++;
            return ready.poll();
        }

        return cpu;
    }
}