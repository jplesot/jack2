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

package com.android.sched.util.print;

import com.android.sched.util.codec.Formatter;

import java.io.PrintWriter;

import javax.annotation.Nonnull;

class FormatterAdapter<T> implements TypePrinter<T> {
  @Nonnull
  private final Formatter<T> formatter;

  public FormatterAdapter(@Nonnull Formatter<T> formatter) {
    this.formatter = formatter;
  }

  @Override
  public boolean print(@Nonnull PrintWriter printer, @Nonnull T value) {
    String str = formatter.formatValue(value);
    printer.print(str);

    return str != null && !str.isEmpty();
  }
}
