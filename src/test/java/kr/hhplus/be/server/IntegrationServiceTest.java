package kr.hhplus.be.server;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationServiceTest {

//    @Autowired
//    private TestContainerDatabaseCleaner testContainerDatabaseCleaner;
//
//    @AfterEach
//    void init() {
//        testContainerDatabaseCleaner.execute();
//    }
}