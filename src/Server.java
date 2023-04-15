import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Server {

    private final Set<Request> requests;
    private int cycle;

    private final RateLimiter rateLimiter;

    public Server(final RateLimiter r) {
        this.requests = new HashSet<>();
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
            this.requests.add(r);
        }

        return true;
    }

    public void processCycle() {
        // every cycle, we add R tokens to the bucket, per rate R
        // And avoid race condition by blocking on rateLimiter
        synchronized (rateLimiter) {
            rateLimiter.addTokens();
        }

        Set<Request> completed = new HashSet<>();
        for (Request r : requests) {
            log.info("Cycle {} PROCESS {}", cycle, r);
            r.timeToComplete--;
            if (r.timeToComplete == 0) {
                completed.add(r);
            }
        }

        for (Request r : completed) {
            log.info("Cycle {} COMPLETED {}", cycle, r);
            requests.remove(r);
        }
        log.info("Cycle {} END InFlight={} {}", cycle, requests.size(), rateLimiter);
        log.info("--CUT--");
        cycle++;
    }
}
