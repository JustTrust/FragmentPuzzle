package org.belichenko.a.fragmentpuzl;

import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, myConstant {

    private DrawerLayout mDrawerLayout;
    private NavigationView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private float startX, startY;
    private int sqQuantity;
    private int sideSize;
    private int colorDelta = 10;
    private ArrayList<TextView> figures = new ArrayList<>();

    private MediaPlayer mpChange;
    private boolean soundOn;
    private boolean imageFromGallery;
    private Bitmap currentBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mpChange = MediaPlayer.create(this, R.raw.change_place);
        mpChange.setAudioStreamType(AudioManager.STREAM_MUSIC);

        SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
        soundOn = mPrefs.getBoolean(SOUND_ON, false);
        imageFromGallery = mPrefs.getBoolean(IMAGE_FROM_GALLERY, false);
        sqQuantity = mPrefs.getInt(SQUARE_QUANTITY, 3);

        // Set up selection on the menu items on the first time
        mDrawerList = (NavigationView) findViewById(R.id.nav_view);
        MenuItem mi_sound = mDrawerList.getMenu().findItem(R.id.nav_sound);
        mi_sound.setChecked(soundOn);
        MenuItem mi_gallery = mDrawerList.getMenu().findItem(R.id.nav_gallery);
        mi_gallery.setChecked(imageFromGallery);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set the left menu click listener
        mDrawerList.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                return selectItem(menuItem);
            }
        });

        // Enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffle();
            }
        });

        fillDisplay();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Get random image from gallery and set it to the shared variable
     * or set null
     */
    private void getRandomImage() {

        if (currentBitmap != null) {
            currentBitmap.recycle();
        }

        if (! isExternalStorageAccessibly()){
            currentBitmap = null;
            return;
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;

        String[] projection = new String[]{
                MediaStore.Images.Media.DATA,
        };

        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = new CursorLoader(this, images, projection, "", null, "").loadInBackground();

        final ArrayList<String> imagesPath = new ArrayList<String>();
        if (cur.moveToFirst()) {

            int dataColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            do {
                imagesPath.add(cur.getString(dataColumn));
            } while (cur.moveToNext());
        }
        cur.close();

        boolean isImageReady = false;
        final Random random = new Random();
        final int count = imagesPath.size();
        if (count < 1) {
            currentBitmap = null;
            return;
        }
        while (!isImageReady) {
            int number = random.nextInt(count);
            String path = imagesPath.get(number);
            currentBitmap = BitmapFactory.decodeFile(path);
            if (currentBitmap.getWidth() > screenWidth && currentBitmap.getHeight() > screenWidth) {
                isImageReady = true;
            } else {
                currentBitmap = null;
            }
        }
    }

    /**
     * Check accessibility of external storage
     *
     * @return true if external storage accessible
     * and false in opposite case
     */
    private boolean isExternalStorageAccessibly() {

        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
        soundOn = mPrefs.getBoolean(SOUND_ON, false);
        imageFromGallery = mPrefs.getBoolean(IMAGE_FROM_GALLERY, false);
        sqQuantity = mPrefs.getInt(SQUARE_QUANTITY, 3);
    }

    private boolean selectItem(MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.new_game3) {
            sqQuantity = 3;
            colorDelta = 15;
            fillDisplay();
        }
        if (id == R.id.new_game4) {
            sqQuantity = 4;
            colorDelta = 10;
            fillDisplay();
        }
        if (id == R.id.new_game5) {
            sqQuantity = 5;
            colorDelta = 5;
            fillDisplay();
        }
        if (id == R.id.nav_gallery) {
            // check image from gallery first
            if (!imageFromGallery) {
                if (currentBitmap != null) {
                    imageFromGallery = true;
                } else {
                    Toast.makeText(this, getString(R.string.gallery_empty), Toast.LENGTH_LONG).show();
                }
            } else {
                imageFromGallery = false;
            }
            SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(IMAGE_FROM_GALLERY, imageFromGallery);
            editor.apply();
            menuItem.setChecked(imageFromGallery);
            fillDisplay();
        }
        if (id == R.id.nav_sound) {
            soundOn = !soundOn;
            SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(SOUND_ON, soundOn);
            editor.apply();
            menuItem.setChecked(soundOn);
        }
        updateNavigationView();
        mDrawerLayout.closeDrawer(mDrawerList);
        return true;
    }

    /**
     * Hack to update navigation view
     * from stack over flow
     */

    private void updateNavigationView() {
        try {
            Field presenterField = NavigationView.class.getDeclaredField("mPresenter");
            presenterField.setAccessible(true);
            ((NavigationMenuPresenter) presenterField.get(mDrawerList)).updateMenuView(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shuffle the figure on the shared list
     * and then show them in new order
     */
    private void shuffle() {
        for (TextView tv : figures) {
            if (imageFromGallery) {
                tv.setTextSize(0f);
            } else {
                ColorDrawable cd = (ColorDrawable) tv.getBackground();
                tv.setTextColor(cd.getColor());
            }
        }
        if (soundOn) {
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.shuffling);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.start();
        }
        Collections.shuffle(figures);
        showFigures();
    }

    /**
     * Shows figures on the screen from shared ArrayList
     */
    private void showFigures() {
        int leftMargin;
        int topMargin;
        int count = 0;
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.main_layot);

        if (figures != null) {
            rl.removeAllViews();

            for (int i = 1; i < (sqQuantity + 1); i++) {
                topMargin = PADDING * i + (sideSize * (i - 1));
                for (int j = 1; j < (sqQuantity + 1); j++) {
                    leftMargin = PADDING * j + (sideSize * (j - 1));
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(sideSize, sideSize);
                    TextView iv = figures.get(count);

                    rl.addView(iv, params);
                    iv.setX(leftMargin);
                    iv.setY(topMargin);
                    count++;
                }
            }
        }
    }

    /**
     * Create figures and put them in to shared ArrayList
     */
    private void fillDisplay() {

        int rColor = 0;
        int gColor = 0;
        int bColor = 0;
        getRandomImage();

        if (!imageFromGallery) {
            rColor = getMagicColor();
            gColor = getMagicColor();
            bColor = getMagicColor();
        }
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setBackgroundDrawable(new ColorDrawable(Color.rgb(rColor, gColor, bColor)));
        }
        calculateSideSize();
        figures.clear();
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.main_layot);
        rl.removeAllViews();
        rl.getLayoutParams().height = PADDING * (sqQuantity + 1) + sideSize * sqQuantity;
        rl.getLayoutParams().width = PADDING * (sqQuantity + 1) + sideSize * sqQuantity;

        for (int i = 0; i < (sqQuantity * sqQuantity); i++) {
            TextView tv = new TextView(this);
            tv.setOnTouchListener(this);
            if (imageFromGallery) {
                tv.setBackground(getPieceOfPicture(i));
            } else {
                tv.setBackgroundColor(Color.rgb(rColor, gColor, bColor));
                rColor = rColor + colorDelta;
                gColor = gColor + colorDelta;
                bColor = bColor + colorDelta;
            }
            tv.setText(String.valueOf(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            tv.setGravity(Gravity.CENTER);
            figures.add(tv);

        }
        showFigures();
    }

    @Nullable
    /**
     * Cut little pieces from random picture
     * @return The Drawable if found image or null otherwise
     * @param  int index of cell in the square
     */
    public Drawable getPieceOfPicture(int i) {
        if (currentBitmap != null) {
            int line = i / sqQuantity;
            int row = i % sqQuantity;
            int x = PADDING * (row + 1) + (sideSize * row);
            int y = PADDING * (line + 1) + (sideSize * line);
            Bitmap resultBitmap = Bitmap.createBitmap(currentBitmap, x, y, sideSize, sideSize);
            return new BitmapDrawable(getResources(), resultBitmap);
        }
        return null;
    }

    /**
     * Return color for first figure
     *
     * @return int index of color
     */
    private int getMagicColor() {
        final Random rd = new Random();

        switch (sqQuantity) {
            case 3:
                return rd.nextInt(120);
            case 4:
                return rd.nextInt(95);
            case 5:
                return rd.nextInt(130);
        }
        return rd.nextInt(95);
    }

    /**
     * Calculate one side of figure depends on screen resolution
     * and set up result in shared variable
     */

    private void calculateSideSize() {

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.i("Screen", "Screen width = " + width + ", height = " + height);
        if (width > 1) {
            sideSize = (width - PADDING * (sqQuantity + 1)) / sqQuantity;
            Log.i("Screen", "SideSize = " + sideSize);
        } else {
            Snackbar.make(findViewById(R.id.main_layot), "You don't have screen", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == R.id.action_cell3) {
            sqQuantity = 3;
            colorDelta = 15;
            fillDisplay();
            return true;
        }
        if (id == R.id.action_cell4) {
            sqQuantity = 4;
            colorDelta = 10;
            fillDisplay();
            return true;
        }
        if (id == R.id.action_cell5) {
            sqQuantity = 5;
            colorDelta = 5;
            fillDisplay();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                startX = view.getX() - event.getRawX();
                startY = view.getY() - event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                view.bringToFront();
                view.animate()
                        .x(event.getRawX() + startX)
                        .y(event.getRawY() + startY)
                        .setDuration(0)
                        .start();
                break;
            case MotionEvent.ACTION_UP:

                for (TextView tv : figures) {
                    if (tv.equals(view)) {
                        continue;
                    }
                    if (isPointInsideView(event.getRawX(), event.getRawY(), tv)) {
                        if (soundOn) {
                            mpChange.start();
                        }
                        Collections.swap(figures, figures.indexOf(tv), figures.indexOf(view));
                        showFigures();
                        checkIsWinn();
                        break;
                    }
                }
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Check is all figure in their places
     * and if it is then play win sound and show message
     */
    private void checkIsWinn() {
        for (int i = 0; i < figures.size(); i++) {
            TextView tv = figures.get(i);
            if (!String.valueOf(i).equals(tv.getText())) {
                // something wrong, not all figure in their places
                return;
            }
        }
        if (soundOn) {
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.winn);
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.start();
        }
        Snackbar.make(findViewById(R.id.main_layot), "Congratulation! You WIN!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    /**
     * Check if point is inside of some figure
     *
     * @param x    coordinate of point
     * @param y    coordinate of point
     * @param view some View witch we check
     * @return boolean if point with coordinate x,y inside view
     */

    public static boolean isPointInsideView(float x, float y, View view) {

        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        //if point is inside view bounds
        return (x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()));
    }
}