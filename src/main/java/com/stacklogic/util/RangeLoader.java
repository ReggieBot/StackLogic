package com.stacklogic.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.stacklogic.model.Action;
import com.stacklogic.model.Position;
import com.stacklogic.model.Range;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading poker ranges from JSON files.
 *
 * HOW JSON LOADING WORKS:
 * =======================
 * 1. We store range data as JSON files in src/main/resources/ranges/
 * 2. When the app runs, these files are bundled inside the JAR
 * 3. We use getResourceAsStream() to read them (works in JAR or IDE)
 * 4. Gson parses the JSON text into Java objects
 *
 * JSON FORMAT:
 * ============
 * {
 *   "name": "UTG RFI",
 *   "position": "UTG",
 *   "situation": "RFI",
 *   "hands": {
 *     "raise": ["AA", "KK", "QQ", "AKs", "AKo", ...],
 *     "call": [],
 *     "3bet": []
 *   }
 * }
 */
public class RangeLoader {

    private static final Gson gson = new Gson();

    // Cache loaded ranges to avoid re-reading files
    private static final Map<String, Range> rangeCache = new HashMap<>();

    /**
     * Load a range from a JSON file in the resources/ranges folder.
     *
     * @param filename The JSON filename (without path, e.g., "utg_rfi.json")
     * @return The loaded Range object, or null if not found
     */
    public static Range loadRange(String filename) {
        // Check cache first
        if (rangeCache.containsKey(filename)) {
            return rangeCache.get(filename);
        }

        String resourcePath = "/ranges/" + filename;

        try (InputStream is = RangeLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Range file not found: " + resourcePath);
                return null;
            }

            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            // Parse the JSON structure
            String name = json.get("name").getAsString();
            String positionStr = json.get("position").getAsString();
            String situation = json.get("situation").getAsString();

            Position position = Position.valueOf(positionStr.toUpperCase());
            Range range = new Range(name, position, situation);

            // Parse hands by action
            JsonObject handsObj = json.getAsJsonObject("hands");

            if (handsObj.has("raise")) {
                for (JsonElement hand : handsObj.getAsJsonArray("raise")) {
                    range.setHandAction(hand.getAsString(), Action.RAISE);
                }
            }

            if (handsObj.has("call")) {
                for (JsonElement hand : handsObj.getAsJsonArray("call")) {
                    range.setHandAction(hand.getAsString(), Action.CALL);
                }
            }

            if (handsObj.has("3bet")) {
                for (JsonElement hand : handsObj.getAsJsonArray("3bet")) {
                    range.setHandAction(hand.getAsString(), Action.THREE_BET);
                }
            }

            // Cache and return
            rangeCache.put(filename, range);
            return range;

        } catch (Exception e) {
            System.err.println("Error loading range: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the filename for an RFI range at a given position.
     * Convention: position_rfi.json (e.g., "utg_rfi.json")
     */
    public static String getRfiFilename(Position position) {
        return position.name().toLowerCase() + "_rfi.json";
    }

    /**
     * Get the filename for a "vs raise" range.
     * Convention: position_vs_raiserPosition.json (e.g., "co_vs_utg.json")
     */
    public static String getVsRaiseFilename(Position yourPosition, Position raiserPosition) {
        return yourPosition.name().toLowerCase() + "_vs_" +
               raiserPosition.name().toLowerCase() + ".json";
    }

    /**
     * Load an RFI range for a position.
     */
    public static Range loadRfiRange(Position position) {
        return loadRange(getRfiFilename(position));
    }

    /**
     * Load a "vs raise" range.
     */
    public static Range loadVsRaiseRange(Position yourPosition, Position raiserPosition) {
        return loadRange(getVsRaiseFilename(yourPosition, raiserPosition));
    }

    /**
     * Clear the cache (useful if you want to reload ranges).
     */
    public static void clearCache() {
        rangeCache.clear();
    }

    /**
     * Create a default/empty range if no file exists.
     * Useful for positions or situations we haven't created ranges for yet.
     */
    public static Range createEmptyRange(Position position, String situation) {
        return new Range(position.getDisplayName() + " " + situation, position, situation);
    }
}
