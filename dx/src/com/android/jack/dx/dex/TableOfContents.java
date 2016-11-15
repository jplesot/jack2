/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.jack.dx.dex;

import com.android.jack.dx.io.DexBuffer;
import com.android.jack.dx.util.DexException;

import java.util.Arrays;

/**
 * The file header and map.
 */
public final class TableOfContents {

  /*
   * TODO(dx team): factor out ID constants.
   */

  public final Section header = new Section(0x0000);
  public final Section stringIds = new Section(0x0001);
  public final Section typeIds = new Section(0x0002);
  public final Section protoIds = new Section(0x0003);
  public final Section fieldIds = new Section(0x0004);
  public final Section methodIds = new Section(0x0005);
  public final Section classDefs = new Section(0x0006);
  public final Section callSiteIds = new Section(0x0007);
  public final Section methodHandleIds = new Section(0x0008);
  public final Section mapList = new Section(0x1000);
  public final Section typeLists = new Section(0x1001);
  public final Section annotationSetRefLists = new Section(0x1002);
  public final Section annotationSets = new Section(0x1003);
  public final Section classDatas = new Section(0x2000);
  public final Section codes = new Section(0x2001);
  public final Section stringDatas = new Section(0x2002);
  public final Section debugInfos = new Section(0x2003);
  public final Section annotations = new Section(0x2004);
  public final Section encodedArrays = new Section(0x2005);
  public final Section annotationsDirectories = new Section(0x2006);
  public final Section[] sections = {header,
      stringIds,
      typeIds,
      protoIds,
      fieldIds,
      methodIds,
      classDefs,
      callSiteIds,
      methodHandleIds,
      mapList,
      typeLists,
      annotationSetRefLists,
      annotationSets,
      classDatas,
      codes,
      stringDatas,
      debugInfos,
      annotations,
      encodedArrays,
      annotationsDirectories};

  public int apiLevel;
  public byte[] signature;
  public int fileSize;
  public int linkSize;
  public int linkOff;
  public int dataSize;
  public int dataOff;

  public TableOfContents() {
    signature = new byte[20];
  }

  public void readFrom(DexBuffer buffer) {
    readHeader(buffer.open(0));
    readMap(buffer.open(mapList.off));
    computeSizesFromOffsets();
  }

  private void readHeader(DexBuffer.Section headerIn) {
    byte[] magic = headerIn.readByteArray(8);

    if (!DexFormat.isSupportedDexMagic(magic)) {
      throw new DexException("Unexpected magic: " + Arrays.toString(magic));
    }

    apiLevel = DexFormat.magicToApi(magic);
    headerIn.readInt(); // read checksum
    signature = headerIn.readByteArray(20);
    fileSize = headerIn.readInt();
    int headerSize = headerIn.readInt();
    if (headerSize != SizeOf.getHeaderSize(apiLevel)) {
      throw new DexException("Unexpected header: 0x" + Integer.toHexString(headerSize));
    }
    int endianTag = headerIn.readInt();
    if (endianTag != DexFormat.ENDIAN_TAG) {
      throw new DexException("Unexpected endian tag: 0x" + Integer.toHexString(endianTag));
    }
    linkSize = headerIn.readInt();
    linkOff = headerIn.readInt();
    mapList.off = headerIn.readInt();
    if (mapList.off == 0) {
      throw new DexException("Cannot merge dex files that do not contain a map");
    }
    stringIds.size = headerIn.readInt();
    stringIds.off = headerIn.readInt();
    typeIds.size = headerIn.readInt();
    typeIds.off = headerIn.readInt();
    protoIds.size = headerIn.readInt();
    protoIds.off = headerIn.readInt();
    fieldIds.size = headerIn.readInt();
    fieldIds.off = headerIn.readInt();
    methodIds.size = headerIn.readInt();
    methodIds.off = headerIn.readInt();
    classDefs.size = headerIn.readInt();
    classDefs.off = headerIn.readInt();
    if (apiLevel >= DexFormat.API_ANDROID_O) {
      callSiteIds.size = headerIn.readInt();
      callSiteIds.off = headerIn.readInt();
      methodHandleIds.size = headerIn.readInt();
      methodHandleIds.off = headerIn.readInt();
    }
    dataSize = headerIn.readInt();
    dataOff = headerIn.readInt();
  }

  private void readMap(DexBuffer.Section in) {
    int mapSize = in.readInt();
    Section previous = null;
    for (int i = 0; i < mapSize; i++) {
      short type = in.readShort();
      in.readShort(); // unused
      Section section = getSection(type);
      int size = in.readInt();
      int offset = in.readInt();

      if ((section.size != 0 && section.size != size)
          || (section.off != -1 && section.off != offset)) {
        throw new DexException("Unexpected map value for 0x" + Integer.toHexString(type));
      }

      section.size = size;
      section.off = offset;

      if (previous != null && previous.off > section.off) {
        throw new DexException("Map is unsorted at " + previous + ", " + section);
      }

      previous = section;
    }
    Arrays.sort(sections);
  }

  public void computeSizesFromOffsets() {
    int end = dataOff + dataSize;
    for (int i = sections.length - 1; i >= 0; i--) {
      Section section = sections[i];
      if (section.off == -1) {
        continue;
      }
      if (section.off > end) {
        throw new DexException("Map is unsorted at " + section);
      }
      section.byteCount = end - section.off;
      end = section.off;
    }
  }

  private Section getSection(short type) {
    for (Section section : sections) {
      if (section.type == type) {
        return section;
      }
    }
    throw new IllegalArgumentException("No such map item: " + type);
  }

  public void writeMap(DexBuffer.Section out) {
    int count = 0;
    for (Section section : sections) {
      if (section.exists()) {
        count++;
      }
    }

    out.writeInt(count);
    for (Section section : sections) {
      if (section.exists()) {
        out.writeShort(section.type);
        out.writeShort((short) 0);
        out.writeInt(section.size);
        out.writeInt(section.off);
      }
    }
  }

  /**
   * TODO(jack team)
   */
  public static class Section implements Comparable<Section> {
    public final short type;
    public int size = 0;
    public int off = -1;
    public int byteCount = 0;

    public Section(int type) {
      this.type = (short) type;
    }

    public boolean exists() {
      return size > 0;
    }

    @Override
    public int compareTo(Section section) {
      if (off != section.off) {
        return off < section.off ? -1 : 1;
      }
      return 0;
    }

    @Override
    public String toString() {
      return String.format("Section[type=%#x,off=%#x,size=%#x]", Short.valueOf(type),
          Integer.valueOf(off), Integer.valueOf(size));
    }
  }
}
