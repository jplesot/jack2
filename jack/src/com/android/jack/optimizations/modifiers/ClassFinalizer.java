/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.jack.optimizations.modifiers;

import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.optimizations.Optimizations;
import com.android.jack.optimizations.common.DirectlyDerivedClassesMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;
import com.android.sched.util.log.stats.Counter;
import com.android.sched.util.log.stats.CounterImpl;
import com.android.sched.util.log.stats.StatisticId;

import javax.annotation.Nonnull;

/** Make classes final when possible */
@Description("Tighten 'final' modifiers on classes.")
@Constraint(need = DirectlyDerivedClassesMarker.class)
@Support(Optimizations.ClassFinalizer.class)
@Transform(add = EffectivelyFinalClassMarker.class)
public class ClassFinalizer
    implements RunnableSchedulable<JDefinedClassOrInterface> {

  private final boolean preserveReflections =
      ThreadConfig.get(Optimizations.ClassFinalizer.PRESERVE_REFLECTIONS).booleanValue();

  @Nonnull
  public static final StatisticId<Counter> TYPES_FINALIZED = new StatisticId<>(
      "jack.optimization.class-finalizer", "Classes made final",
      CounterImpl.class, Counter.class);

  @Nonnull
  private final Tracer tracer = TracerFactory.getTracer();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) {
    if (type instanceof JDefinedClass) {
      JDefinedClass definedClass = (JDefinedClass) type;
      if (!DirectlyDerivedClassesMarker.hasDirectlyDerivedClasses(definedClass)) {
        if (!type.isAbstract() && !type.isFinal()) {
          // Mark as effectively final
          EffectivelyFinalClassMarker.markAsEffectivelyFinal(definedClass);

          if (!preserveReflections) {
            // Mark as actually final if strict reflections are not enforced
            definedClass.setFinal();
            tracer.getStatistic(TYPES_FINALIZED).incValue();
          }
        }
      }
    }
  }
}
