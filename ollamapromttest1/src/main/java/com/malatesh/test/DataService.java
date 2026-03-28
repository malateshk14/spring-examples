package com.malatesh.test;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getData() {
        return "Hello, World!";
    }

    public String getTransactions(String startDate, String endDate) {
        List<Map<String, Object>> transactions = new ArrayList<>();

        // Mock transactions
        transactions.add(createTransaction(1, "2023-01-01", 1500.0));
        transactions.add(createTransaction(2, "2023-01-15", 2000.0));
        transactions.add(createTransaction(3, "2023-02-01", 1200.0));
        transactions.add(createTransaction(4, "2023-02-15", 3000.0));
        transactions.add(createTransaction(5, "2023-03-01", 1050.0));

        // Filter by date range
        List<Map<String, Object>> filtered = transactions.stream()
            .filter(tx -> {
                String txDate = (String) tx.get("date");
                return txDate.compareTo(startDate) >= 0 && txDate.compareTo(endDate) <= 0;
            })
            .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(filtered);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private Map<String, Object> createTransaction(int id, String date, double amount) {
        Map<String, Object> tx = new HashMap<>();
        tx.put("id", id);
        tx.put("date", date);
        tx.put("amount", amount);
        return tx;
    }


}