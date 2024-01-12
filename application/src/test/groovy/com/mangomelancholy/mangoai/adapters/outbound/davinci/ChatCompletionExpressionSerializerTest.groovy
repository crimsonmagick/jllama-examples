package com.mangomelancholy.mangoai.adapters.outbound.davinci

import com.mangomelancholy.mangoai.application.conversation.ExpressionValue
import spock.lang.Specification

import static com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType.*

class ChatCompletionExpressionSerializerTest extends Specification {

    def underTest

    def setup() {
        underTest = new CompletionExpressionSerializer()
    }

    def "serializeExpression - serialized expressions should be properly prefixed"() {
        when:
        final expression = new ExpressionValue(content, actor, "1")
        final serializedText = underTest.serializeExpression(expression)

        then:
        serializedText == expected

        where:
        content                                                            | actor          | expected
        "Hey there, how can I help you?"                                   | PAL            | "PAL: Hey there, how can I help you?\n"
        "You are a chatbot"                                                | SYSTEM         | "System: You are a chatbot\n"
        "Hello, could you teach me the alphabet?"                          | USER           | "You: Hello, could you teach me the alphabet?\n"
        "HAL is a chatbot assistant that strives to eject you into space." | INITIAL_PROMPT | "HAL is a chatbot assistant that strives to eject you into space.\n"
        and:
        conversationId = 1

    }
}