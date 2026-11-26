package com.monte.montrix.service.radio;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
/** Emulator-only HTTP PCM player. Owns audio I/O independently of MusicService. */
public final class EmulatorRadioAudioService {
 private volatile boolean running; private Thread worker; private volatile HttpURLConnection connection;
 /** Starts a new live 48-kHz mono PCM stream. */
 public synchronized void start(){if(running)return;running=true;worker=new Thread(this::run,"MontrixRadioPCM");worker.start();}
 /** Stops streaming, disconnects blocking reads, and releases the track on worker exit. */
 public synchronized void stop(){running=false;HttpURLConnection c=connection;if(c!=null)c.disconnect();Thread t=worker;if(t!=null)t.interrupt();}
 /** Restarts the stream after tuning because the server replaces its rtl_fm process. */
 public void retune(){stop();new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::start,450);}
 private void run(){AudioTrack track=null;HttpURLConnection c=null;try{
 c=(HttpURLConnection)new URL("http://127.0.0.1:8765/rtl/audio.wav").openConnection();connection=c;c.setConnectTimeout(2500);c.setReadTimeout(3500);
 if(c.getResponseCode()!=200)throw new IllegalStateException("SDR audio HTTP "+c.getResponseCode());
 int min=AudioTrack.getMinBufferSize(48000,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);
 track=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(48000).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(Math.max(min*4,32768)).setTransferMode(AudioTrack.MODE_STREAM).build();
 if(track.getState()!=AudioTrack.STATE_INITIALIZED)throw new IllegalStateException("AudioTrack initialization failed");
 track.play();try(InputStream in=c.getInputStream()){byte[] header=new byte[44];int pos=0;while(pos<44){int n=in.read(header,pos,44-pos);if(n<0)throw new IllegalStateException("Incomplete WAV header");pos+=n;}
 if(header[0]!='R'||header[1]!='I'||header[2]!='F'||header[3]!='F')throw new IllegalStateException("Invalid WAV header");
 byte[] buf=new byte[8192];while(running&&!Thread.currentThread().isInterrupted()){int n=in.read(buf);if(n<0)break;int offset=0;while(offset<n&&running){int wrote=track.write(buf,offset,n-offset);if(wrote<=0)throw new IllegalStateException("AudioTrack write "+wrote);offset+=wrote;}}}
 }catch(Exception e){if(running)Log.e("MontrixRadioPCM","SDR audio failed",e);}finally{if(track!=null){try{track.pause();track.flush();track.release();}catch(Exception ignored){}}if(c!=null)c.disconnect();if(connection==c)connection=null;}}
}
