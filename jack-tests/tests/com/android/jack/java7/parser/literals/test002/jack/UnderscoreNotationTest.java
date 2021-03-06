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

package com.android.jack.java7.parser.literals.test002.jack;

/**
 * Test underscore notation.
 */
public class UnderscoreNotationTest {

  public byte getValue1() {
    return 0b1_1001;
  }

  public float getValue2() {
    return -0b1_1001;
  }

  public int getValue3() {
    return 1_1_0_0_1;
  }
}
