package at.xtools.pwawrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.xtools.pwawrapper.ui.UIManager;
import at.xtools.pwawrapper.webview.WebViewHelper;

import static at.xtools.pwawrapper.Constants.WEBAPP_URL;
import static com.android.billingclient.api.BillingClient.SkuType.INAPP;

public class MainActivity extends AppCompatActivity implements PurchasesUpdatedListener {

    // Globals
    private UIManager uiManager;
    private WebViewHelper webViewHelper;
    private boolean intentHandled = false;
    ReviewManager manager;
    private final String languageName = Locale.getDefault().getLanguage();

    public static final String PREF_FILE = "MyPref";
    public static final String PURCHASE_KEY = "Пожертвование";
    public static final String PURCHASE_KEY_2 = "Пожертвование";
    public static final String PRODUCT_ID = "1";
    public static final String PRODUCT_ID_2 = "2";
    public static final String PRODUCT_ID_3 = "3";
    public static final String PRODUCT_ID_4 = "4";
    public static final String PRODUCT_ID_5 = "5";
    public static final String PRODUCT_ID_6 = "6";
    private BillingClient billingClient;
    private boolean payment_status=false;
    private List<String> skuList;
    private CookieManager cookieManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup Theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        skuList = new ArrayList<>();
        skuList.add(PRODUCT_ID);
        skuList.add(PRODUCT_ID_2);
        skuList.add(PRODUCT_ID_3);
        skuList.add(PRODUCT_ID_4);
        skuList.add(PRODUCT_ID_5);
        skuList.add(PRODUCT_ID_6);

         cookieManager=CookieManager.getInstance();



        // Establish connection to billing client
        //check purchase status from google play store cache
        //to check if item already Purchased previously or refunded
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases().setListener(this).build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(INAPP);
                    List<Purchase> queryPurchases = queryPurchase.getPurchasesList();

                    if (queryPurchases != null && queryPurchases.size() > 0) {


                        handlePurchases(queryPurchases);
                    }
                    //if purchase list is empty that means item is not purchased
                    //Or purchase is refunded or canceled
                    else {

                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
            }
        });



        mockPurchase();


        // Setup Helpers
        uiManager = new UIManager(this);
        webViewHelper = new WebViewHelper(this, uiManager);

        // Setup App
        webViewHelper.setupWebView();
        uiManager.changeRecentAppsIcon();
        webViewHelper.webView.addJavascriptInterface(new JavaScriptInterface(this), "androidInterface");

        // Check for Intents
        try {
            Intent i = getIntent();
            String intentAction = i.getAction();
            // Handle URLs opened in Browser
            if (!intentHandled && intentAction != null && intentAction.equals(Intent.ACTION_VIEW)) {
                Uri intentUri = i.getData();
                String intentText = "";
                if (intentUri != null) {
                    intentText = intentUri.toString();
                }
                // Load up the URL specified in the Intent
                if (!intentText.equals("")) {
                    intentHandled = true;
                    webViewHelper.loadIntentUrl(intentText);
                }
            } else {
                // Load up the Web App
                webViewHelper.loadHome();
            }
        } catch (Exception e) {
            // Load up the Web App
            webViewHelper.loadHome();
        }
    }





    private void initiatePurchase(String id) {

        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);


        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {


                                for(SkuDetails skuDetails:skuDetailsList){
                                    if(skuDetails.getSku().equalsIgnoreCase(id)){
                                        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                                                .setSkuDetails(skuDetails)
                                                .build();
                                        billingClient.launchBillingFlow(MainActivity.this, flowParams);
                                    }
                                }
                                JSONArray jsonArray = new JSONArray();
                                try {
                                    for(SkuDetails skuDetails:skuDetailsList){
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("product_id", skuDetails.getSku());
                                        jsonObject.put("amount", skuDetails.getPrice());
                                        jsonObject.put("currency", skuDetails.getPriceCurrencyCode());
                                        jsonArray.put(jsonObject);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                App.config.saveProducts(jsonArray.toString());
                                cookieManager.setCookie(WEBAPP_URL, String.format("products=%s", App.config.getProducts()));


                            } else {
                                //try to add item/product id "purchase" inside managed product in google play console
                                Toast.makeText(getApplicationContext(), "Purchase Item not Found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    @Override
    protected void onPause() {
        webViewHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        webViewHelper.onResume();
        // retrieve content from cache primarily if not connected
        webViewHelper.forceCacheIfOffline();
        super.onResume();
    }

    // Handle back-press in browser
    @Override
    public void onBackPressed() {
        if (!webViewHelper.goBack()) {
            super.onBackPressed();
        }
    }


    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {

        //if item newly purchased
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases);
        }
        //if item already purchased then check and reflect changes
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(INAPP);
            List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
            if (alreadyPurchases != null) {
                handlePurchases(alreadyPurchases);
            }
        }
        //if purchase cancelled
        else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            payment_status=false;
            App.config.saveStatus("false");
            cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
        }
        // Handle any other error msgs
        else {
            payment_status=false;
            App.config.saveStatus("false");
            cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
            Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    void handlePurchases(List<Purchase> purchases) {
        for (Purchase purchase : purchases) {
            for(String product_id:skuList){
                //if item is purchased
                if (product_id.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                        // Invalid purchase
                        // show error to user
                        payment_status=false;
                        App.config.saveStatus("false");
                        cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
                        return;
                    }
                    // else purchase is valid
                    //if item is purchased and not acknowledged
                    if (!purchase.isAcknowledged()) {
                        AcknowledgePurchaseParams acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.getPurchaseToken())
                                        .build();
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
                        payment_status=false;
                        App.config.saveStatus("false");
                        cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
                    }
                    //else item is purchased and also acknowledged
                    else {
                        // Grant entitlement to the user on item purchase
                        // restart activity
                        payment_status=true;
                        App.config.saveStatus("true");
                        cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
                    }
                }
                //if purchase is pending
                else if (product_id.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
                    payment_status=false;
                    App.config.saveStatus("false");
                    cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
                }
                //if purchase is unknown
                else if (product_id.equals(purchase.getSku()) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE) {
                  payment_status=false;
                    App.config.saveStatus("false");
                    cookieManager.setCookie(WEBAPP_URL, String.format("paymentStatus=%s", App.config.getStatus()));
                }

            }


        }
    }

    AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                //if purchase is acknowledged
                // Grant entitlement to the user. and restart activity
            }
        }
    };

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            // To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
            String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjI87IplhBWz0r9K/MxBN+kFUvqypga7ZXDb4cAxbQPUMBQQsEJ2tvtjvWKFa+HaxBWFbzCUs1QFzOWpKSHPS4UBUHJJoUoRqjYtjfhXOgIH8W/SFSWqhdqbIvaCKjO+2VjwIrSNL/prX6FJwf6gDy5AxIEJFVy13qtNMbG9+qMyWX+zzT5/qI9grnwdBgxVcb4d8hoD8+jBnCmb4zVrYB5IJL+UFF43SR7GDX9eYYfUS4qGeggqNdwLrz1ju6ItCuS5gu+n7uetfFr4Eof9wvb1SH6yeGiwr8qGAK2hLnP4KguYcloRA/Vg5ZN0C6kQS3Zry64rjR6GrNbtXgbgSwwIDAQAB";
            return at.xtools.pwawrapper.Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            return false;
        }
    }


    public class JavaScriptInterface {

        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showRatePopup() {

            manager = ReviewManagerFactory.create(mContext);

            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    Task<Void> flow = manager.launchReviewFlow((Activity) mContext, reviewInfo);
                    flow.addOnCompleteListener(res -> {
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.


                    });
                }
            });

        }

        @JavascriptInterface
        public int androidReleaseVersion() {
            return Constants.VERSION;
        }

        @JavascriptInterface
        public String language() {
            return languageName;
        }

        @JavascriptInterface
        public String getPaymentStatus(){
            return App.config.getStatus();
        }

        //initiate purchase on button click
        @JavascriptInterface
        public void purchase(String id) {
            //check if service is already connected
            if (billingClient.isReady()) {
                initiatePurchase(id);
            }
            //else reconnect service
            else {
                billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(MainActivity.this::onPurchasesUpdated).build();
                billingClient.startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(BillingResult billingResult) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            initiatePurchase(id);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                    }
                });
            }
        }
    }


    private void mockPurchase(){

        //check if service is already connected
        if (billingClient.isReady()) {
            billingClient();
        }
        //else reconnect service
        else {
            billingClient = BillingClient.newBuilder(MainActivity.this).enablePendingPurchases().setListener(MainActivity.this::onPurchasesUpdated).build();
            billingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        billingClient();
                    } else {

                    }
                }

                @Override
                public void onBillingServiceDisconnected() {
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }


    private void billingClient() {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            if (skuDetailsList != null && skuDetailsList.size() > 0) {
                                JSONArray jsonArray = new JSONArray();
                                try {
                                    for(SkuDetails skuDetails:skuDetailsList){
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("product_id", skuDetails.getSku());
                                        jsonObject.put("amount", skuDetails.getPrice());
                                        jsonObject.put("currency", skuDetails.getPriceCurrencyCode());
                                        jsonArray.put(jsonObject);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                App.config.saveProducts(jsonArray.toString());
                                cookieManager.setCookie(WEBAPP_URL, String.format("products=%s", App.config.getProducts()));


                            } else {
                                //try to add item/product id "purchase" inside managed product in google play console

                            }
                        } else {

                        }
                    }
                });


    }


}
