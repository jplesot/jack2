/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.android.jack.Options;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.vfs.VPath;

import javax.annotation.Nonnull;

/**
 * Common code used to write dex into a file or a zip file.
 */
public abstract class DexWriter {

  @Nonnull
  public static final String DEX_FILE_EXTENSION = ".dex";

  protected final boolean emitOneDexPerType = ThreadConfig.get(Options.GENERATE_ONE_DEX_PER_TYPE)
        .booleanValue();

  @Nonnull
  protected DexFile getDexFile(@Nonnull JSession session) {
    DexFileMarker dexFileMarker = session.getMarker(DexFileMarker.class);
    assert dexFileMarker != null;
    DexFile dexFile = dexFileMarker.getFinalDexFile();
    assert dexFile != null;
    return dexFile;
  }

  @Nonnull
  static VPath getFilePath(@Nonnull JDefinedClassOrInterface type) {
    return new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type) + DEX_FILE_EXTENSION,
        '/');
  }
}
