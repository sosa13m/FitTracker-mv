package cl.fittracker.app;

import android.os.Bundle;

public class MainActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        prepareInsets(false);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
