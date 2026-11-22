package com.monte.montrix.service.music;
import android.content.*;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import java.util.*;
/** Owns media indexing, playback, seeking and music-derived presentation values. */
public final class MusicService {
 /** Immutable indexed song metadata. */ private static final class Track {/** Media identifier. */final long id;/** Display title. */final String title;/** Artist name. */final String artist;/** Initializes a track. */Track(long i,String t,String a){id=i;title=t;artist=a;}}
 /** Android context. */ private final Context context;
 /** Indexed tracks. */ private final ArrayList<Track> tracks=new ArrayList<>();
 /** Current media player. */ private MediaPlayer player;
 /** Current index. */ private int index=-1;
 /** Creates service. @param context Android context */ public MusicService(Context context){this.context=context.getApplicationContext();}
 /** Queries the device music library. @return track count */ public int scan(){tracks.clear();String[] columns={MediaStore.Audio.Media._ID,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST};try(Cursor c=context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,columns,MediaStore.Audio.Media.IS_MUSIC+"!=0",null,MediaStore.Audio.Media.TITLE+" COLLATE NOCASE")){if(c!=null)while(c.moveToNext())tracks.add(new Track(c.getLong(0),c.getString(1),c.getString(2)));}if(index>=tracks.size())index=-1;return tracks.size();}
 /** Starts or pauses the current song. */ public void toggle(){if(player!=null){if(player.isPlaying())player.pause();else player.start();return;}if(index<0&& !tracks.isEmpty())index=0;playSelected();}
 /** Advances to next song. */ public void next(){if(tracks.isEmpty())return;index=(index+1)%tracks.size();playSelected();}
 /** Selects previous song. */ public void previous(){if(tracks.isEmpty())return;index=(index<=0?tracks.size()-1:index-1);playSelected();}
 /** Seeks within current song. @param fraction position from 0 to 1000 */ public void seekFraction(int fraction){if(player==null)return;int d=player.getDuration();if(d>0)player.seekTo((int)((long)d*Math.max(0,Math.min(1000,fraction))/1000L));}
 /** Plays indexed selection, releasing prior player. */ private void playSelected(){releasePlayer();if(index<0||index>=tracks.size())return;try{Uri uri=ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,tracks.get(index).id);player=MediaPlayer.create(context,uri);if(player!=null){player.setOnCompletionListener(mp->next());player.start();}}catch(RuntimeException error){releasePlayer();}}
 /** Releases playback resources. */ private void releasePlayer(){if(player!=null){player.release();player=null;}}
 /** Returns current title. @return title */ public String title(){return index>=0&&index<tracks.size()?tracks.get(index).title:(tracks.isEmpty()?"No music indexed":"Select Play");}
 /** Returns artist. @return artist */ public String artist(){return index>=0&&index<tracks.size()?tracks.get(index).artist:"";}
 /** Returns current position. @return milliseconds */ public int position(){try{return player==null?0:player.getCurrentPosition();}catch(IllegalStateException e){return 0;}}
 /** Returns duration. @return milliseconds */ public int duration(){try{return player==null?0:player.getDuration();}catch(IllegalStateException e){return 0;}}
 /** Returns playing status. @return true if playing */ public boolean playing(){try{return player!=null&&player.isPlaying();}catch(IllegalStateException e){return false;}}
 /** Formats a duration for display. @param ms milliseconds @return mm:ss */ public static String format(int ms){int s=Math.max(0,ms/1000);return String.format(Locale.US,"%02d:%02d",s/60,s%60);}
 /** Releases resources. */ public void close(){releasePlayer();}
}
