package com.example.stefan.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by stefan on 13.04.2016.
 */
public class PlayerShip {
    RectF rect;
    private Bitmap bitmap;

    // x = left, y = top
    private float length;
    private float height;

    private float x;
    private float y;
    private float shipSpeed;

    // Movement
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int shipMoving = STOPPED;

    public PlayerShip(Context context, int screenX, int screenY){
        rect = new RectF();

        length = screenX / 10;
        height = screenY / 10;

        // Start ship in roughly the screen centre
        x = screenX/2;
        y = screenY-20;

        // Initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);
        // stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)length, (int)height, false);
        // How fast is the spaceship in pixels per second
        shipSpeed = 350;

    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX() {
        return x;
    }

    public float getLength() {
        return length;
    }

    public float getHeight(){ return height; }

    public void setMovementState(int state){
        shipMoving = state;
    }

    public void update(long fps){
        if (shipMoving == LEFT){
            x = x - shipSpeed /fps;
        }
        if (shipMoving == RIGHT){
            x = x + shipSpeed/fps;
        }

        // Update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y - height;
        rect.left = x;
        rect.right = x + length;
    }
}
