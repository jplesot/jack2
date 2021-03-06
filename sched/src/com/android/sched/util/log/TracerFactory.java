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

package com.android.sched.util.log;

import com.android.sched.util.config.HasKeyId;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.config.id.ImplementationPropertyId;

import javax.annotation.Nonnull;

/**
 * Factory class to manage {@link Tracer}
 */
@HasKeyId
public class TracerFactory {
  @Nonnull
  public static final ImplementationPropertyId<Tracer> TRACER = ImplementationPropertyId.create(
      "sched.tracer", "Define which tracer to use", Tracer.class)
      .addDefaultValue("none");

  /**
   * Get a tracer with default parameter
   */
  @Nonnull
  public static Tracer getTracer() {
    return ThreadConfig.get(TRACER);
  }

  private TracerFactory() {}
}
