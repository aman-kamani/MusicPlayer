package com.adproduction.admusicplayer.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adproduction.admusicplayer.GlobalFunction;
import com.adproduction.admusicplayer.MainActivity;
import com.adproduction.admusicplayer.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class Queue_Song_Adapter extends RecyclerView.Adapter<Queue_Song_Adapter.Queue_ViewHolder> {

    GlobalFunction gf = new GlobalFunction();
    private Context context;
    private List<String> data;

    public Queue_Song_Adapter(Context con)
    {
        context = con;
        data = gf.getQueue_List();
    }

    @NonNull
    @Override
    public Queue_Song_Adapter.Queue_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View v = inflater.inflate(R.layout.layout_queue_single_music,viewGroup, false);

        return new Queue_ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Queue_Song_Adapter.Queue_ViewHolder viewHolder, int i)
    {
        viewHolder.q_song_nm.setText(getSongInfo(data.get(i)));
        viewHolder.q_song_id.setText(String.valueOf(data.get(i)));
        viewHolder.q_song_position.setText(String.valueOf(i));

        if(gf.getCurrent_Song_Id().equals(viewHolder.q_song_id.getText().toString()))
        {
            viewHolder.q_song_play.setVisibility(View.VISIBLE);
        }
        else
        {
            viewHolder.q_song_play.setVisibility(View.INVISIBLE);
        }
    }

    private String getSongInfo(String songid)
    {
        ContentResolver cr = context.getContentResolver();

        Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Audio.Media.TITLE}, MediaStore.Audio.Media._ID+"=?",new String[]{songid},null);

        cur.moveToNext();
        String title = cur.getString(0);
        cur.close();

        return title;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class Queue_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView q_song_id,q_song_nm,q_song_option_menu,q_song_position;
        ImageView q_song_play;

        public Queue_ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            q_song_id = (TextView)itemView.findViewById(R.id.queue_single_song_id);
            q_song_nm = (TextView)itemView.findViewById(R.id.queue_single_song_nm);
            q_song_option_menu = (TextView)itemView.findViewById(R.id.queue_single_song_menu);
            q_song_position = (TextView)itemView.findViewById(R.id.queue_single_song_position);
            q_song_play = (ImageView)itemView.findViewById(R.id.queue_single_song_play);
        }
    }
}
