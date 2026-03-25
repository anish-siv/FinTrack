package com.finance.expensetracker.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.finance.expensetracker.model.Expense;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AiInsightService {

    @Value("${anthropic.api.key:}")
    private String apiKey;

    private AnthropicClient client;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isBlank()) {
            client = AnthropicOkHttpClient.builder()
                    .apiKey(apiKey)
                    .build();
        }
    }

    public String getInsight(String userQuestion, List<Expense> expenses) {
        if (client == null) {
            return "AI insights are not configured. Please add your Anthropic API key to application.properties.";
        }
        try {
            String expenseSummary = buildExpenseSummary(expenses);

            MessageCreateParams params = MessageCreateParams.builder()
                    .model(Model.CLAUDE_SONNET_4_6)
                    .maxTokens(1024L)
                    .system("You are a helpful personal finance advisor. " +
                            "Analyze the user's expense data and provide concise, actionable insights. " +
                            "Keep responses under 200 words and focus on practical advice.")
                    .addUserMessage(expenseSummary + "\n\nUser question: " + userQuestion)
                    .build();

            Message message = client.messages().create(params);
            return message.content().get(0).asText().text();

        } catch (Exception e) {
            return "Unable to generate insights at this time. Please try again later.";
        }
    }

    private String buildExpenseSummary(List<Expense> expenses) {
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (Expense e : expenses) {
            byCategory.merge(e.getCategory(), e.getAmount(), BigDecimal::add);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Expense Summary:\n");
        sb.append("Total transactions: ").append(expenses.size()).append("\n");

        BigDecimal total = byCategory.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        sb.append("Total spent: $").append(total).append("\n");

        sb.append("\nSpending by category:\n");
        byCategory.forEach((cat, amt) ->
                sb.append("  - ").append(cat).append(": $").append(amt).append("\n"));

        sb.append("\nMost recent 10 transactions:\n");
        expenses.stream()
                .sorted(Comparator.comparing(Expense::getExpenseDate).reversed())
                .limit(10)
                .forEach(e -> sb.append("  - ")
                        .append(e.getExpenseDate().format(DATE_FMT)).append(": ")
                        .append(e.getDescription()).append(" ($")
                        .append(e.getAmount()).append(", ")
                        .append(e.getCategory()).append(")\n"));

        return sb.toString();
    }
}
