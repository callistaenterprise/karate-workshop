package se.callista.workshop.karate.system;

import com.intuit.karate.junit5.Karate;

class SystemContractRunner {

    @Karate.Test
    Karate testSystem() {
        return Karate
            .run("System")
            .relativeTo(getClass());
    }
}
