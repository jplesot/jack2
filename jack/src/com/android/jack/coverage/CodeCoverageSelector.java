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

package com.android.jack.coverage;

import com.android.jack.digest.OriginDigestMarker;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.formatter.SourceFormatter;
import com.android.jack.shrob.obfuscation.OriginalNames;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nonnull;

/**
 * A schedulable that selects classes that needs to be instrumented based on a given filter.
 * All classes selected for code coverage are marked with a {@link CodeCoverageMarker} marker.
 */
@Description("Filters classes for code coverage")
@Support(CodeCoverage.class)
@Constraint(need = OriginalNames.class)
@Transform(add = CodeCoverageMarker.Initialized.class)
public class CodeCoverageSelector implements RunnableSchedulable<JDefinedClassOrInterface> {
  /**
   * The packages that are excluded from code coverage by default.
   */
  private static final String[] EXCLUDED_PACKAGES = {
      "org.jacoco", // JaCoCo
      "com.vladium.emma" // EMMA
  };

  @Override
  public void run(@Nonnull JDefinedClassOrInterface t) throws Exception {
    if (needsCoverage(t)) {
      long classId = computeClassID(t);
      t.addMarker(new CodeCoverageMarker(classId));
    }
  }

  private static boolean needsCoverage(@Nonnull JDefinedClassOrInterface declaredType) {
    if (declaredType.isExternal()) {
      // Do not instrument classes that will no be part of the output.
      return false;
    }
    if (declaredType instanceof JDefinedInterface) {
      // Interface are not covered.
      return false;
    }
    // Manage excluded packages.
    String typeName = SourceFormatter.getFormatter().getName(declaredType);
    for (String excludedPackage : EXCLUDED_PACKAGES) {
      if (typeName.startsWith(excludedPackage)) {
        return false;
      }
    }
    return true;
  }

  @Nonnull
  private static byte[] computeClassDigest(@Nonnull JDefinedClassOrInterface type)
      throws NoSuchAlgorithmException {
    OriginDigestMarker marker = type.getMarker(OriginDigestMarker.class);
    if (marker != null) {
      // Use the digest that has been already computed.
      return marker.getDigest();
    }
    // Fallback to compute digest based on the class name.
    // Note: this will cause conflicts in Jacoco if multiple classes with the same name are
    // instrumented at the same time.
    String className = SourceFormatter.getFormatter().getName(type);
    byte[] classNameAsBytes = className.getBytes(StandardCharsets.UTF_8);
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
    messageDigest.update(classNameAsBytes);
    byte[] digest = messageDigest.digest();
    assert digest != null;
    return digest;
  }

  private static long computeClassID(@Nonnull JDefinedClassOrInterface type)
      throws NoSuchAlgorithmException {
    // Compute the digest of the class and convert it to a long.
    byte[] digest = computeClassDigest(type);
    BigInteger bigInteger = new BigInteger(digest);
    return bigInteger.longValue();
  }
}