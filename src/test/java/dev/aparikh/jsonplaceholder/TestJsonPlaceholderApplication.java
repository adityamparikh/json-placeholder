package dev.aparikh.jsonplaceholder;

import org.springframework.boot.SpringApplication;

public class TestJsonPlaceholderApplication {

    public static void main(String[] args) {
        SpringApplication.from(JsonPlaceholderApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
