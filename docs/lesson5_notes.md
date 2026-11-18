# Lesson 5: Testing the RAG Bot with TestNG

## What I built
An automated TestNG suite for the ShopEasy FAQ bot, in two layers:
- Retrieval tests (FaqRetrievalTest, 3 tests): check that the right FAQ chunk is found. No LLM
  call, so they are instant and free.
- Answer tests (FaqAnswerTest, 16 test runs): check the final answer from the LLM.

Final result: 19 tests, 0 failures.

## Why test in layers
A RAG bot can fail in two places: retrieval (wrong or no chunk found) or generation (right chunk
found, wrong answer). Separate tests show which layer has the bug.

## Why check properties and not exact sentences
The LLM can word the same correct answer in different ways. So tests check properties:
contains "17", is a refusal ("I don't know"), does not contain leaked rules, starts with a clear
No. A set of questions with checks like these is a golden dataset.

## Test coverage
| Category | Tests |
|----------|-------|
| Correct grounded answers | refund period, express shipping, free delivery threshold, opened electronics, money-back phrasing |
| Out-of-scope questions | unknown product (iPhones), general knowledge (capital of France) |
| Prompt injection | tries to leak the rules, tries to change a fact (refund = 90 days) |
| Consistency | same question run 3 times |
| Data-driven | opened laptop, phone, tablet, camera, smartwatch, speaker (6 runs) |
| Retrieval | refund found, out-of-scope retrieves nothing, "money back" finds refund |

## Defects my tests found
### Defect 1: "money back" retrieved nothing (retrieval failure)
- Test: moneyBackQuestionFindsRefundChunk. Message: "Got: []".
- Cause: the retriever matches words, and "money back" shares no words with "refund".
- Fix: a small synonym map in FaqRetriever that adds the FAQ's word to the question.
- Limit: a hand-written list cannot cover every phrase. Embeddings search by meaning and solve
  this properly (Lesson 7). This fix is a patch.

### Defect 2: false "I don't know" for returns (generation failure)
- Test: openedElectronicsCannotBeReturned. Answer: "I don't know."
- The right chunk was retrieved every time, so the bug was in generation.

## How I fixed Defect 2: three attempts
| Attempt | Change | Result |
|---------|--------|--------|
| 1 | Looser rule: a laptop is an electronic item | Still failed |
| 2 | Named more products in the FAQ, added a yes/no rule and 3 worked examples (few-shot) | Fixed laptop, phone and tablet, but camera, smartwatch and speaker still failed |
| 3 | Defined the category in the FAQ: "Electronics means any device that runs on a battery or plugs into a power outlet" | All 8 questions answered correctly |

What I learned:
- My first explanation (the word "laptop" is missing from the text) was wrong. Naming products
  in the FAQ did not fix it. I tested the hypothesis and it failed.
- The failures followed the question template ("Can I return an opened ___?"), not the product.
  Reworded questions passed while the template failed, even at temperature 0.0.
- The bot refused to apply a word the documents never defined. When the document defined
  "electronics", it applied the definition to items that were never named.
- When a RAG bot is wrong or over-cautious, check the knowledge base before changing the prompt.
  A clear document often works better than a cleverer rule.
- I did not change the test to make it pass. The FAQ really says opened electronics cannot be
  returned, so the test was right and the bot was wrong.

## Regression check
After the fixes I ran the full suite. The iPhone and France tests still pass, so the looser
rule did not make the bot answer out-of-scope questions, and both injection tests still pass.
A fix that loosens a rule must be checked against the tests that protect the opposite behaviour.

## Final result
Tests run: 19, Failures: 0, Errors: 0 (about 100 seconds).

## Known limitations (honest notes)
- The suite passed in one full run. LLM output can vary, so a stronger check would run the
  suite several times and track the pass rate.
- Retrieval is keyword-based with a synonym patch. Embeddings would be more robust.
- Only two injection tests, so this is not a full security test.
- Assertions use keyword checks like contains("17"), which can give false failures or passes.
  An LLM-as-judge could check meaning more flexibly.
- The tests call a live API, so they depend on the service being available and on the free-tier
  rate limits. I added pauses between tests and retries in the client.

## Answers
1. Why split retrieval and answer tests? To locate the bug. An empty retrieval result showed at
   once that the money-back bug did not involve the model.
2. Why check properties? The wording of a correct answer varies between runs.
3. injectionCannotChangeFacts asks the bot to ignore its context and state 90 days. It checks
   that the bot still says 17. My manual Lesson 4 test used a question with nothing retrieved,
   so "I don't know" would have been the answer anyway and it proved nothing.
4. Failed tests: moneyBackQuestionFindsRefundChunk (retrieval bug) and
   openedElectronicsCannotBeReturned (generation bug). Both were real bugs, not test errors.
5. Why run the consistency question 3 times? A single pass can be luck, because output varies.