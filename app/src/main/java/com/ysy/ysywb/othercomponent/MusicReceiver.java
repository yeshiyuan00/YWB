package com.ysy.ysywb.othercomponent;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ysy.ysywb.bean.android.MusicInfo;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.lib.RecordOperationAppBroadcastReceiver;
import com.ysy.ysywb.support.utils.GlobalContext;

/**
 * User: ysy
 * Date: 2015/8/3
 */
public class MusicReceiver extends RecordOperationAppBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String artist=intent.getStringExtra("artist");
        String album = intent.getStringExtra("album");
        String track = intent.getStringExtra("track");
        if(!TextUtils.isEmpty(track)){
            MusicInfo musicInfo = new MusicInfo();
            musicInfo.setArtist(artist);
            musicInfo.setAlbum(album);
            musicInfo.setTrack(track);
            AppLogger.d("Music" + artist + ":" + album + ":" + track);
            GlobalContext.getInstance().updateMusicInfo(musicInfo);
        }
    }
}
