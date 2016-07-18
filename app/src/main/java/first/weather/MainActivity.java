package first.weather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.stream.JsonReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    TextView refreshView,citynmView,temperature_currView,
            weatherView,temperatureView,winpView,windView,
            aqi_remark;
    //未来天气信息文本框
    ImageView[] weather_icons = new ImageView[6];
    TextView[] weeks = new TextView[6];
    TextView[] temperatures = new TextView[6];

    ImageView weather_icon;
    String weather_icon_url;
    String citynm = "";

    String todayURL;    //今天天气URL
    String futureURL;   //未来天气URL
    String airURL;      //空气质量URL

    Bitmap bitmap;
    Button sousuoButton;
    MyHandler myHandler;

    class MyHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0x123) {
                citynmView.setText(msg.getData().getString("citynm"));
                weather_icon_url = msg.getData().getString("weather_icon");
                temperature_currView.setText(msg.getData().getString("temperature_curr"));
                weatherView.setText(msg.getData().getString("weather"));
                temperatureView.setText(msg.getData().getString("temperature"));
                winpView.setText(msg.getData().getString("winp"));
                windView.setText(msg.getData().getString("wind"));

            }

            if(msg.what == 0x125) {
                aqi_remark.setText(msg.getData().getString("aqi_remark"));
            }

            if(msg.what == 0x126) {
                String[] w = msg.getData().getStringArray("week");//星期字符串数组
                String[] t = msg.getData().getStringArray("temperature");//温度字符串数组
                for(int i = 0;i < 6;i++) {
                    weeks[i].setText(w[i]);
                    temperatures[i].setText(t[i]);
                }

            }


        }
    }






    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case RESULT_OK:
                this.citynm = data.getStringExtra("citynm");
                new WeatherThread(citynm).start();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshView = (TextView)findViewById(R.id.refreshtextViewID);
        citynmView = (TextView)findViewById(R.id.citynmID);
        temperatureView = (TextView)findViewById(R.id.temperatureID);
        temperature_currView = (TextView)findViewById(R.id.temperature_currID);
        weatherView = (TextView)findViewById(R.id.weatherID);
        windView = (TextView)findViewById(R.id.windID);
        winpView = (TextView)findViewById(R.id.winpID);
        aqi_remark = (TextView)findViewById(R.id.aqi_remark);


        weather_icon = (ImageView)findViewById(R.id.weather_iconID);
        sousuoButton = (Button)findViewById(R.id.sousuoID);

        //未来天气
        weather_icons[0] = (ImageView)findViewById(R.id.weather_icon1ID);
        weather_icons[1] = (ImageView)findViewById(R.id.weather_icon2ID);
        weather_icons[2] = (ImageView)findViewById(R.id.weather_icon3ID);
        weather_icons[3] = (ImageView)findViewById(R.id.weather_icon4ID);
        weather_icons[4] = (ImageView)findViewById(R.id.weather_icon5ID);
        weather_icons[5] = (ImageView)findViewById(R.id.weather_icon6ID);

        weeks[0] = (TextView)findViewById(R.id.week1ID);
        weeks[1] = (TextView)findViewById(R.id.week2ID);
        weeks[2] = (TextView)findViewById(R.id.week3ID);
        weeks[3] = (TextView)findViewById(R.id.week4ID);
        weeks[4] = (TextView)findViewById(R.id.week5ID);
        weeks[5] = (TextView)findViewById(R.id.week6ID);

        temperatures[0] = (TextView)findViewById(R.id.temperature1ID);
        temperatures[1] = (TextView)findViewById(R.id.temperature2ID);
        temperatures[2] = (TextView)findViewById(R.id.temperature3ID);
        temperatures[3] = (TextView)findViewById(R.id.temperature4ID);
        temperatures[4] = (TextView)findViewById(R.id.temperature5ID);
        temperatures[5] = (TextView)findViewById(R.id.temperature6ID);

        myHandler = new MyHandler();

        //todayURL = "http://api.k780.com:88/?app=weather.today&weaid="+citynm+"&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
        //futureURL =  "http://api.k780.com:88/?app=weather.future&weaid="+citynm+"&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
        //airURL  = "http://api.k780.com:88/?app=weather.pm25&weaid="+citynm+"&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
        sousuoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Search.class);
                startActivityForResult(intent,0);
            }
        });
       new WeatherThread(citynm).start();



    }

//    //WeatherIconThread
//    class WeatherIconThread extends Thread{
//        String url;
//        public WeatherIconThread(String url){
//            this.url = url;
//        }
//        @Override
//        public void run() {
//            try {
//                URL url = new URL(weather_icon_url);
//                InputStream is = url.openStream();
//                bitmap = BitmapFactory.decodeStream(is);
//                myHandler.sendEmptyMessage(0x124);
//                is.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }



    //WeatherThread
    class WeatherThread extends Thread{

        String todayXML;            //代表URL
        String airXML;              //空气质量URL
        String futureXML;           //未来URL

        public WeatherThread(String citynm) {
            if(citynm.equals(""))
                citynm = "西安";
            this.todayXML = "http://api.k780.com:88/?app=weather.today&weaid="+citynm+"&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
            this.airXML =  "http://api.k780.com:88/?app=weather.pm25&weaid="+citynm+"&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
            this.futureXML = "http://api.k780.com:88/?app=weather.future&weaid="+citynm+"&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";

        }
        @Override
        public void run() {

            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(todayXML);
            try {
                //获得todayURL信息
                HttpResponse httpResponse = client.execute(httpGet);
                int code = httpResponse.getStatusLine().getStatusCode();
                if(code == 200) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                    String line;
                    String jsonData ="";
                    while((line = reader.readLine()) != null) {
                        jsonData += line;
                    }
                    JSONObject jsonObject = new JSONObject(jsonData);
                    jsonData = jsonObject.getString("result");
                    JSONObject jsonObject1 = new JSONObject(jsonData);
                    Message msg = new Message();
                    msg.what=0x123;
                    Bundle bundle = new Bundle();
                    bundle.putString("days",jsonObject1.getString("days"));
                    bundle.putString("week",jsonObject1.getString("week"));
                    bundle.putString("citynm",jsonObject1.getString("citynm"));
                    bundle.putString("temperature",jsonObject1.getString("temperature"));
                    bundle.putString("weather",jsonObject1.getString("weather"));
                    bundle.putString("temperature_curr",jsonObject1.getString("temp_curr"));
                    bundle.putString("weather_icon",jsonObject1.getString("weather_icon"));
                    bundle.putString("wind",jsonObject1.getString("wind"));
                    bundle.putString("winp", jsonObject1.getString("winp"));
                    bundle.putString("weather_icon",jsonObject1.getString("weather_icon"));
                    msg.setData(bundle);
                    myHandler.sendMessage(msg);


                }



            } catch (Exception e) {
                e.printStackTrace();
            }

            //获得airURL信息
            HttpClient Client1 = new DefaultHttpClient();
            HttpGet httpGet1 = new HttpGet(airXML);
            HttpResponse httpResponse1 = null;
            try {
                httpResponse1 = Client1.execute(httpGet1);
                int code1 = httpResponse1.getStatusLine().getStatusCode();
                if(code1 == 200) {

                    HttpEntity httpEntity1 = httpResponse1.getEntity();
                    BufferedReader reader1 = new BufferedReader(new InputStreamReader(httpEntity1.getContent()));
                    String line;
                    String jsondata1 = "";
                    while((line = reader1.readLine()) != null) {
                        jsondata1 += line;
                    }
                    JSONObject jsonObject = new JSONObject(jsondata1);
                    jsondata1 = jsonObject.getString("result");
                    JSONObject jsonObject1 = new JSONObject(jsondata1);
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("aqi_remark",jsonObject1.getString("aqi_remark"));
                    msg.setData(bundle);
                    msg.what = 0x125;
                    myHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            //获得futureURL信息
            HttpClient client2 = new DefaultHttpClient();
            HttpGet httpGet2 = new HttpGet();
            try {
                HttpResponse httpResponse2 = client2.execute(httpGet2);
                int code2 = httpResponse2.getStatusLine().getStatusCode();
                if(code2 == 200) {

                    HttpEntity httpEntity = httpResponse2.getEntity();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));
                    String line;
                    String jsondata2 = "";
                    while((line = reader.readLine()) != null) {
                        jsondata2 += line;
                    }
                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    JSONObject jsonObject = new JSONObject(jsondata2);
                    jsondata2 = jsonObject.getString("result");
                    JsonReader jsonReader = new JsonReader(new StringReader(jsondata2));
                    jsonReader.beginArray();
                    String[] weather_icon = new String[6];
                    String[] week = new String[6];
                    String[] temperature = new String[6];
                    int i = 0;
                    while(jsonReader.hasNext()) {

                        jsonReader.beginObject();

                        while(jsonReader.hasNext()) {
                            String tagName = jsonReader.nextName();

                            if(tagName.equals("weather_icon"))
                                weather_icon[i] = jsonReader.nextString();
                            if(tagName.equals("week"))
                                week[i] = jsonReader.nextString();
                            if(tagName.equals("temperature"))
                                temperature[i] = jsonReader.nextString();
                        }
                        jsonReader.endObject();
                        i++;
                    }
                    jsonReader.endArray();
                    bundle.putStringArray("weather_icon", weather_icon);
                    bundle.putStringArray("week", week);
                    bundle.putStringArray("temperature",temperature);
                    msg.setData(bundle);
                    msg.what = 0x126;
                    myHandler.sendMessage(msg);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

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
}
