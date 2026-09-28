package com.sufchat.app;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
public class MainActivity extends AppCompatActivity {
private WebView webView; private static final int AUDIO_PERMISSION=1001;
@SuppressLint("SetJavaScriptEnabled") @Override protected void onCreate(Bundle b){
super.onCreate(b); webView=new WebView(this); setContentView(webView); WebSettings s=webView.getSettings();
s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setMediaPlaybackRequiresUserGesture(false); s.setAllowFileAccess(false);
webView.setWebViewClient(new WebViewClient()); webView.setWebChromeClient(new WebChromeClient(){@Override public void onPermissionRequest(final PermissionRequest r){runOnUiThread(()->{if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED)r.grant(new String[]{PermissionRequest.RESOURCE_AUDIO_CAPTURE});else ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},AUDIO_PERMISSION);});}});
if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},AUDIO_PERMISSION);
webView.loadUrl(BuildConfig.APP_URL);}
@Override public void onBackPressed(){if(webView.canGoBack())webView.goBack();else super.onBackPressed();}
}
