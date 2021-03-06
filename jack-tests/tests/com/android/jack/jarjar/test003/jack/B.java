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

package com.android.jack.jarjar.test003.jack;

import java.util.List;

@AnnotationWithStringArray("com.android.jack.jarjar.test003.jack.B")
public class B {

  public static List<Inner> l;

  public static class Inner {

  }

  public B(String arg) {

  }

  public static B[] getArray() {
    @C
    String local = "com.android.jack.jarjar.test003.jack.B";
    return new B[] {new B(local)};
  }
}
