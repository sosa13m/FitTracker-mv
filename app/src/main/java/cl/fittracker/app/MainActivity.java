package cl.fittracker.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private Spinner training;
    private RadioGroup intensity;
    private TextView selection, habitsSummary, durationSummary;
    private CheckBox warmup, hydration, stretching;
    private EditText minutes;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        prepareInsets(false);
        training=findViewById(R.id.spinnerTraining);
        intensity=findViewById(R.id.radioIntensity);
        selection=findViewById(R.id.txtSelection);
        warmup=findViewById(R.id.checkWarmup);
        hydration=findViewById(R.id.checkHydration);
        stretching=findViewById(R.id.checkStretching);
        minutes=findViewById(R.id.editMinutes);
        habitsSummary=findViewById(R.id.txtHabits);
        durationSummary=findViewById(R.id.txtDuration);

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
        updateSelection();
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

    @Override protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        updateSelection();
    }
}
