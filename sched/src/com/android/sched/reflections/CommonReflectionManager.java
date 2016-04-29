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

package com.android.sched.reflections;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Partial implementation of {@link ReflectionManager} that uses regular reflection API.
 */
public abstract class CommonReflectionManager implements ReflectionManager {
  @Override
  @Nonnull
  public Set<Class<?>> getSuperTypesOf(@Nonnull Class<?> cls) {
    Set<Class<?>> set = new HashSet<Class<?>>();

    for (Class<?> sup = cls.getSuperclass(); sup != null; sup = sup.getSuperclass()) {
      set.add(sup);
      set.addAll(getSuperTypesOf(sup));
    }

    for (Class<?> interf : cls.getInterfaces()) {
      set.add(interf);
      set.addAll(getSuperTypesOf(interf));
    }

    return set;
  }
}
