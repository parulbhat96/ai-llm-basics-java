package ai;

import java.util.List;
import java.util.Scanner;

public class FaqBot {

    // Standing rules for the model (the system instruction)
    private static final String RULES =
            "You are the customer support assistant for ShopEasy.\n"
            + "Answer using ONLY the facts in the Context.\n"
            + "When the Context states a rule about a group of things (for example electronics), "
            + "apply it to items that clearly belong to that group.\n"
            + "For yes/no questions, start with Yes or No and give the reason from the Context.\n"
            + "Reply exactly 'I don't know.' only when the Context says nothing relevant to the question.\n"
            + "Keep the answer to one or two sentences.\n\n"
            + "Examples:\n"
            + "Context: Batteries: Batteries carry a 6 month warranty.\n"
            + "Question: Is my 2 month old battery covered?\n"
            + "Answer: Yes, batteries carry a 6 month warranty.\n\n"
            + "Context: Stickers: Stickers cannot be exchanged.\n"
            + "Question: Can I exchange my unicorn stickers?\n"
            + "Answer: No, stickers cannot be exchanged.\n\n"
            + "Context: Batteries: Batteries carry a 6 month warranty.\n"
            + "Question: Do you sell chargers?\n"
            + "Answer: I don't know.";

    private final GeminiClient llm;
    private final FaqRetriever retriever;

    public FaqBot() throws Exception {
        llm = new GeminiClient();
        retriever = new FaqRetriever("data/shopeasy_faq.txt");
    }

    // Step 1: RETRIEVE the relevant FAQ lines
    public List<String> findContext(String question) {
        return retriever.retrieve(question, 2);
    }

    // Steps 2 and 3: AUGMENT the prompt with those lines, then GENERATE the answer
    public String answer(String question) throws Exception {
        List<String> chunks = findContext(question);

        String context = chunks.isEmpty()
                ? "(no relevant information found)"
                : String.join("\n", chunks);

        String prompt = "Context:\n" + context + "\n\nQuestion: " + question;
        return llm.ask(prompt, RULES, 0.0).trim();
    }

    public static void main(String[] args) throws Exception {
        FaqBot bot = new FaqBot();
        Scanner scanner = new Scanner(System.in);

        System.out.println("ShopEasy FAQ bot. Type a question, or 'exit' to stop.");
        while (true) {
            System.out.print("\nYou: ");
            String question = scanner.nextLine().trim();
            if (question.equalsIgnoreCase("exit")) {
                break;
            }
            if (question.isEmpty()) {
                continue;
            }

            System.out.println("[Retrieved context]");
            for (String chunk : bot.findContext(question)) {
                System.out.println("  - " + chunk);
            }
            System.out.println("Bot: " + bot.answer(question));
        }
    }
}