# Lesson 1: LLM Basics

## What I built
A small Java program that sends questions to an LLM (Gemini) over a REST API and prints the answers.
It reads the API key and model name from `local.properties`, which is not pushed to GitHub.
It retries when the server is busy (status 429, 500 or 503).

## Key ideas
- **LLM:** a model trained on a huge amount of text. It produces an answer by predicting the next
  token, one piece at a time. It generates likely text and does not look facts up.
- **Token:** the small piece of text a model reads and writes. My question "Explain what a test case
  is in one sentence." used 10 tokens. Services bill and limit by tokens.
- **Prompt:** the text sent to the model.
- **System instruction:** standing rules for the model, for example "If you do not know, reply
  exactly: I don't know."
- **Temperature:** controls randomness. Lower values give steadier answers, higher values give
  more varied ones.
- **Knowledge cutoff:** the date after which the model knows nothing. The model told me its
  cutoff is January 2025.
- **Hallucination:** a confident answer that is wrong or invented.
- **Context window:** the maximum number of tokens a model can handle in one request.

## Experiment 1: First call
The model answered correctly. A call to an LLM is just a REST API call: a POST request with a JSON
body and an API key header, so it can be tested like any other API.

## Experiment 2: Private information
- Without context, the model said it had no information about ShopEasy's refund policy and
  suggested checking the website. It did not guess.
- With the policy text added to the prompt, it answered "17 days of delivery" and said it was
  based on the provided context.
- Conclusion: the model answers well when the right information is in the prompt. RAG is a way to
  find that information automatically and add it to the prompt.

## Experiment 3: Made-up product (TestZilla 9.4)
- The model did not invent features. It said it had no information and suggested the product might
  be fictional.
- With the rule "If you do not know, reply exactly: I don't know", the answer was just
  "I don't know". Rules in the system instruction make answers shorter and more predictable.
- The model suggested similar tools: TestRail, Zephyr and LoadZilla.
  My check on LoadZilla:  Loadzilla, a cloud-based web load testing platform designed to simulate traffic and test website performance.
- Conclusion: modern models don't always hallucinate, but a helpful extra detail in an answer can
  still be wrong. Testing needs many varied questions, not one example.

## Experiment 4: Temperature
- At temperature 0.0, I still got different names across runs (Glitchnit, Chrysalis).
- At temperature 1.5, there was more variety (GlitchHound, GlitchHunt, Exterminator).
- Conclusion: low temperature makes output more stable, not identical. That is why tests for AI
  check properties of the answer (does it contain the key fact, is the format right) instead of
  exact wording.

## Problems I hit and how I fixed them
- The program used the name of my API key as the model name, which gave an "Illegal character in
  path" error. I fixed it by putting the real model name in `local.properties`.
- Server errors (503 UNAVAILABLE) happened when the model was busy. I added retries with waiting
  to the client.
- One answer was cut off. The model can return its answer in several parts, so I joined all the
  parts together.

## Why this matters for testing AI
- The same input can give differently worded answers, so exact-match assertions don't work.
- Test results can fail because the service was busy, not because the answer was wrong, so
  retries and clear error handling are needed.
- The model only knows what is in its training data and in the prompt, so wrong or missing
  context leads to wrong answers.

## Questions I can now answer
1. How does an LLM produce an answer? By predicting the next token repeatedly, based on the prompt
   and patterns learned in training.
2. Why can it be wrong while sounding sure? It generates likely-sounding text and does not verify
   facts. It also has a knowledge cutoff and no access to private data.
3. What happens when context is added to the prompt? The model can use that information, so it
   answers correctly instead of saying it doesn't know.
4. Why can't we always use exact-match assertions? The wording changes between runs, so tests must
   check properties of the answer.