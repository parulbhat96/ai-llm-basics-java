package ai;

public class DebugReturns {

    public static void main(String[] args) throws Exception {
        FaqBot bot = new FaqBot();

        String[] questions = {
                "Can I return an opened laptop?",
                "Can I return an opened phone?",
                "Are returns of opened electronics accepted?",
                "I opened the box of my laptop. Can I send it back?",
                "Can I return an opened camera?",
                "Can I return an opened smartwatch?",
                "Can I return an opened tablet?",
                "Can I return an opened speaker?"
            };

        for (String q : questions) {
            System.out.println("\n==============================");
            System.out.println("Question: " + q);
            System.out.println("Retrieved:");
            for (String chunk : bot.findContext(q)) {
                System.out.println("  - " + chunk);
            }
            System.out.println("Answer: " + bot.answer(q));
            Thread.sleep(4000);
        }
    }
}