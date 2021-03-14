package at.xtools.pwawrapper;

import android.content.SharedPreferences;
import android.util.Log;

public class Config {
    private final String TAG = "firebase_token";
    private final String PRODUCTS = "products";
    private final String STATUS = "status";
    private SharedPreferences preferences;
    private String token;
    private String products;
    private String status;

    public Config(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public void saveToken(String token) {
        this.token = token;
        preferences.edit().putString(TAG, token).apply();
    }

    public String getToken() {
        if (this.token == null) token = preferences.getString(TAG, null);
        if (this.token != null) Log.d("firebase_token", token);
        return token;
    }

    public void saveProducts(String products) {
        this.products = products;
        preferences.edit().putString(PRODUCTS, products).apply();
    }

    public String getProducts() {
        if (this.products == null) products = preferences.getString(PRODUCTS, null);
        if (this.products != null) Log.d("products", products);
        return products;
    }

    public void saveStatus(String status) {
        this.status = status;
        preferences.edit().putString(STATUS, status).apply();
    }


    public String getStatus() {
        if (this.status == null) status = preferences.getString(STATUS, null);
        if (this.status != null) Log.d("status", status);
        return status;
    }

}
