package ai;

public class Lab1 {

    // Short wait so we stay under the free tier's limits
    static void pause() throws Exception {
        Thread.sleep(2000);
    }

    public static void main(String[] args) throws Exception {

        GeminiClient llm = new GeminiClient();

        System.out.println("=== Experiment 1: Your first LLM call ===");
        String firstQuestion = "Explain what a test case is in one sentence.";
        System.out.println("Answer: " + llm.ask(firstQuestion, null, 0.2));
        System.out.println("Tokens in my question: " + llm.countTokens(firstQuestion));
        pause();

        System.out.println("\n=== Experiment 2: The model does not know your private information ===");
        String question = "What is the refund period for ShopEasy orders?";

        System.out.println("Without context:");
        System.out.println(llm.ask(question, null, 0.2));
        pause();

        String context = "ShopEasy refund policy: Customers can request a refund within 17 days of delivery.";
        System.out.println("\nWith context added to the question:");
        System.out.println(llm.ask("Context:\n" + context + "\n\nQuestion: " + question, null, 0.2));
        pause();

        System.out.println("\n=== Experiment 3: Rules reduce made-up answers ===");
        String fake = "What are the key features of TestZilla 9.4 by BluePeak Systems?";

        System.out.println("Without a rule:");
        System.out.println(llm.ask(fake, null, 0.2));
        pause();

        String rule = "Answer only if you are sure. If you do not know, reply exactly: I don't know.";
        System.out.println("\nWith a rule:");
        System.out.println(llm.ask(fake, rule, 0.2));
        pause();

        System.out.println("\n=== Experiment 4: Temperature ===");
        String name = "Suggest one creative name for a bug-tracking tool. Reply with only the name.";
        for (double temp : new double[] {0.0, 1.5}) {
            System.out.println("Temperature " + temp + ":");
            for (int i = 0; i < 3; i++) {
                System.out.println("   " + llm.ask(name, null, temp).trim());
                pause();
            }
        }
    }
}