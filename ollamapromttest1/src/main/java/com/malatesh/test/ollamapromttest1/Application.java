package com.malatesh.test.ollamapromttest1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}




@RestController
class AIController {

  private final OllamaChatModel chatModel;

  public AIController(OllamaChatModel chatModel) {
    this.chatModel = chatModel;
  }

  @GetMapping("/ai/generate")
  public String generate(@RequestParam(value = "message") String message) {
    // This sends the prompt to your Docker container running llama3.2:1b
    System.out.println("Shri Ganesh.");

    return chatModel.call(message);
  }
}