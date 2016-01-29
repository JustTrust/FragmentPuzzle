package org.belichenko.a.fragmentpuzl;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, myConstant {

    private String[] menuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ArrayAdapter<String> arrayAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private float startX, startY;
    private int sqQuantity = 3;
    private int sideSize;
    private int colorDelta = 10;
    private ArrayList<TextView> figures = new ArrayList<>();

    private boolean soundOn;
    private boolean imageFromGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
        soundOn = mPrefs.getBoolean(SOUND_ON, false);
        imageFromGallery = mPrefs.getBoolean(IMAGE_FROM_GALLERY, false);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuTitles = getResources().getStringArray(R.array.menu_array);

        // Set the adapter for the list view
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuTitles);
        mDrawerList.setAdapter(arrayAdapter);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
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
    // Change menu item depends on flags
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (soundOn) {
            menuTitles[4] = getString(R.string.sound_on);
        } else {
            menuTitles[4] = getString(R.string.sound_off);
        }
        if (imageFromGallery) {
            menuTitles[3] = getString(R.string.from_gallery);
        } else {
            menuTitles[3] = getString(R.string.from_pallete);
        }
        arrayAdapter.notifyDataSetChanged();
        return super.onPrepareOptionsMenu(menu);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(SOUND_ON, soundOn);
        editor.putBoolean(IMAGE_FROM_GALLERY, imageFromGallery);
        editor.apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences mPrefs = this.getSharedPreferences(MAIN_PREFERENCE, MODE_PRIVATE);
        soundOn = mPrefs.getBoolean(SOUND_ON, false);
        imageFromGallery = mPrefs.getBoolean(IMAGE_FROM_GALLERY, false);
    }

    private void selectItem(int position) {

        if (position == 0) {
            sqQuantity = 3;
            colorDelta = 15;
            fillDisplay();
        }
        if (position == 1) {
            sqQuantity = 4;
            colorDelta = 10;
            fillDisplay();
        }
        if (position == 2) {
            sqQuantity = 5;
            colorDelta = 5;
            fillDisplay();
        }
        if (position == 3) {
            imageFromGallery = !imageFromGallery;
        }
        if (position == 4) {
            soundOn = !soundOn;
        }

        mDrawerLayout.closeDrawer(mDrawerList);
    }

    // shuffle the figure on the screen
    private void shuffle() {
        for (TextView tv : figures) {
            ColorDrawable cd = (ColorDrawable) tv.getBackground();
            tv.setTextColor(cd.getColor());
        }
        Collections.shuffle(figures);
        showFigures();
    }

    // shows figures on the screen
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

    // first time create figures
    private void fillDisplay() {
        int rColor = getMagicColor();
        int gColor = getMagicColor();
        int bColor = getMagicColor();

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
            tv.setBackgroundColor(Color.rgb(rColor, gColor, bColor));
            tv.setText(String.valueOf(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            tv.setGravity(Gravity.CENTER);
            figures.add(tv);
            rColor = rColor + colorDelta;
            gColor = gColor + colorDelta;
            bColor = bColor + colorDelta;
        }
        showFigures();
    }

    // get color for first figure
    private int getMagicColor() {
        Random rd = new Random();

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

    // Calculate one side of figure depends on screen resolution
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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
                            final MediaPlayer mp = MediaPlayer.create(this, R.raw.change_place);
                            mp.start();
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

    // Check is all figure in their places
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
            mp.start();
        }
        Snackbar.make(findViewById(R.id.main_layot), "Congratulation! You WIN!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

    }

    //Check if point is inside of some figure
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
