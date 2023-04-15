import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@Slf4j
public class Server {

    private static final String METRIC_ACCEPTED = "RequestsReceived";
    private static final String METRIC_REJECTED = "RequestsRejected";
    private static final String METRIC_COMPLETED = "RequestsCompleted";

    private Queue<Request> requestQueue;
    private final Set<Request> inflightRequests;

    private int cycle;
    private final RateLimiter rateLimiter;
    private final int maxRequests;
    private final MetricsEmitter metrics;

    public Server(final RateLimiter r, final int maxRequests, final MetricsEmitter metrics) {
        this.inflightRequests = new HashSet<>();
        this.requestQueue = new LinkedList<>();

        this.maxRequests = maxRequests;

        this.rateLimiter = r;
        this.cycle = 0;

        this.metrics = metrics;
    }

    // workaround while I don't make time aware metrics
    public void receiveAll(final List<Request> r) {
        int totalAccepted = 0;
        for (Request request : r) {
            if (receive(request)) {
                totalAccepted++;
            }
        }

        metrics.emit(METRIC_REJECTED, cycle, r.size() - totalAccepted);
        metrics.emit(METRIC_ACCEPTED, cycle, totalAccepted);
    }


    public boolean receive(final Request r) {

        synchronized (rateLimiter) {
            if (!rateLimiter.hasCapacity()) {
                log.warn("Cycle {} REJECT {}", cycle, r);
                return false;
            }

            log.info("Cycle {} RECEIVE request {}", cycle, r);
            String token = rateLimiter.getToken();
            r.setToken(token);
            this.requestQueue.offer(r);
        }

        return true;
    }

    public void processCycle() {
        // every cycle, we add R tokens to the bucket, per rate R
        // And avoid race condition by blocking on rateLimiter
        synchronized (rateLimiter) {
            rateLimiter.addTokens();
        }

        // Process as many concurrent requests as possible
        while (inflightRequests.size() < maxRequests && requestQueue.size() > 0) {
            inflightRequests.add(requestQueue.poll());
        }

        // spend one cycle in each request, save the completed one
        Set<Request> completed = new HashSet<>();
        for (Request r : inflightRequests) {
            r.timeToComplete--;
            log.info("Cycle {} PROCESSED {}", cycle, r);
            if (r.timeToComplete == 0) {
                completed.add(r);
            }
        }

        metrics.emit(METRIC_COMPLETED, cycle, completed.size());

        for (Request r : completed) {
            log.info("Cycle {} COMPLETED {}", cycle, r);
            inflightRequests.remove(r);
        }
        log.info("Cycle {} END InFlight={} {}", cycle, inflightRequests.size(), rateLimiter);
        log.info("--CUT--");
        cycle++;
    }
}
