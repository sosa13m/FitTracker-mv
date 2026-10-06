package cl.fittracker.app;
import java.io.Serializable;

public final class WorkoutSession implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String id, training, intensity;
    public final int minutes;
    public final boolean warmup, hydration, stretching;
    public final float effort;
    public final long createdAt;

    public WorkoutSession(String id, String training, String intensity, int minutes,
            boolean warmup, boolean hydration, boolean stretching, float effort, long createdAt) {
        this.id=id; this.training=training; this.intensity=intensity; this.minutes=minutes;
        this.warmup=warmup; this.hydration=hydration; this.stretching=stretching;
        this.effort=effort; this.createdAt=createdAt;
    }
}
