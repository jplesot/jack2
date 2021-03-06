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

package java.nio.charset;

public abstract class CharsetDecoder {
  protected CharsetDecoder(java.nio.charset.Charset charset, float averageCharsPerByte,
      float maxCharsPerByte) {
    throw new RuntimeException("Stub!");
  }

  public final float averageCharsPerByte() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.Charset charset() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.CharBuffer decode(java.nio.ByteBuffer in)
      throws java.nio.charset.CharacterCodingException {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CoderResult decode(java.nio.ByteBuffer in, java.nio.CharBuffer out,
      boolean endOfInput) {
    throw new RuntimeException("Stub!");
  }

  protected abstract java.nio.charset.CoderResult decodeLoop(java.nio.ByteBuffer in,
      java.nio.CharBuffer out);

  public java.nio.charset.Charset detectedCharset() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CoderResult flush(java.nio.CharBuffer out) {
    throw new RuntimeException("Stub!");
  }

  protected java.nio.charset.CoderResult implFlush(java.nio.CharBuffer out) {
    throw new RuntimeException("Stub!");
  }

  protected void implOnMalformedInput(java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  protected void implOnUnmappableCharacter(java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  protected void implReplaceWith(java.lang.String newReplacement) {
    throw new RuntimeException("Stub!");
  }

  protected void implReset() {
    throw new RuntimeException("Stub!");
  }

  public boolean isAutoDetecting() {
    throw new RuntimeException("Stub!");
  }

  public boolean isCharsetDetected() {
    throw new RuntimeException("Stub!");
  }

  public java.nio.charset.CodingErrorAction malformedInputAction() {
    throw new RuntimeException("Stub!");
  }

  public final float maxCharsPerByte() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetDecoder onMalformedInput(
      java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetDecoder onUnmappableCharacter(
      java.nio.charset.CodingErrorAction newAction) {
    throw new RuntimeException("Stub!");
  }

  public final java.lang.String replacement() {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetDecoder replaceWith(java.lang.String replacement) {
    throw new RuntimeException("Stub!");
  }

  public final java.nio.charset.CharsetDecoder reset() {
    throw new RuntimeException("Stub!");
  }

  public java.nio.charset.CodingErrorAction unmappableCharacterAction() {
    throw new RuntimeException("Stub!");
  }
}
