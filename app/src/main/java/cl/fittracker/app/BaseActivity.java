package cl.fittracker.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {
    protected void prepareInsets(boolean darkBackground) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
            .setAppearanceLightStatusBars(!darkBackground);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
            .setAppearanceLightNavigationBars(!darkBackground);
        View root = findViewById(R.id.root);
        final int left=root.getPaddingLeft(), top=root.getPaddingTop();
        final int right=root.getPaddingRight(), bottom=root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(left+bars.left, top+bars.top, right+bars.right, bottom+bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(root);
    }
    private void event(String name) { Log.d("FitTrackerLifecycle", getClass().getSimpleName()+" "+name); }
    @Override protected void onCreate(Bundle state) { super.onCreate(state); event("onCreate"); }
    @Override protected void onStart() { super.onStart(); event("onStart"); }
    @Override protected void onResume() { super.onResume(); event("onResume"); }
    @Override protected void onPause() { event("onPause"); super.onPause(); }
    @Override protected void onStop() { event("onStop"); super.onStop(); }
    @Override protected void onDestroy() { event("onDestroy"); super.onDestroy(); }
}
