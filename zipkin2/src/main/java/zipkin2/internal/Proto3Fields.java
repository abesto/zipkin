/**
 * Copyright 2015-2018 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin2.internal;

import java.util.Map;

import static zipkin2.internal.JsonCodec.UTF_8;

/**
 * Everything here assumes the field numbers are less than 16, implying a 1 byte tag.
 */
//@Immutable
final class Proto3Fields {
  /**
   * Define the wire types, except the deprecated ones (groups)
   *
   * <p>See https://developers.google.com/protocol-buffers/docs/encoding#structure
   */
  static final int
    WIRETYPE_VARINT = 0,
    WIRETYPE_FIXED64 = 1,
    WIRETYPE_LENGTH_DELIMITED = 2,
    WIRETYPE_FIXED32 = 5;

  static class Field {
    final int fieldNumber;
    final int wireType;
    /**
     * "Each key in the streamed message is a varint with the value {@code (field_number << 3) | wire_type}"
     *
     * <p>See https://developers.google.com/protocol-buffers/docs/encoding#structure
     */
    final int key;

    Field(int fieldNumber, int wireType) {
      this(fieldNumber, wireType, (fieldNumber << 3) | wireType);
    }

    Field(int fieldNumber, int wireType, int key) {
      this.fieldNumber = fieldNumber;
      this.wireType = wireType;
      this.key = key;
    }

    void readThisKey(Buffer buffer) {
      int readKey = buffer.readVarint32();
      if (key != readKey) {
        int lastPositionRead = buffer.pos - 1;
        int readWireType = Proto3Fields.Field.wireType(readKey, lastPositionRead);
        if (readWireType != wireType) {
          throw new IllegalArgumentException(
            "Expected wire type " + wireType + " but was " + readWireType);
        }
        int readFieldNumber = Proto3Fields.Field.fieldNumber(readKey, lastPositionRead);
        if (readFieldNumber != fieldNumber) {
          throw new IllegalArgumentException(
            "Expected field number " + fieldNumber + " but was " + readFieldNumber);
        }
      }
    }

    static int fieldNumber(int key, int pos) {
      int fieldNumber = key >>> 3;
      if (fieldNumber != 0) return fieldNumber;
      throw new IllegalArgumentException("fieldNumber was zero at position: " + pos);
    }

    static int wireType(int key, int pos) {
      int wireType = key & (1 << 3) - 1;
      if (wireType != 0 && wireType != 1 && wireType != 2 && wireType != 5) {
        throw new IllegalArgumentException("invalid wireType " + wireType + " at position: " + pos);
      }
      return wireType;
    }

    static boolean skipValue(Buffer buffer, int wireType) {
      int remaining = buffer.remaining();
      switch (wireType) {
        case WIRETYPE_VARINT:
          for (int i = 0; i < remaining; i++) {
            if (buffer.readByte() >= 0) return true;
          }
          return false;
        case WIRETYPE_FIXED64:
          return buffer.skip(8);
        case WIRETYPE_LENGTH_DELIMITED:
          int length = buffer.readVarint32();
          return buffer.skip(length);
        case WIRETYPE_FIXED32:
          return buffer.skip(4);
        default:
          throw new IllegalArgumentException(
            "invalid wireType " + wireType + " at position: " + (buffer.pos - 1));
      }
    }
  }

  static abstract class LengthDelimitedField<T> extends Field {
    LengthDelimitedField(int fieldNumber) {
      super(fieldNumber, WIRETYPE_LENGTH_DELIMITED);
    }

    final int sizeInBytes(T value) {
      if (value == null) return 0;
      int sizeOfValue = sizeOfValue(value);
      return sizeOfLengthDelimitedField(sizeOfValue);
    }

    final void write(Buffer b, T value) {
      if (value == null) return;
      int sizeOfValue = sizeOfValue(value);
      if (sizeOfValue == 0) return;
      b.writeByte(key);
      b.writeVarint(sizeOfValue); // length prefix
      writeValue(b, value);
    }

    abstract int sizeOfValue(T value);

    abstract void writeValue(Buffer b, T value);

    /** Call this after consuming the field key to ensure there's enough space for the data */
    int ensureLength(Buffer buffer) {
      int length = buffer.readVarint32();
      Proto3Fields.ensureLength(buffer, length);
      return length;
    }
  }

  static class BytesField extends LengthDelimitedField<byte[]> {
    BytesField(int fieldNumber) {
      super(fieldNumber);
    }

    @Override int sizeOfValue(byte[] bytes) {
      return bytes.length;
    }

    @Override void writeValue(Buffer b, byte[] bytes) {
      b.write(bytes);
    }
  }

  static class HexField extends LengthDelimitedField<String> {
    static final char[] HEX_DIGITS =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    HexField(int fieldNumber) {
      super(fieldNumber);
    }

    @Override int sizeOfValue(String hex) {
      if (hex == null) return 0;
      return hex.length() / 2;
    }

    @Override void writeValue(Buffer b, String hex) {
      // similar logic to okio.ByteString.decodeHex
      for (int i = 0, length = hex.length(); i < length; i++) {
        int d1 = decodeLowerHex(hex.charAt(i++)) << 4;
        int d2 = decodeLowerHex(hex.charAt(i));
        b.writeByte((byte) (d1 + d2));
      }
    }

    static int decodeLowerHex(char c) {
      if (c >= '0' && c <= '9') return c - '0';
      if (c >= 'a' && c <= 'f') return c - 'a' + 10;
      throw new AssertionError("not lowerHex " + c); // bug
    }

    String readValue(Buffer buffer) {
      int length = ensureLength(buffer) * 2;
      if (length == 0) return null;

      char[] result = new char[length];

      for (int i = 0; i < length; i += 2) {
        byte b = buffer.readByte();
        result[i + 0] = HEX_DIGITS[(b >> 4) & 0xf];
        result[i + 1] = HEX_DIGITS[b & 0xf];
      }

      return new String(result);
    }
  }

  static class Utf8Field extends LengthDelimitedField<String> {
    Utf8Field(int fieldNumber) {
      super(fieldNumber);
    }

    @Override int sizeOfValue(String utf8) {
      return utf8 != null ? Buffer.utf8SizeInBytes(utf8) : 0;
    }

    @Override void writeValue(Buffer b, String utf8) {
      b.writeUtf8(utf8);
    }

    String readValue(Buffer buffer) {
      int lengthOfString = ensureLength(buffer);
      if (lengthOfString == 0) return null;
      return new String(buffer.toByteArray(), buffer.pos, lengthOfString, UTF_8);
    }
  }

  static final class Fixed64Field extends Field {
    Fixed64Field(int fieldNumber) {
      super(fieldNumber, WIRETYPE_FIXED64);
    }

    void write(Buffer b, long number) {
      if (number == 0) return;
      b.writeByte(key);
      b.writeLongLe(number);
    }

    int sizeInBytes(long number) {
      if (number == 0) return 0;
      return 1 + 8; // tag + 8 byte number
    }

    long readValue(Buffer buffer) {
      ensureLength(buffer, 8);
      return buffer.readLongLe();
    }
  }

  static class VarintField extends Field {
    VarintField(int fieldNumber) {
      super(fieldNumber, WIRETYPE_VARINT);
    }

    int sizeInBytes(int number) {
      return number != 0 ? 1 + Buffer.varintSizeInBytes(number) : 0; // tag + varint
    }

    void write(Buffer b, int number) {
      if (number == 0) return;
      b.writeByte(key);
      b.writeVarint(number);
    }

    int sizeInBytes(long number) {
      return number != 0 ? 1 + Buffer.varintSizeInBytes(number) : 0; // tag + varint
    }

    void write(Buffer b, long number) {
      if (number == 0) return;
      b.writeByte(key);
      b.writeVarint(number);
    }
  }

  static final class BooleanField extends Field {
    BooleanField(int fieldNumber) {
      super(fieldNumber, WIRETYPE_VARINT);
    }

    int sizeInBytes(boolean bool) {
      return bool ? 2 : 0; // tag + varint
    }

    void write(Buffer b, boolean bool) {
      if (!bool) return;
      b.writeByte(key);
      b.writeByte(1);
    }

    boolean read(Buffer b) {
      byte bool = b.readByte();
      if (bool < 0 || bool > 1) {
        throw new IllegalArgumentException("invalid boolean value at position " + (b.pos - 1));
      }
      return bool == 1;
    }
  }

  static class MapEntryField extends LengthDelimitedField<Map.Entry<String, String>> {
    static final int KEY_FIELD = 1;
    static final int VALUE_FIELD = 1;

    static final Utf8Field KEY = new Utf8Field(KEY_FIELD);
    static final Utf8Field VALUE = new Utf8Field(VALUE_FIELD);

    MapEntryField(int fieldNumber) {
      super(fieldNumber);
    }

    @Override int sizeOfValue(Map.Entry<String, String> value) {
      return KEY.sizeInBytes(value.getKey()) + VALUE.sizeInBytes(value.getValue());
    }

    @Override void writeValue(Buffer b, Map.Entry<String, String> value) {
      KEY.write(b, value.getKey());
      VALUE.write(b, value.getValue());
    }
  }

  // added for completion as later we will skip fields we don't use
  static final class Fixed32Field extends Field {
    Fixed32Field(int fieldNumber) {
      super(fieldNumber, WIRETYPE_FIXED32);
    }

    int sizeInBytes(int number) {
      if (number == 0) return 0;
      return 1 + 4; // tag + 4 byte number
    }
  }

  static int sizeOfLengthDelimitedField(int sizeInBytes) {
    return 1 + Buffer.varintSizeInBytes(sizeInBytes) + sizeInBytes; // tag + len + bytes
  }

  static void ensureLength(Buffer buffer, int length) {
    if (length > buffer.remaining()) {
      throw new IllegalArgumentException(
        "truncated: length " + length + " > bytes remaining " + buffer.remaining());
    }
  }
}
