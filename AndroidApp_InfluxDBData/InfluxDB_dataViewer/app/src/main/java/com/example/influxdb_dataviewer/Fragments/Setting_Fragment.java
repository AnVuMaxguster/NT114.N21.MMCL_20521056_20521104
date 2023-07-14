package com.example.influxdb_dataviewer.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.influxdb_dataviewer.ApiRelated.RefreshOption;
import com.example.influxdb_dataviewer.CustomResponse;
import com.example.influxdb_dataviewer.MainActivity;
import com.example.influxdb_dataviewer.R;
import com.example.influxdb_dataviewer.Spinner.Category;
import com.example.influxdb_dataviewer.Spinner.CategoryAdapter;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;

import java.util.ArrayList;
import java.util.List;

public class Setting_Fragment extends Fragment {
    private InfluxDBClient Client;
    private CustomResponse CR;
    public ImageView LoadingEffect;
    public TextView FailedNotification;
    private String ChosenBucketName, ChosenOrgName, ChosenTimeID;
    public Setting_Fragment(InfluxDBClient providedClient,CustomResponse cr) {
        this.Client= providedClient;
        this.CR=cr;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_setting_, container, false);

        //untouched

        Spinner BucketsChoice=v.findViewById(R.id.Setting_spinnerBuckets),ORGsChoice=v.findViewById(R.id.Setting_spinnerORGs),TimeChoice=v.findViewById(R.id.Setting_spinnerTime);
        Button ConfirmBTN=v.findViewById(R.id.Setting_ConfirmBTN),
                SignOutBTN=v.findViewById(R.id.Setting_SignOutBTN);
        ImageButton BoardInformationBTN=v.findViewById(R.id.Setting_InformationBTN);
        LoadingEffect=v.findViewById(R.id.Setting_LoadingImage);
        FailedNotification=v.findViewById(R.id.Setting_FailedNotification);
        setListCategoryBuckets(BucketsChoice);
        setListCategoryORGs(ORGsChoice);
        setListCategoryTime(TimeChoice);
        ConfirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).StartLoadingEffect(LoadingEffect);
                FailedNotification.setVisibility(View.GONE);
                if(ChosenBucketName !=null&& ChosenOrgName !=null&& ChosenTimeID !=null)
                {
                    CR.OnResponse(new RefreshOption(ChosenBucketName, ChosenOrgName, ChosenTimeID));
                }
                else
                {
                    ((MainActivity)getActivity()).Stop_andGONE_LoadingEffect(LoadingEffect);
                    FailedNotification.setVisibility(View.VISIBLE);
                }
            }
        });
        SignOutBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder albuider=new AlertDialog.Builder(getActivity());
                albuider.setTitle("Signing Out ?");
                albuider.setMessage("You are about to sign out, are you sure about this ?");
                albuider.setPositiveButton("Yes. Please sign me out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CR.OnResponse(null);
                    }
                });
                albuider.setNegativeButton("No. This is a mistake", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                albuider.show();
            }
        });
        BoardInformationBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).ChangeFragmentTo(6,false);
            }
        });
        return  v;
    }
    private void setListCategoryBuckets(Spinner BucketsChoice)
    {
        new Thread()
        {
            public void run()
            {
                List<Bucket> buckets=Client.getBucketsApi().findBuckets();
                if(buckets!=null)
                {
                    List<Category> dalist = new ArrayList<>();
                    for (Bucket bucket:
                            buckets) {
                        Category temp=new Category(bucket.getName(),bucket.getId());
                        dalist.add(temp);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            CategoryAdapter BucketscategoryAdapter=new CategoryAdapter(getActivity(),R.layout.item_select,dalist);
                            BucketsChoice.setAdapter(BucketscategoryAdapter);
                            BucketsChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    ChosenBucketName =BucketscategoryAdapter.getItem(position).name;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    });
                }
            }
        }.start();
    }
    private void setListCategoryORGs(Spinner ORGsChoice)
    {
        new Thread()
        {
            public void run()
            {
                List<Organization> ORGs =Client.getOrganizationsApi().findOrganizations();
                if(ORGs !=null)
                {
                    List<Category> dalist = new ArrayList<>();
                    for (Organization org :
                            ORGs) {
                        Category temp=new Category(org.getName(), org.getId());
                        dalist.add(temp);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            CategoryAdapter ORGscategoryAdapter =new CategoryAdapter(getActivity(),R.layout.item_select,dalist);
                            ORGsChoice.setAdapter(ORGscategoryAdapter);
                            ORGsChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    ChosenOrgName =ORGscategoryAdapter.getItem(position).name;
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                    });
                }
            }
        }.start();
    }
    private void setListCategoryTime(Spinner TimeChoice)
    {
        List<Category> dalist = new ArrayList<>();
        dalist.add(new Category("30m","-30m"));
        dalist.add(new Category("1h","-1h"));
        dalist.add(new Category("3h","-3h"));
        dalist.add(new Category("6h","-6h"));
        dalist.add(new Category("9h","-9h"));
        dalist.add(new Category("12h","-12h"));
        CategoryAdapter TimecategoryAdapter =new CategoryAdapter(getActivity(),R.layout.item_select,dalist);
        TimeChoice.setAdapter(TimecategoryAdapter);
        TimeChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ChosenTimeID =TimecategoryAdapter.getItem(position).ID;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}