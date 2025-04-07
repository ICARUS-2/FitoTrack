/*
 * Copyright (c) 2022 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.ui.statistics.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;

import java.util.ArrayList;
import java.util.List;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.GpsWorkout;
import de.tadris.fitness.data.StatsDataTypes;
import de.tadris.fitness.data.StatsProvider;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.statistics.TimeSpanSelection;
import de.tadris.fitness.util.charts.ChartStyles;
import de.tadris.fitness.util.exceptions.NoDataException;
import de.tadris.fitness.data.PersonalBestRecord;
import de.tadris.fitness.data.TimeSpanClassifier;

public class StatsOverviewFragment extends StatsFragment {
    StatsProvider statsProvider;
    FitoTrackActivity activity;

    TimeSpanSelection timeSpanSelection;
    HorizontalBarChart distanceChart;
    HorizontalBarChart numberOfActivitiesChart;
    HorizontalBarChart durationChart;
    TextView pbLabel;
    public StatsOverviewFragment(FitoTrackActivity ctx) {
        super(R.layout.fragment_stats_overview, ctx);
        statsProvider = new StatsProvider(ctx);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (FitoTrackActivity)getContext();

        timeSpanSelection = view.findViewById(R.id.time_span_selection);
        timeSpanSelection.setForegroundColor(getResources().getColor(R.color.textDarkerWhite));

        numberOfActivitiesChart = view.findViewById(R.id.stats_number_of_workout_chart);
        distanceChart = view.findViewById(R.id.stats_distances_chart);
        timeSpanSelection.addOnTimeSpanSelectionListener((aggregationSpan, instance) -> updateCharts());

        ChartStyles.defaultBarChart(numberOfActivitiesChart);
        animateChart(numberOfActivitiesChart);

        ChartStyles.setXAxisLabel(distanceChart, Instance.getInstance(context).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit(), activity);
        ChartStyles.defaultBarChart(distanceChart);
        animateChart(distanceChart);

        durationChart = view.findViewById(R.id.stats_duration_chart);
        ChartStyles.setXAxisLabel(durationChart, getContext().getString(R.string.timeHourShort), activity);
        ChartStyles.defaultBarChart(durationChart);
        animateChart(durationChart);

        updateCharts();

        // PB update on tab load
        timeSpanSelection.addOnTimeSpanSelectionListener((aggregationSpan, instance) ->  updatePbs());
        pbLabel = view.findViewById(R.id.stats_pb_label);
        updatePbs();
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.stats_overview_title);
    }

    private void animateChart (HorizontalBarChart chart) {
        chart.animateY(500, Easing.EaseInExpo);
    }

    private void updateCharts() {
        long start = timeSpanSelection.getSelectedDate();
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start, timeSpanSelection.getSelectedAggregationSpan().getAggregationEnd(start));

        try {
            BarData distanceData = new BarData(statsProvider.totalDistances(span));
            ChartStyles.horizontalBarChartIconLabel(distanceChart, distanceData, context);
            ChartStyles.setXAxisLabel(distanceChart, Instance.getInstance(getContext()).distanceUnitUtils.getDistanceUnitSystem().getLongDistanceUnit(), activity);
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(distanceChart, (FitoTrackActivity)getContext());
        }
        distanceChart.invalidate();

        try {
            BarData durationData = new BarData(statsProvider.totalDurations(span));
            ChartStyles.horizontalBarChartIconLabel(durationChart, durationData, context);
            ChartStyles.setXAxisLabel(durationChart, getContext().getString(R.string.timeHourShort), activity);
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(durationChart, (FitoTrackActivity)getContext());
        }
        durationChart.invalidate();

        try {
            BarData numberOfActivitiesData = new BarData(statsProvider.numberOfActivities(span));
            ChartStyles.horizontalBarChartIconLabel(numberOfActivitiesChart,numberOfActivitiesData, context);
        } catch (NoDataException e) {
            ChartStyles.barChartNoData(numberOfActivitiesChart, (FitoTrackActivity)getContext());
        }
        numberOfActivitiesChart.invalidate();
    }

    // for the moment only calls distance
    private void updatePbs(){
        updateDistancePb();
    }


    private void updateDistancePb(){
        long start = timeSpanSelection.getSelectedDate();
        StatsDataTypes.TimeSpan span = new StatsDataTypes.TimeSpan(start, timeSpanSelection.getSelectedAggregationSpan().getAggregationEnd(start));
        long span_start = span.startTime;
        long span_end = span.endTime;

        // classifies the filter as Day, Week, Month, Year or Overall
        String pbForPeriod = TimeSpanClassifier.classifyTimeSpan(span_start, span_end);

        //gets the actual kilometers pb
        double pbNum = getPbForPeriod(span_start,span_end, pbForPeriod, "Distance");

        if (pbNum > 0){
            pbLabel.setText("Personal Best: "+ String.format("%.2f", pbNum) + " km");
        }
        else {
            pbLabel.setText("Personal Best: ");
        }

    }

    private double getPbForPeriod(long span_start, long span_end, String pbPeriod, String pbType) {

        List<GpsWorkout> workouts;
        // get all gps workouts
        workouts = Instance.getInstance(this.getContext()).db.getGpsWorkouts();
        if(workouts.isEmpty()){return -1;}

        List<GpsWorkout> inRangeWorkouts = new ArrayList<GpsWorkout>();

        // extracts workouts that are in the user-selected period range
        for (GpsWorkout workout : workouts){
            if (workout.start >= span_start && workout.end <= span_end){
                inRangeWorkouts.add(workout);
            }
        }
        if (inRangeWorkouts.isEmpty()){
            return -1;
        }

        ArrayList<PersonalBestRecord> records = PersonalBestRecord.personalBestsFromList(inRangeWorkouts);

        if (records.isEmpty()){
            return -1;
        }

        PersonalBestRecord.PBType enumPbType = null;
        PersonalBestRecord.PBPeriod enumPbPeriod = null;
        switch (pbType){
            case "Distance":
                enumPbType = PersonalBestRecord.PBType.Distance;
                break;
            case "TopSpeed":
                enumPbType = PersonalBestRecord.PBType.TopSpeed;
                break;
            case "AverageSpeed":
                enumPbType = PersonalBestRecord.PBType.AverageSpeed;
                break;
        }

        switch (pbPeriod) {
            // day not part of requirements for the moment
            case "Day": return -1;
            case "Week":
                enumPbPeriod = PersonalBestRecord.PBPeriod.Week;
                break;
            case "Month":
                enumPbPeriod = PersonalBestRecord.PBPeriod.Month;
                break;
            case "Year":
                enumPbPeriod = PersonalBestRecord.PBPeriod.Year;
                break;
            case "Overall": enumPbPeriod = PersonalBestRecord.PBPeriod.Overall;
        }

        if (enumPbType == null || enumPbPeriod == null){
            return -1;
        }

        //finds the pb
        for(PersonalBestRecord record : records){
            try{
                if(record.getWorkoutData().get(enumPbType).contains(enumPbPeriod)){

                    return record.getWorkout().length / 1000.0;
                }
            }
            catch(NullPointerException ignored){
            }
        }
        return -1;
    }

}
