package edu.fullsail.mgms.agd.lottoticket.leeboeckmann;

import android.content.DialogInterface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.Vector;
import android.view.View;
import android.widget.Button;
import android.content.Intent;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Vector<PrizeView> mPrizes;
    MediaPlayer mBackgroundSound;
    MediaPlayer mScratchingSound;
    MediaPlayer mMusic;
    boolean mGameOver;

    PrizeView.PrizeValue mPrize;

    public void setPrize(PrizeView.PrizeValue prize)
    {
        mPrize = prize;
    }

    public boolean isGameOver()
    {
        return mGameOver;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button credits = (Button)findViewById(R.id.btnCredits);
        credits.setOnClickListener(this);

        Button inventory = (Button)findViewById(R.id.btnReset);
        inventory.setOnClickListener(this);
    }

    protected void onStart()
    {
        super.onStart();

        mGameOver = false;

        mPrizes = new Vector<>();
        findPrizeViews((ViewGroup)getWindow().getDecorView().getRootView(), mPrizes);
        for(int i = 0; i < 10; ++i) {
            int idx = (new Random()).nextInt(mPrizes.size());
            PrizeView p = mPrizes.get(0);
            mPrizes.set(0, mPrizes.get(idx));
            mPrizes.set(idx, p);
        }

        for(int i = 0; i < mPrizes.size(); ++i)
        {
            mPrizes.get(i).setPrizeValue(PrizeView.PrizeValue.values()[i%3]);
        }

        mBackgroundSound = MediaPlayer.create(this, R.raw.sportscrowd);
        mBackgroundSound.start();
        mBackgroundSound.setLooping(true);
        mBackgroundSound.setVolume(0.25f, 0.25f);
    }

    protected void onStop()
    {
        super.onStop();

        mMusic.stop();
        mMusic.release();
        mScratchingSound.stop();
        mScratchingSound.release();
        mBackgroundSound.stop();
        mBackgroundSound.release();
    }

    public void findPrizeViews(ViewGroup parent, Vector<PrizeView> list)
    {
        View child = null;
        int count = parent.getChildCount();
        for(int i = 0; i < count; ++i)
        {
            child = parent.getChildAt(i);
            if(child instanceof PrizeView)
                list.add((PrizeView)child);
            else if (child instanceof  ViewGroup)
                findPrizeViews((ViewGroup)child, list);
        }
    }

    @Override
    public void onClick(View view) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        switch(view.getId()) {
            case R.id.btnCredits: {
                dialog.setTitle("Made by");
                dialog.setMessage("Lee Boeckmann\nMGMS | AGD\n01/25/2017");
                dialog.setPositiveButton(" OK ", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            }
            case R.id.btnReset: {

                Intent intent = getIntent();
                finish();
                startActivity(intent);

                break;
            }
        }
    }

    public void playScratchCheer()
    {
        if(mScratchingSound != null)
        {
            if(mScratchingSound.isPlaying())
            {
                return;
            }
        }

        mScratchingSound = MediaPlayer.create(this, R.raw.cheering);
        mScratchingSound.start();
        mScratchingSound.setVolume(0.25f, 0.25f);
    }

    public void setGameOver()
    {
        if(mScratchingSound != null) {
            mScratchingSound.stop();
        }

        if (mPrize == PrizeView.PrizeValue.GOAL)
        {
            mMusic = MediaPlayer.create(this, R.raw.largecheer);
            mMusic.start();
        }
        else if (mPrize == PrizeView.PrizeValue.ALMOST)
        {
            mMusic = MediaPlayer.create(this, R.raw.smallcheer);
            mMusic.start();
        }
        else if (mPrize == PrizeView.PrizeValue.MISS)
        {
            mMusic = MediaPlayer.create(this, R.raw.boo);
            mMusic.start();
        }
    }
}
