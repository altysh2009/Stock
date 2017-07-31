package com.udacity.stockhawk.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;


// most of the code was taken from synjop class with some changes
public class Detial_Stock extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<HistoricalQuote>> {
    public String symbol = null;
    Calendar from = Calendar.getInstance();
    Calendar to = Calendar.getInstance();
    @BindView(R.id.chart)
    LineChart lineChart;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.stock_name)
    TextView symple;
    IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d("network changed", "onReceive: ");
            if (networkUp()) {
                Log.d("network changed", "onReceive: ");
                getSupportLoaderManager().restartLoader(200, null, Detial_Stock.this);
            }

        }
    };

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detial__stock);

        ButterKnife.bind(this);
        if (symbol == null)
            symbol = getIntent().getExtras().getString(MainActivity.name);


        from.add(Calendar.YEAR, -2);

        getSupportLoaderManager().initLoader(200, null, Detial_Stock.this);

    }

    @Override
    public Loader<List<HistoricalQuote>> onCreateLoader(int id, Bundle args) {
        //Log.d("int ", "onCreateLoader: ");
        progressBar.setVisibility(View.VISIBLE);
        symple.setVisibility(View.INVISIBLE);
        return new AsyncTaskLoader<List<HistoricalQuote>>(getApplicationContext()) {
            @Override
            protected void onStartLoading() {
                forceLoad();
                super.onStartLoading();
            }

            @Override
            public List<HistoricalQuote> loadInBackground() {
                Log.d("int ", "loadInBackground: ");

                Stock stock = null;
                List<HistoricalQuote> history;

                try {
                    Log.d(symbol, "loadInBackground: ");
                    stock = YahooFinance.get(symbol);
                    history = stock.getHistory(from, to, Interval.WEEKLY);
                    Log.d("sucss", "loadInBackground: ");
                    Log.d(history.toString() + "", "loadInBackground: ");
                    return history;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("falid", "loadInBackground: ");
                }

                return null;
            }

            @Override
            public void deliverResult(List<HistoricalQuote> data) {
                super.deliverResult(data);
            }
        };
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onLoadFinished(Loader<List<HistoricalQuote>> loader, List<HistoricalQuote> data) {

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<String>();
        int i = 0;
        if (data != null) {
            if (data.size() == 0 && networkUp()) {
                symple.setText(getString(R.string.no_history));
                progressBar.setVisibility(View.GONE);
                symple.setVisibility(View.VISIBLE);
                return;
            } else if (data.size() == 0 && !networkUp()) {
                symple.setText(getString(R.string.network_error));
                progressBar.setVisibility(View.GONE);
                symple.setVisibility(View.VISIBLE);
                return;
            }
            for (HistoricalQuote it : data) {
                labels.add(getDate(it.getDate().getTimeInMillis(), "dd/MM/yyyy"));
                entries.add(new Entry(it.getClose().floatValue(), i));
                i++;
                //Log.d(labels.get(i), "onLoadFinished: ");
            }

            // line chart code taked fo https://www.numetriclabz.com/android-line-chart-using-mpandroidchart-tutorial/
            LineDataSet dataset = new LineDataSet(entries, "# of Calls");
            progressBar.setVisibility(View.INVISIBLE);
            Log.d(symbol, "onLoadFinished: ");
            symple.setText(symbol);
            symple.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.VISIBLE);


            dataset.setColors(ColorTemplate.COLORFUL_COLORS); //
            dataset.setDrawCubic(true);
            dataset.setDrawFilled(true);
            //dataset.setValueTextSize(23);

            LineData da = new LineData(labels, dataset);
            da.setValueTextSize(16);

            lineChart.setData(da);


            lineChart.setVisibleXRangeMaximum(4);
            lineChart.animateY(5000);
            return;
        }
        if (networkUp()) {
            symple.setText(getString(R.string.no_history));
            progressBar.setVisibility(View.GONE);
            symple.setVisibility(View.VISIBLE);
        } else {
            symple.setText(getString(R.string.network_error));
            progressBar.setVisibility(View.GONE);
            symple.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<List<HistoricalQuote>> loader) {
        loader.reset();
    }

}
