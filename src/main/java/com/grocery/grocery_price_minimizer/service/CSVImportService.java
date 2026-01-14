package com.grocery.grocery_price_minimizer.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class CSVImportService {

    private final JdbcTemplate jdbc;

    public CSVImportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Import products from TSV file in resources/csv folder
     */
    public int importFromTSV(String filename) {
        int imported = 0;
        int skipped = 0;

        try {
            ClassPathResource resource = new ClassPathResource("csv/" + filename);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            boolean isHeader = true;

            System.out.println("📂 Reading file: csv/" + filename);

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    System.out.println("⏭️  Skipping header row");
                    continue;
                }

                String[] values = line.split("\t"); // Tab-separated

                if (values.length < 10) {
                    skipped++;
                    continue;
                }

                // Extract barcode
                String barcode = values[0].trim();

                // Skip if barcode is too long for database
                if (barcode.length() > 50) {
                    skipped++;
                    continue;
                }

                // Get the LONGEST/MOST COMPLETE product name from available columns
                // Columns 2, 3, 4 often have different versions - pick the longest one
                String productName = "";
                String name2 = values.length > 2 ? values[2].trim() : "";
                String name3 = values.length > 3 ? values[3].trim() : "";
                String name4 = values.length > 4 ? values[4].trim() : "";

                // Pick the longest non-empty name
                if (name3.length() > productName.length() && !name3.isEmpty()) {
                    productName = name3;
                }
                if (name2.length() > productName.length() && !name2.isEmpty()) {
                    productName = name2;
                }
                if (name4.length() > productName.length() && !name4.isEmpty()) {
                    productName = name4;
                }

                // Extract brand - try multiple columns as they vary
                String brand = "";
                if (values.length > 16 && !values[16].trim().isEmpty()) {
                    brand = values[16].trim();
                }

                // Extract categories
                String categories = "";
                if (values.length > 36 && !values[36].trim().isEmpty()) {
                    categories = values[36].trim();
                }

                // Extract quantity
                String quantity = "";
                if (values.length > 7 && !values[7].trim().isEmpty()) {
                    quantity = values[7].trim();
                }

                // Skip if no product name
                if (productName.isEmpty() || barcode.isEmpty()) {
                    skipped++;
                    continue;
                }

                // Check if already exists
                Integer count = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM products WHERE barcode = ?",
                        Integer.class,
                        barcode
                );

                if (count != null && count > 0) {
                    skipped++;
                    continue;
                }

                // Insert into database
                jdbc.update(
                        """
                        INSERT INTO products (barcode, product_name, brand, category, quantity, openfoodfacts_url)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                        barcode,
                        productName,
                        brand,
                        categories,
                        quantity,
                        "https://world.openfoodfacts.org/product/" + barcode
                );

                imported++;

                if (imported % 10 == 0) {
                    System.out.println("📦 Imported " + imported + " products...");
                }
            }

            br.close();

            System.out.println("✅ Import complete!");
            System.out.println("   Imported: " + imported);
            System.out.println("   Skipped: " + skipped);

        } catch (Exception e) {
            System.err.println("💥 Error importing TSV: " + e.getMessage());
            e.printStackTrace();
        }

        return imported;
    }

    /**
     * Import all CSV files in the csv folder
     */
    public void importAll() {
        System.out.println("🚀 Starting bulk import...");
        importFromTSV("milk.csv");
        importFromTSV("eggs.csv");
        System.out.println("🎉 Bulk import complete!");
    }
}