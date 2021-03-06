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

package com.android.jack.annotation;

import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.AnnotationBuilder;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.ir.ast.JDefinedAnnotationType;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.category.SlowTests;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.junit.KnownIssue;
import com.android.jack.test.junit.Runtime;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchainBase;
import com.android.jack.test.toolchain.JackApiV01;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.jack.test.toolchain.JillBasedToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.sched.util.RunnableHooks;

import junit.framework.Assert;

import org.jf.dexlib.AnnotationItem;
import org.jf.dexlib.ClassDefItem;
import org.jf.dexlib.DexFile;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;

public class AnnotationTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test001"),
    "com.android.jack.annotation.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test002"),
    "com.android.jack.annotation.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test003"),
    "com.android.jack.annotation.test003.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test005"),
    "com.android.jack.annotation.test005.dx.Tests");

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test006"),
    "com.android.jack.annotation.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test007"),
    "com.android.jack.annotation.test007.dx.Tests");

  private RuntimeTestInfo TEST008 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test008"),
    "com.android.jack.annotation.test008.dx.Tests");

  private RuntimeTestInfo TEST009 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.annotation.test009"),
    "com.android.jack.annotation.test009.dx.Tests");

  private RuntimeTestInfo TEST018 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.annotation.test018"),
      "com.android.jack.annotation.test018.dx.Tests");

  private RuntimeTestInfo TEST019 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.annotation.test019"),
      "com.android.jack.annotation.test019.jack.Tests");

  private static final File ANNOTATION001_PATH =
      AbstractTestTools.getTestRootDir("com.android.jack.annotation.test001.jack");

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  public void test001_2() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        new File[] {
          new File(ANNOTATION001_PATH, "Annotation8.java"),
          new File(ANNOTATION001_PATH, "Annotated2.java")});
  }


  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test003() throws Exception {
    new RuntimeTestHelper(TEST003).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  public void test003_1() throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test003.jack"));
    helper.compare();
  }

  @Test
  public void test004() throws Exception {
    File testFolder = AbstractTestTools.getTestRootDir("com.android.jack.annotation.test004");
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath()).srcToExe(
        AbstractTestTools.createTempDir(),
        /* zipFile = */false, testFolder);
  }

  @Test
  public void test004_1() throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test004.jack"));
    helper.compare();
  }

  @Test
  @Runtime
  // Annotation on package are not supported in dex format: http://code.google.com/p/android/issues/detail?id=16149
  @Category(RuntimeRegressionTest.class)
  @KnownIssue
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005).compileAndRunTest();
  }

  @Test
  public void test005_1() throws Exception {
    CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test005.jack"));
    helper.compare();
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test006() throws Exception {
    new RuntimeTestHelper(TEST006).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test007() throws Exception {
    new RuntimeTestHelper(TEST007).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test008() throws Exception {
    new RuntimeTestHelper(TEST008).compileAndRunTest(/* checkStructure = */ true);
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test009() throws Exception {
    new RuntimeTestHelper(TEST009).compileAndRunTest(/* checkStructure = */ true);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  public void test010() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test010.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  public void test011() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test011.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  public void test012() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test012.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  public void test013() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test013.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  public void test014() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test014.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  @KnownIssue
  public void test015() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test015.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  @KnownIssue(candidate=JillBasedToolchain.class)
  public void test015_jill() throws Exception {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JillBasedToolchain.class);
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test015.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  public void test016() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test016.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * Check there is no phantom field created while loading the library of annotation and enum.
   */
  @Test
  @Category(SlowTests.class)
  @KnownIssue
  public void test016_2steps() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File jackOutFile = AbstractTestTools.createTempFile("annotationWithEnum16", ".jack");
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test016.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToLib(jackOutFile, /* zipFile = */ false, testSourceDir);


    File dexOutDir = AbstractTestTools.createTempDir();
    RunnableHooks hooks = new RunnableHooks();
    try {
      Options options = new Options();
      options.setImportedLibraries(Collections.singletonList(jackOutFile));
      options.setOutputDir(dexOutDir);
      JSession session = TestTools.buildSession(options, hooks);
      JDefinedAnnotationType annnotationWithEnum =
          (JDefinedAnnotationType) session.getLookup().getType(
              "Lcom/android/jack/annotation/test016/jack/AnnotationWithEnum;");
      annnotationWithEnum.getMethods();
      JDefinedEnum annotatedEnum =
          (JDefinedEnum) session.getLookup().getType(
              "Lcom/android/jack/annotation/test016/jack/AnnotatedEnum;");
      JVisitor phantomChecker = new JVisitor() {
        @Override
        public void endVisit(JEnumLiteral enumLiteral) {
          Assert.assertTrue(enumLiteral.getFieldId().getField() != null);
        }
      };
      phantomChecker.accept(annnotationWithEnum);
      phantomChecker.accept(annotatedEnum);
    } finally {
      hooks.runHooks();
    }
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  @KnownIssue
  public void test017() throws Exception {
    // JackApiToolchainBase required for ordered-filter
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchainBase.class);
    toolchain.addProperty(Options.INPUT_FILTER.getName(), "ordered-filter");
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test017.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  /**
   * About annotation with enum and its usage.
   */
  @Test
  @Category(SlowTests.class)
  @KnownIssue(candidate=JillBasedToolchain.class)
  public void test017_jill() throws Exception {
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JillBasedToolchain.class);
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test017.jack");

    toolchain.addToClasspath(toolchain.getDefaultBootClasspath())
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);
  }

  @Test
  @Runtime
  @Category(RuntimeRegressionTest.class)
  public void test018() throws Exception {
    new RuntimeTestHelper(TEST018).compileAndRunTest();
  }

  /**
   * Test about type annotations and type parameter annotations.
   */
  @Test
  @Runtime
  @KnownIssue
  @Category(RuntimeRegressionTest.class)
  public void test019() throws Exception {
    new RuntimeTestHelper(TEST019)
    .setSourceLevel(SourceLevel.JAVA_8)
    .addIgnoredCandidateToolchain(JackApiV01.class)
    .compileAndRunTest();
  }

  /**
   * About annotation visibility.
   *
   * Tests that:
   * 1) we drop annotations with SOURCE retention.
   * 2) we drop or keep annotations with CLASS retention depending on a property.
   * 3) we keep annotations with RUNTIME retention.
   */
  @Test
  public void test020() throws Exception {
    File dexOutDir = AbstractTestTools.createTempDir();
    File testSourceDir =
        AbstractTestTools.getTestRootDir("com.android.jack.annotation.test020.jack");

    final String sourceAnnotationSignature =
        "Lcom/android/jack/annotation/test020/jack/SourceAnnotation;";
    final String classAnnotationSignature =
        "Lcom/android/jack/annotation/test020/jack/ClassAnnotation;";
    final String runtimeAnnotationSignature =
        "Lcom/android/jack/annotation/test020/jack/RuntimeAnnotation;";
    final String annotatedClassSignature =
        "Lcom/android/jack/annotation/test020/jack/AnnotatedClass;";

    // Check that annotations with CLASS retention are kept.
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.addProperty(AnnotationBuilder.CLASS_RETENTION.getName(), Boolean.toString(true))
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);

    Set<String> classes = getAnnotationsFromClass(dexOutDir, annotatedClassSignature);
    Assert.assertFalse(classes.contains(sourceAnnotationSignature));
    Assert.assertTrue(classes.contains(classAnnotationSignature));
    Assert.assertTrue(classes.contains(runtimeAnnotationSignature));

    // Check that annotations with CLASS retention are dropped.
    toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addToClasspath(toolchain.getDefaultBootClasspath());
    toolchain.addProperty(AnnotationBuilder.CLASS_RETENTION.getName(), Boolean.toString(false))
    .srcToExe(dexOutDir, /* zipFile = */ false, testSourceDir);

    classes = getAnnotationsFromClass(dexOutDir, annotatedClassSignature);
    Assert.assertFalse(classes.contains(sourceAnnotationSignature));
    Assert.assertFalse(classes.contains(classAnnotationSignature));
    Assert.assertTrue(classes.contains(runtimeAnnotationSignature));
  }

  private static Set<String> getAnnotationsFromClass(@Nonnull File dexOutDir,
      @Nonnull String annotatedClassSignature) throws IOException {
    DexFile dexFile = new DexFile(new File(dexOutDir, DexFileWriter.DEX_FILENAME));
    for (ClassDefItem classDefItem : dexFile.ClassDefsSection.getItems()) {
      if (annotatedClassSignature.equals(classDefItem.getClassType().getTypeDescriptor())) {
        AnnotationItem[] annotations =
            classDefItem.getAnnotations().getClassAnnotations().getAnnotations();
        Set<String> classes = new TreeSet<>();
        for (AnnotationItem annot : annotations) {
          classes.add(annot.getEncodedAnnotation().annotationType.getTypeDescriptor());
        }
        return classes;
      }
    }
    throw new AssertionError();
  }


  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
//    rtTestInfos.add(TEST005); // KnownIssue
    rtTestInfos.add(TEST006);
    rtTestInfos.add(TEST007);
    rtTestInfos.add(TEST008);
    rtTestInfos.add(TEST009);
    rtTestInfos.add(TEST018);
//    rtTestInfos.add(TEST019); // KnownIssue
  }
}
