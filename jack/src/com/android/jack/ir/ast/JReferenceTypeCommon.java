/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.android.jack.ir.ast;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.sched.item.Description;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any reference type.
 */
@Description("Reference type")
abstract class JReferenceTypeCommon extends JNode implements JReferenceType, CanBeRenamed {

  @Nonnull
  protected String name;

  @CheckForNull
  private Object id;

  @CheckForNull
  private JArrayType array;


  public JReferenceTypeCommon(@Nonnull SourceInfo info, @Nonnull String name) {
    super(info);
    this.name = name;
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  @Override
  public void setName(@Nonnull String name) {
    this.name = name;
    assert Jack.getSession().getPhantomLookup().check(this);
  }

  @Nonnull
  @Override
  public JExpression createDefaultValue(@Nonnull SourceInfo sourceInfo) {
    return new JNullLiteral(sourceInfo);
  }

  @Override
  public boolean isExternal() {
    return false;
  }

  protected boolean isTrivialCast(@Nonnull JReferenceType castTo) {
    if (this.equals(castTo) || castTo.equals(
        Jack.getSession().getPhantomLookup().getClass(CommonTypes.JAVA_LANG_OBJECT))) {
      return true;
    }

    return false;
  }

  @CheckForNull
  protected static JPrimitiveType getWrappedType(@Nonnull JClassOrInterface type) {

    for (JPrimitiveTypeEnum primitiveType : JPrimitiveTypeEnum.values()) {
      if (primitiveType.getType().isWrapperType(type)) {
        return primitiveType.getType();
      }
    }
    return null;
  }

  @Override
  @Nonnull
  public JArrayType getArray() {
    if (array == null) {
      array = new JArrayType(this);
    }
    assert array != null;
    return array;
  }

  /**
   * Two reference types are considered equals if they share the same descriptor.
   */
  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof JReferenceTypeCommon) {
      boolean equals = getId() == ((JReferenceTypeCommon) obj).getId();
      assert equals == Jack.getLookupFormatter().getName((JType) obj).equals(
          Jack.getLookupFormatter().getName(this));
      return equals;
    } else {
      return false;
    }
  }

  @Override
  public final int hashCode() {
    return getId().hashCode();
  }

  @Nonnull
  private Object getId() {
    if (id == null) {
      id = Jack.getLookupFormatter().getName(this).intern();
    }
    return id;
  }
}
