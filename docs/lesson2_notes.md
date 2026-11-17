# Lesson 2: Prompt Engineering

## What I built
A test case generator that sends one user story (password reset, link expires after 15 minutes)
to the LLM in three ways, then checks the output format with a small Java method.

## Parts of a good prompt
- Role: who the model should act as (senior QA engineer)
- Task: what to do (write test cases)
- Context: the facts to use (the user story)
- Constraints: rules (exactly 6 cases, 3 positive, 2 negative, 1 boundary, do not invent features)
- Format: how the answer looks (one line per case: ID | Type | Title | Expected Result)
- Example: one sample line (few-shot prompting)

## Results
- Version 1 (vague): 15 test cases in markdown tables. Hard to check automatically. It added
  features not in the story: Confirm Password field, password complexity rules, visibility
  toggle, responsive layout, rate limiting.
- Version 2 (structured): 6 valid rows out of 6 expected.
- Version 3 (structured with an example): 6 valid rows out of 6. Repeated 3 more times: 6/6 each time.

## What I learned
- A fixed format makes AI output testable by code. "Exactly 6 lines in this format" can be
  checked, "write good test cases" cannot.
- A passing format check does not mean good content. Some expected results were vague:
  "an error message is displayed or no reset link is sent" and "still valid or expires as per
  the rule" are not testable.
- The boundary case at exactly 15 minutes is ambiguous in the user story, so the right action
  is to ask the product owner, not accept the model's guess.
- Always review AI-generated test cases against the requirement for invented features.

## Answers
1. Six parts of a prompt: role, task, context, constraints, format, example.
2. Easier to test because the rule is precise and can be checked by code.
3. Adding an example kept the format consistent and made the wording closer to my sample.
4. Yes, in version 1 the model added features that were not in the story.