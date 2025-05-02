// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.videoplayer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import io.flutter.plugins.videoplayer.Messages.PlattformVideoPlaybackOptions;;

final class LocalVideoAsset extends VideoAsset {
  LocalVideoAsset(@NonNull String assetUrl, @NonNull PlattformVideoPlaybackOptions playbackOptions) {
    super(assetUrl, playbackOptions);
  }

  @NonNull
  @Override
  public MediaItem getMediaItem() {
    return getItemBuilder().setUri(assetUrl).build();
  }

  @NonNull
  @Override
  public MediaSource.Factory getMediaSourceFactory(@NonNull Context context) {
    return new DefaultMediaSourceFactory(context);
  }
}
