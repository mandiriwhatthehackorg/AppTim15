package com.myans.mandirihackathon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.myans.mandirihackathon.adapter.ChatAdapter;
import com.myans.mandirihackathon.base_class.BaseSharedPref;
import com.myans.mandirihackathon.interfaces.BaseRequestListener;
import com.myans.mandirihackathon.interfaces.Const;
import com.myans.mandirihackathon.model.ChatModel;
import com.myans.mandirihackathon.model.PhoneCreditModel;
import com.myans.mandirihackathon.utils.LayoutUtils;
import com.myans.mandirihackathon.utils.PhoneNumberUtils;
import com.myans.mandirihackathon.utils.RequestUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity implements BaseRequestListener {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech myTTS;
    private SpeechRecognizer mySpeechRecognizer;

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ArrayList<ChatModel> listChat;

    ImageView imageView;
    ImageView imageView2;
    //top-up requirement
    private PhoneCreditModel phoneCreditModel;

    public static final int STATE_NEW = 0;


    public static final int TOP_UP_STATE_ASK_NUMBER = 1;
    public static final int TOP_UP_STATE_ASK_AMOUNT = 2;
    public static final int TOP_UP_STATE_PROCESSING = 3;
    public static final int TOP_UP_STATE_FINISHED = 4;
    public static final int TOP_UP_STATE_ASK_CONFIRMATION= 5;


    //transfer requirement


    private int currentState = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.icon_app);
        imageView2 = findViewById(R.id.gif_icon);
        ImageView micIcon = findViewById(R.id.ic_mic);

        Glide.with(this)
                .load(R.drawable.listening)
                .into(imageView2);


        initializeTextToSpeech();
        initializeSpeechToText();
        initChatRequirement();
        handleSSLHandshake();
        RequestUtils.getToken(this, this);

        micIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initListening();
            }
        });
    }

    // Top-up pulsa
    private void askNumber(){
        adapter.removeLastItem();
        phoneCreditModel = new PhoneCreditModel();
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Sebutkan nomor telepon yang ingin anda isi", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);
        speakUp(replyChat.getMessage());
    }

    private void askAmount(){
        adapter.removeLastItem();
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Berapa nominal yang ingin anda isi?", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);
        currentState = TOP_UP_STATE_ASK_AMOUNT;
        speakUp(replyChat.getMessage());
    }

    private void askConfirmation(){
        adapter.removeLastItem();
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Saya konfirmasi ya, pulsa " + PhoneNumberUtils.getProviderName(PhoneNumberUtils.getProvider(phoneCreditModel.getPhoneNumber()))+ " dengan nomor " + phoneCreditModel.getPhoneNumber() + " sebesar " + phoneCreditModel.getAmount() + ". Apakah sudah benar?" , getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);
        currentState = TOP_UP_STATE_ASK_CONFIRMATION;
        speakUp(replyChat.getMessage());
    }

    private void processTopUpTransaction(){
        adapter.removeLastItem();
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Silahkan ditunggu, transaksi sedang diproses", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);
        phoneCreditModel.debug();
        speakUp(replyChat.getMessage());

        final int provider = PhoneNumberUtils.getProvider(phoneCreditModel.getPhoneNumber());

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Log.d(Const.TAG, "Merequest telkomsel product");
                if(provider == PhoneNumberUtils.TELKOMSEL)
                    RequestUtils.getAPI(Const.GET_TELKOMSEL_PRODUCT, Request.Method.GET, new JSONObject(), MainActivity.this, MainActivity.this, BaseSharedPref.getSpString(MainActivity.this, Const.AUTH_TOKEN));
                else if(provider == PhoneNumberUtils.XL)
                    RequestUtils.getAPI(Const.GET_XL_PRODUCT, Request.Method.GET, new JSONObject(), MainActivity.this, MainActivity.this, BaseSharedPref.getSpString(MainActivity.this, Const.AUTH_TOKEN));
                else if(provider == PhoneNumberUtils.THREE)
                    RequestUtils.getAPI(Const.GET_THREE_PRODUCT, Request.Method.GET, new JSONObject(), MainActivity.this, MainActivity.this, BaseSharedPref.getSpString(MainActivity.this, Const.AUTH_TOKEN));
                else
                    Toast.makeText(MainActivity.this, "Salah", Toast.LENGTH_SHORT).show();
            }
        },7000);

    }

    private void finishTopUpTransaction(){
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Yeay! isi pulsa berhasil dilakukan", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);

        currentState = STATE_NEW;
        speakUp(replyChat.getMessage());

    }

    private void processInput(List<String> results) {
        String text = results.get(0);
        final String input= text.toLowerCase();

        if(currentState == STATE_NEW){
            if(input.contains("pulsa")){
                listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
                listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        currentState = TOP_UP_STATE_ASK_NUMBER;
                        askNumber();
                    }
                }, 2000);
            }else if(input.contains("saldo")){
                listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
                listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms

                        checkBalance();
                    }
                }, 2000);
            } else if(input.contains("transfer")){
                listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
                listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        askBankName();
                    }
                }, 2000);
            } else{
                listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Maaf saya tidak mengerti", getCurrentTimeStamp()));
                adapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                speakUp("Maaf saya tidak mengerti");
            }
        }
        else if(currentState == TOP_UP_STATE_ASK_NUMBER){
            String phoneNumber = input.replaceAll("\\s+","");
            phoneCreditModel.setPhoneNumber(phoneNumber);

            listChat.add(new ChatModel(ChatModel.USER_INPUT, phoneNumber, getCurrentTimeStamp()));
            listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
            recyclerView.smoothScrollToPosition(listChat.size()-1);
            adapter.notifyDataSetChanged();


            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    askAmount();
                }
            }, 2000);


        } else if(currentState == TOP_UP_STATE_ASK_AMOUNT){
            String amount = input.replaceAll("\\s+","");
            amount = amount.replaceAll("[.]","");
            amount = amount.substring(2);
            phoneCreditModel.setAmount(amount);

            listChat.add(new ChatModel(ChatModel.USER_INPUT, amount, getCurrentTimeStamp()));
            listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
            recyclerView.smoothScrollToPosition(listChat.size()-1);
            adapter.notifyDataSetChanged();

            askConfirmation();


        } else if(currentState == TOP_UP_STATE_ASK_CONFIRMATION){


            listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
            listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
            recyclerView.smoothScrollToPosition(listChat.size()-1);
            adapter.notifyDataSetChanged();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 1000ms
                    processTopUpTransaction();
                }
            }, 2000);

        } else if(currentState == TOP_UP_STATE_PROCESSING){

        }
    }

    private void askBankName() {
    }

    private void checkBalance() {
        RequestUtils.getAPI(Const.URL_CHECK_BALANCE, Request.Method.GET, new JSONObject(), MainActivity.this, MainActivity.this, BaseSharedPref.getSpString(MainActivity.this, Const.AUTH_TOKEN));
    }

    @Override
    public void onRequestFinish(String requestUrl, JSONObject param, String message) {
        Log.d(Const.TAG, "url: " + requestUrl);
        if(requestUrl.equals(Const.TOKEN_URL) || requestUrl == Const.TOKEN_URL){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                String token = param.getString("jwt");
                BaseSharedPref.saveSPString(this, Const.AUTH_TOKEN, token);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestUrl.equals(Const.GET_TELKOMSEL_PRODUCT) || requestUrl == Const.GET_TELKOMSEL_PRODUCT){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                processTelkomselProduct(param.getJSONObject("Response"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestUrl.equals(Const.GET_XL_PRODUCT) || requestUrl == Const.GET_XL_PRODUCT){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                processXLProduct(param.getJSONObject("Response"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestUrl.equals(Const.GET_THREE_PRODUCT) || requestUrl == Const.GET_THREE_PRODUCT){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                processThreeProduct(param.getJSONObject("Response"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        else if(requestUrl.equals(Const.URL_TOP_UP_TELKOMSEL) || requestUrl == Const.URL_TOP_UP_TELKOMSEL ||
                requestUrl.equals(Const.URL_TOP_UP_XL) || requestUrl == Const.URL_TOP_UP_XL ||
                requestUrl.equals(Const.URL_TOP_UP_THREE) || requestUrl == Const.URL_TOP_UP_THREE ){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                currentState = STATE_NEW;

                JSONObject jsonObject = new JSONObject();
                JSONObject requestObject = new JSONObject();
                requestObject.accumulate("transactionID", "00000011");
                requestObject.accumulate("transactionDate", "2018-09-10");
                requestObject.accumulate("referenceID", "Order/2018/001");
                requestObject.accumulate("sourceAccountNumber", Const.SOURCE_ACCOUNT);
                requestObject.accumulate("beneficiaryAccountNumber", Const.DUMMY_ACCOUNT);
                requestObject.accumulate("amount", phoneCreditModel.getAmount());
                requestObject.accumulate("currency", "IDR");
                requestObject.accumulate("sourceAccountCustType", "1");
                requestObject.accumulate("beneficiaryCustType", "1");
                requestObject.accumulate("remark1", "Test RTGS 1");
                requestObject.accumulate("remark2", "BIAYA KLIRING1");
                jsonObject.accumulate("Request", requestObject);

                RequestUtils.getAPI(Const.URL_SUBSTRACT_BALANCE, Request.Method.POST, jsonObject, this, this, BaseSharedPref.getSpString(this, Const.AUTH_TOKEN));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(requestUrl.equals(Const.URL_SUBSTRACT_BALANCE) || requestUrl == Const.URL_SUBSTRACT_BALANCE){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                currentState = STATE_NEW;
                finishTopUpTransaction();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(requestUrl.equals(Const.URL_CHECK_BALANCE) || requestUrl == Const.URL_CHECK_BALANCE){
            try {
                Log.d(Const.TAG, "onRequestFinish: " + param.toString(4));
                JSONObject response = param.getJSONObject("Response");
                JSONObject balance = response.getJSONObject("balance");
                String balanceValue = balance.getJSONObject("balanceInfo").getString("ledgerBalance");
                showBalance(balanceValue);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showBalance(String balanceValue) {

        adapter.removeLastItem();
        ChatModel replyChat;
        String balance = currencyFormat(Float.valueOf(balanceValue));
        balance = balance.substring(2);
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Saldo anda sekarang sebesar " + balance + " Rupiah", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);

        currentState = STATE_NEW;
        speakUp(replyChat.getMessage());

    }

    private String currencyFormat(float number){
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id","ID"));
        String currency = format.format(number);
        return currency;
    }

    private void processTelkomselProduct(JSONObject response) {
        try {
            JSONArray bills = response.getJSONArray("bills");
            Log.d(Const.TAG, "processTelkomselProduct: " + bills.toString(4));
            JSONArray products = bills.getJSONObject(0).getJSONArray("products");

            boolean isAvailable = false;
            for(int i=0; i<products.length(); i++){
                if(products.getJSONObject(i).get("productDesc").equals(phoneCreditModel.getAmount())){
                    isAvailable = true;
                    break;
                }
            }
            if(isAvailable){
                TopUpPhoneCredit(PhoneNumberUtils.getProvider(phoneCreditModel.getPhoneNumber()));
            }
            else{
                listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Maaf, nominal yang anda pilih tidak tersedia", getCurrentTimeStamp()));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();
                speakUp("Maaf, nominal yang anda pilih tidak tersedia");
                currentState = STATE_NEW;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void processXLProduct(JSONObject response) {
        try {
            JSONArray bills = response.getJSONArray("bills");
            Log.d(Const.TAG, "processTelkomselProduct: " + bills.toString(4));
            JSONArray products = bills.getJSONObject(0).getJSONArray("products");

            boolean isAvailable = false;
            for(int i=0; i<products.length(); i++){
                if(products.getJSONObject(i).get("productDesc").equals(phoneCreditModel.getAmount())){
                    isAvailable = true;
                    break;
                }
            }
            if(isAvailable){
                TopUpPhoneCredit(PhoneNumberUtils.getProvider(phoneCreditModel.getPhoneNumber()));
            }
            else{
                listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Maaf, nominal yang anda pilih tidak tersedia", getCurrentTimeStamp()));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();
                speakUp("Maaf, nominal yang anda pilih tidak tersedia");
                currentState = STATE_NEW;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void processThreeProduct(JSONObject response) {
        try {
            JSONArray bills = response.getJSONArray("bills");
            Log.d(Const.TAG, "processTelkomselProduct: " + bills.toString(4));
            JSONArray products = bills.getJSONObject(0).getJSONArray("products");

            boolean isAvailable = false;
            for(int i=0; i<products.length(); i++){
                if(products.getJSONObject(i).get("productDesc").equals(phoneCreditModel.getAmount())){
                    isAvailable = true;
                    break;
                }
            }
            if(isAvailable){
                TopUpPhoneCredit(PhoneNumberUtils.getProvider(phoneCreditModel.getPhoneNumber()));
            }
            else{
                listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Maaf, nominal yang anda pilih tidak tersedia", getCurrentTimeStamp()));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();
                speakUp("Maaf, nominal yang anda pilih tidak tersedia");
                currentState = STATE_NEW;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void TopUpPhoneCredit(int provider) {
        if(provider == PhoneNumberUtils.TELKOMSEL){
            JSONObject jsonObject = new JSONObject();
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.accumulate("currency", "IDR");
                requestObject.accumulate("debitAccount", Const.SOURCE_ACCOUNT);
                requestObject.accumulate("paymentAmount", phoneCreditModel.getAmount());
                requestObject.accumulate("companyAccountNumber", "9910000011230");
                jsonObject.accumulate("Request", requestObject);
                Log.d(Const.TAG, "TopUpPhoneCredit Model: " + jsonObject.toString(4));

                RequestUtils.getAPI(Const.URL_TOP_UP_TELKOMSEL, Request.Method.POST, jsonObject, this, this, BaseSharedPref.getSpString(this, Const.AUTH_TOKEN) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else if(provider == PhoneNumberUtils.XL){
            JSONObject jsonObject = new JSONObject();
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.accumulate("currency", "IDR");
                requestObject.accumulate("debitAccount", Const.SOURCE_ACCOUNT);
                requestObject.accumulate("paymentAmount", phoneCreditModel.getAmount());
                requestObject.accumulate("companyAccountNumber", "9910000011230");
                jsonObject.accumulate("Request", requestObject);
                Log.d(Const.TAG, "TopUpPhoneCredit Model: " + jsonObject.toString(4));

                RequestUtils.getAPI(Const.URL_TOP_UP_XL, Request.Method.POST, jsonObject, this, this, BaseSharedPref.getSpString(this, Const.AUTH_TOKEN) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(provider == PhoneNumberUtils.THREE){
            JSONObject jsonObject = new JSONObject();
            JSONObject requestObject = new JSONObject();
            try {
                requestObject.accumulate("currency", "IDR");
                requestObject.accumulate("debitAccount", Const.SOURCE_ACCOUNT);
                requestObject.accumulate("paymentAmount", phoneCreditModel.getAmount());
                requestObject.accumulate("companyAccountNumber", "9910000011230");
                jsonObject.accumulate("Request", requestObject);
                Log.d(Const.TAG, "TopUpPhoneCredit Model: " + jsonObject.toString(4));

                RequestUtils.getAPI(Const.URL_TOP_UP_THREE, Request.Method.POST, jsonObject, this, this, BaseSharedPref.getSpString(this, Const.AUTH_TOKEN) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private String getCurrentTimeStamp() {
        Log.d(Const.TAG, "getCurrentTimeStamp: " + new SimpleDateFormat("HH:mm").format(new Date()));
        return new SimpleDateFormat("HH:mm").format(new Date());
    }

    private void initializeTextToSpeech() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0){
                    Toast.makeText(MainActivity.this, "not available", Toast.LENGTH_SHORT).show();
                }
                else{
                    myTTS.setLanguage(new Locale("id","ID"));
                }
            }
        });
    }

    private void speakUp(String message) {
        if(Build.VERSION.SDK_INT >= 21){
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        initializeTextToSpeech();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTTS.shutdown();
    }

    private void initializeSpeechToText() {
        if(SpeechRecognizer.isRecognitionAvailable(this)){
            mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    imageView2.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    processInput(results);
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }


    private void initChatRequirement() {
        listChat = new ArrayList<>();
        adapter = new ChatAdapter();
        adapter.setListChat(listChat);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(LayoutUtils.createLinearLayoutManager(this, RecyclerView.VERTICAL));
        final ChatModel replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Halo Ryo! apa yang bisa dibantu hari ini?", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                speakUp(replyChat.getMessage());
            }
        },2000);

    }

    private void startListening() {



        imageView.setVisibility(View.GONE);
//        imageView2.setVisibility(View.VISIBLE);
        Log.d(Const.TAG, "startListening: ");
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        mySpeechRecognizer.startListening(intent);
    }






    private void initListening() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO},MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            startListening();
        }
    }




    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
}
