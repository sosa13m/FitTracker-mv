package cl.fittracker.app;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends BaseActivity {
    private Spinner training;
    private RadioGroup intensity;
    private TextView selection;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        prepareInsets(false);
        training=findViewById(R.id.spinnerTraining);
        intensity=findViewById(R.id.radioIntensity);
        selection=findViewById(R.id.txtSelection);

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
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void updateSelection() {
        RadioButton choice=findViewById(intensity.getCheckedRadioButtonId());
        selection.setText(training.getSelectedItemPosition()>0 && choice!=null
            ? getString(R.string.selection_summary,training.getSelectedItem(),choice.getText())
            : getString(R.string.selection_pending));
    }

    @Override protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        updateSelection();
    }
}
