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
package com.arialyy.simple.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.arialyy.aria.core.download.DownloadEntity;
import com.arialyy.simple.R;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by lyy on 2017/7/17.
 */
public class SubStateLinearLayout extends LinearLayout implements View.OnClickListener {
  private final String TAG = "SubStateLinearLayout";

  interface OnShowCallback {
    void onShow(boolean visibility);
  }

  public interface OnItemClickListener {
    void onItemClick(int position, View view);
  }

  OnShowCallback mShowCallback;
  OnItemClickListener mItemClickListener;

  List<DownloadEntity> mSubData = new LinkedList<>();
  Map<String, Integer> mPosition = new WeakHashMap<>();
  SparseArray<View> mViews = new SparseArray<>();

  public SubStateLinearLayout(Context context) {
    super(context);
    setOrientation(VERTICAL);
  }

  public SubStateLinearLayout(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setOrientation(VERTICAL);
  }

  public SubStateLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOrientation(VERTICAL);
  }

  public void addData(List<DownloadEntity> datas) {
    removeAllViews();
    mSubData.clear();
    mSubData.addAll(datas);
    createShowView();
    int i = 1;
    for (DownloadEntity entity : datas) {
      TextView view = createView(i - 1, entity);
      mPosition.put(entity.getFilePath(), i);
      addView(view, i);
      i++;
    }
  }

  @Override public void onClick(View v) {
    if (mItemClickListener != null) {
      mItemClickListener.onItemClick(mViews.indexOfValue(v), v);
    }
  }

  public void setOnShowCallback(OnShowCallback callback) {
    this.mShowCallback = callback;
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.mItemClickListener = listener;
  }

  public List<DownloadEntity> getSubData() {
    return mSubData;
  }

  public void updateChildProgress(List<DownloadEntity> entities) {
    for (DownloadEntity entity : entities) {
      Integer i = mPosition.get(entity.getFilePath());
      if (i == null) return;
      int position = i;
      if (position != -1) {
        TextView child = ((TextView) getChildAt(position));
        int p = getPercent(entity);
        child.setText(entity.getFileName() + ": " + p + "%" + "   | " + entity.getConvertSpeed());
        child.invalidate();
      }
    }
  }

  public void updateChildState(DownloadEntity entity) {
    Integer i = mPosition.get(entity.getFilePath());
    if (i == null) return;
    int position = i;
    if (position != -1) {
      TextView child = ((TextView) getChildAt(position));
      if (entity.isComplete()) {
        child.setText(entity.getFileName() + " | " + getResources().getText(R.string.complete));
      } else {
        int p = getPercent(entity);
        child.setText(entity.getFileName() + ": " + p + "%" + "   | " + entity.getConvertSpeed());
      }
      child.invalidate();
    }
  }

  private TextView createView(int position, DownloadEntity entity) {
    TextView view =
        (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_child_state, null);
    view.setText(entity.getFileName() + ": " + getPercent(entity) + "%");
    view.setOnClickListener(this);
    mViews.append(position, view);
    return view;
  }

  private void createShowView() {

    TextView view =
        (TextView) LayoutInflater.from(getContext()).inflate(R.layout.layout_child_state, null);
    view.setText("点击显示子任务");
    view.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        int visibility = getChildAt(1).getVisibility();
        if (visibility == GONE) {
          showChild(true);
          ((TextView) v).setText("点击隐藏子任务");
        } else {
          showChild(false);
          ((TextView) v).setText("点击显示子任务");
        }
      }
    });
    addView(view, 0);
  }

  private void showChild(boolean show) {
    for (int i = 1, count = getChildCount(); i < count; i++) {
      getChildAt(i).setVisibility(show ? VISIBLE : GONE);
      invalidate();
    }
  }

  private int getPercent(DownloadEntity entity) {
    long size = entity.getFileSize();
    long progress = entity.getCurrentProgress();
    return size == 0 ? 0 : (int) (progress * 100 / size);
  }
}
