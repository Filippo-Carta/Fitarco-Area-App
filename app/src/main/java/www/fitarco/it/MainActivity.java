package www.fitarco.it;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webview1;
    private static final String LAST_VISITED_URL_KEY = "last_visited_url";
    private static final String INITIAL_URL = "https://www.fitarco.it/area-riservata.html";

    private ValueCallback<Uri[]> mUploadMessage;
    private ActivityResultLauncher<Intent> fileChooserLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fileChooserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (mUploadMessage == null) return;
                        Uri[] results = null;
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            if (result.getData() != null) {
                                results = new Uri[]{result.getData().getData()};
                            }
                        }
                        mUploadMessage.onReceiveValue(results);
                        mUploadMessage = null;
                    }
                });

        webview1 = findViewById(R.id.webview1);
        WebSettings webSettings = webview1.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowFileAccess(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webview1, true);

 
        webview1.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                    mUploadMessage = null;
                }

                mUploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    fileChooserLauncher.launch(intent);
                } catch (Exception e) {
                    mUploadMessage = null;
                    Toast.makeText(MainActivity.this, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });


        webview1.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(LAST_VISITED_URL_KEY, url);
                editor.apply();

                if (url.contains("index.php?Matricola")) {
                    String pdfUrl = url.replace("index.php?", "index.php?SaveFile&");

                    String cookies = CookieManager.getInstance().getCookie(pdfUrl);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(pdfUrl));
                    request.addRequestHeader("Cookie", cookies);
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                    
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FitarcoPass.pdf");

                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);

                    Toast.makeText(MainActivity.this, "Download FitarcoPass.pdf started...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    
        webview1.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            String cookies = CookieManager.getInstance().getCookie(url);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.addRequestHeader("Cookie", cookies);
            request.addRequestHeader("User-Agent", userAgent);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "FitarcoPass.pdf");
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Download started...", Toast.LENGTH_SHORT).show();
        });

   
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String lastUrl = prefs.getString(LAST_VISITED_URL_KEY, INITIAL_URL);
        webview1.loadUrl(lastUrl);
    }


    @Override
    public void onBackPressed() {
        if (webview1.canGoBack()) {
            webview1.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        CookieManager.getInstance().flush();
    }
}
