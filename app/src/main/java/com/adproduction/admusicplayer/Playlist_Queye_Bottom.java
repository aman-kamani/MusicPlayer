package com.adproduction.admusicplayer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adproduction.admusicplayer.Adapters.Queue_Song_Adapter;

public class Playlist_Queye_Bottom extends BottomSheetDialogFragment implements View.OnClickListener
{
    GlobalFunction gf = new GlobalFunction();

    LinearLayout _close;
    ImageView iv_repeatation;
    TextView tv_repeatation_text,tv_musiclist;
    RecyclerView rv_musiclist;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {

        View v = inflater.inflate(R.layout.layout_playlist_queue,container,false);

        _close = (LinearLayout)v.findViewById(R.id.pl_close);
        iv_repeatation = (ImageView)v.findViewById(R.id.pl_song_repeatation);
        tv_repeatation_text = (TextView)v.findViewById(R.id.pl_repeate_mode);
        tv_musiclist = (TextView)v.findViewById(R.id.pl_musiclist_tv);
        rv_musiclist = (RecyclerView)v.findViewById(R.id.pl_musiclist_rv);

        _close.setOnClickListener(this);
        iv_repeatation.setOnClickListener(this);

        LoadRepeateMode();

        if(gf.getQueue_List() == null)
        {
            tv_musiclist.setVisibility(View.VISIBLE);
            rv_musiclist.setVisibility(View.GONE);
        }
        else
        {
            tv_musiclist.setVisibility(View.GONE);
            rv_musiclist.setVisibility(View.VISIBLE);
            LoadQueue();
        }

        return v;
    }

    private void LoadQueue()
    {
        Queue_Song_Adapter adpt = new Queue_Song_Adapter(getContext());
        rv_musiclist.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_musiclist.setAdapter(adpt);
    }

    private void LoadRepeateMode()
    {
        if(gf.getRepeateMode() == null)
        {
            gf.setRepeateMode(GlobalFunction.RepeateMode.REPEATE_ALL);
        }
        else
        {

            if(gf.getRepeateMode() == GlobalFunction.RepeateMode.REPEAT_ONE)
            {
                iv_repeatation.setImageResource(R.drawable.ic_repeat_one_blue_24dp);
                tv_repeatation_text.setText("Repeate Current Song");
            }
            else if(gf.getRepeateMode() == GlobalFunction.RepeateMode.SHUFFLE)
            {
                iv_repeatation.setImageResource(R.drawable.ic_shuffle_blue_24dp);
                tv_repeatation_text.setText("Shuffle All Songs");
            }
        }
    }


    @Override
    public void setupDialog(Dialog dialog, int style)
    {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_playlist_queue,null);
        dialog.setContentView(v);
        BottomSheetBehavior behavior = BottomSheetBehavior.from((View)v.getParent());
        behavior.setPeekHeight(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.pl_close:
                dismiss();
                break;
            case R.id.pl_song_repeatation:
                ChangeRepeatationMode(gf.getRepeateMode());
                break;
        }
    }

    private void ChangeRepeatationMode(GlobalFunction.RepeateMode repeateMode)
    {
        switch (gf.getRepeateMode())
        {
            case REPEATE_ALL:
                iv_repeatation.setImageResource(R.drawable.ic_repeat_one_blue_24dp);
                gf.setRepeateMode(GlobalFunction.RepeateMode.REPEAT_ONE);
                tv_repeatation_text.setText("Repeat Current Song");
                break;
            case REPEAT_ONE:
                iv_repeatation.setImageResource(R.drawable.ic_shuffle_blue_24dp);
                gf.setRepeateMode(GlobalFunction.RepeateMode.SHUFFLE);
                tv_repeatation_text.setText("Shuffle All Songs");
                break;
            case SHUFFLE:
                iv_repeatation.setImageResource(R.drawable.ic_repeat_blue_24dp);
                gf.setRepeateMode(GlobalFunction.RepeateMode.REPEATE_ALL);
                tv_repeatation_text.setText("Repeat All Songs");
                break;
        }
    }
}
