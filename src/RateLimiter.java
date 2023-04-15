import java.util.UUID;

public class RateLimiter {

    private final int refillRate;
    private final int maxCapacity;
    private int tokensLeft;

    public RateLimiter(final int refillRate, final int maxCapacity) {
        this.refillRate = refillRate;

        // it starts full of tokens
        this.tokensLeft = maxCapacity;
        this.maxCapacity = maxCapacity;
    }

    public synchronized boolean hasCapacity() {
        return tokensLeft > 0;
    }

    public synchronized String getToken() {
        if (tokensLeft > 0) {
            tokensLeft--;
            return UUID.randomUUID()
                .toString()
                .substring(0, 8);
        }

        return null;
    }

    public synchronized void addTokens() {
        tokensLeft = Math.min(maxCapacity, tokensLeft + refillRate);
    }

    @Override
    public String toString() {
        return String.format("RateLimiter[%d/%d]", tokensLeft, maxCapacity);
    }
}