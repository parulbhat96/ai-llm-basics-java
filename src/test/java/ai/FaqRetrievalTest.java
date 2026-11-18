package ai;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FaqRetrievalTest {

    private FaqRetriever retriever;

    @BeforeClass
    public void setUp() throws Exception {
        retriever = new FaqRetriever("data/shopeasy_faq.txt");
    }

    private boolean hasChunkStartingWith(List<String> chunks, String prefix) {
        return chunks.stream().anyMatch(chunk -> chunk.startsWith(prefix));
    }

    @Test
    public void refundQuestionFindsRefundChunk() {
        List<String> chunks = retriever.retrieve("What is the refund period?", 2);
        Assert.assertTrue(hasChunkStartingWith(chunks, "Refunds:"),
                "Refund chunk was not retrieved. Got: " + chunks);
    }

    @Test
    public void outOfScopeQuestionRetrievesNothing() {
        List<String> chunks = retriever.retrieve("Do you sell iPhones?", 2);
        Assert.assertTrue(chunks.isEmpty(), "Expected no chunks. Got: " + chunks);
    }

    @Test
    public void moneyBackQuestionFindsRefundChunk() {
        List<String> chunks = retriever.retrieve("I want my money back, how long do I have?", 2);
        Assert.assertTrue(hasChunkStartingWith(chunks, "Refunds:"),
                "Refund chunk was not retrieved for a synonym. Got: " + chunks);
    }
}