package com.example.influxdb_dataviewer.Fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.influxdb_dataviewer.CustomResponse;
import com.example.influxdb_dataviewer.MainActivity;
import com.example.influxdb_dataviewer.R;
import com.influxdb.client.AuthorizationsApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Authorization;
import com.influxdb.query.FluxTable;

import java.util.List;

public class Login_Fragment extends Fragment {

    CustomResponse CR;
    public String APIIp=null;
    public Login_Fragment(CustomResponse cr) {
        this.CR=cr;
    }
    private ImageView LoadingEffect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_login_, container, false);
        Button Proceed_BTN=v.findViewById(R.id.Login_Button);
        EditText IpBox=v.findViewById(R.id.Login_IpBox),
                UsernameBox=v.findViewById(R.id.Login_UserNameBox),
                APIIpBox=v.findViewById(R.id.Login_IpBoxAPI),
                PasswordBox=v.findViewById(R.id.Login_PasswordBox);
        TextView FailedTV=v.findViewById(R.id.Login_FailedNotification);
        LoadingEffect=v.findViewById(R.id.LoadingImage);
        Proceed_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("TestCreateClient", "Button Pressed");
                String IP=IpBox.getText().toString(),
                        Name=UsernameBox.getText().toString(),
                        Password=PasswordBox.getText().toString();
                Proceed_BTN.setEnabled(false);
                TryCreatingClient(IP, Name, Password, new CustomResponse() {
                    @Override
                    public void OnResponse(Object obj) {
                        if(obj==null)
                        {
                            Log.e("TestCreateClient", "Failed Checking");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Proceed_BTN.setEnabled(true);
                                    ((MainActivity)getActivity()).Stop_andGONE_LoadingEffect(LoadingEffect);
                                    FailedTV.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        else
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Proceed_BTN.setEnabled(true);
                                    ((MainActivity)getActivity()).Stop_andGONE_LoadingEffect(LoadingEffect);
                                    FailedTV.setVisibility(View.GONE);
                                    if(!APIIpBox.getText().equals(""))
                                    {
                                        APIIp="http://"+APIIpBox.getText().toString().trim()+"/";
                                        CR.OnResponse(obj);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
        return v;
    }
    void TryCreatingClient(String Ip,String name,String password,CustomResponse checkingResponse)
    {
        ((MainActivity)getActivity()).StartLoadingEffect(LoadingEffect);
        new Thread()
        {
            public void run()
            {
                String FullAddress="http://"+Ip;
                try {
                    Log.e("TestCreateClient", "in try catch");
                    InfluxDBClient Client = InfluxDBClientFactory.create(FullAddress, name, password.toCharArray());
                    AuthorizationsApi authorizationsApi = Client.getAuthorizationsApi();
                    List<Authorization> authorizationsByUserName = authorizationsApi.findAuthorizationsByUserName(name);
                    if(authorizationsByUserName!=null)
                    {
                        Log.e("TestCreateClient", "Success");
                        checkingResponse.OnResponse(Client);
                    }
                    else
                    {
                        Log.e("TestCreateClient", "failed AuthorizeList");
                        checkingResponse.OnResponse(null);
                    }
                }
                catch (Exception ex)
                {
                    Log.e("TestCreateClient", "Failed in Catch");
                    checkingResponse.OnResponse(null);
                }
                Log.e("TestCreateClient", "out of try catch");
            }
        }.start();
    }
}