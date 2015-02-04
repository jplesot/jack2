/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.compile.androidtree.dalvik.finalizer;

import com.android.jack.TestTools;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore("Tree")
public class FinalizerTest {

  private static File[] BOOTCLASSPATH;

  @BeforeClass
  public static void setUpClass() {
    FinalizerTest.class.getClassLoader().setDefaultAssertionStatus(true);
    BOOTCLASSPATH = new File[] {TestTools.getFromAndroidTree(
        "out/target/common/obj/JAVA_LIBRARIES/core-libart_intermediates/classes.jack")};
  }

  @Test
  public void compileFinalizer() throws Exception {
    AndroidToolchain toolchain = AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    toolchain.addToClasspath(BOOTCLASSPATH)
    .srcToExe(
        AbstractTestTools.createTempFile("out", ".zip"),
        /* zipFile = */ true,
        TestTools.getArtTestFolder("036-finalizer"));
  }

}
