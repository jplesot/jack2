/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.tools.jacoco;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageVisitor;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.internal.analysis.ClassCoverageImpl;
import org.jacoco.core.internal.analysis.CounterImpl;
import org.jacoco.core.internal.analysis.MethodCoverageImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Code coverage report analyzer.
 */
public class JackCoverageAnalyzer {
  @Nonnull
  private final ExecutionDataStore executionDataStore;

  @Nonnull
  private final ICoverageVisitor coverageVisitor;

  public JackCoverageAnalyzer(
      @Nonnull ExecutionDataStore executionDataStore, @Nonnull ICoverageVisitor coverageVisitor) {
    this.executionDataStore = executionDataStore;
    this.coverageVisitor = coverageVisitor;
  }

  /**
   * Reads the coverage description file and report each class to the {@link ICoverageVisitor}.
   *
   * @param jackCoverageDescriptionFile
   * @throws IOException
   */
  public void analyze(@Nonnull File jackCoverageDescriptionFile) throws IOException {
    if (!jackCoverageDescriptionFile.exists()) {
      throw new IllegalArgumentException(
          "File " + jackCoverageDescriptionFile + " does not exist.");
    }

    JsonReader jsonReader =
        new JsonReader(new InputStreamReader(new FileInputStream(jackCoverageDescriptionFile)));
    try {
      readClasses(jsonReader);
    } finally {
      jsonReader.close();
    }
  }

  private void readClasses(@Nonnull JsonReader jsonReader) throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      IClassCoverage classCoverage = readClass(jsonReader);
      coverageVisitor.visitCoverage(classCoverage);
    }
    jsonReader.endArray();
  }

  @Nonnull
  private IClassCoverage readClass(@Nonnull JsonReader jsonReader) throws IOException {
    long id = 0;
    String classSignature = null;
    String sourceFile = null;
    String superClassName = null;
    List<IMethodCoverage> methods = new ArrayList<IMethodCoverage>();
    List<ProbeDescription> probes = new ArrayList<ProbeDescription>();
    List<String> interfaces = new ArrayList<String>();

    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if ("id".equals(attributeName)) {
        id = jsonReader.nextLong();
      } else if ("name".equals(attributeName)) {
        classSignature = jsonReader.nextString();
      } else if ("sourceFile".equals(attributeName)) {
        sourceFile = jsonReader.nextString();
      } else if ("superClassName".equals(attributeName)) {
        superClassName = jsonReader.nextString();
      } else if ("interfaces".equals(attributeName)) {
        readInterfaces(jsonReader, interfaces);
      } else if ("methods".equals(attributeName)) {
        readMethods(jsonReader, methods);
      } else if ("probes".equals(attributeName)) {
        readProbes(jsonReader, probes, methods);
      } else {
        throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
      }
    }
    jsonReader.endObject();

    final ExecutionData executionData = executionDataStore.get(id);
    boolean noMatch;
    if (executionData != null) {
      noMatch = false;
      // Check there is no id collision.
      executionData.assertCompatibility(id, classSignature, probes.size());
    } else {
      noMatch = executionDataStore.contains(classSignature);
    }

    // Convert class signature to fully-qualified class name.
    String className = classSignature.substring(1, classSignature.length() - 1);

    // Build the class coverage.
    String[] interfacesArray = interfaces.toArray(new String[0]);
    ClassCoverageImpl c = new ClassCoverageImpl(
        className, id, noMatch, classSignature, superClassName, interfacesArray);
    c.setSourceFileName(sourceFile);

    // Update methods with probes.
    final boolean[] executionProbes = executionData != null ? executionData.getProbes() : null;
    for (ProbeDescription probe : probes) {
      final int probeIndex = probe.id;
      final boolean active = (executionProbes != null && executionProbes[probeIndex]);
      final MethodCoverageImpl methodCoverage = probe.method;
      for (ProbeDescription.Line line : probe.lines) {
        ICounter instructionCounter;
        ICounter branchCounter;
        if (active) {
          instructionCounter = CounterImpl.getInstance(0, line.instructionsCount);
          branchCounter = CounterImpl.getInstance(0, line.branchesCount);
        } else {
          instructionCounter = CounterImpl.getInstance(line.instructionsCount, 0);
          branchCounter = CounterImpl.getInstance(line.branchesCount, 0);
        }
        methodCoverage.increment(instructionCounter, branchCounter, line.line);
      }
    }

    // Now methods have been updated with probes, add them to the class coverage.
    for (IMethodCoverage method : methods) {
      c.addMethod(method);
    }

    return c;
  }

  private void readInterfaces(@Nonnull JsonReader jsonReader, @Nonnull List<String> interfaces)
      throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      interfaces.add(jsonReader.nextString());
    }
    jsonReader.endArray();
  }

  // Parses probes.
  private static void readProbes(@Nonnull JsonReader jsonReader,
      @Nonnull List<ProbeDescription> probes, @Nonnull List<? extends IMethodCoverage> methods)
      throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      probes.add(readProbe(jsonReader, methods));
    }
    jsonReader.endArray();
  }

  // Parses one probe.
  private static ProbeDescription readProbe(
      @Nonnull JsonReader jsonReader, @Nonnull List<? extends IMethodCoverage> methods)
      throws IOException {
    ProbeDescription probe = new ProbeDescription();
    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if ("id".equals(attributeName)) {
        probe.setId(jsonReader.nextInt());
      } else if ("method".equals(attributeName)) {
        int methodId = jsonReader.nextInt();
        probe.setMethod((MethodCoverageImpl) methods.get(methodId));
      } else if ("lines".equals(attributeName)) {
        readLines(jsonReader, probe);
      } else {
        throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
      }
    }
    jsonReader.endObject();
    return probe;
  }

  private static void readLines(@Nonnull JsonReader jsonReader, @Nonnull ProbeDescription probe)
      throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      jsonReader.beginObject();
      int line = -1;
      int instructionsCount = -1;
      int branchesCount = -1;
      while (jsonReader.hasNext()) {
        String attributeName = jsonReader.nextName();
        if ("line".equals(attributeName)) {
          line = jsonReader.nextInt();
        } else if ("insnCount".equals(attributeName)) {
          instructionsCount = jsonReader.nextInt();
        } else if ("branchCount".equals(attributeName)) {
          branchesCount = jsonReader.nextInt();
        } else {
          throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
        }
      }
      probe.addLine(line, instructionsCount, branchesCount);
      jsonReader.endObject();
    }
    jsonReader.endArray();
  }

  private static void readMethods(
      @Nonnull JsonReader jsonReader, @Nonnull List<IMethodCoverage> methods) throws IOException {
    jsonReader.beginArray();
    while (jsonReader.hasNext()) {
      methods.add(readMethod(jsonReader));
    }
    jsonReader.endArray();
  }

  @Nonnull
  private static IMethodCoverage readMethod(@Nonnull JsonReader jsonReader) throws IOException {
    int id = -1;
    String name = null;
    String desc = null;
    String signature = null;

    jsonReader.beginObject();
    while (jsonReader.hasNext()) {
      String attributeName = jsonReader.nextName();
      if ("id".equals(attributeName)) {
        id = jsonReader.nextInt();
      } else if ("name".equals(attributeName)) {
        name = jsonReader.nextString();
      } else if ("desc".equals(attributeName)) {
        desc = jsonReader.nextString();
      } else if ("signature".equals(attributeName)) {
        signature = jsonReader.nextString();
      } else {
        throw new JsonParseException("Unknown attribute \"" + attributeName + "\"");
      }
    }
    jsonReader.endObject();

    return new JackMethodCoverage(id, name, desc, signature);
  }
}
