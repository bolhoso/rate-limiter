import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class Main {
    private static final int SIM_CYCLES = 4;
    private static final int MAX_RATE_LIMITTER_CAPACITY = 3;
    private static final int REFILLL_RATE = 1;

    private static final int MAX_REQUEST_LATENCY = 5;
    private static final int MAX_REQUESTS_PER_CYCLE = 10;


    public void run(int cycles) {
        RateLimiter rateLimiter = new RateLimiter(REFILLL_RATE, MAX_RATE_LIMITTER_CAPACITY);
        Server server = new Server(rateLimiter);

        for (int i = 0; i < cycles; i++) {
            // Add all requests to the server
            for (Request r : generateRequests()) {
                server.receive(r);
            }

            server.processCycle();
        }
    }

    private Set<Request> generateRequests() {
        int nofRequests = (int) (Math.random() * MAX_REQUESTS_PER_CYCLE);

        HashSet<Request> requests = new HashSet<>();
        for (int i = 0; i < nofRequests; i++) {
            Request r = new Request((int) (Math.random() * MAX_REQUEST_LATENCY) + 1);
            requests.add(r);
        }

        return requests;
    }

    public static void main(String[] args) {
        new Main().run(SIM_CYCLES);
    }
}