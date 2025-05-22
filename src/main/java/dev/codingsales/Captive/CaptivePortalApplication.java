package dev.codingsales.Captive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableAutoConfiguration
@SpringBootApplication
@Configuration
public class CaptivePortalApplication implements WebMvcConfigurer{
	/**
	 * Rest template.
	 *
	 * @return the rest template
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */

	public static void main(String[] args) {
		SpringApplication.run(CaptivePortalApplication.class, args);
	}
	/**
	 * Adds the cors mappings.
	 *
	 * @param registry the registry
	 */
}
