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
package aria.apache.commons.net.ftp;

import aria.apache.commons.net.io.CopyStreamListener;

/**
 * Created by AriaL on 2017/9/26.
 * ftp 上传文件流事件监听
 */
public interface OnFtpInputStreamListener {
  /**
   * {@link CopyStreamListener#bytesTransferred(long, int, long)}
   *
   * @param totalBytesTransferred 已经上传的文件长度
   * @param bytesTransferred 上传byte长度
   */
  void onFtpInputStream(FTPClient client, long totalBytesTransferred, int bytesTransferred,
      long streamSize);
}
