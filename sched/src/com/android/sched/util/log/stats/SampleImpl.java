/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.sched.util.log.stats;

import com.android.sched.util.findbugs.SuppressFBWarnings;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Simple statistic computation on a set of values.
 */
// Ignore: "Inconsistent synchronization"
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public class SampleImpl extends Sample {
  @Nonnegative
  private int validCount;
  @Nonnegative
  private int nanCount;

  private double min = Double.POSITIVE_INFINITY;
  @CheckForNull
  private Object minObject;

  private double max = Double.NEGATIVE_INFINITY;
  @CheckForNull
  private Object maxObject;
  private double total;

  protected SampleImpl(@Nonnull StatisticId<? extends Statistic> id) {
    super(id);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public synchronized void add(double value) {
    add(value, null);
  }

  @Override
  public synchronized void add(double value, @CheckForNull Object obj) {
    if (!Double.isNaN(value)) {
      if (value < min) {
        min = value;
        minObject = obj;
      }
      if (value > max) {
        max = value;
        maxObject = obj;
      }

      total += value;
      validCount++;
    } else {
      nanCount++;
    }
  }

  @Override
  @Nonnegative
  public int getCount() {
    return validCount;
  }

  @Override
  @Nonnegative
  public int getNaNCount() {
    return nanCount;
  }

  @Override
  public double getTotal() {
    return total;
  }

  @Override
  public double getMin() {
    return min;
  }

  @Override
  public synchronized double getAverage() {
    return total / validCount;
  }

  @Override
  public double getMax() {
    return max;
  }

  @Override
  @CheckForNull
  public Object getMinObject() {
    return minObject;

  }

  @Override
  @CheckForNull
  public Object getMaxObject() {
    return maxObject;
  }

  @Override
  public synchronized void merge(@Nonnull Statistic statistic) {
    SampleImpl samples = (SampleImpl) statistic;

    synchronized (samples) {
      this.validCount += samples.validCount;
      this.nanCount += samples.nanCount;
      this.total += samples.total;

      if (samples.min < this.min) {
        this.min = samples.min;
        this.minObject = samples.minObject;
      }
      if (samples.max > this.max) {
        this.max = samples.max;
        this.maxObject = samples.maxObject;
      }
    }
  }
}
