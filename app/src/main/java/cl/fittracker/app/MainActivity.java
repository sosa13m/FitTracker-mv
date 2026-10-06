package cl.fittracker.app;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.UUID;
import java.util.TimeZone;

public class MainActivity extends BaseActivity {
    private Spinner training;
    private RadioGroup intensity;
    private TextView selection, habitsSummary, durationSummary;
    private CheckBox warmup, hydration, stretching;
    private EditText minutes;
    private RatingBar effort;
    private ProgressBar progress;
    private TextView progressMinutes, progressPercent;
    private TextView effortText, sessionCount;
    private ArrayList<WorkoutSession> sessions=new ArrayList<>();
    private SessionStore store;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        prepareInsets(false);
        store=new SessionStore(this);
        sessions=store.all();
        training=findViewById(R.id.spinnerTraining);
        intensity=findViewById(R.id.radioIntensity);
        selection=findViewById(R.id.txtSelection);
        warmup=findViewById(R.id.checkWarmup);
        hydration=findViewById(R.id.checkHydration);
        stretching=findViewById(R.id.checkStretching);
        minutes=findViewById(R.id.editMinutes);
        habitsSummary=findViewById(R.id.txtHabits);
        durationSummary=findViewById(R.id.txtDuration);
        effort=findViewById(R.id.ratingEffort);
        effortText=findViewById(R.id.txtEffort);
        sessionCount=findViewById(R.id.txtSessionCount);
        progress=findViewById(R.id.progressDaily);
        progressMinutes=findViewById(R.id.txtProgressMinutes);
        progressPercent=findViewById(R.id.txtProgressPercent);

        ArrayAdapter<CharSequence> options=ArrayAdapter.createFromResource(this,
            R.array.training_options,R.layout.spinner_training);
        options.setDropDownViewResource(R.layout.spinner_training_dropdown);
        training.setAdapter(options);
        training.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent,View view,int position,long id) {
                updateSelection();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { updateSelection(); }
        });
        intensity.setOnCheckedChangeListener((group,id) -> updateSelection());
        warmup.setOnCheckedChangeListener((button,checked) -> updateSelection());
        hydration.setOnCheckedChangeListener((button,checked) -> updateSelection());
        stretching.setOnCheckedChangeListener((button,checked) -> updateSelection());
        minutes.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence text,int start,int count,int after) { }
            @Override public void onTextChanged(CharSequence text,int start,int before,int count) { }
            @Override public void afterTextChanged(Editable text) { updateSelection(); }
        });
        effort.setOnRatingBarChangeListener((bar,rating,fromUser) -> updateEffortLabel());
        findViewById(R.id.btnSave).setOnClickListener(v -> registerSession());
        findViewById(R.id.btnHistory).setOnClickListener(v ->
            startActivity(new Intent(this,HistoryActivity.class)));
        updateSelection();
        updateEffortLabel();
        updateSessionCount();
        updateProgress();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void updateSelection() {
        RadioButton choice=findViewById(intensity.getCheckedRadioButtonId());
        selection.setText(training.getSelectedItemPosition()>0 && choice!=null
            ? getString(R.string.selection_summary,training.getSelectedItem(),choice.getText())
            : getString(R.string.selection_pending));
        ArrayList<String> habits=new ArrayList<>();
        if(warmup.isChecked()) habits.add(getString(R.string.habit_warmup));
        if(hydration.isChecked()) habits.add(getString(R.string.habit_hydration));
        if(stretching.isChecked()) habits.add(getString(R.string.habit_stretching));
        habitsSummary.setText(habits.isEmpty() ? getString(R.string.no_habits)
            : getString(R.string.habits_prefix,TextUtils.join(", ",habits)));
        String duration=minutes.getText().toString().trim();
        durationSummary.setText(duration.isEmpty() ? getString(R.string.duration_pending)
            : getString(R.string.duration_preview,duration));
    }

    private void updateEffortLabel() {
        effortText.setText(effort.getRating()==0 ? getString(R.string.effort_unset)
            : getString(R.string.effort_value,(int)effort.getRating()));
    }

    private void registerSession() {
        String type=training.getSelectedItemPosition()>0 ? training.getSelectedItem().toString() : "";
        RadioButton choice=findViewById(intensity.getCheckedRadioButtonId());
        String level=choice==null ? "" : choice.getText().toString();
        int duration;
        try { duration=Integer.parseInt(minutes.getText().toString().trim()); }
        catch(NumberFormatException exception) {
            minutes.setError(getString(R.string.duration_help));
            minutes.requestFocus();
            return;
        }
        if(duration<1 || duration>600) {
            minutes.setError(getString(R.string.duration_help));
            minutes.requestFocus();
            return;
        }
        try { SessionValidator.validate(type,level,duration,effort.getRating()); }
        catch(IllegalArgumentException exception) {
            Toast.makeText(this,exception.getMessage(),Toast.LENGTH_LONG).show();
            return;
        }
        sessions.add(0,new WorkoutSession(UUID.randomUUID().toString(),type,level,duration,
            warmup.isChecked(),hydration.isChecked(),stretching.isChecked(),effort.getRating(),System.currentTimeMillis()));
        store.save(sessions);
        updateSessionCount();
        updateProgress();
        minutes.setText(""); minutes.setError(null); effort.setRating(0); intensity.clearCheck();
        training.setSelection(0); warmup.setChecked(false); hydration.setChecked(false); stretching.setChecked(false);
        InputMethodManager keyboard=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if(keyboard!=null) keyboard.hideSoftInputFromWindow(minutes.getWindowToken(),0);
        minutes.clearFocus();
        updateSelection();
        Toast.makeText(this,R.string.saved,Toast.LENGTH_SHORT).show();
    }

    private void updateProgress() {
        int total=DailyGoal.minutesToday(sessions,System.currentTimeMillis(),TimeZone.getDefault());
        int percentage=DailyGoal.percent(total);
        progress.setProgress(percentage);
        progress.setContentDescription(getString(R.string.progress_percent,percentage));
        progressMinutes.setText(getString(R.string.progress_minutes,total,DailyGoal.GOAL_MINUTES));
        progressPercent.setText(getString(R.string.progress_percent,percentage));
    }

    @Override protected void onResume() {
        super.onResume();
        if(store!=null) {
            sessions=store.all();
            updateSessionCount();
            updateProgress();
        }
    }

    private void updateSessionCount() {
        sessionCount.setText(getResources().getQuantityString(R.plurals.session_count,sessions.size(),sessions.size()));
    }

    @Override protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        updateSelection();
        updateEffortLabel();
    }
}
