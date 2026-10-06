package cl.fittracker.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public final class SessionAdapter extends ListAdapter<WorkoutSession,SessionAdapter.Holder> {
    public interface OnDeleteListener { void onDelete(WorkoutSession session); }
    private final OnDeleteListener listener;
    public SessionAdapter(OnDeleteListener listener) {
        super(new DiffUtil.ItemCallback<WorkoutSession>() {
            @Override public boolean areItemsTheSame(@NonNull WorkoutSession a,@NonNull WorkoutSession b) { return a.id.equals(b.id); }
            @Override public boolean areContentsTheSame(@NonNull WorkoutSession a,@NonNull WorkoutSession b) { return a.id.equals(b.id); }
        });
        this.listener=listener;
        setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session,parent,false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h,int position) {
        WorkoutSession s=getItem(position); Context c=h.itemView.getContext();
        h.type.setText(s.training); h.minutes.setText(c.getString(R.string.session_duration,s.minutes));
        String date=new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date(s.createdAt));
        h.details.setText(c.getString(R.string.session_details,date,s.intensity));
        ArrayList<String> habits=new ArrayList<>();
        if(s.warmup) habits.add(c.getString(R.string.habit_warmup));
        if(s.hydration) habits.add(c.getString(R.string.habit_hydration));
        if(s.stretching) habits.add(c.getString(R.string.habit_stretching));
        h.habits.setText(habits.isEmpty() ? c.getString(R.string.no_habits)
            : c.getString(R.string.habits_prefix,android.text.TextUtils.join(", ",habits)));
        h.effort.setRating(s.effort); h.effort.setContentDescription(c.getString(R.string.session_effort,(int)s.effort));
        h.effortText.setText(c.getString(R.string.session_effort,(int)s.effort));
        h.delete.setContentDescription(c.getString(R.string.delete_session,s.training));
        h.delete.setOnClickListener(v -> listener.onDelete(s));
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final TextView type,minutes,details,habits,effortText;
        final RatingBar effort;
        final ImageButton delete;
        Holder(View view) {
            super(view); type=view.findViewById(R.id.txtSessionType); minutes=view.findViewById(R.id.txtSessionMinutes);
            details=view.findViewById(R.id.txtSessionDetails); habits=view.findViewById(R.id.txtSessionHabits);
            effort=view.findViewById(R.id.ratingSessionEffort); effortText=view.findViewById(R.id.txtSessionEffort);
            delete=view.findViewById(R.id.btnDeleteSession);
        }
    }
}
