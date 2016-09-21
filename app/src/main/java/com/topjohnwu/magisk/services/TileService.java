package com.topjohnwu.magisk.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.service.quicksettings.Tile;

import com.kcoppock.broadcasttilesupport.BroadcastTileIntentBuilder;
import com.topjohnwu.magisk.utils.Shell;
import com.topjohnwu.magisk.R;
import com.topjohnwu.magisk.utils.Utils;

import java.util.List;

public class TileService extends Service {
    private static BroadcastReceiver clickTileReceiver;

    private static boolean running = false;
    private static boolean root, autoRoot;

    public static final String TILE_ID = "com.shovelgrill.magiskmmtile.TILE";
    public static final String ACTION_TILE_CLICK = "com.shovelgrill.magiskmmtile.ACTION_TILE_CLICK";
    public static final String EXTRA_CLICK_TYPE = "com.shovelgrill.magiskmmtile.EXTRA_CLICK_TYPE";
    public static final int CLICK_TYPE_UNKNOWN = -1;
    public static final int CLICK_TYPE_SIMPLE = 0;
    public static final int CLICK_TYPE_LONG = 1;

    public TileService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        root = true;
        registerClickTileReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        root = Utils.rootEnabled();
        autoRoot = Utils.autoRootEnabled(getApplicationContext());
        updateTile();
        return super.onStartCommand(intent, flags, startId);
    }



    private void registerClickTileReceiver() {
        clickTileReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int clickType = intent.getIntExtra(EXTRA_CLICK_TYPE, CLICK_TYPE_UNKNOWN);
                switch (clickType) {
                    case CLICK_TYPE_SIMPLE:
                        onSimpleClick();
                        break;
                    case CLICK_TYPE_LONG:
                        onLongClick();
                        break;
                }
            }
        };
        registerReceiver(clickTileReceiver, new IntentFilter(ACTION_TILE_CLICK));
    }


    private void onSimpleClick() {
        Utils.toggleRoot(!root);
    }

    private void onLongClick() {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        sendBroadcast(it);
        openApp(this,"com.topjohnwu.magisk");
    }

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(i);
        return true;
    }

    private void updateTile() {
        BroadcastTileIntentBuilder broadcastTileIntentBuilder = new BroadcastTileIntentBuilder(this, TILE_ID);
        if (autoRoot) {
            broadcastTileIntentBuilder.setLabel("Auto-root");
            broadcastTileIntentBuilder.setIconResource(R.drawable.ic_autoroot_white);

        } else {
            if (root) {
                broadcastTileIntentBuilder.setLabel("Root enabled");
                broadcastTileIntentBuilder.setIconResource(R.drawable.root_white);

            } else {
                broadcastTileIntentBuilder.setLabel("Root disabled");
                broadcastTileIntentBuilder.setIconResource(R.drawable.root_grey);

            }
        }

        Intent simpleClick = new Intent(ACTION_TILE_CLICK);
        simpleClick.putExtra(EXTRA_CLICK_TYPE, CLICK_TYPE_SIMPLE);
        Intent longClick = new Intent(ACTION_TILE_CLICK);
        longClick.putExtra(EXTRA_CLICK_TYPE, CLICK_TYPE_LONG);

        broadcastTileIntentBuilder.setVisible(true);
        broadcastTileIntentBuilder.setOnClickBroadcast(simpleClick);
        broadcastTileIntentBuilder.setOnLongClickBroadcast(longClick);
        this.sendBroadcast(broadcastTileIntentBuilder.build());

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(clickTileReceiver);
        running = false;
    }

}
