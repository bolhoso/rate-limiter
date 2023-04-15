import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@Slf4j
public class Server {

    private Queue<Request> requestQueue;
    private final Set<Request> inflightRequests;

    private int cycle;
    private final RateLimiter rateLimiter;
    private final int maxRequests;

    public Server(final RateLimiter r, final int maxRequests) {
        this.inflightRequests = new HashSet<>();
        this.requestQueue = new LinkedList<>();

        this.maxRequests = maxRequests;

        this.rateLimiter = r;
        this.cycle = 0;
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

        for (Request r : completed) {
            log.info("Cycle {} COMPLETED {}", cycle, r);
            inflightRequests.remove(r);
        }
        log.info("Cycle {} END InFlight={} {}", cycle, inflightRequests.size(), rateLimiter);
        log.info("--CUT--");
        cycle++;
    }
}
