package likelion.mcmshowcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@EnableJpaAuditing
@SpringBootApplication
public class McmShowcaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(McmShowcaseApplication.class, args);
    }

}
