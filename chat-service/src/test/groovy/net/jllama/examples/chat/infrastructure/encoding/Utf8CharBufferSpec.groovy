package net.jllama.examples.chat.infrastructure.encoding

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class Utf8CharBufferSpec extends Specification {

  def "Buffer can properly buffer UTF-8 bytes and return valid UTF-8 String"() {
    given:
    final underTest = new Utf8CharBuffer()

    and: "a UTF-8 character"
    final byte[] utf8Bytes = new String(original).getBytes(StandardCharsets.UTF_8)

    when:
    for (byte piece : utf8Bytes) {
      underTest.buffer(piece)
    }
    final unbuffered = underTest.unbuffer()

    then:
    unbuffered.isPresent()
    unbuffered.get() == original

    where:
    original << ["a", "😊", "本", "Ä"]
  }

  def "Buffer will throw BufferFullException when caller attempts to buffer extra bytes"() {
    given:
    final underTest = new Utf8CharBuffer()

    and: "a UTF-8 character"
    final byte[] utf8Bytes = new String(original).getBytes(StandardCharsets.UTF_8)

    when:
    for (byte piece : utf8Bytes) {
      underTest.buffer(piece)
    }
    underTest.buffer((byte) 0x40)

    then:
    thrown(BufferFullException)

    where:
    original << ["a", "😊", "本", "Ä"]
  }

  def "Buffer will throw EncodingException when caller attempts to buffer an invalid continuity byte"() {
    given:
    final underTest = new Utf8CharBuffer()

    and: "a UTF-8 character"
    final byte[] utf8Bytes = new String(original).getBytes(StandardCharsets.UTF_8)

    when:
    underTest.buffer(utf8Bytes[0])
    underTest.buffer((byte) badByte)

    then:
    thrown(EncodingException)

    where:
    original | badByte
    "😊"      | 0x0F
    "本"     | 0xFF
  }

  def "Buffer throws EncodingException for invalid leading byte"() {
    given:
    final underTest = new Utf8CharBuffer()

    when:
    underTest.buffer((byte) badByte)

    then:
    thrown(EncodingException)

    where:
    badByte << [0x80, 0xFF] // first byte is a continuity byte, second is invalid in any context
  }

}
