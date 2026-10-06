package cl.fittracker.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class SessionStore {
    private final SharedPreferences preferences;
    public SessionStore(Context context) {
        preferences=context.getApplicationContext().getSharedPreferences("fittracker_sessions",Context.MODE_PRIVATE);
    }
    public ArrayList<WorkoutSession> all() {
        ArrayList<WorkoutSession> result=new ArrayList<>();
        String raw=preferences.getString("sessions","[]");
        try {
            JSONArray entries=new JSONArray(raw);
            for(int i=0;i<entries.length();i++) {
                JSONObject item=entries.getJSONObject(i);
                result.add(new WorkoutSession(item.getString("id"),item.getString("training"),item.getString("intensity"),
                    item.getInt("minutes"),item.getBoolean("warmup"),item.getBoolean("hydration"),
                    item.getBoolean("stretching"),(float)item.getDouble("effort"),item.getLong("createdAt")));
            }
        } catch(JSONException exception) {
            Log.e("FitTrackerStore","No se pudo leer el historial",exception);
            preferences.edit().putString("unreadable_backup",raw).apply();
        }
        return result;
    }
    public void save(List<WorkoutSession> sessions) {
        JSONArray entries=new JSONArray();
        try {
            for(WorkoutSession s:sessions) {
                JSONObject item=new JSONObject();
                item.put("id",s.id); item.put("training",s.training); item.put("intensity",s.intensity);
                item.put("minutes",s.minutes); item.put("warmup",s.warmup); item.put("hydration",s.hydration);
                item.put("stretching",s.stretching); item.put("effort",s.effort); item.put("createdAt",s.createdAt);
                entries.put(item);
            }
        } catch(JSONException exception) { throw new IllegalStateException("No se pudo guardar la sesión",exception); }
        preferences.edit().putString("sessions",entries.toString()).apply();
    }
    public void remove(String id) {
        ArrayList<WorkoutSession> sessions=all();
        sessions.removeIf(session -> session.id.equals(id));
        save(sessions);
    }
}
