package com.example.influxdb_dataviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.influxdb_dataviewer.ApiRelated.Connection.API_client;
import com.example.influxdb_dataviewer.ApiRelated.Connection.API_interface;
import com.example.influxdb_dataviewer.ApiRelated.RefreshOption;
import com.example.influxdb_dataviewer.CustomAPIClasses.GetJson.Json_DeviceData;
import com.example.influxdb_dataviewer.CustomAPIClasses.UsableClasses.DeviceData;
import com.example.influxdb_dataviewer.Fragments.Gauge_Fragment;
import com.example.influxdb_dataviewer.Fragments.Graph_Fragment;
import com.example.influxdb_dataviewer.Fragments.Information_Fragment;
import com.example.influxdb_dataviewer.Fragments.Login_Fragment;
import com.example.influxdb_dataviewer.Fragments.Setting_Fragment;
import com.example.influxdb_dataviewer.Fragments.Table_Fragment;
import com.example.influxdb_dataviewer.RecyclerView.GaugeAdapter;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements CustomResponse {
    public InfluxDBClient currentClient;
    public RefreshOption CurrentRefreshOption;
    public List<FluxTable> CurrentTables;
    private Fragment CurrentFragment;
    private boolean RefreshPressedCheck = false;
    public int CurrentFragmentID = 1;


    public Handler UpdateHandler = new Handler();
    public Runnable UpdateRunnable;
    private long ReceiverIntervalTime =2000;
    ArrayList<DeviceData> LatestReceivedList;

    API_interface current_API_interface;

    public CustomResponse PostReceivedDB = new CustomResponse() {
        @Override
        public void OnResponse(Object obj) {
            CurrentTables = (List<FluxTable>) obj;
            NotificationSetup(CurrentTables);
            //Log.d("testTransferTables", CurrentTables.get(0).getRecords().get(0).getMeasurement());
            if (CurrentFragmentID != 2 && CurrentFragmentID != 3 && CurrentFragmentID != 5) {
                ChangeFragmentTo(2, false);
            } else {
                switch (CurrentFragmentID) {
                    case 2:
                        if (RefreshPressedCheck == true) {
                            ChangeFragmentTo(2, true);
                            RefreshPressedCheck = false;
                        }
                        break;
                    case 3:
                        ChangeFragmentTo(3, true);
                        break;
                    case 5:
                        ((Gauge_Fragment) CurrentFragment).SetupGlobalData(CurrentTables, true);
                }
            }
        }
    };
    private CustomResponse PostReceiveDataResponse=new CustomResponse() {
        @Override
        public void OnResponse(Object obj) {
            switch (CurrentFragmentID)
            {
                case 6:
                    ((Information_Fragment)CurrentFragment).RefreshList(LatestReceivedList);
                    Log.d("TaskCheck", "Complete");
                    break;
            }
            UpdateHandler.postDelayed(UpdateRunnable, ReceiverIntervalTime);
        }
    };

    public ArrayList<GaugeAdapter.GaugeValue> ComposeGaugeValueList(List<FluxTable> tables) {
        ArrayList<GaugeAdapter.GaugeValue> listGaugeValue = new ArrayList<>();
        for (FluxTable table :
                tables) {
            String Measurement = table.getRecords().get(0).getMeasurement();
            double value = (double) table.getRecords().get(table.getRecords().size() - 1).getValue();
            if (Measurement.contains("Vibration"))
                listGaugeValue.add(new GaugeAdapter.GaugeValue(Measurement, 1, value));
            else listGaugeValue.add(new GaugeAdapter.GaugeValue(Measurement, 0, value));
        }
        return listGaugeValue;
    }

    private void NotificationSetup(List<FluxTable> tables) {
        ArrayList<GaugeAdapter.GaugeValue> DataList = ComposeGaugeValueList(tables);
        int count = 0;
        for (GaugeAdapter.GaugeValue value :
                DataList) {
            count++;
            if (value.Value > 71) {
                Notification notification = new NotificationCompat.Builder(this, ForNotification.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.gauge_icon)
                        .setColor(Color.BLACK)
                        .setContentTitle("InfluxDB DataViewer Emergency notification")
                        .setContentText(value.Measurement + " is at Dangerous state: " + value.Value)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .build();

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                boolean alreadynotiflag=false;
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
                for (StatusBarNotification noti :
                        notifications) {
                    if(noti.getId()==count) alreadynotiflag=true;
                }

                if(alreadynotiflag==false)notificationManagerCompat.notify(count, notification);
            }
        }
    }
    RelativeLayout NavMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //untouched

        this.NavMenu=findViewById(R.id.NavMenu);


        State1();
        this.OnResponse(CurrentFragmentID);


        ImageButton RefreshButton=findViewById(R.id.Refresh_BTN),GaugeButton=findViewById(R.id.GaugeFragmentBTN),TableButton=findViewById(R.id.TableFragmentBTN),GraphButton=findViewById(R.id.GraphFragmentBTN),SettingButton=findViewById(R.id.SettingBTN);
        RefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RefreshPressedCheck=true;
                ReceiveDataFromInfluxDB(PostReceivedDB,true);
            }
        });
        GaugeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeFragmentTo(5,false);
            }
        });
        TableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ChangeFragmentTo(2,false);
            }
        });
        GraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeFragmentTo(3,false);
            }
        });
        SettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeFragmentTo(4,false);
            }
        });
    }
    public void ChangeFragmentTo(int FragmentID,boolean ForceFull)
    {
        try {
            ((Gauge_Fragment)CurrentFragment).UpdateHandler.removeCallbacks(((Gauge_Fragment) CurrentFragment).UpdateRunnable);
        }
        catch (Exception ex)
        {

        }
        if(CurrentFragmentID!=FragmentID)
        {
            CurrentFragmentID =FragmentID;
            MainActivity.this.OnResponse(CurrentFragmentID);
        }
        else if (ForceFull==true)
        {
            CurrentFragmentID =FragmentID;
            MainActivity.this.OnResponse(CurrentFragmentID);
        }
    }
    public void ReceiveDataNow(CustomResponse CR)
    {
        if(current_API_interface!=null) {
            Call<ArrayList<Json_DeviceData>> CallData = current_API_interface.GetData();
            CallData.enqueue(new Callback<ArrayList<Json_DeviceData>>() {
                @Override
                public void onResponse(Call<ArrayList<Json_DeviceData>> call, Response<ArrayList<Json_DeviceData>> response) {
                    Log.d("GetData", "code: " + response.code() + "");
                    //Log.d("GetData","code: "+response.body().toString());
                    LatestReceivedList = ConvertingResponseToDeviceDataList(response.body());
                    CR.OnResponse(null);
                }

                @Override
                public void onFailure(Call<ArrayList<Json_DeviceData>> call, Throwable t) {
                    Log.e("GetData", t.getMessage());

                }
            });
        }
        else CR.OnResponse(null); //for testing purpose
    }
    public ArrayList<DeviceData> ConvertingResponseToDeviceDataList(ArrayList<Json_DeviceData> response)
    {
        ArrayList<DeviceData> ConvertedList=new ArrayList<>();
        for (Json_DeviceData board:
                response) {
            DeviceData tmp=new DeviceData(
                    board.Board_name,
                    board.Board_ip,
                    board.Board_id,
                    board.Board_status,
                    ConvertingTimeStamp(board.TimeStamp,0)
            );
            ConvertedList.add(tmp);
        }
        return ConvertedList;
    }
    public String ConvertingTimeStamp(long raw,int type)
    {
        if (type==0)return new SimpleDateFormat("dd/MM HH:mm").format(new Date(raw*1000));
        else return new SimpleDateFormat("dd/MM\nHH:mm").format(new Date(raw*1000));
    }
    public void ReceiveDataFromInfluxDB(CustomResponse CR,boolean UiThreadLock)
    {
        if(CurrentRefreshOption==null) return;
        String BucketName=CurrentRefreshOption.BucketName;
        String ORG=CurrentRefreshOption.OrgName;
        String SampleFrom=CurrentRefreshOption.SampleTime;
        new Thread()
        {
            public void run()
            {
                try {
                    String query = "from(bucket: \""+BucketName+"\") |> range(start: "+SampleFrom+")";
                    List<FluxTable> tables = currentClient.getQueryApi().query(query, ORG);
                    for (FluxTable table : tables) {
                        Log.d("ReceivedTables", table.toString());
                        for (FluxRecord record : table.getRecords()) {
                            Log.d("ReceivedQuery", record.getMeasurement()+" : "+record.getValue().toString());
                        }
                    }
                    if(UiThreadLock==false) MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(tables.size()>0) {
                                MainActivity.this.OnResponse(-3);
                                CR.OnResponse(tables);
                            }
                            else
                                MainActivity.this.OnResponse(-4);
                        }
                    });
                    else if(tables.size()>0) CR.OnResponse(tables);
                }
                catch (Exception ex)
                {
                    Log.e("Query_Exception", ex.getMessage());
                    if(UiThreadLock==false) MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.OnResponse(-4);
                        }
                    });
                }
            }
        }.start();
    }
    public void SetFragmentToFocus(Fragment theFragment)
    {
        try {
            FragmentManager fragmentManager=getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.FragmentHolder,theFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            CurrentFragment=theFragment;
        }
        catch (Exception ex)
        {
            Log.d("Attach Fragment Failed",ex.getMessage());
        }
    }
    public void State1()
    {
        NavMenu.setVisibility(View.GONE);
    }
    public void State2()
    {
        NavMenu.setVisibility(View.VISIBLE);
    }
    public void StartLoadingEffect(ImageView srcLoadingImage)
    {
        Log.d("Loading","Start");
        srcLoadingImage.setVisibility(View.VISIBLE);
        AnimationDrawable LoadingAnimation;
        ImageView loadingImage=srcLoadingImage;
        LoadingAnimation=(AnimationDrawable) loadingImage.getDrawable();
        LoadingAnimation.start();
    }
    public void Stop_andGONE_LoadingEffect(ImageView srcLoadingImage)
    {
        Log.d("Loading","Stop");
        AnimationDrawable LoadingAnimation;
        ImageView loadingImage=srcLoadingImage;
        LoadingAnimation=(AnimationDrawable) loadingImage.getDrawable();
        LoadingAnimation.stop();
        srcLoadingImage.setVisibility(View.GONE);
    }

    @Override
    public void OnResponse(Object obj) {
        int FragmentOption=(int) obj;
        switch (FragmentOption)
        {
            case -4:        //only for Setting fragment
                ((Setting_Fragment)CurrentFragment).FailedNotification.setVisibility(View.VISIBLE);
                Stop_andGONE_LoadingEffect(((Setting_Fragment)CurrentFragment).LoadingEffect);
                break;
            case -3:        //only for Setting fragment
                ((Setting_Fragment)CurrentFragment).FailedNotification.setVisibility(View.GONE);
                Stop_andGONE_LoadingEffect(((Setting_Fragment)CurrentFragment).LoadingEffect);
                break;
            case 1:
                SetFragmentToFocus(new Login_Fragment(new CustomResponse() {
                    @Override
                    public void OnResponse(Object obj) {
                        currentClient=(InfluxDBClient) obj;
                        Log.e("TestTransferClient", "Success");
                        String APIIp=((Login_Fragment)CurrentFragment).APIIp;
                        if(APIIp!=null && !APIIp.equals(""))current_API_interface= API_client.getClient(((Login_Fragment)CurrentFragment).APIIp).create(API_interface.class);

                        UpdateRunnable = new Runnable() {
                            @Override
                            public void run() {
                                ReceiveDataNow(PostReceiveDataResponse);
                            }
                        };

                        UpdateHandler.postDelayed(UpdateRunnable, 500);

                        State2();
                        ChangeFragmentTo(4,false);

                    }
                }));
                break;
            case 2:
                SetFragmentToFocus(new Table_Fragment(CurrentTables));
                break;
            case 3:
                SetFragmentToFocus(new Graph_Fragment(CurrentTables));
                break;
            case 4:
                Setting_Fragment setting_fragment=new Setting_Fragment(currentClient, new CustomResponse() {
                    @Override
                    public void OnResponse(Object obj) {
                        if(obj==null)
                        {
                            State1();
                            ChangeFragmentTo(1,false);
                        }
                        else
                        {
                            CurrentRefreshOption=(RefreshOption) obj;
                            ReceiveDataFromInfluxDB(PostReceivedDB,false);
                        }
                    }
                });
                SetFragmentToFocus(setting_fragment);
                break;
            case 5:
                SetFragmentToFocus(new Gauge_Fragment(CurrentTables, new CustomResponse() {
                    @Override
                    public void OnResponse(Object obj) {
                        ReceiveDataFromInfluxDB(PostReceivedDB,true);
                    }
                }));
                break;
            case 6:
                SetFragmentToFocus(new Information_Fragment(LatestReceivedList));
                break;
        }
    }

    public Date ConvertTimeToGMT_Plus7(Instant instant)
    {
        Date date=Date.from(instant);
        // Convert to ZonedDateTime with UTC time zone
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneOffset.UTC);

        // Change the time zone to America/New_York
        zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Convert back to Date
        Date newDate = Date.from(zonedDateTime.toInstant());
        return newDate;
    }
}