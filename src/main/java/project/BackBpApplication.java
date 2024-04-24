package project;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import project.model.project.Project;
import project.service.*;

import java.util.Arrays;

@SpringBootApplication
@EnableMongoRepositories
public class BackBpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackBpApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(
            UserDatasetService datasetService
    ) {
        return args -> {
//            System.out.println(
//                    datasetService.deleteAllByDatasetId("6621b1451d8d2e4499a2ab07")
//            );
        };
    }
}
