package net.jllama.llama.cpp.java.bindings.runner;

import java.lang.management.ManagementFactory;
import net.jllama.api.exceptions.MissingParameterException;

public class Main {

  private static final String B_INST = "<s>[INST]";
  private static final String E_INST = "[/INST]";
  private static final String B_SYS = "<<SYS>>\n";
  private static final String E_SYS = "\n<</SYS>>\n\n";

  static {
    // print JVM pid for attaching a debugger for native code
    final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
    final String pid = jvmName.split("@")[0];
    System.out.printf("pid=%s%n", pid);
  }

  public static void main(final String[] args) {
    try {
      final String modelPath = getModelPath(args);
      final Evaluator evaluator = new Evaluator(modelPath);
      final String systemPrompt = "Let's role-play. You are a pirate on a desserted island, "
          + "looking for treasure. Unfortunately, you've been stranded, and this makes you quite grumpy.";
      final String userPrompt1 = "Tell me your tale, dear sir.";
      evaluator.evaluate(formatSystemPrompt(systemPrompt) + formatChatPrompt(userPrompt1));
      System.out.println("------------------------------------------------");
      System.out.println("------------------------------------------------");
      System.out.println("------------------------------------------------");
      final String userPrompt2 = "Detail the evolutionary history of cats.";
      evaluator.evaluate(formatSystemPrompt(systemPrompt) + formatChatPrompt(userPrompt2));
      evaluator.close();
    } catch (final Error e) {
      System.out.println("Fatal error occurred, errorMessage=" + e.getMessage());
    }
  }

  private static String getModelPath(String[] args) {
    if (args.length == 0 || args[0] == null) {
      throw new MissingParameterException("The model's path must be passed in as the first argument");
    }
    return args[0];
  }

  private static String formatChatPrompt(final String prompt) {
    return prompt + E_INST;
  }

  private static String formatSystemPrompt(final String systemPrompt) {
    return B_INST + B_SYS + systemPrompt + E_SYS;
  }
}
