package com.malatesh.test.micrometer_test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@SpringBootApplication
public class MicrometerTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicrometerTestApplication.class, args);
	}

}

@Component
class RunnerHolder implements CommandLineRunner {
  @Override
  public void run(String... args) throws Exception {
    System.out.println("Application is running... Press Ctrl+C to stop.");
    Thread.currentThread().join(); // Keeps the application alive
  }
}




@Service
class PaymentService {

  private final Counter successCounter;
  private final Timer paymentTimer;

  public PaymentService(MeterRegistry registry) {
    // 1. A Counter: Only goes up (e.g., total payments)
    this.successCounter = Counter.builder("payment.success")
        .description("Total successful payments")
        .tag("type", "credit_card")
        .register(registry);

    // 2. A Timer: Tracks how long a task takes
    this.paymentTimer = Timer.builder("payment.processing.time")
        .description("Time taken to process payment")
        .register(registry);
  }

  public void processPayment() {
    // Wrap the logic in a timer
    paymentTimer.record(() -> {
      try {
        // Simulate payment processing logic
        Thread.sleep((long) (Math.random() * 1000));
        successCounter.increment();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
  }
}