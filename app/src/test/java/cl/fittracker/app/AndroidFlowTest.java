package cl.fittracker.app;
import android.content.Context;
import android.os.Bundle;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w320dp-h640dp")
public class AndroidFlowTest {
    @Before public void clearData() {
        RuntimeEnvironment.getApplication().getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).edit().clear().commit();
    }
    private void fill(MainActivity a) {
        ((Spinner)a.findViewById(R.id.spinnerTraining)).setSelection(1);
        ((RadioGroup)a.findViewById(R.id.radioIntensity)).check(R.id.radioMedium);
        ((EditText)a.findViewById(R.id.editMinutes)).setText("30");
        ((RatingBar)a.findViewById(R.id.ratingEffort)).setRating(3);
        ((CheckBox)a.findViewById(R.id.checkWarmup)).setChecked(true);
        ((CheckBox)a.findViewById(R.id.checkHydration)).setChecked(true);
    }
    @Test public void welcomeOpensRegistration() {
        try(ActivityController<WelcomeActivity> c=Robolectric.buildActivity(WelcomeActivity.class).setup()) {
            c.get().findViewById(R.id.btnStart).performClick();
            assertEquals(MainActivity.class.getName(),shadowOf(c.get()).getNextStartedActivity().getComponent().getClassName());
        }
    }
    @Test public void registrationCapturesFieldsPersistsAndUpdatesProgress() {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity a=c.get(); fill(a); a.findViewById(R.id.btnSave).performClick();
            WorkoutSession s=new SessionStore(a).all().get(0);
            assertEquals("Fuerza",s.training); assertEquals("Media",s.intensity); assertEquals(30,s.minutes);
            assertTrue(s.warmup); assertTrue(s.hydration); assertFalse(s.stretching); assertEquals(3,s.effort,0);
            assertEquals(50,((ProgressBar)a.findViewById(R.id.progressDaily)).getProgress());
            assertEquals("",((EditText)a.findViewById(R.id.editMinutes)).getText().toString());
        }
    }
    @Test public void incompleteFormDoesNotRegister() {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            c.get().findViewById(R.id.btnSave).performClick();
            assertTrue(new SessionStore(c.get()).all().isEmpty());
        }
    }
    @Test public void missingIntensityDoesNotRegister() {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            fill(c.get()); ((RadioGroup)c.get().findViewById(R.id.radioIntensity)).clearCheck();
            c.get().findViewById(R.id.btnSave).performClick();
            assertTrue(new SessionStore(c.get()).all().isEmpty());
        }
    }
    @Test public void recreatingActivityRestoresFormAndDailyProgress() {
        ActivityController<MainActivity> original=Robolectric.buildActivity(MainActivity.class).setup();
        fill(original.get()); Bundle state=new Bundle(); original.saveInstanceState(state).pause().stop().destroy();
        try(ActivityController<MainActivity> restored=Robolectric.buildActivity(MainActivity.class).create(state).start().restoreInstanceState(state).resume().visible()) {
            MainActivity a=restored.get();
            assertEquals("30",((EditText)a.findViewById(R.id.editMinutes)).getText().toString());
            assertEquals(1,((Spinner)a.findViewById(R.id.spinnerTraining)).getSelectedItemPosition());
            assertEquals(R.id.radioMedium,((RadioGroup)a.findViewById(R.id.radioIntensity)).getCheckedRadioButtonId());
            assertEquals(3,((RatingBar)a.findViewById(R.id.ratingEffort)).getRating(),0);
            assertTrue(((CheckBox)a.findViewById(R.id.checkWarmup)).isChecked());
        }
    }
    @Test public void historyUsesRecyclerViewAndCustomItem() {
        SessionStore store=new SessionStore(RuntimeEnvironment.getApplication());
        store.save(Collections.singletonList(new WorkoutSession("test","Yoga","Baja",20,true,false,true,2,System.currentTimeMillis())));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            RecyclerView list=c.get().findViewById(R.id.recyclerSessions);
            assertTrue(list.getAdapter() instanceof SessionAdapter); assertEquals(1,list.getAdapter().getItemCount());
            assertEquals(android.view.View.GONE,c.get().findViewById(R.id.txtEmptyHistory).getVisibility());
        }
    }
    @Test public void deletingPersistedSessionRemovesIt() {
        SessionStore store=new SessionStore(RuntimeEnvironment.getApplication());
        store.save(Collections.singletonList(new WorkoutSession("test","Yoga","Baja",20,true,false,true,2,System.currentTimeMillis())));
        store.remove("test"); assertTrue(new SessionStore(RuntimeEnvironment.getApplication()).all().isEmpty());
    }
}
