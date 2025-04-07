package de.tadris.fitness;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.PersonalBestRecord;

public class PersonalBestRecordTest {

    private  List<GpsWorkout> workoutList = new ArrayList<>();

    @Before
    public void setUp() {
        // Workouts for Week 1 of 2023
        workoutList.add(new GpsWorkout(1000, 10, 5, LocalDateTime.of(2023, 1, 2, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // Monday
        workoutList.add(new GpsWorkout(1500, 12, 6, LocalDateTime.of(2023, 1, 4, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // Wednesday

        // Workouts for Week 2 of 2023
        workoutList.add(new GpsWorkout(2000, 14, 7, LocalDateTime.of(2023, 1, 9, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // Monday
        workoutList.add(new GpsWorkout(2500, 15, 8, LocalDateTime.of(2023, 1, 11, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // Wednesday

        // Workouts for February 2023
        workoutList.add(new GpsWorkout(3000, 16, 9, LocalDateTime.of(2023, 2, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // February 1
        workoutList.add(new GpsWorkout(3500, 17, 10, LocalDateTime.of(2023, 2, 15, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // February 15



        // Workouts for March 2023
        workoutList.add(new GpsWorkout(4000, 18, 11, LocalDateTime.of(2023, 3, 1, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // March 1
        workoutList.add(new GpsWorkout(4500, 19, 12, LocalDateTime.of(2023, 3, 15, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // March 15

        //duplicate of 2022 workout, set 10 min ahead of the next instance, but placed first to test if the list is sorted.
        workoutList.add(new GpsWorkout(5000, 20, 13, LocalDateTime.of(2022, 12, 30, 10, 10).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // Last day of 2022

        // Workouts for 2022: this should be the earliest, and the highest scores for ease of testing.
        workoutList.add(new GpsWorkout(5000, 20, 13, LocalDateTime.of(2022, 12, 30, 10, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())); // Last day of 2022

    }

    /**
     * Tests that each workout was added and the stats from each workout are being tracked as best in Overall.
     */

    @Test
    public void testAllPersonalBestsWereAdded(){
        ArrayList<PersonalBestRecord> personalBestRecords = PersonalBestRecord.personalBestsFromList(workoutList);
        assertEquals(10, personalBestRecords.size());
    }

    @Test
    public void testOverallPersonalBestsFromList() {

        ArrayList<PersonalBestRecord> personalBestRecords = PersonalBestRecord.personalBestsFromList(workoutList);

        // Check personal bests for Distance
        assertTrue(personalBestRecords.get(0).getWorkoutData().containsKey(PersonalBestRecord.PBType.Distance));
        assertTrue(personalBestRecords.get(0).getWorkoutData().get(PersonalBestRecord.PBType.Distance).contains(PersonalBestRecord.PBPeriod.Overall));

        // Check personal bests for TopSpeed
        assertTrue(personalBestRecords.get(0).getWorkoutData().containsKey(PersonalBestRecord.PBType.TopSpeed));
        assertTrue(personalBestRecords.get(0).getWorkoutData().get(PersonalBestRecord.PBType.TopSpeed).contains(PersonalBestRecord.PBPeriod.Overall));

        // Check personal bests for AverageSpeed
        assertTrue(personalBestRecords.get(0).getWorkoutData().containsKey(PersonalBestRecord.PBType.AverageSpeed));
        assertTrue(personalBestRecords.get(0).getWorkoutData().get(PersonalBestRecord.PBType.AverageSpeed).contains(PersonalBestRecord.PBPeriod.Overall));

    }

    /**
     * Tests that personal bests are recorded for each week, month, and year.
     */
    @Test
    public void testTimeSectionedPersonalBestsFromList() {

        ArrayList<PersonalBestRecord> personalBestRecords = PersonalBestRecord.personalBestsFromList(workoutList);


        assertTrue(personalBestRecords.get(0).getWorkoutData().get(PersonalBestRecord.PBType.Distance).contains(PersonalBestRecord.PBPeriod.Overall));

        assertTrue(personalBestRecords.get(1).getWorkoutData().isEmpty());

        assertTrue(personalBestRecords.get(2).getWorkoutData().isEmpty());

        assertTrue(personalBestRecords.get(3).getWorkoutData().get(PersonalBestRecord.PBType.Distance).size() == 1);
        assertTrue(personalBestRecords.get(3).getWorkoutData().get(PersonalBestRecord.PBType.TopSpeed).size() == 1);
        assertTrue(personalBestRecords.get(3).getWorkoutData().get(PersonalBestRecord.PBType.AverageSpeed).size() == 1);

        assertTrue(personalBestRecords.get(4).getWorkoutData().isEmpty());

        assertTrue(personalBestRecords.get(5).getWorkoutData().get(PersonalBestRecord.PBType.Distance).size() == 2);
        assertTrue(personalBestRecords.get(5).getWorkoutData().get(PersonalBestRecord.PBType.TopSpeed).size() == 2);
        assertTrue(personalBestRecords.get(5).getWorkoutData().get(PersonalBestRecord.PBType.AverageSpeed).size() == 2);
    }


    @Test
    public void testWithNoWorkouts() {
        ArrayList<PersonalBestRecord> personalBestRecords = PersonalBestRecord.personalBestsFromList(new ArrayList<GpsWorkout>());
        assertTrue(personalBestRecords.isEmpty());

    }

    private final String NULL_EXCEPTION_STRING = "Workout List input must not be null. It should be a list of GpsWorkout objects.";
    @Test
    public void testWithNullArray() {
        String exceptionMessage = "";
        try{

            ArrayList<PersonalBestRecord> personalBestRecords = PersonalBestRecord.personalBestsFromList(null);
        } catch (Exception e){
            exceptionMessage = e.getMessage();
        }
        assertEquals(exceptionMessage, NULL_EXCEPTION_STRING);

    }

}
