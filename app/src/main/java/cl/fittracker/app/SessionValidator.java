package cl.fittracker.app;
import java.util.Arrays;

public final class SessionValidator {
    private SessionValidator() { }
    public static void validate(String training, String intensity, int minutes, float effort) {
        if (!Arrays.asList("Fuerza", "Cardio", "Yoga", "Calistenia").contains(training))
            throw new IllegalArgumentException("Selecciona un tipo de entrenamiento");
        if (!Arrays.asList("Baja", "Media", "Alta").contains(intensity))
            throw new IllegalArgumentException("Selecciona la intensidad");
        if (minutes < 1 || minutes > 600)
            throw new IllegalArgumentException("La duración debe ser entre 1 y 600 minutos");
        if (!Float.isFinite(effort) || effort < 1 || effort > 5 || effort != Math.floor(effort))
            throw new IllegalArgumentException("Selecciona un esfuerzo de 1 a 5 estrellas");
    }
}
