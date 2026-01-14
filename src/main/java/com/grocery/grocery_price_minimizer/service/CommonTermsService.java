package com.grocery.grocery_price_minimizer.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class CommonTermsService {

    // 100 common grocery search terms
    private static final Set<String> COMMON_TERMS = Set.of(
            // Dairy (15)
            "milk", "eggs", "butter", "cheese", "yogurt", "cream", "sour cream",
            "cottage cheese", "cream cheese", "whipped cream", "ice cream",
            "mozzarella", "cheddar", "parmesan", "milk 2%",

            // Meat & Protein (15)
            "chicken", "chicken breast", "ground beef", "beef", "pork", "bacon",
            "sausage", "ham", "turkey", "ground turkey", "steak", "ribs",
            "pork chops", "salmon", "shrimp",

            // Produce - Fruits (15)
            "bananas", "apples", "oranges", "grapes", "strawberries", "blueberries",
            "raspberries", "watermelon", "pineapple", "lemons", "limes", "avocado",
            "tomatoes", "mangoes", "peaches",

            // Produce - Vegetables (15)
            "lettuce", "spinach", "carrots", "broccoli", "potatoes", "onions",
            "garlic", "peppers", "cucumbers", "celery", "mushrooms", "green beans",
            "corn", "cauliflower", "sweet potatoes",

            // Bakery (8)
            "bread", "white bread", "whole wheat bread", "bagels", "tortillas",
            "buns", "croissants", "rolls",

            // Pantry Staples (15)
            "pasta", "rice", "flour", "sugar", "olive oil", "vegetable oil",
            "salt", "pepper", "cereal", "oatmeal", "peanut butter", "jam",
            "honey", "beans", "canned tomatoes",

            // Beverages (10)
            "orange juice", "apple juice", "coffee", "tea", "soda", "water",
            "sparkling water", "lemonade", "cranberry juice", "energy drinks",

            // Condiments & Sauces (7)
            "ketchup", "mustard", "mayonnaise", "hot sauce", "soy sauce",
            "salad dressing", "barbecue sauce"
    );

    /**
     * Check if search query contains any common term
     * Uses "contains" matching - "milk 2%", "whole milk", etc. all match "milk"
     */
    public boolean isCommonTerm(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Check if query exactly matches a common term
        if (COMMON_TERMS.contains(lowerQuery)) {
            return true;
        }

        // Check if query contains any common term
        for (String term : COMMON_TERMS) {
            if (lowerQuery.contains(term)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the matching common term from a query
     * Returns the term that matched, or null if none
     */
    public String getMatchingTerm(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Exact match first
        if (COMMON_TERMS.contains(lowerQuery)) {
            return lowerQuery;
        }

        // Contains match
        for (String term : COMMON_TERMS) {
            if (lowerQuery.contains(term)) {
                return term;
            }
        }

        return null;
    }


}