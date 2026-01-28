import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class SalesDataCleaner {

    public static void main(String[] args) {
        String inputFile = "sales.csv";
        String outputFile = "clean_sales.json";
        
        // Fix 1: Added <String> generics for type safety
        List<String> jsonRows = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        // Fix 2: Used 'try-with-resources' to automatically close the file (no manual .close() needed)
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip empty lines to prevent errors
                if (line.trim().isEmpty()) continue;

                try {
                    String[] parts = line.split(",");
                    
                    // Basic validation to ensure line has enough columns
                    if (parts.length < 4) continue; 

                    int id = Integer.parseInt(parts[0].trim());
                    String product = parts[1].replace("\"", "").trim();
                    // Clean currency symbols and quotes
                    String priceString = parts[2].replace("$", "").replace("\"", "").trim();
                    String country = parts[3].trim();
                    
                    double priceUsd = Double.parseDouble(priceString);
                    
                    // Create a unique key (Product + Price) to avoid duplicates
                    String uniqueKey = product + priceUsd;
                    
                    if (!uniqueKeys.contains(uniqueKey)) {
                        uniqueKeys.add(uniqueKey);
                        double priceInr = priceUsd * 83.0; // Conversion rate
                        
                        String jsonEntry = String.format("  {\n    \"id\": %d,\n    \"product\": \"%s\",\n    \"price_inr\": %.2f,\n    \"country\": \"%s\"\n  }", 
                                           id, product, priceInr, country);
                        jsonRows.add(jsonEntry);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Skipping invalid row: " + line);
                }
            }

            writeJson(outputFile, jsonRows);
            System.out.println("✅ Data cleaned and saved to " + outputFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeJson(String filePath, List<String> data) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("[\n");
            writer.write(String.join(",\n", data));
            writer.write("\n]");
        }
    }
}