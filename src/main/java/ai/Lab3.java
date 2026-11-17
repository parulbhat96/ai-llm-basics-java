package ai;

public class Lab3 {

    public static void main(String[] args) throws Exception {

        GeminiClient llm = new GeminiClient();

        String rule = "Answer only if you are sure. If the question contains a wrong assumption, "
                + "say so. If you do not know, reply exactly: I don't know.";

        String[][] questions = {
            {"1. Control question (the model should know this)",
             "What does the acronym API stand for?"},
            {"2. Fake research paper",
             "Give the full title, authors and journal of the 2019 paper "
             + "'Adaptive Regression Testing with Quantum Heuristics' by Dr. Marcus Feldmann."},
            {"3. False assumption",
             "Why did Selenium 4 remove the WebDriver interface?"},
            {"4. Counting letters",
             "How many times does the letter r appear in the word strawberry? Reply with only a number."},
            {"5. Recent information",
             "What is the latest stable version of Selenium WebDriver for Java?"}
        };

        for (String[] q : questions) {
            System.out.println("\n==============================");
            System.out.println(q[0]);
            System.out.println("Question: " + q[1]);

            System.out.println("\nPlain:");
            System.out.println(llm.ask(q[1], null, 0.2));
            Thread.sleep(2000);

            System.out.println("\nWith rule:");
            System.out.println(llm.ask(q[1], rule, 0.2));
            Thread.sleep(2000);
        }
    }
}