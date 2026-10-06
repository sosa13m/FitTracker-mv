package cl.fittracker.app;
import org.junit.Test;

public class SessionValidatorTest {
    @Test public void acceptsEachTraining() {
        for(String s:new String[]{"Fuerza","Cardio","Yoga","Calistenia"}) SessionValidator.validate(s,"Media",30,3);
    }
    @Test public void acceptsBoundaries() { SessionValidator.validate("Yoga","Baja",1,1); SessionValidator.validate("Fuerza","Alta",600,5); }
    @Test(expected=IllegalArgumentException.class) public void requiresTraining() { SessionValidator.validate("","Media",30,3); }
    @Test(expected=IllegalArgumentException.class) public void requiresIntensity() { SessionValidator.validate("Cardio","",30,3); }
    @Test(expected=IllegalArgumentException.class) public void rejectsZeroMinutes() { SessionValidator.validate("Cardio","Media",0,3); }
    @Test(expected=IllegalArgumentException.class) public void rejectsExcessiveMinutes() { SessionValidator.validate("Cardio","Media",601,3); }
    @Test(expected=IllegalArgumentException.class) public void requiresEffort() { SessionValidator.validate("Cardio","Media",30,0); }
    @Test(expected=IllegalArgumentException.class) public void rejectsFractionalStars() { SessionValidator.validate("Cardio","Media",30,2.5f); }
    @Test(expected=IllegalArgumentException.class) public void rejectsNonFiniteEffort() { SessionValidator.validate("Cardio","Media",30,Float.NaN); }
}
