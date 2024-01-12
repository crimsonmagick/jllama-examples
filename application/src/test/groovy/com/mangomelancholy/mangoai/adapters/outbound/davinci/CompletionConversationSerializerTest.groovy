package com.mangomelancholy.mangoai.adapters.outbound.davinci

import com.mangomelancholy.mangoai.application.conversation.ConversationEntity
import com.mangomelancholy.mangoai.application.conversation.ExpressionValue
import spock.lang.Specification

import static com.mangomelancholy.mangoai.application.conversation.ExpressionValue.ActorType.*

class CompletionConversationSerializerTest extends Specification {

    CompletionConversationSerializer underTest
    def mockedExpressionSerializer = Mock(CompletionExpressionSerializer)

    def setup() {
        underTest = new CompletionConversationSerializer(mockedExpressionSerializer)
    }

    def "ParseConversation - expressions should be separated by a newline"() {
        given: "A conversation with a list of expressions"
        final expressions = [new ExpressionValue("You are a chatbot.", SYSTEM, conversationId),
                             new ExpressionValue("Hello, how can I help you today fine sir and/or madame?", PAL, conversationId),
                             new ExpressionValue("How do replace the batteries in my remote?", USER, conversationId),
                             new ExpressionValue("Take off the backcover. Then remove the batteries for safe disposal. Insert the new batteries, and put the cover back on.", PAL, conversationId)]
        final conversation = new ConversationEntity("1234", expressions, "Battery replacement instructions")

        when:
        final serialized = underTest.serializeConversation(conversation)

        then:
        4 * mockedExpressionSerializer.serializeExpression(_ as ExpressionValue) >> "dummy data\n"
        serialized.split("\n").length == 4

        where:
        conversationId = "1"

    }
}
