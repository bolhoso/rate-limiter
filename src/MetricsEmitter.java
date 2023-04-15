import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import java.util.List;

public class MetricsEmitter {
    public record Datapoint(String metricName, long timestamp, long value) {}

    private MultiValuedMap<String, Datapoint> metrics;

    public MetricsEmitter() {
        metrics = new HashSetValuedHashMap<>();
    }

    public void emit(final String metricName, final long value) {
        metrics.put(metricName, new Datapoint(metricName, System.currentTimeMillis(), value));
    }

    public void emit(final String metricName, final long timestamp, final long value) {
        metrics.put(metricName, new Datapoint(metricName, timestamp, value));
    }

    public List<Long> getValues(final String metricName) {
        return metrics.get(metricName)
            .stream()
            .map(Datapoint::value)
            .toList();
    }

    public double getAverage(final String metricName) {
        return metrics.get(metricName)
            .stream()
            .mapToLong(Datapoint::value)
            .average()
            .orElse(0);
    }

    public List<String> getAllMetrics() {
        return metrics.keySet()
            .stream()
            .toList();
    }
}
