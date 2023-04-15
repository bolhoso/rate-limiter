import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Main {
    private static final int SIM_CYCLES = 10;
    private static final int MAX_RATE_LIMITTER_CAPACITY = 4;
    private static final int REFILL_RATE = 2;

    private static final int MAX_REQUEST_LATENCY = 5;
    private static final int MAX_REQUESTS_PER_CYCLE = 5;

    public void run(int cycles) {
        RateLimiter rateLimiter = new RateLimiter(REFILL_RATE, MAX_RATE_LIMITTER_CAPACITY);
        MetricsEmitter metricsEmitter = new MetricsEmitter();

        Server server = new Server(rateLimiter, 3, metricsEmitter);
        for (int i = 0; i < cycles; i++) {
            server.receiveAll(generateRequests());
            server.processCycle();
        }

        processMetrics(metricsEmitter);
    }

    private List<Request> generateRequests() {
        int nofRequests = (int) (Math.random() * MAX_REQUESTS_PER_CYCLE);

        List<Request> requests = new ArrayList<>();
        for (int i = 0; i < nofRequests; i++) {
            requests.add(new Request((int) (Math.random() * MAX_REQUEST_LATENCY) + 1));
        }

        return requests;
    }

    private void processMetrics(final MetricsEmitter emitter) {
        // summarize all metrics
        for (String metric : emitter.getAllMetrics()) {
            log.info("data({}) = {}", metric, emitter.getValues(metric));
            log.info("avg({}) = {}", metric, emitter.getAverage(metric));
        }
    }

    public static void main(String[] args) {
        new Main().run(SIM_CYCLES);
    }
}