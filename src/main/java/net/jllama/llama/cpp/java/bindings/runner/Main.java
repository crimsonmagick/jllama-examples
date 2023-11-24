package net.jllama.llama.cpp.java.bindings.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.jllama.llama.cpp.java.bindings.LlamaContextParams;
import net.jllama.llama.cpp.java.bindings.LlamaCpp;
import net.jllama.llama.cpp.java.bindings.LlamaCppManager;
import net.jllama.llama.cpp.java.bindings.LlamaModelParams;
import net.jllama.llama.cpp.java.bindings.LlamaContext;
import net.jllama.llama.cpp.java.bindings.LlamaOpaqueModel;
import net.jllama.llama.cpp.java.bindings.LlamaTokenDataArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;

public class Main {

  private static final String SYSTEM_PROMPT = "You are a helpful, respectful and honest assistant. Always answer as helpfully as possible. If you don't know something, answer that you do not know.";
  private static final String COMPLETION_PROMPT = "I love my Cat Winnie, he is such a great cat! Let me tell you more about ";

  private static final String B_INST = "<s>[INST]";
  private static final String E_INST = "[/INST]";
  private static final String B_SYS = "<<SYS>>\n";
  private static final String E_SYS = "\n<</SYS>>\n\n";

  static {
    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    final String pid = jvmName.split("@")[0];
    System.out.printf("pid=%s%n", pid);
  }
  private static LlamaCpp llamaCpp;
  private static LlamaOpaqueModel llamaOpaqueModel;
<<<<<<< Updated upstream
  private static LlamaOpaqueContext llamaOpaqueContext;
  final private static Logger log = LogManager.getLogger(LlamaCpp.class);
=======
  private static LlamaContext llamaOpaqueContext;
>>>>>>> Stashed changes

  public static void main(final String[] args) {
    try {
      llamaCpp = LlamaCppManager.getLlamaCpp();
      final Detokenizer detokenizer = new Detokenizer(llamaCpp);
      final String modelPath = System.getProperty("modelpath");
      llamaCpp.loadLibrary();
      llamaCpp.llamaBackendInit(true);
      llamaCpp.llamaLogSet((logLevel, message) -> {
        final String messageText = new String(message, StandardCharsets.UTF_8);
        log.info(messageText);
      });

      final LlamaContextParams llamaContextParams = llamaCpp.llamaContextDefaultParams();
      final int threads = Runtime.getRuntime().availableProcessors() / 2 - 1;
      llamaContextParams.setnThreads(threads);
      llamaContextParams.setnThreadsBatch(threads);

      final LlamaModelParams llamaModelParams = llamaCpp.llamaModelDefaultParams();
      final long initStartUs = llamaCpp.llamaTimeUs();
      llamaOpaqueModel = llamaCpp.llamaLoadModelFromFile(
          modelPath.getBytes(StandardCharsets.UTF_8), llamaModelParams);
      llamaOpaqueContext =
          llamaCpp.llamaNewContextWithModel(llamaOpaqueModel, llamaContextParams);
      final long initStopUs = llamaCpp.llamaTimeUs();

      System.out.printf("timestamp1=%s, timestamp2=%s, initialization time=%s%n", initStartUs, initStopUs, initStartUs - initStopUs);

      final String prompt = B_INST + B_SYS + SYSTEM_PROMPT + E_SYS + "Explain Jonathan Joestar's special ability, \"Hamon.\"" + E_INST;
      final int[] tokens = tokenize(prompt, true);

      System.out.print(detokenizer.detokenize(toList(tokens), llamaOpaqueModel));

      llamaCpp.llamaEval(llamaOpaqueContext, tokens, tokens.length, 0);
      float[] logits = llamaCpp.llamaGetLogits(llamaOpaqueContext);
      LlamaTokenDataArray candidates = LlamaTokenDataArray.logitsToTokenDataArray(logits);
      final float temp = 0.35f;
      llamaCpp.llamaSampleTemperature(llamaOpaqueContext, candidates, temp);
      int previousToken = llamaCpp.llamaSampleToken(llamaOpaqueContext, candidates);

      System.out.print(detokenizer.detokenize(previousToken, llamaOpaqueModel));

      final List<Integer> previousTokenList = new ArrayList<>();
      previousTokenList.add(previousToken);

      final List<Long> evalTimestamps = new ArrayList<>();
      final List<Long> logitsTimestamps = new ArrayList<>();
      final List<Long> sampleTimestamps = new ArrayList<>();

      for (int i = tokens.length + 1; previousToken != llamaCpp.llamaTokenEos(llamaOpaqueContext) && i < llamaContextParams.getnCtx(); i++) {

        long evalStartUs = llamaCpp.llamaTimeUs();
        final int res = llamaCpp.llamaEval(llamaOpaqueContext, new int[]{previousToken}, 1, i);
        long evalStopUs = llamaCpp.llamaTimeUs();
        evalTimestamps.add(evalStopUs - evalStartUs);
        if (res != 0) {
          throw new RuntimeException("Non zero response from eval");
        }
        long logitsStartUs = llamaCpp.llamaTimeUs();
        logits = llamaCpp.llamaGetLogits(llamaOpaqueContext);
        candidates = LlamaTokenDataArray.logitsToTokenDataArray(logits);
        long logitsStopUs = llamaCpp.llamaTimeUs();
        logitsTimestamps.add(logitsStopUs - logitsStartUs);
//        llamaCpp.llamaSampleRepetitionPenalty(llamaOpaqueContext, candidates, toArray(previousTokenList), 1.2f);
//        llamaCpp.llamaSampleFrequencyAndPresencePenalties(llamaOpaqueContext, candidates, toArray(previousTokenList), -0.2f, -0.2f);
//        llamaCpp.llamaSampleTopK(llamaOpaqueContext, candidates, 100, 1);
//        llamaCpp.llamaSampleSoftMax(llamaOpaqueContext, candidates);
//        llamaCpp.llamaSampleTopP(llamaOpaqueContext, candidates, 0.001f, 1);
//        llamaCpp.llamaSampleTailFree(llamaOpaqueContext, candidates, 0.5f, 1);
//        llamaCpp.llamaSampleTypical(llamaOpaqueContext, candidates, 0.5f, 1);
        llamaCpp.llamaSampleTemperature(llamaOpaqueContext, candidates, temp);
        long sampleStartUs = llamaCpp.llamaTimeUs();
        previousToken = llamaCpp.llamaSampleToken(llamaOpaqueContext, candidates);
        long sampleStopUs = llamaCpp.llamaTimeUs();
        sampleTimestamps.add(sampleStopUs - sampleStartUs);
        previousTokenList.add(previousToken);
        System.out.print(detokenizer.detokenize(previousToken, llamaOpaqueModel));
      }

      llamaCpp.llamaFree(llamaOpaqueContext);
      llamaCpp.llamaFreeModel(llamaOpaqueModel);
      llamaCpp.llamaBackendFree();
      llamaCpp.closeLibrary();
      final double avgEvalTime = evalTimestamps.stream()
          .mapToDouble(Long::doubleValue)
          .average()
          .orElseThrow(() -> new RuntimeException("average() returned no value"));
      final double avgLogitsTime = logitsTimestamps.stream()
          .mapToDouble(Long::doubleValue)
          .average()
          .orElseThrow(() -> new RuntimeException("average() returned no value"));
      final double avgSampleTime = sampleTimestamps.stream()
          .mapToDouble(Long::doubleValue)
          .average()
          .orElseThrow(() -> new RuntimeException("average() returned no value"));
      System.out.println();
      System.out.printf("averageEvalTime=%fms", avgEvalTime / 1000);
      System.out.printf("averageLogitsTime=%fms", avgLogitsTime / 1000);
      System.out.printf("averageSampleTime=%fms", avgSampleTime / 1000);
    } catch (RuntimeException e) {
      System.out.println("Fatal exception occurred, exceptionMessage=" + e.getMessage());
    }
  }

  private static int[] tokenize(final String text, boolean addBos) {
    final int maxLength = text.length();
    final int[] temp = new int[maxLength];
    int length = llamaCpp.llamaTokenize(llamaOpaqueModel, text.getBytes(StandardCharsets.UTF_8), temp, maxLength, addBos);
    final int[] ret = new int[length];
    System.arraycopy(temp, 0, ret, 0, length);
    return ret;
  }

  private static List<Integer> toList(int[] tokens) {
    return Arrays.stream(tokens).boxed().collect(Collectors.toList());
  }

  private static int[] toArray(List<Integer> tokenList) {
    int[] tokens = new int[tokenList.size()];
    Arrays.setAll(tokens, tokenList::get);
    return tokens;
  }

}
