package com.vish.paytmdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.vish.paytmdemo.models.Checksum;
import com.vish.paytmdemo.models.Paytm;
import com.vish.paytmdemo.utils.WebServiceCaller;

import java.util.HashMap;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    AppCompatButton paytmButton;
    AppCompatEditText amountText;
    ProgressBar progressBar;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        paytmButton = findViewById(R.id.paytmButton);
        amountText = findViewById(R.id.amountText);
        progressBar = findViewById(R.id.progressBar);
        paytmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPaytm();
            }
        });
    }

    private void processPaytm() {
        progressBar.setVisibility(View.VISIBLE);
        String custID = generateString();
        String orderID = generateString();
        String callBackurl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=" + orderID;

        final Paytm paytm = new Paytm(
                "KPSFhZ90030999897221",
                "WAP",
                amountText.getText().toString().trim(),
                "WEBSTAGING",
                callBackurl,
                "Retail",
                orderID,
                custID
        );

        WebServiceCaller.getClient().getChecksum(paytm.getmId(), paytm.getOrderId(), paytm.getCustId()
                , paytm.getChannelId(), paytm.getTxnAmount(), paytm.getWebsite(), paytm.getCallBackUrl(), paytm.getIndustryTypeId()
        ).enqueue(new Callback<Checksum>() {
            @Override
            public void onResponse(Call<Checksum> call, Response<Checksum> response) {
                if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    processToPay(response.body().getChecksumHash(),paytm);
                }
            }

            @Override
            public void onFailure(Call<Checksum> call, Throwable t) {

            }
        });


    }

    private void processToPay(String checksumHash, Paytm paytm) {
        PaytmPGService Service = PaytmPGService.getStagingService();

        HashMap<String, String> paramMap = new HashMap<String,String>();
        paramMap.put( "MID" , paytm.getmId());
// Key in your staging and production MID available in your dashboard
        paramMap.put( "ORDER_ID" , paytm.getOrderId());
        paramMap.put( "CUST_ID" , paytm.getCustId());
        paramMap.put( "CHANNEL_ID" , paytm.getChannelId());
        paramMap.put( "TXN_AMOUNT" , paytm.getTxnAmount());
        paramMap.put( "WEBSITE" , paytm.getWebsite());
// This is the staging value. Production value is available in your dashboard
        paramMap.put( "INDUSTRY_TYPE_ID" , paytm.getIndustryTypeId());
// This is the staging value. Production value is available in your dashboard
        paramMap.put( "CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put( "CHECKSUMHASH" , checksumHash);
        PaytmOrder Order = new PaytmOrder(paramMap);
        Service.initialize(Order, null);

        Service.startPaymentTransaction(this, true, true, new PaytmPaymentTransactionCallback() {
            /*Call Backs*/
            public void onTransactionResponse(Bundle inResponse) {
                //Toast.makeText(getContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                Log.d(TAG,"Payment Transaction response " + inResponse.toString());
                if(inResponse.getString("RESPMSG").equals("Txn Success") && inResponse.getString("STATUS").equals("TXN_SUCCESS")){
                    showAlert("Transaction Successful");
                }else if(inResponse.getString("RESPMSG").equalsIgnoreCase("Txn Failure")&& inResponse.getString("STATUS").equals("TXN_FAILURE")){
                    showAlert("Transaction Failure");
                }
            }

            @Override
            public void networkNotAvailable() {
                Toast.makeText(MainActivity.this, "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
            }

            @Override
            public void clientAuthenticationFailed(String inErrorMessage) {
                Log.d(TAG,"Authentication failed: Server error" + inErrorMessage);
            }

            @Override
            public void someUIErrorOccurred(String inErrorMessage) {
                Toast.makeText(MainActivity.this, "UI Error " + inErrorMessage , Toast.LENGTH_LONG).show();
                Log.d(TAG,"UI Error " + inErrorMessage);
            }

            @Override
            public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                Toast.makeText(MainActivity.this, "Unable to load webpage " + inErrorMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG,"Unable to load webpage " + inErrorMessage);
            }

            @Override
            public void onBackPressedCancelTransaction() {
                Toast.makeText(MainActivity.this, "Transaction cancelled" , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                Toast.makeText(MainActivity.this, "Transaction cancelled" , Toast.LENGTH_LONG).show();
                Log.d(TAG,inErrorMessage);
                Log.d(TAG,inResponse.toString());
            }
        });

    }

    private void showAlert(String msg){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();
    }

    private String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }

}
