package com.example.influxdb_dataviewer.ApiRelated;

public class RefreshOption {
    public String BucketName;
    public String OrgName;
    public String SampleTime;

    public RefreshOption(String bucketName, String orgName, String sampleTime) {
        BucketName = bucketName;
        OrgName = orgName;
        SampleTime = sampleTime;
    }
}
