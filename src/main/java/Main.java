import org.quarkos.Model;
import org.quarkos.ai.Gemini;
import org.quarkos.util.ContextUtil;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        Map<String, byte[]> allContexts = ContextUtil.getAllContexts();
        List<Map.Entry<String, Long>> results = new ArrayList<>();
        int runs = 5;

        for (Model model : Arrays.stream(Model.values()).toList()) {
            System.out.println("\n--- Testing model: " + model + " ---");
            long totalTime = 0;
            int successfulRuns = 0;
            for (int i = 0; i < runs; i++) {
                try {
                    System.out.println("Run " + (i + 1) + " of " + runs + "...");
                    Map.Entry<String, Long> result = Gemini.generateStructuredResponseWithMultipleContexts(
                            "Answer this question based on the context: Welches Hauptproblem trat bei der Virtualisierung älterer x86-Prozessoren auf?",
                            allContexts,
                            model.toString()
                    );

                    System.out.println(result.getKey());

                    totalTime += result.getValue();
                    successfulRuns++;
                } catch (Exception e) {
                    System.out.println("Run " + (i + 1) + " for model " + model + " failed: " + e.getMessage());
                }
            }

            if (successfulRuns > 0) {
                long averageTime = totalTime / successfulRuns;
                results.add(new java.util.AbstractMap.SimpleEntry<>(model.toString(), averageTime));
            }
        }

        results.sort(Comparator.comparing(Map.Entry::getValue));

        System.out.println("\n--- Model Speed Benchmark Results (Average of " + runs + " runs) ---");
        for (int i = 0; i < results.size(); i++) {
            Map.Entry<String, Long> result = results.get(i);
            System.out.printf("%d. %s: %dms%n", i + 1, result.getKey(), result.getValue());
        }
    }
}