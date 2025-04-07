package de.tadris.fitness.data;

import android.app.Person;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.time.Instant;
import java.util.Locale;

import kotlin.NotImplementedError;

//Allows for personal best data to be calculated.
//Done via maintaining a list of key value pairs containing the category (Distance, TopSpeed, AverageSpeed) mapping to the time periods it is for (Week/Month/Year/Overall)
public class PersonalBestRecord
{
    //Defines a personal best record for a given workout
    public PersonalBestRecord(GpsWorkout workout_)
    {
        _workout = workout_;
        _personalBestData = new HashMap<PBType, ArrayList<PBPeriod>>();
    }
    private GpsWorkout _workout;
    public HashMap<PBType, ArrayList<PBPeriod>> _personalBestData;

    public GpsWorkout getWorkout()
    {
        return _workout;
    }

    public HashMap<PBType, ArrayList<PBPeriod>> getWorkoutData()
    {
        return _personalBestData;
    }

    private static final String NULL_EXCEPTION_MESSAGE = "Workout List input must not be null. It should be a list of GpsWorkout objects.";
    //Given a list of workouts, returns a list of personal best records showing what the personal bests are for and for what time period.
    public static ArrayList<PersonalBestRecord> personalBestsFromList(List<GpsWorkout> workoutsList)
    {
        if (workoutsList == null){
            throw new IllegalArgumentException(NULL_EXCEPTION_MESSAGE);
        }
        if (workoutsList.isEmpty())
            return new ArrayList<PersonalBestRecord>();

        //sorts list by date first
        workoutsList.sort(Comparator.comparingLong(a -> a.start));

        //Begins by creating the list that will be returned.
        ArrayList<PersonalBestRecord> personalBestRecords = new ArrayList<>();
        for(GpsWorkout item : workoutsList)
        {
            personalBestRecords.add(new PersonalBestRecord(item));
        }

        //Divides the workouts by year week.
        HashMap<String, ArrayList<PersonalBestRecord>> yearWeekMap = new HashMap<String, ArrayList<PersonalBestRecord>>();

        //Sort each item in the hashmap
        for(PersonalBestRecord item : personalBestRecords)
        {
            //Will be used to identify the year-week.
            String hashMapKey = convertLongToYearWeekKey(item.getWorkout().start);

            //If this is the first time the year week is seen, add the list for it.
            if (!yearWeekMap.containsKey(hashMapKey))
            {
                yearWeekMap.put(hashMapKey, new ArrayList<PersonalBestRecord>());
            }

            //Add the record to its corresponding year-week.
            yearWeekMap.get(hashMapKey).add(item);
        }

        //Goes through every year-week to calculate the bests for that week.
        for(String key : yearWeekMap.keySet())
        {
            //Represents the list of workouts for a given week.
            ArrayList<PersonalBestRecord> listForCurrentWeek = yearWeekMap.get(key);

            calculatePersonalBestsForArrayInPlace(listForCurrentWeek, PBPeriod.Week);
        }

        //Records are mapped based on year-month
        HashMap<String, ArrayList<PersonalBestRecord>> yearMonthMap = new HashMap<String, ArrayList<PersonalBestRecord>>();

        //Sorts into year-month arrays
        for(PersonalBestRecord item : personalBestRecords)
        {
            String hashMapKey = convertLongToYearMonthKey(item.getWorkout().start);

            //If this is the first time the year month is seen, add the list for it.
            if (!yearMonthMap.containsKey(hashMapKey))
            {
                yearMonthMap.put(hashMapKey, new ArrayList<PersonalBestRecord>());
            }

            //Add the record to its corresponding year-month.
            yearMonthMap.get(hashMapKey).add(item);
        }

        //Loops through each of the year-months and finds the personal bests for each category
        for(String key : yearMonthMap.keySet())
        {
            ArrayList<PersonalBestRecord> listForCurrentMonth = yearMonthMap.get(key);

            calculatePersonalBestsForArrayInPlace(listForCurrentMonth, PBPeriod.Month);
        }

        //Records are mapped based on year
        HashMap<String, ArrayList<PersonalBestRecord>> yearMap = new HashMap<String, ArrayList<PersonalBestRecord>>();

        //For each record, store it in the group corresponding to its year.
        for (PersonalBestRecord item : personalBestRecords)
        {
            //Represents the year the workout took place in.
            String hashMapKey = convertLongToYearKey(item.getWorkout().start);

            //If the key (year) does not exist, add it.
            if (!yearMap.containsKey(hashMapKey))
            {
                yearMap.put(hashMapKey, new ArrayList<PersonalBestRecord>());
            }

            //Add the workout to the current year's group.
            yearMap.get(hashMapKey).add(item);
        }

        //For each year, calculate the personal bests.
        for (String key : yearMap.keySet())
        {
            ArrayList<PersonalBestRecord> listForCurrentYear = yearMap.get(key);

            calculatePersonalBestsForArrayInPlace(listForCurrentYear, PBPeriod.Year);
        }

        calculatePersonalBestsForArrayInPlace(personalBestRecords, PBPeriod.Overall);

        return personalBestRecords;
    }

    //Represents whether a personal best is for the week/month/year/overall
    public enum PBPeriod
    {
        Week,
        Month,
        Year,
        Overall
    }

    //Represents whether this personal best is distance/top speed/average speed
    public enum PBType
    {
        Distance,
        TopSpeed,
        AverageSpeed,
    }

    //Takes in an array of records and calculates the personal bests for it, and adds the corresponding time period.
    private static void calculatePersonalBestsForArrayInPlace(ArrayList<PersonalBestRecord> records, PBPeriod timePeriod)
    {
        //Represents the tracked personal bests.
        PersonalBestRecord highestDistanceWorkoutRecord = null;
        PersonalBestRecord highestTopSpeedWorkoutRecord = null;
        PersonalBestRecord highestAverageSpeedWorkoutRecord = null;

        //Check every record in order to find the personal bests for that year-week in each category.
        for (PersonalBestRecord currentRecord : records)
        {
            //If there is no current highest distance workout, set it.
            if(highestDistanceWorkoutRecord == null)
            {
                highestDistanceWorkoutRecord = currentRecord;
            }
            else //If there is a current highest distance workout, compare and set accordingly.
            {
                if (currentRecord.getWorkout().length > highestDistanceWorkoutRecord.getWorkout().length)
                {
                    highestDistanceWorkoutRecord = currentRecord;
                }
            }

            //If there is no current highest top speed workout, set it.
            if (highestTopSpeedWorkoutRecord == null)
            {
                highestTopSpeedWorkoutRecord = currentRecord;
            }
            else //If there is a current highest top speed workout, compare and set accordingly.
            {
                if (currentRecord.getWorkout().topSpeed > highestTopSpeedWorkoutRecord.getWorkout().topSpeed)
                {
                    highestTopSpeedWorkoutRecord = currentRecord;
                }
            }

            //If there is no current highest average speed workout, set it.
            if (highestAverageSpeedWorkoutRecord == null)
            {
                highestAverageSpeedWorkoutRecord = currentRecord;
            }
            else //If there is a current highest top speed workout, compare and set accordingly.
            {
                if (currentRecord.getWorkout().avgSpeed > highestAverageSpeedWorkoutRecord.getWorkout().avgSpeed)
                {
                    highestAverageSpeedWorkoutRecord = currentRecord;
                }
            }
        }
        highestDistanceWorkoutRecord.addPersonalBestKVP(PBType.Distance, timePeriod);
        highestTopSpeedWorkoutRecord.addPersonalBestKVP(PBType.TopSpeed, timePeriod);
        highestAverageSpeedWorkoutRecord.addPersonalBestKVP(PBType.AverageSpeed, timePeriod);
    }

    //Adds a new key value pair of personal bests with the key being the type and the period being the value.
    private void addPersonalBestKVP(PBType type, PBPeriod period)
    {
        if (!_personalBestData.containsKey(type))
        {
            _personalBestData.put(type, new ArrayList<PBPeriod>());
        }

        if (!_personalBestData.get(type).contains(period))
        {
            _personalBestData.get(type).add(period);
        }
    }

    //Mapping: "<year>-<week index>" -> List of workouts that took place that year week.
    private static String convertLongToYearWeekKey(long val)
    {
        LocalDateTime startDate = Instant.ofEpochMilli(val).atZone(ZoneId.systemDefault()).toLocalDateTime();
        String year = String.valueOf(startDate.getYear());
        String weekOfYearIndex = String.valueOf(startDate.get(WeekFields.of(Locale.getDefault()).weekOfYear()));
        return year + "-" + weekOfYearIndex;
    }

    //Mapping: "<year>-<month index>" -> List of workouts that took place that year month.
    private static String convertLongToYearMonthKey(long val)
    {
        LocalDateTime startDate = Instant.ofEpochMilli(val).atZone(ZoneId.systemDefault()).toLocalDateTime();
        String year = String.valueOf(startDate.getYear());
        String monthOfYear = String.valueOf(startDate.getMonthValue());
        return year + "-" + monthOfYear;
    }

    //Mapping "<year>" -> List of workouts that took place that year.
    private static String convertLongToYearKey(long val)
    {
        LocalDateTime startDate = Instant.ofEpochMilli(val).atZone(ZoneId.systemDefault()).toLocalDateTime();
        return String.valueOf(startDate.getYear());
    }
}
