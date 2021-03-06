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

package com.android.jack.java8.methodref.test003.jack;

interface I {
  int add(int v1, int v2);
}

class A {
  public int add(int v1, int v2) {
    return v1 + v2;
  }
}

public class MethodReference {


  public int add(I i) {
    return i.add(1, 3);
  }

  public int test() {
    A a = new A();
    return add(a::add);
  }
}
