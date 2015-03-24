package com.mobojobo.vivideodownloader.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobojobo.vivideodownloader.R;
import com.mobojobo.vivideodownloader.models.FoundedVideo;

import java.util.ArrayList;

/**
 * Created by pc on 23.03.2015.
 */
public class DownloadAdapter extends BaseAdapter {

    Context context;
    ArrayList<FoundedVideo> videos = new ArrayList<FoundedVideo>();
    protected LayoutInflater inflater;

    public DownloadAdapter(Context context){
         this.context=context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

    public void addItem(FoundedVideo video){
        videos.add(video);
        notifyDataSetChanged();
        Log.i("DownloadAdapter",videos.size()+"");
    }

    public void refresh(){
      /*videos.clear();
      notifyDataSetChanged();*/
    }
    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    public FoundedVideo getItem(int position) {
        return videos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {


        protected TextView name;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.founded_row,null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.video_found);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(videos.get(position).getTitle());

        return convertView;

    }
}
