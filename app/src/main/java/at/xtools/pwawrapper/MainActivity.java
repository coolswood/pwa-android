package at.xtools.pwawrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import at.xtools.pwawrapper.ui.UIManager;
import at.xtools.pwawrapper.webview.WebViewHelper;

public class MainActivity extends AppCompatActivity {
    // Globals
    private UIManager uiManager;
    private WebViewHelper webViewHelper;
    private boolean intentHandled = false;
    ReviewManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup Theme
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
             if (!intentHandled && intentAction != null && intentAction.equals(Intent.ACTION_VIEW)){
                    Uri intentUri = i.getData();
                    String intentText = "";
                    if (intentUri != null){
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

    public class JavaScriptInterface {

        Context mContext;

        /** Instantiate the interface and set the context */
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
        public String androidLanguage() {
            return Constants.LANGUAGE;
        }
    }
}
