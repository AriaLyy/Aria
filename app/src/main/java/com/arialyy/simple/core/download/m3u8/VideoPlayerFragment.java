package com.arialyy.simple.core.download.m3u8;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.SeekBar;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.util.ALog;
import com.arialyy.frame.base.BaseFragment;
import com.arialyy.simple.R;
import com.arialyy.simple.common.LoadingDialog;
import com.arialyy.simple.databinding.FragmentVideoPlayerBinding;
import com.arialyy.simple.to.PeerIndex;
import java.io.IOException;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

@SuppressLint("ValidFragment")
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class VideoPlayerFragment extends BaseFragment<FragmentVideoPlayerBinding> {

  private M3U8VodModule mModule;
  private DownloadEntity mEntity;
  private int mPeerIndex;
  private SparseArray<String> mPlayers = new SparseArray<>();
  private SurfaceHolder mSurfaceHolder;
  private NextMediaPlayer mNextPlayer;
  private MediaPlayer mCurrentPlayer;
  private LoadingDialog mLoadingDialog;
  private int mJumpIndex = -1;
  private boolean needPaint= true;

  VideoPlayerFragment(int peerIndex, DownloadEntity entity) {
    mEntity = entity;
    mPeerIndex = peerIndex;
    M3U8Entity m3U8Entity = entity.getM3U8Entity();
    if (m3U8Entity == null) {
      return;
    }
    List<M3U8Entity.PeerInfo> peerInfos = m3U8Entity.getCompletedPeer();
    if (peerInfos != null) {
      for (M3U8Entity.PeerInfo info : peerInfos) {
        mPlayers.put(info.peerId, info.peerPath);
      }
    }
  }

  @Override protected void init(Bundle savedInstanceState) {
    mModule = ViewModelProviders.of(this).get(M3U8VodModule.class);

    getBinding().surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        //if (needPaint){
        //  needPaint = false;
        //  Canvas canvas = holder.lockCanvas();
        //  canvas.drawColor(Color.BLACK);
        //  holder.unlockCanvasAndPost(canvas);
        //}
        startNewPlayer(mPlayers.valueAt(0));
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

      }

      @Override public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCurrentPlayer != null) {
          mCurrentPlayer.release();
        }
        if (mNextPlayer != null) {
          mNextPlayer.getPlayer().release();
        }
      }
    });
    if (mEntity.getM3U8Entity() != null) {
      getBinding().seekBar.setMax(mEntity.getM3U8Entity().getPeerNum());
    }
    getBinding().seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
        PeerIndex index = new PeerIndex();
        index.index = seekBar.getProgress();
        mJumpIndex = index.index;
        EventBus.getDefault().post(index);
        if (mCurrentPlayer != null && mCurrentPlayer.isPlaying()) {
          mCurrentPlayer.stop();
          mCurrentPlayer.setDisplay(null);
          mCurrentPlayer.release();
        }
        showLoadingDialog();
      }
    });

    getBinding().controlBt.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        if (mCurrentPlayer != null) {
          if (mCurrentPlayer.isPlaying()) {
            getBinding().controlBt.setSelected(true);
            mCurrentPlayer.start();
          } else {
            getBinding().controlBt.setSelected(false);
            mCurrentPlayer.stop();
          }
        }
      }
    });
  }

  private void startNewPlayer(String source) {
    if (TextUtils.isEmpty(source)) {
      ALog.e(TAG, "资源路径为空");
      return;
    }
    try {
      mCurrentPlayer = new MediaPlayer();
      mCurrentPlayer.setScreenOnWhilePlaying(true);
      mCurrentPlayer.setSurface(mSurfaceHolder.getSurface());
      mCurrentPlayer.setDataSource(source);
      mCurrentPlayer.prepareAsync();
      mCurrentPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override public void onPrepared(MediaPlayer mp) {
          mp.start();
          setNextMediaPlayer(mp);
        }
      });

      mCurrentPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        @Override public void onCompletion(MediaPlayer mp) {
          mp.setDisplay(null);
          mNextPlayer.getPlayer().setSurface(mSurfaceHolder.getSurface());
          mNextPlayer.start();
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 设置下一个分片
   */
  private void setNextMediaPlayer(final MediaPlayer lastPlayer) {
    mPeerIndex++;
    String nextPeerPath = mPlayers.get(mPeerIndex);
    if (!TextUtils.isEmpty(nextPeerPath)) {
      mNextPlayer = new NextMediaPlayer(nextPeerPath);
      mNextPlayer.prepareAsync();

      mNextPlayer.setListener(new NextMediaPlayer.StateListener() {
        @Override public void onStart(MediaPlayer mp) {
          mCurrentPlayer = mp;
          setNextMediaPlayer(mp);
        }

        @Override public void onCompletion(MediaPlayer mp) {
          //mp.setDisplay(null);
          //mNextPlayer.getPlayer().setSurface(mSurfaceHolder.getSurface());
          mNextPlayer.start();
        }

        @Override public void onPrepared(MediaPlayer mp) {
          lastPlayer.setNextMediaPlayer(mNextPlayer.getPlayer());
        }
      });
    }
  }

  private void showLoadingDialog() {
    if (mLoadingDialog == null) {
      mLoadingDialog = new LoadingDialog(this);
    }
    if (!mLoadingDialog.isHidden()) {
      mLoadingDialog.show(getChildFragmentManager(), "mLoadingDialog");
    }
  }

  private void dismissDialog() {
    if (mLoadingDialog != null) {
      mLoadingDialog.dismiss();
    }
  }

  @Override protected int setLayoutId() {
    return R.layout.fragment_video_player;
  }

  public void addPlayer(int peerIndex, String peerPath) {
    mPlayers.put(peerIndex, peerPath);
    if (mJumpIndex != -1 && mJumpIndex == peerIndex) {
      startNewPlayer(mPlayers.get(peerIndex));
      dismissDialog();
    }
  }

  private static class NextMediaPlayer implements MediaPlayer.OnPreparedListener,
      MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private StateListener listener;

    public interface StateListener {
      void onStart(MediaPlayer mp);

      void onCompletion(MediaPlayer mp);

      void onPrepared(MediaPlayer mp);
    }

    private MediaPlayer player;
    private String videoPath;

    private NextMediaPlayer(String videoPath) {
      player = new MediaPlayer();
      player.setAudioStreamType(AudioManager.STREAM_MUSIC);
      player.setOnPreparedListener(this);
      player.setOnErrorListener(this);
      player.setOnCompletionListener(this);
      player.setScreenOnWhilePlaying(true);
      this.videoPath = videoPath;
      player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override public void onPrepared(MediaPlayer mp) {
          if (listener != null) {
            listener.onPrepared(mp);
          }
        }
      });
    }

    private void prepareAsync() {
      try {
        player.setDataSource(videoPath);
        player.prepareAsync();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public void setListener(StateListener listener) {
      this.listener = listener;
    }

    public String getVideoPath() {
      return videoPath;
    }

    public void start() {
      player.start();
      if (listener != null) {
        listener.onStart(player);
      }
    }

    @Override public void onCompletion(MediaPlayer mp) {
      if (listener != null) {
        listener.onCompletion(mp);
      }
    }

    @Override public boolean onError(MediaPlayer mp, int what, int extra) {
      return false;
    }

    @Override public void onPrepared(MediaPlayer mp) {
    }

    private MediaPlayer getPlayer() {
      return player;
    }
  }
}
