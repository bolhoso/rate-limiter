import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@EqualsAndHashCode
public class Request {

    @EqualsAndHashCode.Exclude
    int timeToComplete;

    @EqualsAndHashCode.Exclude
    @Getter @Setter
    private String token;

    final String uuid;

    public Request(int timeToComplete) {
        this.uuid = UUID.randomUUID().toString().substring(0, 5);
        this.timeToComplete = timeToComplete;
    }

    @Override
    public String toString() {
        return String.format("Request[%s, %d]", uuid, timeToComplete);
    }
}