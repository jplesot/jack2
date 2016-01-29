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

package com.android.jack.java8.lambda.test030.jack;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests using lambda into anonymous class.
 */
public class Tests {

  @Test
  public void test001() {
    Lambda l = new Lambda();
    Assert.assertEquals(1, l.test(1));
    Assert.assertEquals(2, l.test(2));
    Assert.assertEquals(3, l.test(3));
  }
}