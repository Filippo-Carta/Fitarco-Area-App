package www.fitarco.it;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private WebView webview1;
    private static final String LAST_VISITED_URL_KEY = "last_visited_url";
    private static final String INITIAL_URL = "https://www.fitarco.it/area-riservata.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        webview1 = findViewById(R.id.webview1);
        WebSettings webSettings = webview1.getSettings();

        // --- Settings to persist login ---

        // 1. Enable JavaScript
        webSettings.setJavaScriptEnabled(true);

        // 2. Configure CookieManager to accept cookies from all domains
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webview1, true); // Crucial for cross-domain redirects

        // 3. Enable DOM Storage and other necessary features
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // --- End of settings ---

        // Set a custom WebViewClient to track URL changes
        webview1.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Save the current URL after the page has finished loading
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(LAST_VISITED_URL_KEY, url);
                editor.apply();
            }
        });

        // Load the last visited URL, or the initial login page if none is saved
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String lastUrl = prefs.getString(LAST_VISITED_URL_KEY, INITIAL_URL);
        webview1.loadUrl(lastUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Flush cookies to storage when the activity is paused. This is the most reliable place.
        CookieManager.getInstance().flush();
    }
}
