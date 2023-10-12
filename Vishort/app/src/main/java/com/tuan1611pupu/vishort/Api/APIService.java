package com.tuan1611pupu.vishort.Api;

public class APIService {
    private static String base_url = "https://mymusicpupu.000webhostapp.com/server/server/";
    public static DataService getService(){
        return APIRetrofitClient.getClient(base_url).create(DataService.class);
    }

}
