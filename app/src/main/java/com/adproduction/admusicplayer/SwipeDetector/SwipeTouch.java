package com.adproduction.admusicplayer.SwipeDetector;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SwipeTouch implements View.OnTouchListener
{
    private GestureDetector gestureDetector;


    public SwipeTouch(Context context)
    {
        gestureDetector = new GestureDetector(context,new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        Log.e("--->","onTouch executed");
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        private static final int SWIPE_THRESHHOLD = 30;
        private static final int SWIPE_VELOCITY_THRESHHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Boolean result = false;

            Log.e("--->","inside Fling");

            float diff_x = e2.getX()-e1.getX();
            float diff_y = e2.getY()-e1.getY();
            diff_x = Math.abs(diff_x);
            diff_y = Math.abs(diff_y);


            Log.e("--->",diff_x+">"+diff_y);
            if(diff_x > diff_y) // means user swaped horizontally
            {
                if(diff_x > SWIPE_THRESHHOLD && diff_x > SWIPE_VELOCITY_THRESHHOLD)
                {
                    Log.e("--->","x is greater then threshhold");
                    if(e2.getX() > e1.getX())
                    {
                        UserSwiped_LeftToRight();
                    }
                    else
                    {
                        Log.e("--->","right to left");
                        UserSwiped_RightToLeft();
                    }
                    result = true;
                }
            }
            else if(diff_y > SWIPE_THRESHHOLD && diff_y > SWIPE_VELOCITY_THRESHHOLD)
            {
                if(diff_y > 0) {
                    //User swiped from Bottom To Top
                }
                else
                {
                    //User swiped from Top To Bottom
                }
                result=false;
            }


            return result;
        }
    }
    public void UserSwiped_LeftToRight() {
    }

    public void UserSwiped_RightToLeft() {
    }
}
