/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.jack.dx.dex.file;

import com.android.jack.dx.dex.DexFormat;
import com.android.jack.dx.dex.SizeOf;
import com.android.jack.dx.rop.cst.CstString;
import com.android.jack.dx.util.AnnotatedOutput;
import com.android.jack.dx.util.Hex;

import javax.annotation.Nonnegative;

/**
 * File header section of a {@code .dex} file.
 */
public final class HeaderItem extends IndexedItem {

  @Nonnegative
  private final int dexVersion;

  /**
   * Constructs an instance.
   */
  public HeaderItem(@Nonnegative int dexVersion) {
    this.dexVersion = dexVersion;
  }

  /** {@inheritDoc} */
  @Override
  public ItemType itemType() {
    return ItemType.TYPE_HEADER_ITEM;
  }

  /** {@inheritDoc} */
  @Override
  @Nonnegative
  public int writeSize() {
    return SizeOf.getHeaderSize(dexVersion);
  }

  /** {@inheritDoc} */
  @Override
  public void addContents(DexFile file) {
    // Nothing to do here.
  }

  /** {@inheritDoc} */
  @Override
  public void writeTo(DexFile file, AnnotatedOutput out) {
    int mapOff = file.getMap().getFileOffset();
    Section firstDataSection = file.getFirstDataSection();
    Section lastDataSection = file.getLastDataSection();
    int dataOff = firstDataSection.getFileOffset();
    int dataSize = lastDataSection.getFileOffset() + lastDataSection.writeSize() - dataOff;

    String magic = file.getDexOptions().getMagic();

    if (out.annotates()) {
      out.annotate(8, "magic: " + new CstString(magic).toQuoted());
      out.annotate(4, "checksum");
      out.annotate(20, "signature");
      out.annotate(4, "file_size:       " + Hex.u4(file.getFileSize()));
      out.annotate(4, "header_size:     " + Hex.u4(SizeOf.getHeaderSize(dexVersion)));
      out.annotate(4, "endian_tag:      " + Hex.u4(DexFormat.ENDIAN_TAG));
      out.annotate(4, "link_size:       0");
      out.annotate(4, "link_off:        0");
      out.annotate(4, "map_off:         " + Hex.u4(mapOff));
    }

    // Write the magic number.
    for (int i = 0; i < 8; i++) {
      out.writeByte(magic.charAt(i));
    }

    // Leave space for the checksum and signature.
    out.writeZeroes(24);

    out.writeInt(file.getFileSize());
    out.writeInt(SizeOf.getHeaderSize(dexVersion));
    out.writeInt(DexFormat.ENDIAN_TAG);

    /*
     * Write zeroes for the link size and data, as the output
     * isn't a staticly linked file.
     */
    out.writeZeroes(8);

    out.writeInt(mapOff);

    // Write out each section's respective header part.
    file.getStringIds().writeHeaderPart(out);
    file.getTypeIds().writeHeaderPart(out);
    file.getProtoIds().writeHeaderPart(out);
    file.getFieldIds().writeHeaderPart(out);
    file.getMethodIds().writeHeaderPart(out);
    file.getClassDefs().writeHeaderPart(out);

    if (dexVersion == DexFormat.O_BETA2_DEX_VERSION) {
      file.getCallSiteIds().writeHeaderPart(out);
      file.getMethodHandleIds().writeHeaderPart(out);
    }

    if (out.annotates()) {
      out.annotate(4, "data_size:       " + Hex.u4(dataSize));
      out.annotate(4, "data_off:        " + Hex.u4(dataOff));
    }

    out.writeInt(dataSize);
    out.writeInt(dataOff);
  }
}