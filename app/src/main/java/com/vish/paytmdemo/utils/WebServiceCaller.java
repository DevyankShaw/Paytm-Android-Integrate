package com.vish.paytmdemo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vish.paytmdemo.models.Checksum;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class WebServiceCaller {

    private static WebServiceApiInterface webApiInterface;

    public static WebServiceApiInterface getClient() {
        if (webApiInterface == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okclient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(10, TimeUnit.MINUTES)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .writeTimeout(10, TimeUnit.MINUTES)
                    .build();
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            Retrofit client = new Retrofit.Builder()
                    .baseUrl("https://sizy-millimeters.000webhostapp.com/")
                    .client(okclient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            webApiInterface = client.create(WebServiceApiInterface.class);
        }
        return webApiInterface;
    }


    public interface WebServiceApiInterface {


        @FormUrlEncoded
        @POST("paytm/generateChecksum.php")
        Call<Checksum> getChecksum(
                @Field(value = "MID", encoded = true) String mId,
                @Field(value = "ORDER_ID", encoded = true) String orderId,
                @Field(value = "CUST_ID", encoded = true) String custId,
                @Field(value = "CHANNEL_ID", encoded = true) String channelId,
                @Field(value = "TXN_AMOUNT", encoded = true) String txnAmount,
                @Field(value = "WEBSITE", encoded = true) String website,
                @Field(value = "CALLBACK_URL", encoded = true) String callbackUrl,
                @Field(value = "INDUSTRY_TYPE_ID", encoded = true) String industryTypeId
        );


    }
}