package com.example.stefan.spaceinvaders;

import android.graphics.RectF;

/**
 * Created by stefan on 13.04.2016.
 */
public class DefenceBrick {
    private RectF rect;
    private boolean isVisible;

    public DefenceBrick(int row, int column, int shelterNumber, int screenX, int screenY) {
        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;

        // Sometimes a bullet slips through this padding.
        // Set padding to zero if this annoys you
        int brickPadding = 1;
        // The number of shelters
        int shelterPadding = screenX / 9;
        int startHeight = screenY - (screenY / 8 * 2);

        rect = new RectF(column * width + brickPadding + (shelterPadding * shelterNumber) + shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding + (shelterPadding * shelterNumber) + shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
    }

    public RectF getRect(){
        return rect;
    }

    public boolean getVisibility(){
        return isVisible;
    }

    public void setInvisible(){
        isVisible = false;
    }
}
