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

package com.android.sched.vfs;

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotDeleteFileException;
import com.android.sched.util.file.CannotGetModificationTimeException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.CannotWriteException;
import com.android.sched.util.file.NoSuchFileException;
import com.android.sched.util.file.NotDirectoryException;
import com.android.sched.util.file.NotFileException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.stream.LocationByteStreamSucker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.Collection;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * A base implementation of a {@link VFS}.
 */
abstract class BaseVFS<DIR extends BaseVDir, FILE extends BaseVFile> implements VFS {

  @CheckForNull
  private Tracer tracer;

  protected boolean closed = false;

  @Override
  @Nonnull
  public abstract DIR getRootDir();

  //
  // Stream related
  //

  @Nonnull
  abstract InputStream openRead(@Nonnull FILE file) throws WrongPermissionException;

  @Nonnull
  abstract OutputStream openWrite(@Nonnull FILE file) throws WrongPermissionException;

  @Nonnull
  abstract OutputStream openWrite(@Nonnull FILE file, boolean append)
      throws WrongPermissionException;

  //
  // VElement related
  //

  @Nonnull
  abstract DIR getVDir(@Nonnull DIR parent, @Nonnull String name) throws NotDirectoryException,
  NoSuchFileException;

  @Nonnull
  abstract FILE getVFile(@Nonnull DIR parent, @Nonnull String name) throws NotFileException,
  NoSuchFileException;

  @Nonnull
  abstract DIR createVDir(@Nonnull DIR parent, @Nonnull String name)
    throws CannotCreateFileException;

  @Nonnull
  abstract FILE createVFile(@Nonnull DIR parent, @Nonnull String name)
      throws CannotCreateFileException;

  @Nonnull
  abstract void delete(@Nonnull FILE file) throws CannotDeleteFileException;

  @Nonnull
  abstract Collection<? extends BaseVElement> list(@Nonnull DIR dir);

  abstract boolean isEmpty(@Nonnull DIR dir);

  @Nonnull
  abstract VPath getPathFromDir(@Nonnull DIR parent, @Nonnull FILE file);

  @Nonnull
  abstract VPath getPathFromRoot(@Nonnull FILE file);

  @Nonnull
  abstract FileTime getLastModified(@Nonnull FILE file) throws CannotGetModificationTimeException;

  //
  // Location related
  //

  @Nonnull
  abstract Location getVFileLocation(@Nonnull FILE file);

  @Nonnull
  abstract Location getVFileLocation(@Nonnull DIR parent, @Nonnull String name);

  @Nonnull
  abstract Location getVFileLocation(@Nonnull DIR parent, @Nonnull VPath path);

  @Nonnull
  abstract Location getVDirLocation(@Nonnull DIR dir);

  @Nonnull
  abstract Location getVDirLocation(@Nonnull DIR parent, @Nonnull String name);

  @Nonnull
  abstract Location getVDirLocation(@Nonnull DIR parent, @Nonnull VPath path);

  //
  // Misc
  //
  @Override
  public synchronized boolean isClosed() {
    return closed;
  }

  @Override
  @CheckForNull
  public String getDigest() {
    return null;
  }

  public void copy(@Nonnull VFile srcFile, @Nonnull FILE dstFile) throws WrongPermissionException,
      CannotCloseException, CannotReadException, CannotWriteException {

    try (InputStream is = srcFile.getInputStream()) {
      try (OutputStream os = dstFile.getOutputStream()) {
        new LocationByteStreamSucker(is, os, srcFile, dstFile).suck();
      } catch (IOException e) {
        throw new CannotCloseException(dstFile, e);
      }
    } catch (IOException e) {
      throw new CannotCloseException(srcFile, e);
    }
    VFSStatCategory.OPTIMIZED_COPIES.getPercentStat(getTracer(), getInfoString()).addFalse();
  }

  @CheckForNull
  protected Tracer getTracer() {
    // lazy because the tracer may not be available yet when initializing the VFS
    if (tracer == null) {
      try {
        tracer = TracerFactory.getTracer();
      } catch (ConfigurationError e) {
        // ignore and return null
      }
    }
    return tracer;
  }
}
