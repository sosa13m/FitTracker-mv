package cl.fittracker.app;
import android.content.Intent;
import android.os.Bundle;

public class WelcomeActivity extends BaseActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_welcome);
        prepareInsets(true);
        findViewById(R.id.btnStart).setOnClickListener(v ->
            startActivity(new Intent(this, MainActivity.class)));
    }
}
