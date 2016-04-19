package com.example.stefan.spaceinvaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

/**
 * Created by stefan on 13.04.2016.
 */
public class Invader {

    RectF rect;

    Random generator = new Random();

    private Bitmap bitmap1, bitmap2;

    private float length, height, x, y;

    private float invaderSpeed;

    public final int LEFT = 1;
    public final int RIGHT = 2;

    private int invaderMoving = RIGHT;

    boolean isVisible;

    public Invader(Context context, int row, int column, int screenX, int screenY) {
        rect = new RectF();

        length = screenX / 20;
        height = screenY / 20;

        isVisible = true;

        int padding = screenX / 25;

        x = column * (length + padding);
        y = row * (height + padding / 4);

        // Initialize the bitmap
        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);

        bitmap1 = Bitmap.createScaledBitmap(bitmap1, (int) length, (int) height, false);
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, (int) length, (int) height, false);

        // How fast is the invader in pixels per second
        invaderSpeed = 40;
    }

    public void setInvisible() {
        isVisible = false;
    }

    public boolean getVisibility() {
        return isVisible;
    }

    public RectF getRect() {
        return rect;
    }

    public Bitmap getBitmap1() {
        return bitmap1;
    }

    public Bitmap getBitmap2() {
        return bitmap2;
    }

    public float getX() {
        return x;
    }


    public float getY() {
        return y;
    }

    public float getLength() {
        return length;
    }

    public void update(long fps) {
        if (invaderMoving == RIGHT) {
            x = x + invaderSpeed / fps;
        }
        if (invaderMoving == LEFT) {
            x = x - invaderSpeed / fps;
        }

        // Update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;
    }

    public void dropAndReverse() {
        if (invaderMoving == RIGHT) {
            invaderMoving = LEFT;
        } else {
            invaderMoving = RIGHT;
        }
        y = y + height;
        invaderSpeed = invaderSpeed * 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength) {
        int randomNumber = -1;

        // If near the player
        if ((playerShipX + playerShipLength > x && playerShipX + playerShipLength < x + length) ||
                (playerShipX > x && playerShipX < x + length)) {
            // A 1 in 500 chance to shoot

            randomNumber = generator.nextInt(150);
            if (randomNumber == 0) {
                return true;
            }
        }
        randomNumber = generator.nextInt(2000);
        if (randomNumber == 0){
            return true;
        }
        return false;
    }




}
