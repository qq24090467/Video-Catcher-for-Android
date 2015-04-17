package com.mobojobo.videodownloader;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.mobojobo.videodownloader.utils.FileDialog;


public class VideoDownloader extends Activity implements OnClickListener{
	 FileDialog fileDialog;
	 String downloadlink;
	 String file_name;
	 static  Context context;
     public static	DownloadManager downloadManager ;

    @SuppressLint("SdCardPath")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.downloadialog);

        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

        context = getApplicationContext();
		Intent i;

		i = getIntent();
		
		file_name = i.getStringExtra("filename");
		downloadlink = i.getStringExtra("downloadlink");

        file_name = file_name.replaceAll("(\\p{Punct})","");
        if(file_name.length()>20){file_name = file_name.substring(0,10);}
        if(!file_name.endsWith(".mp4")){
            file_name = file_name+".mp4".replace("mp4.mp4",".mp4");
        }
		final EditText filename = (EditText) findViewById(R.id.filename_edittext);
		final EditText filepath= (EditText) findViewById(R.id.fileway_edittext);
		ImageView selectfilepath = (ImageView) findViewById(R.id.file_path_imageview);
		Button download = (Button) findViewById(R.id.confrim);
		Button cancel = (Button) findViewById(R.id.cancel);
		
		
		filename.setText(file_name);
		filepath.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator);

		
		 File mPath = new File(Environment.getExternalStorageDirectory()+"");
         fileDialog = new FileDialog(this, mPath);
         //fileDialog.setFileEndsWith(".txt");
         fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
             public void fileSelected(File file) {
                 Log.d(getClass().getName(), "selected file " + file.toString());
             }
         });
         fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
           public void directorySelected(File directory) {
           filepath.setText(directory.toString());
           }
         });
         fileDialog.setSelectDirectoryOption(true);
         
         selectfilepath.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

		         fileDialog.showDialog();
					
			}
		});
         
         
	
        download.setOnClickListener(new OnClickListener() {
 
        	
        	@Override
			public void onClick(View v) {
			// TODO Auto-generated method stub
			 	
			   Uri Download_Uri = Uri.parse(downloadlink);
			   Request request = new Request(Download_Uri);
			    
			   //Restrict the types of networks over which this download may proceed.
			   request.setAllowedNetworkTypes(Request.NETWORK_WIFI | Request.NETWORK_MOBILE);
			   //Set whether this download may proceed over a roaming connection.
			   request.setAllowedOverRoaming(false);
			   //Set the title of this download, to be displayed in notifications (if enabled).
			   request.setTitle(filename.getText().toString());
			   //Set a description of this download, to be displayed in notifications (if enabled)
			   request.setDescription(downloadlink);
			   //Set the local destination for the downloaded file to a path within the application's external files directory
			   //request.setDestinationInExternalFilesDir(getApplicationContext(),filepath.getText().toString(),filename.getText().toString());
			   request.setDestinationUri(Uri.fromFile(new File(filepath.getText().toString()+file_name)));
			 //  request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename.getText().toString());
			   request.setNotificationVisibility(	 Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
						);
			   //Enqueue a new download and same the referenceId
			   
			   downloadManager.enqueue(request);
			   finish();
			    
			
		}
	});
	
	
	cancel.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
	});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
		finish();
	}
	
	
		
	
	
}
