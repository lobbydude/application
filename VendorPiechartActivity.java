package com.drnds.titlelogy.activity.vendor;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.drnds.titlelogy.R;
import com.drnds.titlelogy.fragments.client.MyValueFormatter;
import com.drnds.titlelogy.util.AppController;
import com.drnds.titlelogy.util.Config;
import com.drnds.titlelogy.util.Logger;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VendorPiechartActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private PieChart mChart;
    SharedPreferences sp;
    //    private PieChart chartContainer;
    private FrameLayout chartContainer;
    //we are going to display the pie chart for smartphone market shares
    private Toolbar toolbar;

    ArrayList<String> xVals = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_piechart);
        toolbar = (Toolbar) findViewById(R.id.vendor_toolbar_piechart);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Pie Chart");

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if (toolbar != null) {

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mainLayout = (RelativeLayout) findViewById(R.id.vendor_mainLayout);
        chartContainer =(FrameLayout) findViewById(R.id.vendor_chartContainer);
        sp =getApplicationContext().getSharedPreferences(
                "VendorLoginActivity", 0);
        mChart = new PieChart(this);

        chartContainer.addView(mChart);
        //mainLayout.setBackgroundColor(Color.LTGRAY);
        //  mainLayout.setBackgroundColor(Color.parseColor("#55656C"));
        mainLayout.setBackgroundColor(Color.WHITE);
        xVals = new ArrayList<String>();


        mChart.setDrawHoleEnabled(true);
        // mChart.setHoleColorTransparent(true);
        mChart.setHoleRadius(25);
        mChart.setCenterText("Orders");
        mChart.setCenterTextColor(Color.WHITE);
        mChart.setHoleColor(Color.DKGRAY);
        mChart.setTransparentCircleRadius(5);
        mChart.setBackgroundColor(Color.WHITE);

        // enable rotation of the chart by touch
        mChart.setRotation(0);
        mChart.setRotationEnabled(true);

        //set a chart value selected listener
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                //display message when value selected
                if (e == null)
                    return;

                Toast.makeText(VendorPiechartActivity.this, xVals.toArray()[e.getXIndex()]+" = "+e.getVal()+"", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });
        // add Data
        addData();
        //addDataa();


        //customize legends
        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setTextColor(Color.BLACK);
        l.setYEntrySpace(40f);
        l.setFormSize(10f);
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setEnabled(true);

    }

    private void addData(){
        StringRequest stringRequest=new StringRequest(Request.Method.GET, Config.VENDORDASHBOARD_URL+getVendorId(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response)
            {
                Logger.getInstance().Log("in response");


                try {
                    JSONObject jObj = new JSONObject(response);
                    {

                        JSONArray jsonArray=jObj.getJSONArray("ScoreBoard");

                        ArrayList<Entry> yvalues = new ArrayList<Entry>();
                        ArrayList<Entry> zvalues = new ArrayList<Entry>();
                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            int Value = jsonObject.getInt("Value");
                            String No_Of_Orders = jsonObject.getString("No_Of_Orders");
                            yvalues.add(new Entry(Value, i));

                            xVals.add(No_Of_Orders);


                            Logger.getInstance().Log("in yvalues"+yvalues);
                            Logger.getInstance().Log("in xvalues"+xVals);
                        }



                        PieDataSet dataSet = new PieDataSet(yvalues, "Orders");
                        dataSet.setSelectionShift(5);
//        dataSet.setSliceSpace(2);
                        dataSet.setDrawValues(false);



                        // add many colors
                        ArrayList<Integer> colors = new ArrayList<Integer>();

                        for (int c: ColorTemplate.VORDIPLOM_COLORS)
                            colors.add(c);

                        for (int c: ColorTemplate.JOYFUL_COLORS)
                            colors.add(c);

                        for (int c: ColorTemplate.COLORFUL_COLORS)
                            colors.add(c);
                        for (int c: ColorTemplate.LIBERTY_COLORS)
                            colors.add(c);
                        for (int c: ColorTemplate.PASTEL_COLORS)
                            colors.add(c);

                        colors.add(ColorTemplate.getHoloBlue());
                        dataSet.setColors(colors);

                        //instantiate pie data object now
                        PieData data = new PieData(xVals, dataSet);
                        data.setValueFormatter(new MyValueFormatter());
                        data.setValueTextSize(11f);
                        data.setValueTextColor(Color.BLACK);
//        data.setValueFormatter(new PercentFormatter());
                        dataSet.setDrawValues(true);
                        mChart.setData(data);
                        mChart.setDrawSliceText(false);
                        //undo all higlights
                        mChart.highlightValues(null);

                        // update pie chart
                        mChart.invalidate();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },                          new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }
        });
        AppController.getInstance().addToRequestQueue(stringRequest);

    }
    public String getVendorId() {
//         num3=sp.getString("Client_Id","");
//        Log.e("Client_Id of num3", num3);
        return sp.getString("Vendor_Id", "");


    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//    private void addDataa(){
//
//
//        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
//
//
//        for (int i=0; i< yData.length; i++) {
//            yVals1.add(new Entry(yData[i], i));
//        }
//        ArrayList<String> xVals = new ArrayList<String>();
//        for (int i=0; i< xData.length; i++) {
//            xVals.add(xData[i]);
//        }
//        //create pie data set
//        PieDataSet dataSet = new PieDataSet(yVals1, "orders");
//        dataSet.setSelectionShift(5);
////        dataSet.setSliceSpace(2);
//        dataSet.setDrawValues(false);
//
//
//
//        // add many colors
//        ArrayList<Integer> colors = new ArrayList<Integer>();
//
//        for (int c: ColorTemplate.VORDIPLOM_COLORS)
//            colors.add(c);
//
//        for (int c: ColorTemplate.JOYFUL_COLORS)
//            colors.add(c);
//
//        for (int c: ColorTemplate.COLORFUL_COLORS)
//            colors.add(c);
//        for (int c: ColorTemplate.LIBERTY_COLORS)
//            colors.add(c);
//        for (int c: ColorTemplate.PASTEL_COLORS)
//            colors.add(c);
//
//        colors.add(ColorTemplate.getHoloBlue());
//        dataSet.setColors(colors);
//
//        //instantiate pie data object now
//        PieData data = new PieData(xVals, dataSet);
//        data.setValueFormatter(new PercentFormatter());
//        data.setValueTextSize(11f);
//        data.setValueTextColor(Color.BLACK);
////        data.setValueFormatter(new PercentFormatter());
//
//        mChart.setData(data);
//        mChart.setDrawSliceText(false);
//        //undo all higlights
//        mChart.highlightValues(null);
//
//        // update pie chart
//        mChart.invalidate();
//    }
}
