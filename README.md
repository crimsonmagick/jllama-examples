# jllama Examples

Examples of LLM text generation using [jLlama](https://github.com/crimsonmagick/jllama)

Examples are provided as Gradle subprojects.

## Requirements
A [gguf model](https://github.com/ggerganov/llama.cpp#prepare-data--run) derived from llama2-chat must be provided to the examples. 

NodeJS >= v20 with NPM > 9.7 is required for running the chat frontend.

Examples target Java 17.

## Evaluator
Simulates a chat with a chatbot using llama2 instruct format:

```
<s>[INST]<<SYS>>
{System prompt goes here}
<</SYS>>

{User prompt goes here}[/INST]
```

### Running
Chat model is provided as the first parameter to the Evaluator program.

Alternatively, run using gradle:

```
./gradlew evaluator:runProgram -DmodelPath=path_to_model
```

## Llama Chat
An example chat program is provided in two parts:

1. llama-service
  * A Spring application that provides a REST interface for interacting with llama.cpp
  * Provides rudimentary message history using h2
  * A Model path must be provided as the environment variable `LLAMA_MODEL_PATH`
  * Run with `./gradlew bootRun`
* chat-frontend
  * Provides a rudimentary UI for interacting the llama-service
  * Run with `npm-start`

### Running with Docker
Coming Soon