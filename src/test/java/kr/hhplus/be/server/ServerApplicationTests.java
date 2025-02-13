package kr.hhplus.be.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

@Testcontainers
@SpringBootTest
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}

	@Container
	public static DockerComposeContainer<?> composeContainer =
			new DockerComposeContainer<>(new File("docker-compose.yml"))
					.withExposedService("mysql", 3306); // docker-compose 서비스 이름과 포트



}
