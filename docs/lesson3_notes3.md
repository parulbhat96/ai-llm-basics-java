# Lesson 3: Where LLMs Go Wrong

## What I built
A hallucination spotter in Java. It asks five questions of different types, each one plain and
then with a rule ("answer only if sure, point out wrong assumptions, otherwise say I don't know").
I graded every answer myself.

## Types of errors and hallucinations
1. Invented facts or sources (fake papers, quotes, features)
2. Going along with a false assumption
3. Outdated information (knowledge cutoff)
4. Reasoning and counting errors
5. Ignoring the context that was supplied

## Results
| # | Test type | Plain answer | With rule | What I noticed |
|---|-----------|--------------|-----------|----------------|
| 1 | Control (API acronym) | Correct | Correct | Rule version was shorter |
| 2 | Fake research paper | Correct: said it could not find the paper | Correct: "I don't know" | No invented title or authors. The plain answer claimed no such paper exists, which the model cannot truly verify |
| 3 | False assumption (Selenium 4 removed WebDriver) | Correct core: rejected the false premise. Extra details need checking | "I don't know": worse | The rule made the model drop a correct, useful correction (over-refusal) |
| 4 | Counting r in strawberry | Correct (3) | Correct (3) | Model handled it, but I would keep it in a test set because other models can fail it |
| 5 | Latest Selenium Java version | Outdated: said 4.21.0 "as of May 2024" | "I don't know" | My own project uses 4.25.0. Real latest version: [what I found on selenium.dev] |

## My checks of the extra claims in question 3
[Write which two claims I checked (for example DesiredCapabilities, OperaDriver) and whether each
was correct, with the source I used.]

## What I learned
- A model can reject a false assumption and still add unverified details. Check the extras.
- A rule reduces risky answers but can also cause over-cautious, unhelpful answers. Test for
  both wrong answers and needless refusals.
- Time-sensitive facts (latest versions, prices, dates) go stale. They need live data or retrieved
  context.
- The model's description of its own knowledge date was inconsistent (January 2025 in Lesson 1,
  May 2024 in this lab), so do not trust a model's statements about itself.
- Always grade AI output yourself against a trusted source.

## Answers
1. Five types: invented facts, false assumptions, outdated information, reasoning and counting
   errors, ignoring supplied context.
2. The weakest results were question 5 (outdated answer) and question 3 (a correct correction
   mixed with unverified extras).
3. The rule helped in questions 2 and 5 but hurt in question 3, so it did not fix everything.
4. The context window is the maximum number of tokens a model can handle in one request, counting
   the prompt and the answer. RAG sends only the relevant pieces because more text costs more,
   is slower, and the model can miss details in very long input.