package com.example.stepappv4.ui.ReportDay;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.stepappv4.StepAppOpenHelper;
import com.example.stepappv4.databinding.FragmentDayBinding;
import com.example.stepappv4.R;


public class DayFragment extends Fragment {

    public int todaySteps = 0;
    TextView numStepsTextView;
    AnyChartView anyChartView;

    Date cDate = new Date();
    String current_time = new SimpleDateFormat("yyyy-MM-dd").format(cDate);

    public Map<String, Integer> stepsByDay = null;

    private FragmentDayBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentDayBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Create column chart
        anyChartView = root.findViewById(R.id.dayBarChart);
        anyChartView.setProgressBar(root.findViewById(R.id.loadingBarDay));

        Cartesian cartesian = createColumnChart();
        anyChartView.setBackgroundColor("#00000000");
        anyChartView.setChart(cartesian);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public Cartesian createColumnChart(){

        //***** Read data from SQLiteDatabase *********/
        // Get the map with days and number of steps for this week
        //  from the database and assign it to variable stepsByDay

        String lastWeek = new SimpleDateFormat("yyyy-MM-dd").format(new Date(cDate.getTime() - 7 * 24 * 3600 * 1000));
        stepsByDay = StepAppOpenHelper.loadStepsByDay(getContext(), lastWeek);

        // Creating a new map that contains days of the week from 0 to 6 and
        //  number of steps during each day set to 0
        Map<String, Integer> graph_map = new TreeMap<>();
        Date lastWeekDate = new Date(cDate.getTime() - 7 * 24 * 3600 * 1000);
        for(int i =0; i<7; i++){
            Date day = new Date(lastWeekDate.getTime() + i * 24 * 3600 * 1000);
            String dayString = new SimpleDateFormat("yyyy-MM-dd").format(day);
            graph_map.put(dayString, 0);
        }

        // Replace the number of steps for each day in graph_map
        //  with the number of steps read from the database
        graph_map.putAll(stepsByDay);

        //***** Create column chart using AnyChart library *********/
        // Create and get the cartesian coordinate system for column chart
        Cartesian cartesian = AnyChart.column();

        // Create data entries for x and y axis of the graph
        List<DataEntry> data = new ArrayList<>();

        for (Map.Entry<String,Integer> entry : graph_map.entrySet())
            data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        // Add the data to column chart and get the columns
        Column column = cartesian.column(data);

        //***** Modify the UI of the chart *********/
        // Change the color of column chart and its border
        column.fill("#1EB980");
        column.stroke("#1EB980");


        // Modifying properties of tooltip
        column.tooltip()
                .titleFormat("At Day: {%X}")
                .format("{%Value} Steps")
                .anchor(Anchor.RIGHT_BOTTOM);

        // Modify column chart tooltip properties
        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        // Modifying properties of cartesian
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);

        // Modify the UI of the cartesian
        cartesian.yAxis(0).title("Number of steps");
        cartesian.xAxis(0).title("Day of the week");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }
}