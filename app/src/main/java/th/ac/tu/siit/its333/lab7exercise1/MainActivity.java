package th.ac.tu.siit.its333.lab7exercise1;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {
    int preClick = 0;
    long time = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        WeatherTask w = new WeatherTask();
        w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
    }

    public void buttonClicked(View v) {
       long currentTime = System.currentTimeMillis();
       long currentTimeMins = TimeUnit.MILLISECONDS.toMinutes(currentTime);
       int id = v.getId();
       WeatherTask w = new WeatherTask();


               switch (id) {
                   case R.id.btBangkok:
                       if(preClick != id || currentTimeMins - time >= 1) {
                           w.execute("http://ict.siit.tu.ac.th/~cholwich/bangkok.json", "Bangkok Weather");
                           time = currentTimeMins;
                       }
                       break;
                   case R.id.btNon:
                       if(preClick != id || currentTimeMins - time >= 1) {
                           w.execute("http://ict.siit.tu.ac.th/~cholwich/nonthaburi.json", "Nonthaburi Weather");
                           time = currentTimeMins;
                       }
                       break;
                   case R.id.btPathum:
                       if(preClick != id || currentTimeMins - time >= 1) {
                           w.execute("http://ict.siit.tu.ac.th/~cholwich/pathumthani.json", "Pathumthani Weather");
                           time = currentTimeMins;
                       }
                       break;



       }
       preClick = id;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class WeatherTask extends AsyncTask<String, Void, Boolean> {
        String errorMsg = "";
        ProgressDialog pDialog;
        String title;


        String weather;
        double temp,temp_min,temp_max;
        double Rtemp,Rtemp_min,Rtemp_max;
        double humid;
        double windSpeed;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading weather data ...");
            pDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line;
            try {
                title = params[1];
                URL u = new URL(params[0]);
                HttpURLConnection h = (HttpURLConnection)u.openConnection();
                h.setRequestMethod("GET");
                h.setDoInput(true);
                h.connect();

                int response = h.getResponseCode();
                if (response == 200) {
                    reader = new BufferedReader(new InputStreamReader(h.getInputStream()));
                    while((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //Start parsing JSON
                    JSONObject jWeather = new JSONObject(buffer.toString());
                    JSONObject jMain = jWeather.getJSONObject("main");
                    JSONObject jWind = jWeather.getJSONObject("wind");
                    JSONArray jW = jWeather.getJSONArray("weather");
                    JSONObject jWo = jW.getJSONObject(0);
                    weather = jWo.getString("main");
                    temp = jMain.getDouble("temp");
                    temp_min = jMain.getDouble("temp_min");
                    temp_max = jMain.getDouble("temp_max");
                    humid = jMain.getDouble("humidity");
                    windSpeed = jWind.getDouble("speed");
                    Rtemp = temp-273.15;
                    Rtemp_min = temp_min-273.15;
                    Rtemp_max = temp_max-273.15;

                    errorMsg = "";
                    return true;
                }
                else {
                    errorMsg = "HTTP Error";
                }
            } catch (MalformedURLException e) {
                Log.e("WeatherTask", "URL Error");
                errorMsg = "URL Error";
            } catch (IOException e) {
                Log.e("WeatherTask", "I/O Error");
                errorMsg = "I/O Error";
            } catch (JSONException e) {
                Log.e("WeatherTask", "JSON Error");
                errorMsg = "JSON Error";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            TextView tvTitle,tvWeather,tvTemp,tvHumid, tvWind;
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            tvTitle = (TextView)findViewById(R.id.tvTitle);
            tvWeather = (TextView)findViewById(R.id.tvWeather);
            tvTemp = (TextView)findViewById(R.id.tvTemp);
            tvHumid = (TextView)findViewById(R.id.tvHumid);
            tvWind = (TextView)findViewById(R.id.tvWind);

            if (result) {
                tvTitle.setText(title);
                tvWeather.setText(weather);
                tvTemp.setText(String.format("%.1f (min= %.1f,max=%.1f)",Rtemp,Rtemp_min,Rtemp_max));
                tvHumid.setText(String.format("%.1f",humid));
                tvWind.setText(String.format("%.1f", windSpeed));
            }
            else {
                tvTitle.setText(errorMsg);
                tvWeather.setText("");
                tvWind.setText("");
            }
        }
    }
}
