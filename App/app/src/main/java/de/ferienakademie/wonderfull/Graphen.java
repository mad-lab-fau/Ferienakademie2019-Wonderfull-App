package de.ferienakademie.wonderfull;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

import de.ferienakademie.wonderfull.service.BleService;


public class Graphen extends AppCompatActivity {

    private LineChart mChart;
    private LineData data;
    private int entryCount;

    private HeightChangeCallback callback = values ->
    {
        for (int i = entryCount; i < values.size(); i++)
        {
            data.addEntry(values.get(entryCount),0);
        }
        entryCount = values.size();
        mChart.notifyDataSetChanged();
        mChart.invalidate();


        //if (mChart.getData() != null &&
        //        mChart.getData().getDataSetCount() > 0) {
        //    set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
        //    set1.setValues(values);
        //    mChart.getData().notifyDataChanged();
        //    mChart.notifyDataSetChanged();
        //} else {
        //
        //}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphen);

        mChart = findViewById(R.id.chart);
        mChart.setTouchEnabled(true);
        mChart.setPinchZoom(true);

        LineDataSet set1;

        ArrayList<Entry> values = new ArrayList<>();
        entryCount = values.size();

        set1 = new LineDataSet(values, "Altitude");
        //set1.setDrawIcons(true);
        //set1.enableDashedLine(50f, 10f, 0f);
        //set1.enableDashedHighlightLine(100f, 5f, 0f);
        set1.setColor(getResources().getColor(R.color.darkGreen));
        set1.setFillColor(getResources().getColor(R.color.backgroundGreen));
        //set1.setCircleColor(Color.DKGRAY);
        set1.setLineWidth(1f);
        set1.setCircleRadius(0f);
        //set1.setDrawCircleHole(false);
        set1.setValueTextSize(0f);
        set1.setDrawFilled(true);
        set1.setFormLineWidth(1f);
        //set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);
        set1.setDrawCircles(false);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        mChart.getLegend().setEnabled(false);

        mChart.getAxisRight().setEnabled(false);
        XAxis xAxis = mChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.setExtraBottomOffset(10);
        mChart.getXAxis().setTextSize(15);
        mChart.getAxisLeft().setTextSize(15);

        Description des = mChart.getDescription();
        des.setEnabled(false);


        //mChart.setDescription();
        //TextView xAxisName = new TextView(this);
            /*TextView xAxisName;
            xAxisName = (TextView) findViewById(R.id.x_axis_title);
            xAxisName.setText("Distanz [km]");
            TextView yAxisName; // = new TextView(this, null);
            yAxisName = (TextView) findViewById(R.id.y_axis_title);
            yAxisName.setText("Yaxis Label");
*/


            /*if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }*/

        data = new LineData();
        data.addDataSet(set1);
        mChart.setData(data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        BleService.registerHeightChangedCallback(callback);
    }

    @Override
    protected void onPause() {
        super.onPause();

        BleService.deregisterHeightChangedCallback(callback);
    }
}