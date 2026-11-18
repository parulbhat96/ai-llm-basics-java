package ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class FaqRetriever {

    // Common words that do not help find the right FAQ entry
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "an", "is", "are", "do", "does", "what", "how", "can", "my", "of", "to",
            "for", "in", "on", "and", "or", "it", "me", "you", "your", "with", "when",
            "will", "be", "at", "if", "there", "any", "have", "has", "about", "get");
    
    // Phrases customers use, mapped to the word the FAQ uses
    private static final Map<String, String> SYNONYMS = Map.of(
            "money back", "refund",
            "get my money", "refund",
            "send back", "return",
            "give back", "return",
            "send it back", "return");

    // Adds the FAQ's own word when the question uses a known synonym
    private String expandSynonyms(String question) {
        String lower = question.toLowerCase();
        StringBuilder expanded = new StringBuilder(question);
        for (Map.Entry<String, String> entry : SYNONYMS.entrySet()) {
            if (lower.contains(entry.getKey())) {
                expanded.append(" ").append(entry.getValue());
            }
        }
        return expanded.toString();
    }

    private final List<String> chunks = new ArrayList<>();

    // Loads the FAQ file. Each non-empty line becomes one chunk.
    public FaqRetriever(String filePath) throws Exception {
        for (String line : Files.readAllLines(Path.of(filePath))) {
            if (!line.isBlank()) {
                chunks.add(line.trim());
            }
        }
    }

    // Turns a text into a set of lowercase keywords
    private Set<String> keywords(String text) {
        Set<String> words = new HashSet<>();
        for (String word : text.toLowerCase().split("[^a-z0-9]+")) {
            if (word.length() > 3 && word.endsWith("s")) {
                word = word.substring(0, word.length() - 1);   // "refunds" becomes "refund"
            }
            if (word.length() > 1 && !STOP_WORDS.contains(word)) {
                words.add(word);
            }
        }
        return words;
    }

    // Returns up to topK chunks that share the most keywords with the question
    public List<String> retrieve(String question, int topK) {
    	Set<String> questionWords = keywords(expandSynonyms(question));

        record Scored(String chunk, int score) { }
        List<Scored> scored = new ArrayList<>();

        for (String chunk : chunks) {
            Set<String> chunkWords = keywords(chunk);
            int score = 0;
            for (String word : questionWords) {
                if (chunkWords.contains(word)) {
                    score++;
                }
            }
            if (score > 0) {
                scored.add(new Scored(chunk, score));
            }
        }

        scored.sort((a, b) -> b.score() - a.score());

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            result.add(scored.get(i).chunk());
        }
        return result;
    }
}