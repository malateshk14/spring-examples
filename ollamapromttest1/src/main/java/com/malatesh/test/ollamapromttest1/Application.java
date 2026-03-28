package com.malatesh.test.ollamapromttest1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import com.malatesh.test.DataService;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.malatesh.test", 
    "com.malatesh.test.ollamapromttest1"
})
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

    System.out.println("Shri Ganesh.");

    return chatModel.call(message);
  }
}

@RestController
class BankController {

  private final OllamaChatModel chatModel;
  private final ChatClient chatClient;
  private final DataService dataService;

  @Value("classpath:/prompts/bank-prompt.st")
  private Resource bankResource;

  public BankController(OllamaChatModel chatModel, ChatClient.Builder builder, DataService dataService) {
    this.chatModel = chatModel;
    this.chatClient = builder.build();
    this.dataService = dataService;
  }

  @GetMapping("/ai/bank")
  public String generate(@RequestParam(value = "message") String message) {

    System.out.println("Shri Ganesh.");

    // Parse dates from message - support multiple formats
    String startDate = null;
    String endDate = null;
    
    // First try ISO format (YYYY-MM-DD)
    Pattern isoPattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    Matcher isoMatcher = isoPattern.matcher(message);
    if (isoMatcher.find()) {
      startDate = isoMatcher.group();
      if (isoMatcher.find()) {
        endDate = isoMatcher.group();
      } else {
        endDate = startDate;
      }
    }
    
    // If no ISO dates found, try to parse a natural language range first
    if (startDate == null) {
      String[] rangeDates = parseNaturalLanguageDateRange(message);
      if (rangeDates != null) {
        startDate = rangeDates[0];
        endDate = rangeDates[1];
      }
    }

    // If still no explicit dates, fallback to generic date detector
    if (startDate == null) {
      startDate = parseNaturalLanguageDate(message, true); // first date
      endDate = parseNaturalLanguageDate(message, false); // second date
      if (startDate != null && endDate == null) {
        endDate = startDate;
      }
    }

    if (startDate == null) {
      // Default to current month
      LocalDate now = LocalDate.now();
      startDate = now.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      endDate = now.withDayOfMonth(now.lengthOfMonth()).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    String transactionsJson = dataService.getTransactions(startDate, endDate);

    PromptTemplate template = new PromptTemplate(bankResource);
    Prompt prompt = template.create(Map.of(
        "userInput", message,
        "transactionsJson", transactionsJson
    ));
    String result = chatClient.prompt(prompt).call().content();
    return result;
  }

  private String parseNaturalLanguageDate(String message, boolean getFirst) {
    // Pattern to match: Month Day, Year (e.g., "January 1, 2023")
    Pattern pattern = Pattern.compile("(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{1,2}),?\\s+(\\d{4})");
    Matcher matcher = pattern.matcher(message);
    
    int count = 0;
    while (matcher.find()) {
      count++;
      if ((getFirst && count == 1) || (!getFirst && count == 2)) {
        String monthName = matcher.group(1);
        int day = Integer.parseInt(matcher.group(2));
        int year = Integer.parseInt(matcher.group(3));
        int month = monthNameToInt(monthName);

        try {
          LocalDate date = LocalDate.of(year, month, day);
          return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception e) {
          return null;
        }
      }
    }
    return null;
  }

  private String[] parseNaturalLanguageDateRange(String message) {
    Pattern range = Pattern.compile("(?i)from\\s+(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{1,2})(?:,?\\s*(\\d{4}))?\\s+to\\s+(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+(\\d{1,2})(?:,?\\s*(\\d{4}))?");
    Matcher m = range.matcher(message);
    if (m.find()) {
      String startMonthName = m.group(1);
      int startDay = Integer.parseInt(m.group(2));
      String startYearText = m.group(3);

      String endMonthName = m.group(4);
      int endDay = Integer.parseInt(m.group(5));
      String endYearText = m.group(6);

      String effectiveYear = endYearText != null ? endYearText : startYearText;
      if (effectiveYear == null) {
        return null;
      }

      int startMonth = monthNameToInt(startMonthName);
      int endMonth = monthNameToInt(endMonthName);

      try {
        String start = LocalDate.of(Integer.parseInt(effectiveYear), startMonth, startDay).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String end = LocalDate.of(Integer.parseInt(effectiveYear), endMonth, endDay).format(DateTimeFormatter.ISO_LOCAL_DATE);
        return new String[] { start, end };
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  private int monthNameToInt(String monthName) {
    return switch (monthName.toLowerCase()) {
      case "january", "jan" -> 1;
      case "february", "feb" -> 2;
      case "march", "mar" -> 3;
      case "april", "apr" -> 4;
      case "may" -> 5;
      case "june", "jun" -> 6;
      case "july", "jul" -> 7;
      case "august", "aug" -> 8;
      case "september", "sep", "sept" -> 9;
      case "october", "oct" -> 10;
      case "november", "nov" -> 11;
      case "december", "dec" -> 12;
      default -> throw new IllegalArgumentException("Unknown month: " + monthName);
    };
  }
}

@RestController
class PromptController {

  private final ChatClient chatClient;

  @Value("classpath:/prompts/joke-prompt.st")
  private Resource jokeResource;

  public PromptController(ChatClient.Builder builder) {
    this.chatClient = builder.build();
  }

  @GetMapping("/ai/joke")
  public String generateJoke(String subject) {
    // 1. Create the template from the Resource
    PromptTemplate template = new PromptTemplate(jokeResource);
    // 2. Render the template with a Map of values
    Prompt prompt = template.create(Map.of(
        "subject", subject,
        "audience", "medical professional",
        "maxLength", "3"
    ));

    System.out.println("call joke api.");

    // 3. Call the LLM
    return chatClient.prompt(prompt).call().content();
  }
}