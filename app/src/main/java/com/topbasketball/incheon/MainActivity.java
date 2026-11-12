package com.topbasketball.incheon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String WEB_APP_URL = "https://script.google.com/macros/s/AKfycbw6Jz6lhCtzz_25Lnr3tiQX8THMqUy1wVzFAIlEtCGgH0ak7AsRlv2YhAz0Y_MZRgbLhw/exec";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 상단 상태바 및 배경 다크 테마(#090d16) 적용
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor("#090d16"));
        window.setNavigationBarColor(Color.parseColor("#030712"));

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#090d16"));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 모바일 크롬 User-Agent 설정 (구글 로그인 및 Apps Script 호환성 보장)
        String defaultUA = settings.getUserAgentString();
        settings.setUserAgentString(defaultUA.replace("; wv", ""));

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String url = uri.toString();

                // 카카오톡 실행 링크(kakaotalk:// 또는 open.kakao.com)는 외부 카카오톡 앱으로 즉시 연결
                if (url.startsWith("kakaotalk://") || url.startsWith("intent://") || url.contains("open.kakao.com")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {
                        try {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(marketIntent);
                        } catch (Exception ignored) {}
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 구글 Apps Script 상단 회색 경고 배너가 있을 경우 자동 숨김
                view.evaluateJavascript(
                    "(function(){" +
                    "  var b = document.getElementById('warning-bar-table') || document.querySelector('.warning-bar');" +
                    "  if(b) b.style.display='none';" +
                    "})();", null
                );
            }
        });

        // 웹앱 로드
        webView.loadUrl(WEB_APP_URL);

        // 휴대폰 뒤로가기 키를 눌러도 앱이 종료되지 않고 웹앱 내부 뒤로가기(초기화면 복귀 -> 저장 후 새로고침)가 작동하도록 연결
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    webView.evaluateJavascript(
                        "if(typeof window.dispatchEvent === 'function') { window.dispatchEvent(new PopStateEvent('popstate')); }",
                        null
                    );
                }
            }
        });
    }
}
