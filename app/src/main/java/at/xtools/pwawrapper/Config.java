package at.xtools.pwawrapper;

import android.content.SharedPreferences;
import android.util.Log;

public class Config {
    private final String TAG = "firebase_token";
    private SharedPreferences preferences;
    private String token;

    public Config(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void saveToken(String token){
        this.token = token;
        preferences.edit().putString(TAG, token).apply();
    }

    public String getToken(){
        if(this.token == null) token = preferences.getString(TAG, null);
        if(this.token != null) Log.d("firebase_token", token);
        return token;
    }
}
