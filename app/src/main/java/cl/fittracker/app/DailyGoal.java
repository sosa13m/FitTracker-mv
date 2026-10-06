package cl.fittracker.app;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public final class DailyGoal {
    public static final int GOAL_MINUTES = 60;
    private DailyGoal() { }
    public static int minutesToday(List<WorkoutSession> sessions, long now, TimeZone zone) {
        Calendar today=Calendar.getInstance(zone); today.setTimeInMillis(now);
        Calendar date=Calendar.getInstance(zone);
        long total=0;
        for (WorkoutSession session : sessions) {
            date.setTimeInMillis(session.createdAt);
            if (date.get(Calendar.YEAR)==today.get(Calendar.YEAR)
                && date.get(Calendar.DAY_OF_YEAR)==today.get(Calendar.DAY_OF_YEAR)) total+=session.minutes;
        }
        return (int)Math.min(total,Integer.MAX_VALUE);
    }
    public static int percent(int minutes) {
        return (int)Math.min(100L, Math.max(0L,(long)minutes*100/GOAL_MINUTES));
    }
}
