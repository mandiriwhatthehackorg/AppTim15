package com.myans.mandirihackathon.interfaces;

public interface Const {
    String TAG = "DEBUG_BYAN";
    String BASE_URL = "http://10.151.254.195/TA/public/";
    String AUTH_TOKEN = "TOKEN";

    String APP_ID = "5139c2b9-d36a-4a43-ad4b-6f737533854e";
    String USERNAME = "f16043f7-fc20-4b29-b200-516f66506791";
    String PASSWORD = "c41d30e3-32a4-4c51-90e6-fb3a6046327a";
    String TOKEN_URL = "https://apigateway.mandiriwhatthehack.com/rest/pub/apigateway/jwt/getJsonWebToken?app_id=5139c2b9-d36a-4a43-ad4b-6f737533854e";

    String GET_TELKOMSEL_PRODUCT = "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/bill/telkomsel";
    String GET_XL_PRODUCT = "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/bill/xl";
    String GET_THREE_PRODUCT = "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/bill/three";



    String URL_SUBSTRACT_BALANCE = "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/transfer";
    String URL_CHECK_BALANCE = "http://apigateway.mandiriwhatthehack.com/gateway/ServicingAPI/1.0/customer/casa/1111006393899/balance";
    String URL_TOP_UP_TELKOMSEL = "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/bill/telkomsel/PULSA_25000/082386130150";
    String URL_TOP_UP_XL= "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/bill/xl/PULSA_25000/082386130150";
    String URL_TOP_UP_THREE= "https://apigateway.mandiriwhatthehack.com/gateway/TrxAndPaymentAPI/1.0/bill/three/PULSA_25000/082386130150";

    String SOURCE_ACCOUNT = "1111006393899";
    String DUMMY_ACCOUNT = "1111006403954";
}
