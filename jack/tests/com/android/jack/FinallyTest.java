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

package com.android.jack;

import com.android.jack.ir.ast.JBinaryOperation;
import com.android.jack.ir.ast.JBinaryOperator;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.optimizations.Optimizations;
import com.android.sched.util.config.ThreadConfig;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * JUnit test for compilation of finally blocks.
 */
public class FinallyTest {

  @After
  public void tearDown() {
    ThreadConfig.unsetConfig();
  }

  @Test
  public void testCompile005() throws Exception {
    String classBinaryName = "com/android/jack/tryfinally/finally005/jack/Finally005";
    String methodSignature = "get()V";

    Map<String, String> additionalProps = new HashMap<String, String>();
    additionalProps.put(Optimizations.IfSimplifier.ENABLE.getName(), "true");
    additionalProps.put(Optimizations.ExpressionSimplifier.ENABLE.getName(), "true");
    JMethod m = TestTools.getJMethodWithSignatureFilter(
        TestTools.getJackTestFromBinaryName(classBinaryName), "L" + classBinaryName + ";",
        methodSignature, additionalProps);
    Assert.assertNotNull(m);

    new checkUselessIf().accept(m);
  }

  private static class checkUselessIf extends JVisitor {

    @Override
    public boolean visit(@Nonnull JIfStatement jif) {
      if (jif.getIfExpr() instanceof JBinaryOperation) {
        JBinaryOperation bin = (JBinaryOperation) jif.getIfExpr();
        if (bin.getOp() == JBinaryOperator.NEQ && bin.getLhs() instanceof JNullLiteral
            && bin.getRhs() instanceof JNullLiteral) {
          Assert.fail("Useless if exists.");
        }
      }
      return super.visit(jif);
    }
  }
}
