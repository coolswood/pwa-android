package at.xtools.pwawrapper;

import android.content.Intent;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static at.xtools.pwawrapper.Constants.WEBAPP_URL;

public class MessageService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        App.config.saveToken(token);
        CookieManager.getInstance().setCookie(WEBAPP_URL, String.format("firebaseToken=%s", token));
    }

    @Override
    public void handleIntent(Intent intent) {
        super.handleIntent(intent);
        App.notificator.notify(intent.getStringExtra("title"), intent.getStringExtra("text"));
    }
}
