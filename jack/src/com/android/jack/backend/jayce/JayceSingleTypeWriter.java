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
import com.android.jack.JackFileException;
import com.android.jack.Options;
import com.android.jack.Options.Container;
import com.android.jack.backend.VDirPathFormatter;
import com.android.jack.experimental.incremental.CompilerState;
import com.android.jack.experimental.incremental.JackIncremental;
import com.android.jack.ir.JackFormatIr;
import com.android.jack.ir.NonJackFormatIr;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.jayce.JayceWriter;
import com.android.jack.scheduling.feature.JackFileOutput;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.direct.OutputDirectDir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Writer of Jayce files in a folder organized according to package names.
 */
@Description("Writer of Jayce files in a folder organized according to package names")
@Name("JayceSingleTypeWriter")
@Constraint(need = {JackFormatIr.class}, no = {NonJackFormatIr.class})
@Produce(JackFormatProduct.class)
@Support(JackFileOutput.class)
@Synchronized
public class JayceSingleTypeWriter implements RunnableSchedulable<JDefinedClassOrInterface> {

  @CheckForNull
  private final Directory outputDir;

  {
    if (ThreadConfig.get(Options.GENERATE_JACK_FILE).booleanValue() &&
        ThreadConfig.get(Options.JACK_OUTPUT_CONTAINER_TYPE) == Container.DIR) {
      outputDir = ThreadConfig.get(Options.JACK_FILE_OUTPUT_DIR);
    } else {
      outputDir = null;
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OutputVDir vDir = type.getSession().getOutputVDir();
    assert vDir != null;
    VDirPathFormatter formatter = new VDirPathFormatter(vDir);
    String filePath = getFilePath(type, formatter);
    OutputVFile vFile = vDir.createOutputVFile(filePath);


    try {
      OutputStream out = new BufferedOutputStream(vFile.openWrite());
      try {
        // Write to file
        JayceWriter writer = new JayceWriter(out);
        writer.write(type, "jack " + Jack.getVersionString());

        if (ThreadConfig.get(JackIncremental.GENERATE_COMPILER_STATE).booleanValue()) {
          assert vDir instanceof OutputDirectDir;
          assert outputDir != null;
          CompilerState csm = JackIncremental.getCompilerState();
          assert csm != null;
          csm.addMappingBetweenJavaAndJackFile(type.getSourceInfo().getFileName(),
              new File(outputDir.getFile(), filePath).getAbsolutePath());
        }
      } finally {
        out.close();
      }
    } catch (IOException e) {
      throw new JackFileException("Could not write Jack file to output '" + vFile + "'", e);
    }
  }

  @Nonnull
  protected static String getFilePath(@Nonnull JDefinedClassOrInterface type,
      @Nonnull TypeFormatter formatter) {
    return formatter.getName(type) + JayceFileImporter.JAYCE_FILE_EXTENSION;
  }
}
