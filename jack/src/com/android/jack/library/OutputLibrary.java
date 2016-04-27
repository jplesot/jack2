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

package com.android.jack.library;

import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.vfs.InputOutputVFile;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Library generated by Jack.
 */
public interface OutputLibrary extends Library {

  public boolean needsSequentialWriting();

  public OutputVFile createFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws CannotCreateFileException;

  @Override
  @Nonnull
  public LibraryLocation getLocation();

  public boolean containsFileType(@Nonnull FileType fileType);

  @Nonnull
  public Iterator<InputVFile> iterator(@Nonnull FileType fileType);

  @Nonnull
  public InputOutputVFile getFile(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws FileTypeDoesNotExistException;

  @Nonnull
  public void delete(@Nonnull FileType fileType, @Nonnull VPath typePath)
      throws CannotDeleteFileException, FileTypeDoesNotExistException;
}
