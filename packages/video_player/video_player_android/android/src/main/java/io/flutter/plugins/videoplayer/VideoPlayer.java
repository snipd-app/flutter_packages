// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import static androidx.media3.common.Player.REPEAT_MODE_ALL;
import static androidx.media3.common.Player.REPEAT_MODE_OFF;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import io.flutter.plugins.videoplayer.Messages.PlattformVideoPlaybackOptions;

/**
 * A class responsible for managing video playback using {@link ExoPlayer}.
 *
 * <p>It provides methods to control playback, adjust volume, and handle seeking.
 */
public abstract class VideoPlayer {
  @NonNull private final ExoPlayerProvider exoPlayerProvider;
  @NonNull private final MediaItem mediaItem;
  @NonNull private final VideoPlayerOptions options;
  @NonNull protected final VideoPlayerCallbacks videoPlayerEvents;
  @NonNull protected ExoPlayer exoPlayer;
  @NonNull private final PlattformVideoPlaybackOptions playbackOptions;

  /** A closure-compatible signature since {@link java.util.function.Supplier} is API level 24. */
  public interface ExoPlayerProvider {
    /**
     * Returns a new {@link ExoPlayer}.
     *
     * @return new instance.
     */
    @OptIn(markerClass = UnstableApi.class) @NonNull
    ExoPlayer get(LoadControl loadControl);
  }

  public VideoPlayer(
      @NonNull VideoPlayerCallbacks events,
      @NonNull MediaItem mediaItem,
      @NonNull VideoPlayerOptions options,
      @NonNull ExoPlayerProvider exoPlayerProvider,
      @NonNull PlattformVideoPlaybackOptions playbackOptions) {
    this.videoPlayerEvents = events;
    this.mediaItem = mediaItem;
    this.options = options;
    this.exoPlayerProvider = exoPlayerProvider;
    this.playbackOptions = playbackOptions;
    this.exoPlayer = createVideoPlayer();
  }

  @NonNull
  protected ExoPlayer createVideoPlayer() {
    int bufferDuration  = playbackOptions.getMaxBufferDurationSeconds().intValue() * 1000;

    @SuppressLint("UnsafeOptInUsageError")
    ExoPlayer exoPlayer = exoPlayerProvider.get(new DefaultLoadControl.Builder()
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .setBufferDurationsMs(bufferDuration, bufferDuration, 2000, 2000)
            .build());
    exoPlayer.setMediaItem(mediaItem);
    exoPlayer.prepare();

    exoPlayer.addListener(createExoPlayerEventListener(exoPlayer));
    setAudioAttributes(exoPlayer, options.mixWithOthers);

    return exoPlayer;
  }

  @NonNull
  protected abstract ExoPlayerEventListener createExoPlayerEventListener(
      @NonNull ExoPlayer exoPlayer);

  void sendBufferingUpdate() {
    videoPlayerEvents.onBufferingUpdate(exoPlayer.getBufferedPosition());
  }

  private static void setAudioAttributes(ExoPlayer exoPlayer, boolean isMixMode) {
    exoPlayer.setAudioAttributes(
        new AudioAttributes.Builder().setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),
        !isMixMode);
  }

  void play() {
    exoPlayer.play();
  }

  void pause() {
    exoPlayer.pause();
  }

  void setLooping(boolean value) {
    exoPlayer.setRepeatMode(value ? REPEAT_MODE_ALL : REPEAT_MODE_OFF);
  }

  void setVolume(double value) {
    float bracketedValue = (float) Math.max(0.0, Math.min(1.0, value));
    exoPlayer.setVolume(bracketedValue);
  }

  void setPlaybackSpeed(double value) {
    // We do not need to consider pitch and skipSilence for now as we do not handle them and
    // therefore never diverge from the default values.
    final PlaybackParameters playbackParameters = new PlaybackParameters(((float) value));

    exoPlayer.setPlaybackParameters(playbackParameters);
  }

  void setMaxBufferDuration(long bufferDurationSeconds) {
//    exoPlayer.setPlaybackParameters(playbackParameters);
  }

  void seekTo(int location) {
    exoPlayer.seekTo(location);
  }

  long getPosition() {
    return exoPlayer.getCurrentPosition();
  }

  @NonNull
  public ExoPlayer getExoPlayer() {
    return exoPlayer;
  }

  public void dispose() {
    exoPlayer.release();
  }
}
