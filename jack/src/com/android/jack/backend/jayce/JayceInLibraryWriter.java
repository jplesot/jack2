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

package com.android.jack.backend.jayce;

import com.android.jack.Jack;
import com.android.jack.JackAbortException;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.jayce.JayceWriterFactory;
import com.android.jack.library.FileType;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryLocation;
import com.android.jack.library.LibraryWritingException;
import com.android.jack.library.OutputJackLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.reporting.Reporter.Severity;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.file.CannotCloseException;
import com.android.sched.util.file.CannotCreateFileException;
import com.android.sched.util.file.CannotReadException;
import com.android.sched.util.file.WrongPermissionException;
import com.android.sched.util.location.Location;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * An abstract class to write Jayce files in a library organized according to package names.
 */
public abstract class JayceInLibraryWriter
    implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final OutputJackLibrary outputJackLibrary;

  {
    OutputJackLibrary ojl = Jack.getSession().getJackOutputLibrary();
    assert ojl != null;
    this.outputJackLibrary = ojl;
  }

  @Synchronized
  public boolean needsSynchronization() {
    return outputJackLibrary.needsSequentialWriting();
  }

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    Location loc = type.getLocation();
    if (loc instanceof TypeInInputLibraryLocation) {
      InputLibrary inputLibrary = ((TypeInInputLibraryLocation) loc).getInputLibrary();
      LibraryLocation inputLibraryLocation = inputLibrary.getLocation();
      if (inputLibrary.containsFileType(FileType.JAYCE)) {
        if (outputJackLibrary.containsLibraryLocation(inputLibraryLocation)) {
          return;
        }
      }
    }

    try {
      OutputVFile vFile = outputJackLibrary.createFile(FileType.JAYCE,
          new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
      try (OutputStream out = new BufferedOutputStream(vFile.getOutputStream())) {
        try {
          JayceWriterFactory.get(outputJackLibrary, out).write(type);
        } catch (IOException e) {
          throw new CannotReadException(vFile, e);
        }
      } catch (IOException e) {
        throw new CannotCloseException(vFile, e);
      }
    } catch (CannotReadException | CannotCloseException | CannotCreateFileException
        | WrongPermissionException e) {
      LibraryWritingException reportable =
          new LibraryWritingException(new LibraryIOException(outputJackLibrary.getLocation(), e));
      Jack.getSession().getReporter().report(Severity.FATAL, reportable);
      throw new JackAbortException(reportable);
    }
  }

  @Nonnull
  protected VPath getFilePath(@Nonnull JDefinedClassOrInterface type) {
    return new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type)
        + JayceFileImporter.JAYCE_FILE_EXTENSION, '/');
  }
}
