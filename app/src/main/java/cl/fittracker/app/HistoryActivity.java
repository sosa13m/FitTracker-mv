package cl.fittracker.app;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HistoryActivity extends BaseActivity {
    private SessionStore store;
    private SessionAdapter adapter;
    private RecyclerView list;
    private TextView empty,count;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); setContentView(R.layout.activity_history); prepareInsets(false);
        store=new SessionStore(this); list=findViewById(R.id.recyclerSessions);
        empty=findViewById(R.id.txtEmptyHistory); count=findViewById(R.id.txtSessionCount);
        adapter=new SessionAdapter();
        list.setLayoutManager(new LinearLayoutManager(this)); list.setAdapter(adapter);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
    @Override protected void onResume() { super.onResume(); if(store!=null) reload(); }
    private void reload() {
        ArrayList<WorkoutSession> sessions=store.all(); adapter.submitList(sessions);
        count.setText(getResources().getQuantityString(R.plurals.session_count,sessions.size(),sessions.size()));
        empty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(sessions.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
