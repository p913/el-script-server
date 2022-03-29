package ru.ptvi.elscriptserver;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.ptvi.elscriptserver.config.NettyProperties;
import ru.ptvi.elscriptserver.config.ScriptServerProperties;
import ru.ptvi.elscriptserver.tcpserver.ScriptServer;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
@EnableConfigurationProperties({NettyProperties.class, ScriptServerProperties.class})
public class ElScriptServerApplication {

	private final ScriptServer scriptServer;

	/**
	 * This can not be implemented with lambda, because of the spring framework limitation
	 * (https://github.com/spring-projects/spring-framework/issues/18681)
	 *
	 * @return bean
	 */
	@SuppressWarnings({"Convert2Lambda", "java:S1604"})
	@Bean
	public ApplicationListener<ApplicationReadyEvent> readyEventApplicationListener() {
		return new ApplicationListener<ApplicationReadyEvent>() {
			@Override
			public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
				scriptServer.start();
			}
		};
	}


	public static void main(String[] args) {
		SpringApplication.run(ElScriptServerApplication.class, args);
	}

}
