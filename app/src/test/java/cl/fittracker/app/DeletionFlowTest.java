package cl.fittracker.app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w320dp-h640dp")
public class DeletionFlowTest {
    private SessionStore store;
    @Before public void clearData() {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).edit().clear().commit();
        store=new SessionStore(context);
    }
    private WorkoutSession session(String id,int minutes,long time) {
        return new WorkoutSession(id,"Fuerza","Media",minutes,true,false,true,3,time);
    }
    private AlertDialog requestDelete(HistoryActivity a,int position) {
        RecyclerView list=a.findViewById(R.id.recyclerSessions);
        SessionAdapter adapter=(SessionAdapter)list.getAdapter();
        SessionAdapter.Holder holder=adapter.onCreateViewHolder(list,0);
        adapter.onBindViewHolder(holder,position);
        holder.itemView.findViewById(R.id.btnDeleteSession).performClick();
        AlertDialog dialog=(AlertDialog)ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());
        return dialog;
    }
    private void answer(AlertDialog dialog,int button) {
        dialog.getButton(button).performClick(); shadowOf(Looper.getMainLooper()).idle();
    }
    private int progress(MainActivity a) { return ((ProgressBar)a.findViewById(R.id.progressDaily)).getProgress(); }
    @Test public void cancelKeepsStoredSession() {
        store.save(Collections.singletonList(session("keep",30,System.currentTimeMillis())));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            answer(requestDelete(c.get(),0),DialogInterface.BUTTON_NEGATIVE);
            assertEquals("keep",store.all().get(0).id);
            assertEquals(1,((RecyclerView)c.get().findViewById(R.id.recyclerSessions)).getAdapter().getItemCount());
        }
    }
    @Test public void confirmLastSessionShowsEmptyHistory() {
        store.save(Collections.singletonList(session("delete",30,System.currentTimeMillis())));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            answer(requestDelete(c.get(),0),DialogInterface.BUTTON_POSITIVE);
            assertTrue(store.all().isEmpty());
            assertEquals(View.VISIBLE,c.get().findViewById(R.id.txtEmptyHistory).getVisibility());
            assertEquals(View.GONE,c.get().findViewById(R.id.recyclerSessions).getVisibility());
            assertEquals("0 sesiones registradas",((TextView)c.get().findViewById(R.id.txtSessionCount)).getText().toString());
        }
    }
    @Test public void deletesSelectedIdWhenSessionsHaveSameTraining() {
        long now=System.currentTimeMillis();
        store.save(Arrays.asList(session("keep",30,now),session("delete",15,now)));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            answer(requestDelete(c.get(),1),DialogInterface.BUTTON_POSITIVE);
            assertEquals(1,store.all().size()); assertEquals("keep",store.all().get(0).id);
            assertEquals("1 sesión registrada",((TextView)c.get().findViewById(R.id.txtSessionCount)).getText().toString());
        }
    }
    @Test public void returningToMainRecalculatesTodayAndCount() {
        long now=System.currentTimeMillis();
        store.save(Arrays.asList(session("keep",30,now),session("delete",30,now)));
        try(ActivityController<MainActivity> main=Robolectric.buildActivity(MainActivity.class).setup()) {
            assertEquals(100,progress(main.get())); main.pause().stop();
            try(ActivityController<HistoryActivity> history=Robolectric.buildActivity(HistoryActivity.class).setup()) {
                answer(requestDelete(history.get(),1),DialogInterface.BUTTON_POSITIVE);
                history.get().findViewById(R.id.btnBack).performClick();
            }
            main.start().resume(); assertEquals(50,progress(main.get()));
            assertEquals("30 de 60 min",((TextView)main.get().findViewById(R.id.txtProgressMinutes)).getText().toString());
            assertEquals("1 sesión registrada",((TextView)main.get().findViewById(R.id.txtSessionCount)).getText().toString());
        }
    }
    @Test public void deletingYesterdayDoesNotSubtractToday() {
        Calendar yesterday=Calendar.getInstance(); yesterday.add(Calendar.DAY_OF_YEAR,-1);
        store.save(Arrays.asList(session("today",15,System.currentTimeMillis()),session("old",40,yesterday.getTimeInMillis())));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            answer(requestDelete(c.get(),1),DialogInterface.BUTTON_POSITIVE);
        }
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            assertEquals(25,progress(c.get())); assertEquals("today",store.all().get(0).id);
        }
    }
    @Test public void deletedSessionStaysDeletedWhenReopening() {
        store.save(Collections.singletonList(session("gone",30,System.currentTimeMillis())));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            answer(requestDelete(c.get(),0),DialogInterface.BUTTON_POSITIVE);
        }
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            assertEquals(0,((RecyclerView)c.get().findViewById(R.id.recyclerSessions)).getAdapter().getItemCount());
        }
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            assertEquals(0,progress(c.get()));
        }
    }
    @Test public void dismissingConfirmationDoesNotDelete() {
        store.save(Collections.singletonList(session("keep",30,System.currentTimeMillis())));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            requestDelete(c.get(),0).cancel(); shadowOf(Looper.getMainLooper()).idle();
            assertEquals(1,store.all().size());
        }
    }
}
