package com.adproduction.admusicplayer.Adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adproduction.admusicplayer.GlobalFunction;
import com.adproduction.admusicplayer.MainActivity;
import com.adproduction.admusicplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

public class All_Song_Adapter extends RecyclerView.Adapter<All_Song_Adapter.Song_ViewHolder>
{
    Context context;
    List<String> id_data;
    String title,duration,album_art_path;

    GlobalFunction gf = new GlobalFunction();

    public All_Song_Adapter(Context con, List<String> data)
    {
        this.context = con;
        id_data = data;
    }

    @NonNull
    @Override
    public Song_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.layout_music_list,parent,false);
        gf.setSong_List(id_data);
        return new Song_ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final Song_ViewHolder holder, final int position)
    {
        String[] temp_det = GetSong(id_data.get(position));

        holder.song_id.setText(id_data.get(position));
        holder.song_nm.setText(temp_det[0]);
        holder.song_duration.setText(temp_det[1]);


        if(gf.getCurrent_Song_Id() != null)
        {
            //The following if condition is for checking that the song is playing and its music bar visibility
            if (gf.getCurrent_Song_Id().equals(holder.song_id.getText().toString()))
            {
                holder.song_album.setVisibility(View.GONE);
                holder.song_bars.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.song_album.setVisibility(View.VISIBLE);
                holder.song_bars.setVisibility(View.GONE);
            }
        }

        holder.song_layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                Song_Click(position, holder);
            }
        });

        holder.song_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context wrapper = new ContextThemeWrapper(context,R.style.JB_PopupMenu);
                android.support.v7.widget.PopupMenu popupMenu = new android.support.v7.widget.PopupMenu(wrapper,v);
                popupMenu.inflate(R.menu.song_popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return PopUpMenu_Click(menuItem,holder.song_id.getText().toString());
                    }
                });
                popupMenu.show();
            }
        });
    }

    private boolean PopUpMenu_Click(MenuItem item,String song_id)
    {
        if(item.getItemId() == R.id.p_menu_playNext || item.getItemId() == R.id.p_menu_addToQueue)
        {
            if(gf.getQueue_List() == null)
            {
                TextView tv_curr_song = (TextView)((MainActivity)context).findViewById(R.id.tv_current_song);
                HashMap<String,String> temp_data = gf.getSongInformation(context,song_id);
                tv_curr_song.setText(temp_data.get("TITLE"));
                notifyDataSetChanged();
                enable_ActionControl();
            }
        }

        switch(item.getItemId())
        {
            case R.id.p_menu_addToQueue:
                gf.AddToQueue(context,song_id,"add");
                return true;

            case R.id.p_menu_playNext:
                gf.AddToQueue(context,song_id,"next");
                return true;
            default:
                return false;
        }
    }

    private void Song_Click(int position, @NonNull Song_ViewHolder holder)
    {
        GlobalFunction.setCurrent_Song_Position(position);
        notifyDataSetChanged();

        if(gf.getQueue_List() == null)
        {
            gf.setQueue_List(gf.getSong_List());
        }

        TextView curr_TextView = (TextView)((MainActivity)context).findViewById(R.id.tv_current_song);
        curr_TextView.setText(holder.song_nm.getText());
        curr_TextView.setTag("hassong");

        gf.setCurrent_Song_Id(holder.song_id.getText().toString());

        gf.PlaySong(context,gf.getCurrent_Song_Id());

        enable_ActionControl();
    }

    private void enable_ActionControl() {
        ImageView iv_play_pause = (ImageView)((MainActivity)context).findViewById(R.id.iv_play_pause);
        ImageView iv_next = (ImageView)((MainActivity)context).findViewById(R.id.iv_next);
        ImageView iv_previous = (ImageView)((MainActivity)context).findViewById(R.id.iv_previous);
        SeekBar sb_progress = (SeekBar)((MainActivity)context).findViewById(R.id.sb_song_progress);

        iv_play_pause.setEnabled(true);
        iv_next.setEnabled(true);
        iv_previous.setEnabled(true);
        sb_progress.setEnabled(true);
    }

    private String[] GetSong(String s)
    {
        ContentResolver cr = context.getContentResolver();

        Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null, MediaStore.Audio.Media._ID+"=?",new String[]{s},null);
        if(cur != null && cur.getCount() > 0)
        {
            cur.moveToNext();

            title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
            duration = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION));

            long milliseconds = Long.parseLong(duration);
            //Toast.makeText(context, ""+milliseconds, Toast.LENGTH_SHORT).show();
            duration = gf.MilliSecondToTime(milliseconds);

            //long albumId = cur.getLong(cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

            album_art_path = null;

        }
        else
        {
            title = "Song Not found";
            duration="123";
            album_art_path = null;
        }
        cur.close();

        return new String[]{title,duration,album_art_path};
    }


    @Override
    public int getItemCount() {
        return id_data.size();
    }

    public class Song_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView song_nm,song_duration,song_id,song_menu;
        ImageView song_album;
        LinearLayout song_layout;
        VuMeterView song_bars;

        public Song_ViewHolder(View v)
        {
            super(v);
            song_nm = (TextView)v.findViewById(R.id.song_name_tv);
            song_duration = (TextView)v.findViewById(R.id.song_duration_tv);
            song_id = (TextView)v.findViewById(R.id.song_id);
            song_menu = (TextView)v.findViewById(R.id.song_menu);

            song_layout = (LinearLayout)v.findViewById(R.id.song_layout);

            song_album = (ImageView)v.findViewById(R.id.song_album_art);

            song_bars = (VuMeterView)v.findViewById(R.id.song_bars);
        }
    }

    public void UpdateList(List<String> newList)
    {
        id_data = new ArrayList<String>();
        id_data.addAll(newList);
        notifyDataSetChanged();
    }


}
