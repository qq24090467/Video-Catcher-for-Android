package com.mobojobo.vivideodownloader;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import org.apache.commons.io.FilenameUtils;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mobojobo.vivideodownloader.MyApp;
import com.mobojobo.vivideodownloader.models.FoundedVideo;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Created by pc on 23.03.2015.
 */
public class FrameDetectorService extends WakefulIntentService {
    String LOG = "FrameDetector";
    public FrameDetectorService() {
        super("FrameDetector");

    }

    public void postFoundedVideo(FoundedVideo video){
      /*  Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
*/      String link = video.getLink();
        String basename = FilenameUtils.getBaseName(link);
        String extension = FilenameUtils.getExtension(link);
        String filename = basename+"."+extension;
        video.setTitle(filename);
        MyApp.bus.post(video);
                Log.i("postedvideo",video.getLink());
  /*          }
        });
*/
    }

    ArrayList<FoundedVideo> html5VideoFounder(String html,String url){
        ArrayList<FoundedVideo> videos = new ArrayList<FoundedVideo>();
        Document doc = Jsoup.parse(html,"UTF-8");
        Elements elements = doc.getElementsByAttributeValue("type","video/mp4");
        for(Element el:elements){
           String link = el.attr("src");
           if(link.equals("")||link!=null){
               if(link.contains("http")||link.contains("https")){
                   String basename = FilenameUtils.getBaseName(link);
                   String extension = FilenameUtils.getExtension(link);
                   String filename = basename+"."+extension;
                   videos.add(new FoundedVideo(link,filename));
               }
           }
        }
        Elements tags = doc.getElementsByTag("video");
        for(Element el:tags){
            String link = el.attr("src");
            if(link.equals("")||link!=null){
                if(link.contains("http")||link.contains("https")) {
                    String basename = FilenameUtils.getBaseName(link);
                    String extension = FilenameUtils.getExtension(link);
                    String filename = basename + "." + extension;
                    videos.add(new FoundedVideo(link, filename));
                }
            }
        }
        Elements mailruvids = doc.getElementsByAttributeValueContaining("data-src","mail.ru");
        for(Element el:mailruvids){
            String link = el.attr("data-src");
            if(link.equals("")||link!=null){
                if(link.contains("http")||link.contains("https")) {
                    String basename = FilenameUtils.getBaseName(link);
                    String extension = FilenameUtils.getExtension(link);
                    String filename = basename + "." + extension;
                    videos.add(new FoundedVideo(link, filename));
                }
            }
        }

        return videos;
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Log.i(LOG,"starting..");
        String url = intent.getStringExtra("url");
        String html = intent.getStringExtra("html");
        Document doc = Jsoup.parse(html,"UTF-8");


        ArrayList<FoundedVideo> videos = html5VideoFounder(html,url);
        Log.i(LOG,"Found html5 "+videos.size());
        for(final FoundedVideo video:videos){
              Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                MyApp.bus.post(video);
                Log.i("postedvideo",video.getLink());

            }
        });
        }
        ArrayList<String> knownFrames = getKnownFrames(html);

        for(String s :knownFrames){

            if(s.startsWith("//http")){
               s = s.replace("//http","http");
            }

            if(s.startsWith("//")){
               if(!s.contains("http")){
                 s =  s.replaceFirst("//","http://");
               }else{
                  s= s.replaceFirst("//","");
               }
            }

            new AsyncTask<String,String,String>(){
                String url;
                @Override
                protected String doInBackground(String... params) {
                    url = params[0];
                    try {
                        String html = Jsoup.connect(params[0]).userAgent("Mozilla/5.0 (Linux; Android 4.4.2; Nexus 5 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.99 Mobile Safari/537.36").get().toString();
                        return html;
                    } catch (Exception e) {
                       // e.printStackTrace();
                        return "null";
                    }

                }

                @Override
                protected void onPostExecute(String html) {
                    super.onPostExecute(html);

                    if(!url.equals("null")){
                        postVideos(url, html);
                    }

                }
            }.execute(s);
           /* AsyncHttpClient client = new AsyncHttpClient();
            client.setUserAgent("Mozilla/5.0 (Linux; Android 4.4.2; Nexus 5 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.99 Mobile Safari/537.36");
            client.get(s, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String content = new String(responseBody);

                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
            */
        }
    }


    private void onException(Exception e,String url,String content){
        Log.i("url",url);
        Log.i("html", content);
        e.printStackTrace();


    }
    private void onError(String url) {


    }

    private void postVideos(String url,String response){

        if(url.contains("veterok.tv")){
            try{
                String s = getVeterok(response);
                postFoundedVideo(new FoundedVideo(s,"Veterok"));
            }catch (Exception e){
                onException(e,url,response);
            }

        }

        if(url.contains("videoraj.ch")){
            try{
                String s =getVideoraj(response);
                postFoundedVideo(new FoundedVideo(s,"Videoraj"));
            }catch (Exception e){
                onException(e,url,response);
            }

        }

        if(url.contains("cloudy.ec")){
            try{
                String s =getCloudyVideoUrl(response);
                postFoundedVideo(new FoundedVideo(s,"Cloudy"));

            }catch (Exception e){
                onException(e,url,response);
            }

        }

        if(url.contains("vk.com")){

            try{
                ArrayList<FoundedVideo> videos = getVkVideos(response);
                for(FoundedVideo v: videos){
                    postFoundedVideo(v);
                }
            }catch (Exception e){
                onException(e,url,response);
            }

        }

        if(url.contains("mail.ru")){

            try {
                ArrayList<FoundedVideo> videos = getMailRuVideoLinks(response);
                for(FoundedVideo v: videos){
                    postFoundedVideo(v);
                }
            } catch (JSONException e) {
                onException(e,url,response);
            }


        }

        if(url.contains("myvideo.az")){

            try {
                String s= getMyVideoazVideoUrl(response);
                postFoundedVideo(new FoundedVideo(s,"myvideo.az"));

            } catch (Exception e) {
                onException(e,url,response);
            }

        }

    }



    String getMyVideoazVideoUrl(String response) throws Exception{
        org.jsoup.nodes.Document doc = Jsoup.parse(response);
        Element  script = doc.getElementsByTag("source").get(0);
        String link =script.attr("src");
        return link;
    }

    String getCloudyVideoUrl(String response) throws  Exception{

        Document doc;
        doc = Jsoup.parse(response);

        Element script = doc.select("script").get(3);
        String mpf = script.toString();
        int a = mpf.indexOf("source src=");
        int b =mpf.indexOf("?cloudy_stream=true");

        String link= mpf.substring(a+12,b).trim();
        return link;
    }
    String getVideoraj(String response) throws  Exception{

        Document doc;
        doc = Jsoup.parse(response);

        Element script = doc.select("script").get(3);
        String mpf = script.toString();
        int a = mpf.indexOf("source src=");
        int b =mpf.indexOf("?cloudy_stream=true");

        String link= mpf.substring(a+12,b).trim();
        return link;
    }

    String getVeterok(String response) throws  Exception{

        Document doc;
        doc = Jsoup.parse(response);

        Element script = doc.select("script").get(6);
        String mpf = script.tagName("a").toString().replace("<a>","").replace("</a>","");
        int a = mpf.indexOf("]=\"");
        int b =mpf.indexOf("\";");

        String link= mpf.substring(a+3,b).trim();

        return link;
    }
    ArrayList<FoundedVideo>  getMailRuVideoLinks (String response) throws JSONException {


        final ArrayList<FoundedVideo> urls = new ArrayList<FoundedVideo>();

        JSONObject obj = new JSONObject(response);

        final JSONArray videos = obj.getJSONArray("videos");

        final CharSequence [] videos_ = new CharSequence[videos.length()];

        for (int i = 0 ;i<videos.length(); i++ ){
            try{
            urls.add(new FoundedVideo("Mail.ru "+videos.getJSONObject(i).getString("key"),videos.getJSONObject(i).getString("url").toString()));
            }catch (Exception e){
                e.printStackTrace();
            }
        }

      return urls;
    }

    ArrayList<FoundedVideo> getVkVideos(String response){


        Document doc;
        doc = Jsoup.parse(response);

        final ArrayList<FoundedVideo> urls = new ArrayList<FoundedVideo>();

        Elements videos = doc.getElementsByAttributeValue("type", "video/mp4");
        for(Element video : videos){
            String link = video.attr("src");
            if(link.contains("720.mp4")){
                urls.add(new FoundedVideo("VK 720p",video.attr("src")));
            }
            if(link.contains("VK 480.mp4")){
                urls.add(new FoundedVideo("VK 480p",video.attr("src")));
            }
            if(link.contains("360.mp4")){
                urls.add(new FoundedVideo("VK 360p",video.attr("src")));
            }
            if(link.contains("240.mp4")){
                urls.add(new FoundedVideo("VK 240p",video.attr("src")));
            }
        }


        return urls;

    }





    ArrayList<String> getKnownFrames(String html){
        ArrayList<String> frames = new ArrayList<String>();
        String mailruframe = MailRuDetector(html).trim()
                .replace(" ", "").trim();
        String vkframe = VkFrameDetector(html).trim()
                .replace(" ", "").trim();
        String myvideoazframe = myvideoAzdetector(html).trim()
                .replace(" ", "").trim();
        String odnframe =odndetector(html).trim()
                .replace(" ", "").trim();
        String cloudyframe = cloudydetector(html).trim()
                .replace(" ", "").trim();
        String videorajframe = videorajdetector(html).trim()
                .replace(" ", "").trim();
        String veterokframe = veterokdetector(html).trim()
                .replace(" ", "").trim();

        if (!mailruframe.equals("")) {
            frames.add(mailruframe);
        }
        if (!vkframe.equals("")) {
            frames.add(vkframe);
        }
        if (!myvideoazframe.equals("")) {
            frames.add(myvideoazframe);
        }
        if (!odnframe.equals("")) {
            frames.add(odnframe);
        }
        if (!cloudyframe.equals("")) {
            frames.add(cloudyframe);
        }
        if (!videorajframe.equals("")) {
            frames.add(videorajframe);
        }
        if (!veterokframe.equals("")) {
            frames.add(veterokframe);
        }


        return frames;
    }



    private static String VkFrameDetector(String HTML){

        Document doc = Jsoup.parse(HTML);
        Elements docs = doc.getElementsByAttributeValueContaining("src",
                "vk.com");



        String source = docs.attr("src");

        return source;

    }

    private static String myvideoAzdetector(String HTML){

        Document doc = Jsoup.parse(HTML);
        Elements docs = doc.getElementsByAttributeValueContaining("src",
                "myvideo.az");



        String source = docs.attr("src");

        return source;

    }
    private static String odndetector(String HTML){

        Document doc = Jsoup.parse(HTML);
        Elements docs = doc.getElementsByAttributeValueContaining("src",
                "odnoklassniki.ru");



        String source = docs.attr("src");

        return source;

    }

    private static String cloudydetector(String HTML){

        Document doc = Jsoup.parse(HTML);
        Elements docs = doc.getElementsByAttributeValueContaining("src",
                "cloudy.ec");



        String source = docs.attr("src");

        return source;

    }
    private static String videorajdetector(String HTML){

        Document doc = Jsoup.parse(HTML);
        Elements docs = doc.getElementsByAttributeValueContaining("src",
                "videoraj.ch");



        String source = docs.attr("src");

        return source;

    }

    private static String veterokdetector(String HTML){

        Document doc = Jsoup.parse(HTML);
        Elements docs = doc.getElementsByAttributeValueContaining("src",
                "veterok.tv");



        String source = docs.attr("src");

        return source;

    }
    private static String MailRuDetector(String html){
        Document doc = Jsoup.parse(html);

        Elements rudocs = doc.getElementsByAttributeValueContaining("src",
                "mail.ru");

        String source = rudocs.attr("src");

        return source;
    }

    private static String youtubeframedetector(String html) {
        Document doc;
        String link = "";
        doc = Jsoup.parse(html);
        Elements rudocs = doc.getElementsByAttributeValueContaining("src",
                "youtube");
        link = rudocs.attr("src");

        return link;
    }

}

