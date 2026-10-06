package cl.fittracker.app;

import android.content.Context;
import android.view.View;
import android.widget.RatingBar;
import android.widget.ScrollView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,qualifiers="w320dp-h640dp-mdpi")
public class SmallScreenTest {
    @Before public void clearData() {
        RuntimeEnvironment.getApplication().getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE).edit().clear().commit();
    }
    private void checkForm(int width,int height) {
        try(ActivityController<MainActivity> c=Robolectric.buildActivity(MainActivity.class).setup()) {
            View root=c.get().findViewById(R.id.root);
            root.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));
            root.layout(0,0,width,height);
            RatingBar stars=c.get().findViewById(R.id.ratingEffort);
            View parent=(View)stars.getParent();
            assertTrue(stars.getRight()<=parent.getWidth()-parent.getPaddingRight());
            ScrollView scroll=c.get().findViewById(R.id.formScroll);
            scroll.scrollTo(0,scroll.getChildAt(0).getHeight()); assertTrue(scroll.getScrollY()>0);
            View save=c.get().findViewById(R.id.btnSave);
            View history=c.get().findViewById(R.id.btnHistory);
            int[] origin=new int[2],savePosition=new int[2],historyPosition=new int[2];
            scroll.getLocationInWindow(origin);save.getLocationInWindow(savePosition);history.getLocationInWindow(historyPosition);
            assertTrue(savePosition[1]>=origin[1]);
            assertTrue(savePosition[1]+save.getHeight()<=origin[1]+scroll.getHeight());
            assertTrue(historyPosition[1]+history.getHeight()<=origin[1]+scroll.getHeight());
        }
    }
    @Test public void formFitsSmallPortrait() { checkForm(320,640); }
    @Test public void formFitsReducedHeight() { checkForm(320,400); }
    @Test public void formFitsLandscape() { checkForm(640,320); }
}
