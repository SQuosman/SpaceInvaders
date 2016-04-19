package com.example.stefan.spaceinvaders;

import android.graphics.RectF;

/**
 * Created by stefan on 13.04.2016.
 */
public class Bullet {
    // Bullet coordinates
    private float x,y;
    private RectF rect;

    // Bullet direction
    public final int UP = 0;
    public final int DOWN = 1;

    int heading = -1;
    int speed = 350;

    private int width = 1;
    private int height;

    private boolean isActive;

    public Bullet(int screenY){
        height = screenY / 20;
        isActive = false;
        rect = new RectF();
    }

    public RectF getRect() {
        return rect;
    }

    public boolean getStatus() {
        return isActive;
    }

    public void setInactive(){
        isActive = false;
    }

    public float getImpactPoint(){
        if (heading == DOWN){
            return y + height;
        } else return y;
    }

    public boolean shoot(float startX, float startY, int direction){
        if(!isActive){
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        } else {
            // Bullet already active
            return false;
        }
    }

    public void update(long fps) {
        if (heading == UP) {
            y = y - speed / fps;
        }
        if (heading == DOWN){
            y = y + speed / fps;
        }
        // Update rect
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + width;

    }
}
