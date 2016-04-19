package com.example.stefan.spaceinvaders;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class SpaceInvadersView extends SurfaceView implements Runnable{

    Context context;
    // Up to 60 invaders
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;
    // The score
    int score = 0;
    // This is our thread
    private Thread gameThread = null;
    // Our SurfaceHolder to lock the surface before we draw our graphics
    private SurfaceHolder ourHolder;
    // A boolean which we will set and unset
    // when the game is running- or not.
    private volatile boolean playing;
    // Game is paused at the start
    private boolean paused = true;
    // A Canvas and a Paint object
    private Canvas canvas;
    private Paint paint;
    // This variable tracks the game frame rate
    private long fps;
    // This is used to help calculate the fps
    private long timeThisFrame;
    // The size of the screen in pixels
    private int screenX;
    private int screenY;
    // The players ship
    private PlayerShip playerShip;
    // The player's playerBullet
    private Bullet playerBullet;
    // The invaders bullets
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;
    // The player's shelters are built from bricks
    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;
    // For sound FX
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;
    // Lives
    private int lives = 3;

    // How menacing should the sound be?
    private long menaceInterval = 1000;
    // Which menace sound should play next
    private boolean uhOrOh;
    // When did we last play a menacing sound
    private long lastMenaceTime = System.currentTimeMillis();
    //play sound effects
    private boolean soundOn = false;

    SpaceInvadersView(Context context, int x, int y){
        super(context);
        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        //noinspection deprecation
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        }catch(IOException e){
            // Print an error message to the console
            Log.e("error", "failed to load sound files");
        }

        prepareLevel();

    }

    private void prepareLevel(){
        // Reset the menace level
        menaceInterval = 1000;
        // Make a new player space ship
        playerShip = new PlayerShip(context, screenX, screenY);
        // Prepare the players playerBullet
        playerBullet = new Bullet(screenY);
        // Initialize the invadersBullets array
        for (int i = 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Bullet(screenY);

            // Build an army of invaders
            numInvaders = 0;
            for (int column = 0; column < 6; column++){
                for (int row = 0; row < 5; row++){
                    invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                    numInvaders++;
                }
            }
            // Build the shelters
            numBricks = 0;
            for (int shelterNumber = 0; shelterNumber < 4; shelterNumber++){
                for (int column = 0; column < 10; column++){
                    for (int row = 0; row < 5; row++){
                        bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                        numBricks++;
                    }
                }
            }
        }

    }

    @Override
    public void run() {
        while(playing) {
            long startFrameTime = System.currentTimeMillis();

            if (!paused) {
                update();
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame > 1) {
                fps = 1000 / timeThisFrame;
            }


            // Play a sound based on the menace level
            if (!paused) {
                if ((startFrameTime - lastMenaceTime)>menaceInterval){
                    if(soundOn) {
                        if (uhOrOh) {
                            // Play Uh
                            soundPool.play(uhID, 1, 1, 0, 0, 1);
                        } else {
                            // Play Oh
                            soundPool.play(ohID, 1, 1, 0, 0, 1);
                        }
                    }
                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }
        }
    }

    private void update(){
        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;

        // Move the player's ship
        playerShip.update(fps);

        // Update the invaders if visible
        for (int i = 0; i < numInvaders; i++){
            if (invaders[i].getVisibility()){
                // Move the next invader
                invaders[i].update(fps);

                // Does he want to take a shot?
                if (invaders[i].takeAim(playerShip.getX(), playerShip.getLength())){
                    // If so try and spawn a bullet
                    if (invadersBullets[nextBullet].shoot(invaders[i].getX() + invaders[i].getLength() / 2,invaders[i].getY(), playerBullet.DOWN )){
                        // Shot fired
                        // Prepare for the next shot
                        nextBullet++;

                        // Loop back to the first one if we have reached the last
                        if(nextBullet == maxInvaderBullets){
                            // This stops the firing of another bullet until one completes its journey
                            // Because if bullet 0 is still active shoot returns false.
                            nextBullet = 0;
                        }
                    }
                }
            }
            // If that move caused them to bump the screen change bumped to true
            if (invaders[i].getX() > screenX - invaders[i].getLength() || invaders[i].getX() < 0){
                bumped = true;
            }
        }

        // Update all the invaders bullets if active
        for (int i = 0; i < invadersBullets.length; i++){
            if (invadersBullets[i].getStatus()){
                invadersBullets[i].update(fps);
            }
        }
        // Did an invader bump into the edge of the screen
        if(bumped){
            for (int i = 0; i < numInvaders; i++){
                invaders[i].dropAndReverse();

                // Have the invaders landed
                if (invaders[i].getY() > screenY - screenY / 10){
                    lost = true;
                }
            }
            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }

        if(lost){
            prepareLevel();
        }

        // Update the players playerBullet
        if (playerBullet.getStatus()) {
            playerBullet.update(fps);
        }

        // Has the player's Bullet hit the top of the screen
        if(playerBullet.getImpactPoint() < 0){
            playerBullet.setInactive();
        }

        // Has an invaders Bullet hit the bottom of the screen
        for (int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getImpactPoint() > screenY){
                invadersBullets[i].setInactive();
            }
        }

        // Has the player's Bullet hit an invader
        if(playerBullet.getStatus()){
            for(int i = 0; i < numInvaders; i++){
                if(invaders[i].getVisibility()){
                    if(RectF.intersects(playerBullet.getRect(), invaders[i].getRect())){
                        invaders[i].setInvisible();
                        if (soundOn) {
                            soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        }
                        playerBullet.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }

        // Has an Invader Bullet hit a shelter brick
        for (int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                for (int j = 0; j < numBricks; j++){
                    if(bricks[j].getVisibility()){
                        if(RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())) {
                            //HIT!
                            bricks[j].setInvisible();
                            invadersBullets[i].setInactive();
                            if (soundOn) {
                                soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                            }
                        }
                    }
                }

            }
        }

        // Has a player Bullet hit a shelter brick
        if(playerBullet.getStatus()){
            for (int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(playerBullet.getRect(), bricks[i].getRect())) {
                        //HIT!
                        bricks[i].setInvisible();
                        playerBullet.setInactive();
                        if (soundOn) {
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }
        }

        // Has an Invader Bullet hit the player ship
        for(int i = 0; i < invadersBullets.length;i++){
            if(invadersBullets[i].getStatus()){
                if(RectF.intersects(invadersBullets[i].getRect(), playerShip.getRect())){
                    //HIT!!
                    invadersBullets[i].setInactive();
                    lives--;
                    if (soundOn) {
                        soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);
                    }

                    // Game over?
                    if(lives == 0){
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();
                    }
                }
            }
        }

    }

    private void draw(){
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.argb(255, 26, 128, 182));

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255, 255, 255, 255));

            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY-playerShip.getHeight(), paint);

            // Draw the invaders
            for (int i = 0; i < numInvaders; i++){
                Invader in = invaders[i];
                if (in.getVisibility()) {
                    if (uhOrOh) {
                        canvas.drawBitmap(in.getBitmap1(), in.getX(), in.getY(), paint);
                    } else {
                        canvas.drawBitmap(in.getBitmap2(), in.getX(), in.getY(), paint);
                    }
                }
            }

            // Draw the bricks if visible
            for (int i = 0; i < numBricks; i++){
                if (bricks[i].getVisibility()){
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            // Draw the players playerBullet if active
            if (playerBullet.getStatus()){
                canvas.drawRect(playerBullet.getRect(), paint);
            }
            // Draw the invaders bullets if active
            for (int i = 0; i < invadersBullets.length; i++){
                if (invadersBullets[i].getStatus()){
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            // Draw the score and remaining lives
            // Change the brush color
            paint.setColor(Color.argb(255, 249, 129, 0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10, 50, paint);

            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void resume(){
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause(){
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;
                // Move the Ship...
                if(motionEvent.getY() > screenY - screenY / 8){
                    // ...to the left
                    if(motionEvent.getX() < screenX / 2){
                        playerShip.setMovementState(playerShip.LEFT);
                    } else {
                        playerShip.setMovementState(playerShip.RIGHT);
                    }
                }
                // FIRE!!!
                if(motionEvent.getY() < screenY - screenY / 8){
                    if(playerBullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY, playerBullet.UP)){
                        if (soundOn) {
                            soundPool.play(shootID,1,1,0,0,1);
                        }
                    }
                }
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                //if(motionEvent.getY() > screenY - screenY / 10) {
                    playerShip.setMovementState(playerShip.STOPPED);
                //}
                break;
        }
        return true;
    }
}
