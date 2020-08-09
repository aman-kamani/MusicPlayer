package com.adproduction.admusicplayer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adproduction.admusicplayer.Adapters.All_Song_Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
{
    public final int WRITE_EXTERNAL_STORAGE = 2;

    RecyclerView musiclist;
    ImageView iv_play_pause,iv_next,iv_previous,iv_repeate,iv_queue;
    static TextView tv_current_song,tv_current_song_duration;
    static SeekBar sb_song_progress;

    Handler handler=new Handler();
    Runnable runnable;

    List<String> songs_list = new ArrayList<String>();
    List<String> id_list = new ArrayList<String>();

    static All_Song_Adapter song_adpt;

    GlobalFunction gf = new GlobalFunction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AskPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
    }

    public void LoadData()
    {
        musiclist = (RecyclerView)findViewById(R.id.musiclist);
        tv_current_song = (TextView)findViewById(R.id.tv_current_song);
        tv_current_song_duration = (TextView)findViewById(R.id.tv_current_song_duration);
        iv_play_pause = (ImageView)findViewById(R.id.iv_play_pause);
        iv_next = (ImageView)findViewById(R.id.iv_next);
        iv_previous = (ImageView)findViewById(R.id.iv_previous);
        iv_queue = (ImageView)findViewById(R.id.iv_song_queue);
        sb_song_progress = (SeekBar)findViewById(R.id.sb_song_progress);

        iv_queue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*View view = getLayoutInflater().inflate(R.layout.layout_playlist_queue,null);
                BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
                dialog.setContentView(view);
                dialog.show();*/
                Playlist_Queye_Bottom bottomSheet = new Playlist_Queye_Bottom();
                bottomSheet.show(getSupportFragmentManager(),bottomSheet.getTag());
            }
        });

        iv_play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Play_Pause_Click();
            }
        });

        iv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Next_Click();
            }
        });

        iv_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Previous_Click();
            }
        });

        ContentResolver cr = getContentResolver();
        Cursor m_list = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.TITLE+" asc");

        while(m_list.moveToNext())
        {
            songs_list.add(m_list.getString(m_list.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            id_list.add(m_list.getString(m_list.getColumnIndex(MediaStore.Audio.Media._ID)));
        }
        m_list.close();

        musiclist.setLayoutManager(new LinearLayoutManager(this));
        song_adpt = new All_Song_Adapter(MainActivity.this,id_list);
        musiclist.setAdapter(song_adpt);

        if(gf.getCurrent_Song_Id() == null)
        {
            iv_play_pause.setEnabled(false);
            iv_next.setEnabled(false);
            iv_previous.setEnabled(false);
            sb_song_progress.setEnabled(false);
        }

        sb_song_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if(fromUser)
                {
                    gf.fromUser = true;
                    gf.mp.seekTo(progress);
                    tv_current_song_duration.setText(gf.MilliSecondToTime(gf.mp.getCurrentPosition()));
                }
                else
                {
                    gf.fromUser = false;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(seekBar.getProgress() == gf.mp.getDuration())
                {
                    ChangePlayPause("PLAY",true);
                    Next_Click();
                }
            }
        });

        /**********Creating Notification Channel***************/
        gf.CreateNotificationChannel(this);
    }

    private void Play_Pause_Click()
    {
        Toast.makeText(this,""+gf.getQueue_List().size(),Toast.LENGTH_LONG).show();

        if(iv_play_pause.getTag().equals("PLAY"))
        {
            Log.e("###","inside play");
            ChangePlayPause("PAUSE",false);
        }
        else
        {
            Log.e("###","inside pause");
            ChangePlayPause("PLAY",false);
        }
    }

    public void ChangePlayPause(String tag,boolean isCompleted)
    {
        iv_play_pause.setTag(tag);
        if(tag.equals("PAUSE"))
        {
            Log.e("###","inside changePlayPause - pause");
            iv_play_pause.setImageResource(android.R.drawable.ic_media_pause);
            gf.mp.start();
            LoadProgress();
            tv_current_song.setSelected(true);
        }
        else
        {
            Log.e("###","inside changePlayPause - play");
            iv_play_pause.setImageResource(android.R.drawable.ic_media_play);
            if(!isCompleted)
                gf.mp.pause();
            tv_current_song.setSelected(false);
        }
    }

    public void Next_Click()
    {
        String Current_Song_Id = gf.getCurrent_Song_Id();
        List<String> temp_song_list = gf.getQueue_List();

        int index = temp_song_list.indexOf(Current_Song_Id);

        if(index == temp_song_list.size()-1)
        {
            Toast.makeText(this, "This is Last Song", Toast.LENGTH_SHORT).show();
        }
        else
        {
            gf.PlaySong(this,id_list.get(index+1));
            song_adpt.notifyDataSetChanged();

            ChangeSong_Name();
        }
    }
    private void Previous_Click()
    {

        String Current_Song_Id = gf.getCurrent_Song_Id();
        List<String> temp_song_list = gf.getQueue_List();

        int index = temp_song_list.indexOf(Current_Song_Id);
        if(index == 0)
        {
            Toast.makeText(this, "This is First Song", Toast.LENGTH_SHORT).show();
        }
        else
        {
            gf.PlaySong(this,id_list.get(index-1));
            song_adpt.notifyDataSetChanged();
            ChangeSong_Name();
        }
        //musiclist.findViewHolderForAdapterPosition(index-1).itemView.performClick();//to manually click the view of recycler view
    }

    public void LoadProgress()
    {
        sb_song_progress.setMax(gf.mp.getDuration());
        CurrentProcess();
    }

    private void CurrentProcess()
    {
        sb_song_progress.setProgress(gf.mp.getCurrentPosition());
        tv_current_song_duration.setText(gf.MilliSecondToTime(gf.mp.getCurrentPosition()));

        if(gf.mp.isPlaying())
        {
            runnable = new Runnable() {
                @Override
                public void run() {
                    CurrentProcess();
                }
            };
            handler.postDelayed(runnable,1000);
        }
    }

    private void ChangeSong_Name()
    {
        HashMap<String,String> temp_data = gf.getSongInformation(this);
        tv_current_song.setText(temp_data.get("TITLE"));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        int song_position = getIntent().getIntExtra("SONG_POSITION",-1);
        if(song_position != -1) // notification is clicked
        {
            musiclist.scrollToPosition(song_position);
            String curr_song_id = getIntent().getStringExtra("SONG_ID");
        }

        if(gf.mp != null && gf.getCurrent_Song_Id() != null)
        {

            ResumeData();
        }
    }

    private void ResumeData()
    {
        HashMap<String,String> temp_data = gf.getSongInformation(this);
        this.tv_current_song.setText(temp_data.get("TITLE"));

        this.sb_song_progress.setMax(gf.mp.getDuration());
        this.sb_song_progress.setProgress(gf.mp.getCurrentPosition());
        this.tv_current_song_duration.setText(gf.MilliSecondToTime(gf.mp.getCurrentPosition()));

        if(gf.mp.isPlaying())
        {
            ChangePlayPause("PAUSE",false);
        }
        else
        {
            ChangePlayPause("PLAY",false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.songs_menu,menu);

        MenuItem menuItem = menu.findItem(R.id.menu_song_search);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getItemId() == R.id.menu_song_count)
        {
            Toast.makeText(this, "Total Songs - "+id_list.size(), Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        String userInput = newText.toLowerCase();
        List<String> tempList = new ArrayList<String>();

        for(String name : songs_list)
        {
            if(name.toLowerCase().contains(userInput))
            {
                int idx = songs_list.indexOf(name);
                tempList.add(id_list.get(idx));
            }
        }

        song_adpt.UpdateList(tempList);

        return true;
    }


    public void AskPermission(String permission_name, int request_code)
    {
        if(ContextCompat.checkSelfPermission(this,permission_name) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{permission_name},request_code);
        }
        else
        {
            LoadData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            LoadData();
        }
        else
        {
            Toast.makeText(this, "Permission Required For Music Player", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
