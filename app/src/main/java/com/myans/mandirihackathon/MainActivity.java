package com.myans.mandirihackathon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.google.android.material.navigation.NavigationView;
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
import java.text.ParseException;
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

public class MainActivity extends AppCompatActivity implements BaseRequestListener, TextToSpeech.OnUtteranceCompletedListener {

    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech myTTS;
    private SpeechRecognizer mySpeechRecognizer;

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ArrayList<ChatModel> listChat;

    private ImageView andiniImage;

    boolean canListening;

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


    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle drawerToggle;

    ImageView micIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        ImageView menuButton  =findViewById(R.id.hard_menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        micIcon = findViewById(R.id.ic_mic);
        initializeTextToSpeech();
        initializeSpeechToText();
        initChatRequirement();

        andiniImage = findViewById(R.id.andini);
        handleSSLHandshake();
        RequestUtils.getToken(this, this);

        micIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(canListening){
                    canListening = false;

                    changeAndini(R.drawable.ic_listening_fix);
                    initListening();
                }
            }
        });


        Bundle extras = getIntent().getExtras();
        String newString= extras.getString("extra");

        processInput(newString);
    }

    private void initToolbar() {
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);


        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null){
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }

        drawerToggle = setupDrawerToggle();

        drawerLayout.addDrawerListener(drawerToggle);

        NavigationView navigationView = findViewById(R.id.nav_view);
        setupNavDrawer(navigationView);

        setSupportActionBar(toolbar);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home:
                drawerLayout.openDrawer(drawerLayout);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }



    private void setupNavDrawer(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_item_four:
                        break;
                    case R.id.nav_item_five:
                        logout();
                        break;
                    default:
                }


                setTitle(item.getTitle());
                drawerLayout.closeDrawers();

                return true;
            }
        });
    }

    private void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private ActionBarDrawerToggle setupDrawerToggle(){
        return new ActionBarDrawerToggle(
                this,
                drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    // Top-up pulsa
    private void askNumber(){
        adapter.removeLastItem();
        phoneCreditModel = new PhoneCreditModel();
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Sebutkan nomor telepon yang ingin anda isi", getCurrentTimeStamp());
        replyChat.setVisibleMessage(replyChat.getMessage());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);
        speakUp(replyChat.getMessage());
    }

    private void askAmount(){
        adapter.removeLastItem();
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Berapa nominal yang ingin anda isi?", getCurrentTimeStamp());
        replyChat.setVisibleMessage(replyChat.getMessage());

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
                {
                    listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Mohon maaf, provider tidak ditemukan.", ""));

                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(listChat.size()-1);

                    currentState = STATE_NEW;
                    speakUp("Mohon maaf, provider tidak ditemukan.");
                }
            }
        },7000);

    }

    private void finishTopUpTransaction(){
        ChatModel replyChat;
        replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Transaksi berhasil, silahkan cek pulsa anda", getCurrentTimeStamp());
        listChat.add(replyChat);
        adapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(listChat.size()-1);

        currentState = STATE_NEW;
        speakUp(replyChat.getMessage());

    }

    private void processInput(String text) {
        final String input= text.toLowerCase();

        if(input.contains("ulangi transaksi")){
            listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
            listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
            recyclerView.smoothScrollToPosition(listChat.size()-1);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    adapter.removeLastItem();
                    ChatModel replyChat;
                    replyChat = new ChatModel(ChatModel.SYSTEM_INPUT, "Baiklah, mari kita ulangi, ada yang bisa saya bantu?", getCurrentTimeStamp());
                    listChat.add(replyChat);
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(listChat.size()-1);

//                    andiniImage.setImageResource(R.drawable.ic_listening_fix);
                    speakUp(replyChat.getMessage());

                    currentState = STATE_NEW;

                }
            }, 2000);

            return;
        }

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

                listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
                listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        adapter.removeLastItem();
                        listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Maaf saya tidak mengerti", getCurrentTimeStamp()));
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(listChat.size()-1);
                        speakUp("Maaf saya tidak mengerti");
                    }
                }, 2000);

            }
        }
        else if(currentState == TOP_UP_STATE_ASK_NUMBER){
            String phoneNumber = input.replaceAll("\\s+","");
            phoneNumber = phoneNumber.replaceAll("[-]","");
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

            String[] amountText = input.split(" ");
            String amount = "";
            for(int i=0; i<amountText.length; i++){
                String temp = amountText[i];
                if(temp.contains("rp")){
                    temp = temp.substring(2);
                    temp = temp.replaceAll("[.]","");
                    if (temp.matches("[0-9]+") && temp.length() > 2) {
                        amount = temp;
                        break;
                    }
                }
                else{
                    temp = temp.replaceAll("[.]","");
                    if (temp.matches("[0-9]+") && temp.length() > 2) {
                        amount = temp;
                        break;
                    }
                }

            }

            if(amount.isEmpty()){
                listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
                listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 1000ms
                        adapter.removeLastItem();
                        listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Mohon sebutkan kembali nominal dengan benar", "00"));
                        recyclerView.smoothScrollToPosition(listChat.size()-1);
                        adapter.notifyDataSetChanged();
                        speakUp("Mohon sebutkan kembali nominal dengan benar");
                        return;
                    }
                }, 2000);
                return;
            }
            Log.d(Const.TAG, "processInput amount 1: " + amount);


//            amount = amount.replaceAll("[.]","");
//            if(amount.contains("rp"))
//                amount = amount.substring(2);
            String visibleMessage = input;
//            if(input.contains("rp"))
//                visibleMessage = input.substring(2);
            phoneCreditModel.setAmount(amount);

            ChatModel chatModel = new ChatModel(ChatModel.USER_INPUT, amount, getCurrentTimeStamp());
            chatModel.setVisibleMessage(visibleMessage);
            listChat.add(chatModel);

            listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
            recyclerView.smoothScrollToPosition(listChat.size()-1);
            adapter.notifyDataSetChanged();

            askConfirmation();


        } else if(currentState == TOP_UP_STATE_ASK_CONFIRMATION){
            if(input.contains("benar")){
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
            }
            else if(input.contains("bukan") || input.contains("tidak") || input.contains("salah")){
                listChat.add(new ChatModel(ChatModel.USER_INPUT, input, getCurrentTimeStamp()));
                listChat.add(new ChatModel(ChatModel.SYSTEM_TYPING_INPUT, "Typing . . . ", "00"));
                recyclerView.smoothScrollToPosition(listChat.size()-1);
                adapter.notifyDataSetChanged();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 1000ms
                        adapter.removeLastItem();
                        listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Baiklah, transaksi dibatalkan", getCurrentTimeStamp()));
                        adapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(listChat.size()-1);
                        currentState = STATE_NEW;
                        speakUp("Baiklah, transaksi dibatalkan");

                    }
                }, 2000);
            }



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
        myTTS.setOnUtteranceCompletedListener(this);
//        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
//            @Override
//            public void onStart(String utteranceId) {
//
//            }
//
//            @Override
//            public void onDone(String utteranceId) {
//
//                Log.d(Const.TAG, "Selesai ngomong");
//                canListening = true;
//            }
//
//            @Override
//            public void onError(String utteranceId) {
//
//            }
//        });
    }

    private void speakUp(String message) {
        canListening = false;

        changeAndini(R.drawable.ic_talking_fix);
//        andiniImage.setImageResource(R.drawable.ic_talking_fix);

        if(Build.VERSION.SDK_INT >= 21){
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null, "asdasd");
        }else{
            myTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        canListening = true;
        initializeTextToSpeech();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myTTS.shutdown();
    }

    private void initializeSpeechToText() {
        canListening = true;
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
                    Log.d(Const.TAG, "onError: " + error);
                    micIcon.setImageResource(R.drawable.ic_icon_mic_svg);
                    canListening = true;
                    listChat.add(new ChatModel(ChatModel.SYSTEM_INPUT, "Maaf apakah bisa diulangi? saya kurang mendengarkan", ""));
                    adapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(listChat.size()-1);
                    speakUp("Maaf apakah bisa diulangi? saya kurang mendengarkan");
                }

                @Override
                public void onResults(Bundle bundle) {
                    List<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    changeAndini(R.drawable.ic_thinking_fix);
//                    andiniImage.setImageResource(R.drawable.ic_thinking_fix);
//                    Glide.with(MainActivity.this).load(R.drawable.ic_listening1).into(andiniImage);

                    micIcon.setImageResource(R.drawable.ic_icon_mic_svg);
                    processInput(results.get(0));
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
    }

    private void startListening() {
        Log.d(Const.TAG, "startListening: ");

        if (micIcon.getDrawable() instanceof GifDrawable) {
            ((GifDrawable)micIcon.getDrawable()).stop();
            ((GifDrawable)micIcon.getDrawable()).startFromFirstFrame();
        }
        Glide.with(this).load(R.drawable.loading).into(micIcon);Drawable drawable = micIcon.getDrawable();
        if (drawable instanceof GifDrawable) {

//            Animatable gif = (Animatable)drawable;
            GifDrawable gif = (GifDrawable)drawable;
            if (gif.isRunning()){
                gif.stop();
                gif.start();

            }
        }
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

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        canListening = true;
        Log.d(Const.TAG, "Selesai ngomong");
        changeAndini(R.drawable.ic_talking_fix);
    }

    private void changeAndini(int resource){
        andiniImage =findViewById(R.id.andini);
        andiniImage.setImageResource(resource);
    }
}
