package com.bask.studios.depremBilgi.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bask.studios.depremBilgi.R;
import com.bask.studios.depremBilgi.models.Quake;
import com.bask.studios.depremBilgi.settings.SettingsActivity;
import com.bask.studios.depremBilgi.utilities.Utility;


public class QuakeStatisticsActivity extends AppCompatActivity {

    private TextView updateTime;
    private TextView updateFilter;

    private TextView todayCount;
    private TextView weeklyCount;
    private TextView monthlyCount;

    private TextView todayText;
    private TextView weeklyText;
    private TextView monthlyText;
    private AdView mAdView;

    private TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_statistics);


        MobileAds.initialize(this, "ca-app-pub-1285295181757267/3618025003");

        mAdView = findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#324857")));

        updateTime = (TextView) findViewById(R.id.update_time);
        updateFilter = (TextView) findViewById(R.id.update_filter);

        todayCount = (TextView) findViewById(R.id.today_count);
        weeklyCount = (TextView) findViewById(R.id.week_count);
        monthlyCount = (TextView) findViewById(R.id.month_count);

        todayText = (TextView) findViewById(R.id.today_text);
        weeklyText = (TextView) findViewById(R.id.week_text);
        monthlyText = (TextView) findViewById(R.id.month_text);

        noData = (TextView) findViewById(R.id.no_data_text);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mini, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                loadStatistics(this);
                break;
            case R.id.action_settings:
                startActivity(new Intent(QuakeStatisticsActivity.this, SettingsActivity.class));
                break;
            default:
                Toast.makeText(this, "Böyle Bir Eylem Desteklenmiyor!", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AsyncTask to fetch statistics related data from USGS.
     */
    public static class FetchQuakeStatisticsTask extends AsyncTask<Void, Void, Map<String, Integer>> {

        private Context context;
        private ProgressDialog dialog;
        private TextView noData;

        private TextView todayCount;
        private TextView weeklyCount;
        private TextView monthlyCount;

        private TextView todayText;
        private TextView weeklyText;
        private TextView monthlyText;

        public FetchQuakeStatisticsTask(TextView todayCount, TextView todayText, TextView weeklyCount, TextView weeklyText, TextView monthlyCount, TextView monthlyText, TextView noData, Context context) {
            this.context = context;
            this.todayCount = todayCount;
            this.todayText = todayText;
            this.weeklyCount = weeklyCount;
            this.weeklyText = weeklyText;
            this.monthlyCount = monthlyCount;
            this.monthlyText = monthlyText;
            this.noData = noData;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "İstatistik Bilgisi", "Veriler USGS'den toplanılıyor... ", false);
        }

        @Override
        protected Map<String, Integer> doInBackground(Void... voids) {
            return getCounts(context);
        }

        @Override
        protected void onPostExecute(Map<String, Integer> result) {
            super.onPostExecute(result);
            if (dialog != null
                    && dialog.isShowing()) {
                dialog.dismiss();
            }

            if (result != null) {
                noData.setText("");
                todayText.setText("Bugünkü Depremler");
                weeklyText.setText("Bu Hafta");
                monthlyText.setText("Bu Ay");
                todayCount.setText((result.get("today") != null) ? result.get("today").toString() : "0");
                weeklyCount.setText((result.get("thisweek") != null) ? result.get("thisweek").toString() : "0");
                monthlyCount.setText((result.get("thismonth") != null) ? result.get("thismonth").toString() : "0");
            } else {
                noData.setText("İnternet Bağlantınızı Kontrol Ediniz");
                todayText.setText("");
                weeklyText.setText("");
                monthlyText.setText("");
                todayCount.setText("");
                weeklyCount.setText("");
                monthlyCount.setText("");
            }
        }

        /**
         * Get today, weekly and monthly number of quakes with the user filters.
         *
         * @return
         */
        private Map<String, Integer> getCounts(Context context) {
            boolean error = false;
            Map<String, Integer> results = new HashMap<String, Integer>();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String magnitude = prefs.getString(context.getString(R.string.preference_magnitude_key), null);
            String duration = prefs.getString(context.getString(R.string.preference_duration_key), null);
            String distance = prefs.getString(context.getString(R.string.preference_distance_key), null);
            List<Quake> today = Utility.getQuakeData("QuakeStatisticsActivity - today", Utility.urlType.get("today"), magnitude, duration, distance, context);
            List<Quake> weekly = Utility.getQuakeData("QuakeStatisticsActivity - weekly", Utility.urlType.get("thisweek"), magnitude, duration, distance, context);
            List<Quake> monthly = Utility.getQuakeData("QuakeStatisticsActivity - monthly", Utility.urlType.get("thismonth"), magnitude, duration, distance, context);
            if (today != null) {
                results.put("today", today.size());
            } else {
                error = true;
            }
            if (weekly != null) {
                results.put("thisweek", weekly.size());
            } else {
                error = true;
            }
            if (monthly != null) {
                results.put("thismonth", monthly.size());
            } else {
                error = true;
            }
            return ((error) ? null : results);
        }
    }

    /**
     * load statistics
     *
     */
    private void loadStatistics(Context context) {
        todayText.setText("Bugünkü Depremler");
        weeklyText.setText("Bu Hafta");
        monthlyText.setText("Bu Ay");
        noData.setText("");
        new FetchQuakeStatisticsTask(todayCount, todayText, weeklyCount, weeklyText, monthlyCount, monthlyText, noData, context).execute();
        updateFooter();
    }

    /**
     * refresh the footer with last updated time and filters.
     *
     */
    private void updateFooter() {
        Date date = new Date();
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String magnitude = prefs.getString(this.getString(R.string.preference_magnitude_key), null);
        String duration = prefs.getString(this.getString(R.string.preference_duration_key), null);
        updateTime.setText(("Son Güncelleme: " + timeFormatter.format(date)).toString());
    }
}
