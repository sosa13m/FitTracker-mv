package cl.fittracker.app;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w320dp-h640dp")
public class HistoryFlowTest {
    private SessionStore store;
    @Before public void clearData() {
        Context context=RuntimeEnvironment.getApplication();
        context.getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).edit().clear().commit();
        store=new SessionStore(context);
    }
    private WorkoutSession session(String id) {
        return new WorkoutSession(id,"Fuerza","Media",30,true,true,true,4,1234567890000L);
    }
    private RecyclerView list(HistoryActivity a) { return a.findViewById(R.id.recyclerSessions); }
    private SessionAdapter adapter(HistoryActivity a) { return (SessionAdapter)list(a).getAdapter(); }
    private void layout(RecyclerView rv) {
        rv.measure(View.MeasureSpec.makeMeasureSpec(320,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY));
        rv.layout(0,0,320,400);
    }
    @Test public void customRowShowsDateHabitsEffortAndDeleteDescription() {
        store.save(Collections.singletonList(session("details")));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            SessionAdapter ad=adapter(c.get());
            SessionAdapter.Holder h=ad.onCreateViewHolder(list(c.get()),0); ad.onBindViewHolder(h,0);
            assertEquals("Fuerza",h.type.getText().toString()); assertEquals("30 min",h.minutes.getText().toString());
            String date=new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(1234567890000L));
            assertEquals(date+" · Intensidad Media",h.details.getText().toString());
            assertEquals("Hábitos: calentamiento, hidratación, estiramiento",h.habits.getText().toString());
            assertEquals(4,h.effort.getRating(),0); assertTrue(h.effort.isIndicator());
            assertEquals("Esfuerzo: 4 de 5",h.effortText.getText().toString());
            assertEquals("Eliminar sesión de Fuerza",h.delete.getContentDescription().toString());
        }
    }
    @Test public void recycledRowReplacesAllHabitsAndEffort() {
        store.save(Arrays.asList(session("habits"),new WorkoutSession("none","Yoga","Baja",15,false,false,false,1,1)));
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            SessionAdapter ad=adapter(c.get()); SessionAdapter.Holder h=ad.onCreateViewHolder(list(c.get()),0);
            ad.onBindViewHolder(h,0); ad.onBindViewHolder(h,1);
            assertEquals("Sin hábitos marcados",h.habits.getText().toString());
            assertEquals("Yoga",h.type.getText().toString()); assertEquals("15 min",h.minutes.getText().toString());
            assertEquals(1,h.effort.getRating(),0);
            assertEquals("Eliminar sesión de Yoga",h.delete.getContentDescription().toString());
        }
    }
    @Test public void mainOpensHistoryAndBackFinishesIt() {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            c.get().findViewById(R.id.btnHistory).performClick();
            assertEquals(HistoryActivity.class.getName(),shadowOf(c.get()).getNextStartedActivity().getComponent().getClassName());
        }
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            c.get().findViewById(R.id.btnBack).performClick(); assertTrue(c.get().isFinishing());
        }
    }
    @Test public void recreatingHistoryRetainsSavedOrder() {
        store.save(Arrays.asList(session("first"),session("second")));
        ActivityController<HistoryActivity> first=Robolectric.buildActivity(HistoryActivity.class).setup();
        Bundle state=new Bundle(); first.saveInstanceState(state).pause().stop().destroy();
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).create(state).start().restoreInstanceState(state).resume().visible()) {
            assertEquals(2,adapter(c.get()).getItemCount());
            assertEquals("first",adapter(c.get()).getCurrentList().get(0).id);
            assertEquals("second",adapter(c.get()).getCurrentList().get(1).id);
        }
    }
    @Test public void thirtySessionsScrollToLastItem() {
        ArrayList<WorkoutSession> sessions=new ArrayList<>();
        for(int i=0;i<30;i++) sessions.add(session("id-"+i));
        store.save(sessions);
        try(ActivityController<HistoryActivity> c=Robolectric.buildActivity(HistoryActivity.class).setup()) {
            RecyclerView rv=list(c.get()); layout(rv);
            assertEquals(30,adapter(c.get()).getItemCount()); assertTrue(rv.canScrollVertically(1));
            rv.scrollToPosition(29); rv.forceLayout(); layout(rv);
            assertEquals(29,((LinearLayoutManager)rv.getLayoutManager()).findLastVisibleItemPosition());
        }
    }
}
