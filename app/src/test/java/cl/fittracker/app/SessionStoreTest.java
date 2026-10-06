package cl.fittracker.app;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class SessionStoreTest {
    private Context context;
    private SessionStore store;
    private WorkoutSession session(String id) {
        return new WorkoutSession(id,"Fuerza","Media",30,true,false,true,4,123456789L);
    }
    @Before public void clearData() {
        context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).edit().clear().commit();
        store=new SessionStore(context);
    }
    @Test public void restoresEveryFieldInOriginalOrder() {
        store.save(Arrays.asList(session("new"),session("old")));
        WorkoutSession restored=new SessionStore(context).all().get(0);
        assertEquals("new",restored.id); assertEquals("Fuerza",restored.training);
        assertEquals("Media",restored.intensity); assertEquals(30,restored.minutes);
        assertTrue(restored.warmup); assertFalse(restored.hydration); assertTrue(restored.stretching);
        assertEquals(4,restored.effort,0); assertEquals(123456789L,restored.createdAt);
        assertEquals("old",new SessionStore(context).all().get(1).id);
    }
    @Test public void removesOnlyMatchingId() {
        store.save(Arrays.asList(session("first"),session("second")));
        store.remove("second");
        assertEquals(1,store.all().size()); assertEquals("first",store.all().get(0).id);
    }
    @Test public void unknownIdKeepsAllSessions() {
        store.save(Collections.singletonList(session("first"))); store.remove("missing");
        assertEquals(1,store.all().size()); assertEquals("first",store.all().get(0).id);
    }
    @Test public void emptySaveRemainsEmptyOnReopen() {
        store.save(Collections.singletonList(session("first")));
        store.save(Collections.emptyList()); assertTrue(new SessionStore(context).all().isEmpty());
    }
    @Test public void unreadableJsonIsPreservedForRecovery() {
        context.getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).edit().putString("sessions","broken").commit();
        assertTrue(store.all().isEmpty());
        assertEquals("broken",context.getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).getString("unreadable_backup",null));
        assertEquals("broken",context.getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).getString("sessions",null));
    }
}
