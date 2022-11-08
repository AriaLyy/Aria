/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.util;

/**
 * 速度限制
 */
public class BandwidthLimiter {
  public static int maxBandWith = 2 * 1024; //KB

  /* KB */
  private static Long KB = 1024L;
  /* The smallest count chunk length in bytes */
  private static Long CHUNK_LENGTH = 1024L;
  /* How many bytes will be sent or receive */
  private int bytesWillBeSentOrReceive = 0;
  /* When the last piece was sent or receive */
  private long lastPieceSentOrReceiveTick = System.nanoTime();
  /* Default rate is 1024KB/s */
  private int maxRate = 1024;
  /* Time cost for sending CHUNK_LENGTH bytes in nanoseconds */
  private long timeCostPerChunk = (1000000000L * CHUNK_LENGTH)
      / (this.maxRate * KB);

  /**
   * Initialize a BandwidthLimiter object with a certain rate.
   *
   * @param maxRate the download or upload speed in KBytes
   */
  public BandwidthLimiter(int maxRate, int threadNum) {
    if (threadNum > 1) {
      maxRate = maxRate / threadNum;
    }
    this.setMaxRate(maxRate);
  }

  /**
   * Set the max upload or download rate in KB/s. maxRate must be grater than
   * 0. If maxRate is zero, it means there is no bandwidth limit.
   *
   * @param maxRate If maxRate is zero, it means there is no bandwidth limit.
   * @throws IllegalArgumentException
   */
  public synchronized void setMaxRate(int maxRate)
      throws IllegalArgumentException {
    if (maxRate < 0) {
      throw new IllegalArgumentException("maxRate can not less than 0");
    }
    this.maxRate = maxRate;
    if (maxRate == 0) {
      this.timeCostPerChunk = 0;
    } else {
      this.timeCostPerChunk = (1000000000L * CHUNK_LENGTH)
          / (this.maxRate * KB);
    }
  }

  /**
   * Next 1 byte should do bandwidth limit.
   */
  public synchronized void limitNextBytes() {
    this.limitNextBytes(1);
  }

  /**
   * Next len bytes should do bandwidth limit
   */
  public synchronized void limitNextBytes(int len) {
    this.bytesWillBeSentOrReceive += len;

    /* We have sent CHUNK_LENGTH bytes */
    while (!Thread.currentThread().isInterrupted() && this.bytesWillBeSentOrReceive > CHUNK_LENGTH) {
      long nowTick = System.nanoTime();
      long missedTime = this.timeCostPerChunk
          - (nowTick - this.lastPieceSentOrReceiveTick);
      if (missedTime > 0) {
        try {
          Thread.currentThread().sleep(missedTime / 1000000,
              (int) (missedTime % 1000000));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      this.bytesWillBeSentOrReceive -= CHUNK_LENGTH;
      this.lastPieceSentOrReceiveTick = nowTick
          + (missedTime > 0 ? missedTime : 0);
    }
  }
}