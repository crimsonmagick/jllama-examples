package net.jllama.examples.chat.infrastructure.encoding;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Utf8CharBuffer {

  private static final int MAX_CHAR_BYTES = 4;
  private final ByteBuffer byteBuffer;

  public Utf8CharBuffer() {
    byteBuffer = ByteBuffer.allocate(MAX_CHAR_BYTES);
    byteBuffer.mark();
  }

  public Utf8CharBuffer buffer(byte charPiece) {
    if (byteBuffer.position() == byteBuffer.limit()) {
      throw new BufferFullException(
          "Max expected byte length for a UTF-8 char has already been reached, expectedByteLength="
              + byteBuffer.limit());
    }
    if (byteBuffer.position() == 0) {
      byteBuffer.limit(getExpectedByteLength(charPiece));
    } else if (!isContinuationByte(charPiece)) {
      throw new EncodingException("Expected a valid continuity byte");
    }
    byteBuffer.put(charPiece);
    return this;
  }

  public void clear() {
    byteBuffer.reset();
  }

  public Optional<String> unbuffer() {
    if (byteBuffer.position() == byteBuffer.limit()) {
      final byte[] temp = new byte[byteBuffer.limit()];
      byteBuffer.reset()
          .get(temp, 0, byteBuffer.limit())
          .reset();
      return Optional.of(new String(temp, StandardCharsets.UTF_8));
    }
    return Optional.empty();
  }

  private static int getExpectedByteLength(final byte leadingByte) {
    if (isContinuationByte(leadingByte)) {
      throw new EncodingException("The first byte of a UTF-8 char cannot be a continuity piece.");
    }
    if ((leadingByte & 0x80) == 0x00) {
      return 1;
    }
    if ((leadingByte & 0b11100000) == 0b11000000) {
      return 2;
    } else if ((leadingByte & 0b11110000) == 0b11100000) {
      return 3;
    } else if ((leadingByte & 0b11111000) == 0b11110000) {
      return 4;
    }
    throw new EncodingException("Invalid UTF-8 leading byte.");
  }

  private static boolean isContinuationByte(final byte charPiece) {
    return (charPiece & 0xC0) == 0x80;
  }

}
