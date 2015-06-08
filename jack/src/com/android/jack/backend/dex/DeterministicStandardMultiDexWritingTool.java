/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.jack.backend.dex;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.tools.merger.ConstantManager;
import com.android.jack.tools.merger.JackMerger;
import com.android.sched.util.codec.ImplementationName;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, in several dex
 * files. It is assumed that all types marked for MainDex will be submitted before any not marked
 * type. Dex files emited are deterministic.
 */
@ImplementationName(iface = DexWritingTool.class, name = "deter-multidex",
    description = "allow deterministically emitting several dex files")
public class DeterministicStandardMultiDexWritingTool extends StandardMultiDexWritingTool {

  @Override
  @Nonnull
  protected MergingManager getManager() {
    return new DeterministicMergingManager();
  }

  @Override
  @Nonnull
  protected JackMerger createMainMerger(int numberOfMainTypes) {
    return manager.getIterator().next(ConstantManager.FIRST_DETERMINISTIC_MODE_INDEX);
  }

  @Override
  protected void sortAndPrepareInternal(@Nonnull ArrayList<JDefinedClassOrInterface> defaultList,
      @Nonnull ArrayList<JDefinedClassOrInterface> mainList) {
    Collections.sort(defaultList, nameComp);
    int number = ConstantManager.FIRST_DETERMINISTIC_MODE_INDEX;
    for (JDefinedClassOrInterface type : mainList) {
      type.addMarker(new NumberMarker(number++));
    }
    for (JDefinedClassOrInterface type : defaultList) {
      type.addMarker(new NumberMarker(number++));
    }
  }

}