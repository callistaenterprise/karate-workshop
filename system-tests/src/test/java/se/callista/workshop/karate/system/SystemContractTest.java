package se.callista.workshop.karate.system;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemContractTest {

    @Test
    void testSystem() {
        Results results = Runner
            .path("classpath:se/callista/workshop/karate/system")
            .parallel(1);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

}
