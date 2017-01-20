package ai.agusibrahim.flikr;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.*;
import android.webkit.*;
import android.graphics.*;
import android.net.*;
import android.support.v4.widget.*;
import android.support.design.widget.*;
import android.support.v7.app.*;
import org.json.*;
import android.content.*;
import android.app.Activity;
import android.os.Handler;
import android.content.SharedPreferences.*;

/*
Facebook LikeAll with XHR for Android WebView Friendly
Original Script by Agus Ibrahim

//Non-compressed version
var els = document.querySelectorAll("div[role='article']");
var liked = [];
for (var i = 0; i < els.length; i++) {
    ll = els[i].querySelectorAll("a[href^='/a/like.php']");
    if (1 == ll.length) {
        var xhr = new XMLHttpRequest();
        xhr.open("GET", ll[0].href, true);
        xhr.send(null);
        yy = els[i].getElementsByTagName("a");
        n = yy[0].innerHTML;
        liked.push(n);
    }
    if (i == els.length - 1) window.javaface.process(JSON.stringify(liked));
}
*/

public class MainActivity extends AppCompatActivity {
	Toolbar toolbar;
	SwipeRefreshLayout swipr;
	private WebView web;
	FloatingActionButton fab;

	private Handler mHandler;
	private SharedPreferences pref;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mHandler=new Handler();
		pref=getSharedPreferences("mysettings", MODE_PRIVATE);
		toolbar=(Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		web=(WebView)findViewById(R.id.web);
		fab=(FloatingActionButton) findViewById(R.id.fab);
		swipr=(SwipeRefreshLayout) findViewById(R.id.swipr);
		
		// aktifkan kemampuan javascript
		web.getSettings().setJavaScriptEnabled(true);
		// membuat fungsi javascript
		web.addJavascriptInterface(new JsFace(), "javaface");
		// set aksi saat Swipe Refresh
		swipr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
				@Override
				public void onRefresh() {
					web.reload();
				}
			});
		// atur saat fab (tombol like) di klik
		fab.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1) {
					// untuk JS non-compressed lihat diatas
					web.loadUrl("javascript:for(var els=document.querySelectorAll(\"div[role='article']\"),liked=[],i=0;i<els.length;i++){if(ll=els[i].querySelectorAll(\"a[href^='/a/like.php']\"),1==ll.length){var xhr=new XMLHttpRequest;xhr.open(\"GET\",ll[0].href,!0),xhr.send(null),yy=els[i].getElementsByTagName(\"a\"),n=yy[0].innerHTML,liked.push(n)}i==els.length-1&&window.javaface.process(JSON.stringify(liked))}");
				}
			});
		// tangani lalulintas browsing
		web.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap img) {
					// saat meload halaman, loading refresher ditampilkan
					swipr.setRefreshing(true);
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					// saat selesai meload, sembunyikan loading
					swipr.setRefreshing(false);
					web.setVisibility(View.VISIBLE);
					// atur judul app menurut judul halaman web
					MainActivity.this.setTitle(view.getTitle());
					// simpan url halaman terakhir
					SharedPreferences.Editor edit=pref.edit();
					edit.putString("lastvisited", url);
					edit.commit();
				}
			});
		// jika halaman terakhir ga ada di pengaturan (sharedPreferences), maka load url awal
		web.loadUrl(pref.getString("lastvisited", "https://mbasic.facebook.com/"));
	}

	// antarmuka java yang di-js in
	class JsFace {
		private JSONArray ja;
		@JavascriptInterface
		@SuppressWarnings("unused")
		public void process(String data) {
			try {
				ja=new JSONArray(data);
			}
			catch(JSONException e) {}
			AlertDialog.Builder dlg=new AlertDialog.Builder(MainActivity.this);
			dlg.setMessage("Total kiriman yang dilike " + ja.length() + "\n" + data);
			dlg.setPositiveButton("Reload", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2) {
						mHandler.post(new Runnable() {
								@Override
								public void run() {
									web.reload();
								}
							});
					}
				});
			dlg.show();
		}
	}
	// fungsi ketika menekan tombol kembali
	// jika browser bisa di back, maka kembali ke halaman sebelumnya
	// tapi jika ga ada halaman sebelumnya, maka tutup app
	@Override
	public void onBackPressed() {
		if(web.canGoBack()) {
			web.goBack();
		}
		else {
			super.onBackPressed();
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
