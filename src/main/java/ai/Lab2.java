package ai;

public class Lab2 {

    // Counts how many lines look like: TC01 | Positive | Title | Expected result
    static int countValidRows(String answer) {
        int count = 0;
        for (String line : answer.split("\n")) {
            line = line.trim();
            if (line.startsWith("|")) {
                line = line.substring(1);
            }
            if (line.endsWith("|")) {
                line = line.substring(0, line.length() - 1);
            }
            String[] cells = line.split("\\|", -1);
            if (cells.length == 4 && cells[0].trim().startsWith("TC")) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) throws Exception {

        GeminiClient llm = new GeminiClient();

        String userStory = "As a shopper, I want to reset my password using my registered email, "
                + "so that I can log in again if I forget it. "
                + "The reset link expires after 15 minutes.";

        // Version 1: a vague prompt
        System.out.println("=== Version 1: vague prompt ===");
        System.out.println(llm.ask("Write test cases for this: " + userStory, null, 0.2));
        Thread.sleep(2000);

        // Version 2: role + task + constraints + format
        String role = "You are a senior QA engineer with 10 years of experience in web application testing.";

        String structured = "Write test cases for the user story below.\n\n"
                + "User story: " + userStory + "\n\n"
                + "Rules:\n"
                + "- Write exactly 6 test cases: 3 positive, 2 negative, 1 boundary.\n"
                + "- Use only information from the user story. Do not invent features.\n"
                + "- Write one test case per line, in exactly this format, with no other text:\n"
                + "ID | Type | Title | Expected Result\n";

        System.out.println("\n=== Version 2: structured prompt ===");
        String answer2 = llm.ask(structured, role, 0.2);
        System.out.println(answer2);
        System.out.println("Format check: " + countValidRows(answer2) + " valid rows (expected 6)");
        Thread.sleep(2000);

        // Version 3: the same, plus one example (few-shot)
        String withExample = structured
                + "\nExample of one line:\n"
                + "TC01 | Positive | Reset link sent to registered email | A reset email is received\n";

        System.out.println("\n=== Version 3: structured prompt with an example ===");
        String answer3 = llm.ask(withExample, role, 0.2);
        System.out.println(answer3);
        System.out.println("Format check: " + countValidRows(answer3) + " valid rows (expected 6)");
        Thread.sleep(2000);

        // Run version 3 three more times: is the format stable?
        System.out.println("\n=== Consistency: version 3 repeated 3 times ===");
        for (int i = 1; i <= 3; i++) {
            String answer = llm.ask(withExample, role, 0.2);
            System.out.println("Run " + i + ": " + countValidRows(answer) + " valid rows (expected 6)");
            Thread.sleep(2000);
        }
    }
}