package at.xtools.pwawrapper;

import android.app.Application;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;

import static at.xtools.pwawrapper.Constants.WEBAPP_URL;


public class App extends Application {

    public static Config config;
    public static Notificator notificator;

    @Override
    public void onCreate() {
        super.onCreate();
        config = new Config(PreferenceManager.getDefaultSharedPreferences(this));
        notificator = new Notificator(this);
        CookieManager manager = CookieManager.getInstance();
        manager.setAcceptCookie(true);
        manager.setCookie(WEBAPP_URL, String.format("firebaseToken=%s", config.getToken()));
    }
}
