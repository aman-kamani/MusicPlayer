package com.adproduction.admusicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

public class AD_MediaPlayer_Service extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnInfoListener
{

    private final IBinder iBinder = new LocalBinder();
    private MediaPlayer mediaPlayer;
    private String mediaFile;
    private int resumePosition; //used to pause/resume MediaPlayer

    public AD_MediaPlayer_Service() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return iBinder;
    }

    private void initMediaPlayer()
    {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();

        try
        {
            mediaPlayer.setDataSource(mediaFile);
        }
        catch (Exception ex)
        {
            stopSelf();
        }

        mediaPlayer.prepareAsync();
    }

    private void PlayMedia()
    {
        if(!mediaPlayer.isPlaying())
        {
            mediaPlayer.start();
        }
    }

    private void StopMedia()
    {
        if(mediaPlayer != null)
        {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
        }
    }

    private void PauseMedia()
    {
        if(mediaPlayer != null)
        {
            if(mediaPlayer.isPlaying())
            {
                mediaPlayer.pause();
                resumePosition = mediaPlayer.getCurrentPosition();
            }
        }
    }

    private void ResumeMedia()
    {
        if(mediaPlayer != null)
        {
            if(!mediaPlayer.isPlaying())
            {
                mediaPlayer.seekTo(resumePosition);
                mediaPlayer.start();
            }
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp)
    {
        //called when the song is completed
        StopMedia();

        stopSelf();//this will stop the service
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        //Used to handle errors when any error raises during the asynchronous operation
        Log.e("--->","Error ==  "+extra);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra)
    {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        //called when the song is ready for playback.
        PlayMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp)
    {

    }



    public class LocalBinder extends Binder
    {
        public AD_MediaPlayer_Service getService()
        {
            return AD_MediaPlayer_Service.this;
        }
    }
}
