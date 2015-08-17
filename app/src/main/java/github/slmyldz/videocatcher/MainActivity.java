package github.slmyldz.videocatcher;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.IconTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import github.slmyldz.videocatcher.models.WebData;
import github.slmyldz.videocatcher.adapters.DownloadAdapter;
import github.slmyldz.videocatcher.models.FoundedVideo;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends ActionBarActivity {
    static WebView webview;
    static IconTextView home,refresh;//,down;
    static EditText url_edittext;
    static TextView count_text;
    static LinearLayout next_back_layout;
    static ListView videoslistView;
    static DownloadAdapter adapter;
    private boolean doubleBackToExitPressedOnce=false;
    static SharedPreferences sharedPreferences;
    static FrameDetectorService detectorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyApp.bus.register(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(github.slmyldz.videocatcher.R.layout.url_toolbar);
        View urltab_view = getSupportActionBar().getCustomView();
        url_edittext = (EditText) urltab_view.findViewById(github.slmyldz.videocatcher.R.id.filename_edittext);
        home = (IconTextView) urltab_view.findViewById(github.slmyldz.videocatcher.R.id.home_button);
        refresh = (IconTextView) urltab_view.findViewById(github.slmyldz.videocatcher.R.id. reload_button);

        count_text = (TextView)  urltab_view.findViewById(github.slmyldz.videocatcher.R.id.video_count);
        next_back_layout= (LinearLayout) urltab_view.findViewById(github.slmyldz.videocatcher.R.id.next_back_layout);
        Drawable background =  new IconDrawable(this, Iconify.IconValue.fa_download)
                .colorRes(github.slmyldz.videocatcher.R.color.text_color)
                .actionBarSize();
        next_back_layout.setBackground(background);

        next_back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDownload();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                refresh();

            }
        });
        count_text.setText("0");


        setContentView(github.slmyldz.videocatcher.R.layout.activity_main);

        videoslistView = (ListView) findViewById(github.slmyldz.videocatcher.R.id.videoslist);
        videoslistView.setVisibility(View.INVISIBLE);
        adapter = new DownloadAdapter(this);
        videoslistView.setAdapter(adapter);
        videoslistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FoundedVideo v = adapter.getItem(position);
                Intent i = new Intent(MainActivity.this,VideoDownloader.class);
                i.putExtra("filename",v.getTitle());
                i.putExtra("downloadlink",v.getLink());
                i.putExtra("title",webview.getTitle());
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(github.slmyldz.videocatcher.R.id.container, new PlaceholderFragment())
                    .commit();
        }




        url_edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                String url = url_edittext.getText().toString();
                try {
                    URL link = new URL(url);
                    url = link.toString();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    url = "https://www.google.com/search?q=" + url;


                }



                webview.loadUrl(url);


                return true;
            }

            private boolean isLink(String url) {
                if (url.endsWith(".com") || url.endsWith(".net") || url.endsWith(".com.tr") || url.endsWith(".us") || url.endsWith(".net")) {
                    return true;
                } else {
                    return false;
                }

            }
        });

        home.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                webview.loadUrl("http://www.google.com");

            }
        });




        detectorService= new FrameDetectorService(this);



    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adapter.refresh();
    }

    @Override
    public void onBackPressed() {
        if(webview!=null)
            if(webview.canGoBack()){
                webview.goBack();
            }else{

                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this,"Press back once more to exit.",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 2000);

            }
    }

    public static void refresh(){
        String url = webview.getUrl();
        webview.loadUrl(url);
    }

    public void onClickDownload(){

        if(View.INVISIBLE == videoslistView.getVisibility()){
            videoslistView.setVisibility(View.VISIBLE);
            Log.i("MainActivity","VISIBLE");

        }else
        if(View.VISIBLE == videoslistView.getVisibility()){
            videoslistView.setVisibility(View.INVISIBLE);
            Log.i("MainActivity","INVISIBLE");
        }

    }

    public static void back() {
        if(webview!=null)
            if(webview.canGoBack()){
                webview.goBack();
            }
    }

    public static void next() {
        if(webview!=null)
            if(webview.canGoForward()){
                webview.goForward();
            }
    }


    @Subscribe
    public static void onFoundNewVideo(FoundedVideo video){
        String t = count_text.getText().toString();
        int i = Integer.parseInt(t);
        i=i+1;
        count_text.setText(i+"");
        adapter.addItem(video);
    }

    public static void resetVideoCount(){
        count_text.setText("0");
        adapter.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(github.slmyldz.videocatcher.R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == github.slmyldz.videocatcher.R.id.action_report) {
            report(webview.getUrl());
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    public void report(String url){
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{"smhyldz51@gmail.com"});
        email.putExtra(Intent.EXTRA_SUBJECT, "ViDown App");
        email.putExtra(Intent.EXTRA_TEXT, "ViDown app don't catch videos on this url => "+url);
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        String now_url;
        public PlaceholderFragment() {
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(github.slmyldz.videocatcher.R.layout.fragment_main, container, false);
            webview = (WebView) rootView.findViewById(github.slmyldz.videocatcher.R.id.webView1);
            String lastlink;
            if(!sharedPreferences.contains("last")){
                lastlink="http://www.google.com";
            }else{
                lastlink =sharedPreferences.getString("last", url_edittext.getText().toString());

            }

            webview.getSettings().setJavaScriptEnabled(true);
            webview.addJavascriptInterface(new MyJavaScriptInterface(getActivity().getBaseContext()), "HtmlViewer");

            webview.setWebViewClient(new WebViewClient(){


                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    resetVideoCount();
                    url_edittext.setText(url);


                }

                @Override
                public void onLoadResource(WebView view, String url) {
                    super.onLoadResource(view, url);
                }


                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    now_url = url;
                    webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                    sharedPreferences.edit().putString("last", url).commit();
                    Handler handler = new Handler(Looper.getMainLooper());

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            StringBuilder sb = new StringBuilder();
                            sb.append("var oytsa = jwplayer().getPlaylist();");
                            sb.append("var assfg = JSON.stringify(oytsa);");
                            sb.append("HtmlViewer.getSources(assfg,document.title);");
                            webview.loadUrl("javascript:" + sb.toString());

                            StringBuilder sbframe = new StringBuilder();
                            sbframe.append("for( i=0;i<frames.length;i++) {");
                            sbframe.append("try {");
                            sbframe.append("   var playlist = frames[i].window.jwplayer().getPlaylist();");
                            sbframe.append("   var ytruza = JSON.stringify(playlist);");
                            sbframe.append("HtmlViewer.getSources(ytruza,document.title);");
                            sbframe.append("}catch(err){}}");
                            webview.loadUrl("javascript:" + sbframe.toString());
                        }

                    }, 100  );

                }

            });

            webview.setWebChromeClient(new WebChromeClient());


            webview.loadUrl(lastlink);


            return rootView;
        }



        class MyJavaScriptInterface {

            private Context ctx;

            MyJavaScriptInterface(Context ctx) {
                this.ctx = ctx;
            }

            @JavascriptInterface
            public void getSources(String html,String title)
            {
                //Toast.makeText(context,html,Toast.LENGTH_SHORT).show();
                Log.i("javascript",html);
                try {
                    JSONArray jsonArray = new JSONArray(html);
                    for(int i =0;i<jsonArray.length();i++){
                        JSONObject obj =jsonArray.getJSONObject(i);
                        String file = jsonArray.getJSONObject(i).getString("file");
                        String name;
                        try{

                            name = title.substring(0,10)+" "+obj.getString("label")+".mp4";
                        }catch (Exception e){
                            try{
                                name = title.substring(0,10)+".mp4";
                            }catch (Exception e2){
                                name = title+".mp4";
                            }
                        }
                            final FoundedVideo foundedVideo = new FoundedVideo(file,name);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onFoundNewVideo(foundedVideo);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @JavascriptInterface
            public void showHTML(final String html) {
                //new AlertDialog.Builder(ctx).setTitle("HTML").setMessage(html)
                  //      .setPositiveButton(android.R.string.ok, null).setCancelable(false).create().show();
                new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getActivity(),FrameDetectorService.class);
                        i.putExtra("html",html);
//                        i.putExtra("url",webview.getUrl());
                         //MyApp.bus.post(webData);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                WebData webData= new WebData();
                                webData.setHtml(html);
                                webData.setUrl(now_url);
                                detectorService.startDetector(webData);

                            }
                        });}
                }.run();

               // Log.i("html",html);

            }

        }
    }

}
