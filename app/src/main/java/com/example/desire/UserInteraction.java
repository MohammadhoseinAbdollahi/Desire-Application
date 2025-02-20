package com.example.desire;

import java.util.HashMap;
import java.util.Map;

public class UserInteraction {
    public String subUserId;
    public int countRate;
    public double totalRate;
    public static double averageRate;

    public UserInteraction() {
    }

    public UserInteraction(String subUserId, int countRate, double totalRate) {
        this.subUserId = subUserId;
        this.countRate = countRate;
        this.totalRate = totalRate;
        averageRate = totalRate / countRate;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("subUserId", subUserId);
        result.put("countRate", countRate);
        result.put("totalRate", totalRate);
        result.put("averageRate", averageRate);
        return result;
    }

    public static double getAverageRating() {
        return averageRate;
    }
}