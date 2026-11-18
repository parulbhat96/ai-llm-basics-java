# AI & LLM Testing: RAG Chatbot and Test Suite (Java)

A small RAG (Retrieval-Augmented Generation) FAQ chatbot for a made-up shop, ShopEasy, plus an
automated TestNG suite that tests it for accuracy, grounding, out-of-scope handling, prompt
injection and consistency. Built in Java with the Gemini REST API. No AI framework is used, so
every step (retrieval, prompt building, API call) is plain, readable code.

## What's in this repo
- A Java client for the Gemini REST API with retries for rate limits (429) and busy servers (503)
- Learning labs on tokens, temperature, knowledge cutoff, hallucination and prompt engineering
- A RAG FAQ bot: keyword retrieval, grounded prompt, "I don't know" rule
- A 19-test TestNG suite in two layers (retrieval tests and answer tests)
- Lesson notes in `docs/` describing what I observed and learned

## How the RAG bot works
```
Question -> retrieve relevant FAQ lines -> build prompt (rules + lines + question) -> LLM -> answer
```
1. Retrieve: `FaqRetriever` scores each FAQ entry by shared keywords with the question.
2. Augment: `FaqBot` adds the best entries to the prompt as context.
3. Generate: the LLM answers using only that context, or says "I don't know".

## Tech stack
Java 17, Maven, TestNG, Jackson (JSON), Gemini REST API

## Project structure
```
src/main/java/ai
  ├── GeminiClient.java     API client (key from local.properties, retries)
  ├── Lab1.java             tokens, temperature, knowledge cutoff, hallucination
  ├── Lab2.java             prompt engineering and test case generator with format check
  ├── Lab3.java             hallucination spotter
  ├── FaqRetriever.java     keyword retrieval with a small synonym map
  ├── FaqBot.java           the RAG bot (also runs as a console chat)
  └── DebugReturns.java     script used to investigate a bug
src/test/java/ai
  ├── FaqRetrievalTest.java retrieval layer tests (no LLM call)
  └── FaqAnswerTest.java    answer layer tests (calls the LLM)
data/shopeasy_faq.txt       the knowledge base
docs/                       lesson notes
```

## Test approach
- **Two layers:** retrieval tests check that the right FAQ entry is found, and answer tests check
  the final answer. When a test fails, the layer shows where the bug is.
- **Properties, not exact sentences:** an LLM can word the same correct answer differently, so
  tests check things like "contains 17", "is a refusal", "does not leak the rules".
- **Categories covered:** grounded answers, out-of-scope questions, prompt injection, consistency
  across runs, and a data-driven set of products.

Latest result: 19 tests, 0 failures.

## Defects the tests found
| Defect | Layer | Cause | Fix |
|--------|-------|-------|-----|
| "I want my money back" retrieved nothing | Retrieval | Keyword search cannot match "money back" to "refund" | Small synonym map (a patch; embeddings are the proper fix) |
| "Can I return an opened laptop?" gave a false "I don't know" | Generation | The FAQ never defined "electronics", so the model refused to apply the rule | Added a definition to the FAQ and a clearer rule with worked examples |

Three fix attempts were needed for the second defect: a looser rule (failed), naming more products
(partial), and defining the category in the knowledge base (worked). I did not change the test to
make it pass. Details are in `docs/lesson5_notes.md`.

## How to run
Prerequisites: Java 17+, Maven, and a Gemini API key from Google AI Studio.

1. Clone the repo.
2. Create `local.properties` in the project root:
```
GEMINI_API_KEY=your_key_here
GEMINI_MODEL=model_name_from_ai_studio
```
3. Run the tests: `mvn clean test`
4. Or chat with the bot: run `FaqBot.java` from your IDE.

`local.properties` is in `.gitignore`, so the key is never committed. The free API tier has
request limits, so the tests pause between calls. Use only public or made-up documents with a
free-tier key.

## Known limitations
- Keyword retrieval with a synonym patch. Embeddings would search by meaning.
- LLM output can vary, so one passing run is not proof. A stronger setup would repeat runs and
  track the pass rate.
- Keyword assertions can give false passes or failures. An LLM-as-judge could check meaning.
- Only two prompt injection tests, so this is not a full security test.
- Tests call a live API, so they depend on the service being available.

## Future improvements
- Embeddings and similarity search for retrieval
- LLM-as-judge scoring of answers
- A small tool-using agent with tests for tool choice
- Repeated runs with a pass-rate report