package com.adproduction.admusicplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.adproduction.admusicplayer.Adapters.Queue_Song_Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GlobalFunction
{
    public static MediaPlayer mp = new MediaPlayer();

    private static String Current_Song_Id;
    private static List<String> Song_List,Queue_List;

    public enum RepeateMode{REPEATE_ALL,REPEAT_ONE,SHUFFLE};
    private static RepeateMode repeateMode;

    public boolean fromUser = false;

    public static int current_Song_Position,current_Queue_Position;

    public int getCurrent_Queue_Position() {
        return current_Queue_Position;
    }

    public static void setCurrent_Queue_Position(int current_Queue_Position) {
        current_Queue_Position = current_Queue_Position;
    }

    public static int getCurrent_Song_Position() {
        return current_Song_Position;
    }

    public static void setCurrent_Song_Position(int current_Song_Position) {
        GlobalFunction.current_Song_Position = current_Song_Position;
    }

    public RepeateMode getRepeateMode()
    {
        return repeateMode;
    }

    public void setRepeateMode(RepeateMode repeateMode)
    {
        this.repeateMode = repeateMode;
    }

    public List<String> getSong_List() {
        return Song_List;
    }

    public void setSong_List(List<String> song_List) {
        Song_List = song_List;
    }

    public List<String> getQueue_List() {
        return Queue_List;
    }

    public void setQueue_List(List<String> queue_list) {Queue_List = queue_list;}

    public void AddToQueue(Context context, String song_id,String type)
    {
        List<String> temp_queue = new ArrayList<String>();

        if(Queue_List != null)
        {
            temp_queue = getQueue_List();
        }

        if (type.equals("add"))
        {
            if(getCurrent_Song_Id() != null && getCurrent_Song_Id().equals(song_id))
            {
                Toast.makeText(context, "Song is playing", Toast.LENGTH_SHORT).show();
            }
            else
            {
                if(temp_queue.contains(song_id))
                    temp_queue.remove(song_id);
                temp_queue.add(song_id);
            }
        }
        else
        {
            if(getQueue_List() == null)
            {
                temp_queue.add(song_id);
            }
            else
            {
                int curr_index = getQueue_List().indexOf(getCurrent_Song_Id());
                temp_queue.add(curr_index + 1, song_id);
            }
        }
        if(getCurrent_Song_Id() == null)
        {
            setCurrent_Song_Id(song_id);
        }
        setQueue_List(temp_queue);
    }

    public String getCurrent_Song_Id() {
        return Current_Song_Id;
    }

    public void setCurrent_Song_Id(String current_Song_Id) {
        Current_Song_Id = current_Song_Id;
    }

    public void PlaySong(final Context context, final String songId)
    {
        setCurrent_Song_Id(songId);
        setCurrent_Song_Position(getSong_List().indexOf(songId));

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.DATA}, MediaStore.Audio.Media._ID+"=?",new String[]{songId},null);

        cur.moveToNext();

        String data = cur.getString(0);

        cur.close();

        /*****Play Song*****/

        try
        {
            GlobalFunction.mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp)
                {
                    if(!fromUser)
                    {
                        ((MainActivity) context).ChangePlayPause("PLAY", true);
                        ((MainActivity) context).Next_Click();
                    }
                    if(Queue_List.indexOf(songId) == Queue_List.size()-1)
                    {
                        setCurrent_Song_Id(null);
                        ImageView iv_next = ((MainActivity)context).findViewById(R.id.iv_next);
                        iv_next.setEnabled(false);
                    }
                }
            });

            GlobalFunction.mp.reset();
            GlobalFunction.mp.setDataSource(data);
            GlobalFunction.mp.prepare();

            ((MainActivity)context).ChangePlayPause("PAUSE",false);
        }
        catch (Exception ex)
        {
            Toast.makeText(context, ""+ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //GenerateNotification(context);
    }

    private void GenerateNotification(Context context)
    {
        HashMap<String,String> temp = getSongInformation(context);
        String name = temp.get("TITLE");

        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews rv = new RemoteViews(context.getPackageName(),R.layout.layout_music_notification);
        rv.setTextViewText(R.id.noti_song_name,name);


        Intent intent = new Intent(context,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("SONG_POSITION",GlobalFunction.getCurrent_Song_Position());
        intent.putExtra("SONG_ID",getCurrent_Song_Id());
        PendingIntent pi = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context,"007");
        nb.setCustomContentView(rv);
        nb.setSmallIcon(android.R.drawable.ic_menu_agenda);
        nb.setAutoCancel(false);
        nb.setTicker(name);
        nb.setContentTitle("AD Music Player");
        nb.setContentIntent(pi);
        nm.notify(7,nb.build());

    }

    public void CreateNotificationChannel(Context context)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("007","AD_MusicPlayer",NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Song is Playing");

            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }

    public HashMap<String, String> getSongInformation(Context context)
    {
        HashMap<String,String> SongInfo = new HashMap<String, String>();

        String SongId = this.getCurrent_Song_Id();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Audio.Media.TITLE}, MediaStore.Audio.Media._ID +"=?",new String[]{SongId},null);

        cur.moveToNext();
        SongInfo.put("TITLE",cur.getString(0));
        cur.close();

        return SongInfo;
    }

    public HashMap<String, String> getSongInformation(Context context,String songId)
    {
        HashMap<String,String> SongInfo = new HashMap<String, String>();

        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Audio.Media.TITLE}, MediaStore.Audio.Media._ID +"=?",new String[]{songId},null);

        cur.moveToNext();
        SongInfo.put("TITLE",cur.getString(0));
        cur.close();

        return SongInfo;
    }

    public String MilliSecondToTime(long milliseconds)
    {
        String finalTime = "";

        int hour = (int)(milliseconds / (1000 * 60 * 60));
        int minute = (int)(milliseconds % (1000 * 60 *60)) / (1000 * 60);
        int second = (int)((milliseconds % (1000 * 60 *60)) % (1000 * 60) / 1000);

        if(hour > 0)
            finalTime += hour+":";

        finalTime+=String.format("%02d:%02d",minute,second);

        return finalTime;
    }


}
