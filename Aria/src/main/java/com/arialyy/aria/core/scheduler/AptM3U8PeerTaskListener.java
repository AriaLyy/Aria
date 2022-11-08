/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.core.scheduler;

/**
 * Created by Aria.Lao on 2019/6/26.
 * m3u8切片事件回调类
 */
public class AptM3U8PeerTaskListener implements M3U8PeerTaskListener, ISchedulerListener{

  @Override public void onPeerStart(final String m3u8Url, final String peerPath, final int peerIndex) {
  }

  @Override public void onPeerComplete(final String m3u8Url, final String peerPath, final int peerIndex) {
  }

  @Override public void onPeerFail(final String m3u8Url, final String peerPath, final int peerIndex) {
  }

  @Override public void setListener(Object obj) {

  }
}