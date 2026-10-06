package cl.fittracker.app;
import org.junit.Test;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.TimeZone;
import static org.junit.Assert.*;

public class DailyGoalTest {
    private static final TimeZone ZONE=TimeZone.getTimeZone("America/Santiago");
    private long at(int day,int hour,int minute) {
        Calendar c=Calendar.getInstance(ZONE); c.clear(); c.set(2026,Calendar.OCTOBER,day,hour,minute); return c.getTimeInMillis();
    }
    private WorkoutSession session(int minutes,long date) {
        return new WorkoutSession("id","Yoga","Baja",minutes,true,false,true,2,date);
    }
    @Test public void emptyDayIsZero() { assertEquals(0,DailyGoal.minutesToday(Collections.emptyList(),at(2,20,0),ZONE)); }
    @Test public void addsSessionsFromSameDay() {
        assertEquals(60,DailyGoal.minutesToday(Arrays.asList(session(25,at(2,8,0)),session(35,at(2,19,0))),at(2,20,0),ZONE));
    }
    @Test public void excludesYesterdayAndTomorrow() {
        assertEquals(15,DailyGoal.minutesToday(Arrays.asList(session(40,at(1,23,59)),session(15,at(2,0,1)),session(20,at(3,0,0))),at(2,12,0),ZONE));
    }
    @Test public void midnightStartsNewDailyGoal() {
        assertEquals(0,DailyGoal.minutesToday(Collections.singletonList(session(60,at(2,23,59))),at(3,0,0),ZONE));
    }
    @Test public void halfGoalIsFiftyPercent() { assertEquals(50,DailyGoal.percent(30)); }
    @Test public void progressNeverExceedsOneHundred() { assertEquals(100,DailyGoal.percent(120)); }
    @Test public void progressHandlesLargeTotals() { assertEquals(100,DailyGoal.percent(Integer.MAX_VALUE)); }
    @Test public void progressNeverNegative() { assertEquals(0,DailyGoal.percent(-1)); }
}
