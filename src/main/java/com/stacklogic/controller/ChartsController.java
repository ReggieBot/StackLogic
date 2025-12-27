package com.stacklogic.controller;

import com.stacklogic.model.Session;
import com.stacklogic.util.SessionDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller for the Charts and Analytics view.
 *
 * This controller:
 * 1. Loads session data from the database
 * 2. Calculates summary statistics
 * 3. Builds and displays charts (profit over time, profit by stakes)
 * 4. Updates when filters change
 *
 * JAVAFX CHARTS EXPLAINED:
 * - LineChart: Shows data points connected by lines (good for trends over time)
 * - BarChart: Shows data as vertical bars (good for comparing categories)
 * - XYChart.Series: A named collection of data points
 * - XYChart.Data: A single data point with X and Y values
 */
public class ChartsController implements Initializable {

    // FXML injected components - filter controls
    @FXML private ComboBox<String> timeRangeCombo;
    @FXML private ComboBox<String> stakesFilterCombo;

    // Stats summary labels
    @FXML private Label totalSessionsLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label winRateLabel;
    @FXML private Label avgSessionLabel;
    @FXML private Label hoursPlayedLabel;
    @FXML private Label hourlyRateLabel;

    // Charts
    @FXML private LineChart<String, Number> profitChart;
    @FXML private CategoryAxis profitChartXAxis;
    @FXML private NumberAxis profitChartYAxis;

    @FXML private BarChart<String, Number> stakesProfitChart;
    @FXML private CategoryAxis stakesChartXAxis;
    @FXML private NumberAxis stakesChartYAxis;

    // Best/Worst session labels
    @FXML private Label bestSessionLabel;
    @FXML private Label bestSessionDateLabel;
    @FXML private Label worstSessionLabel;
    @FXML private Label worstSessionDateLabel;
    @FXML private Label winningSessionsLabel;
    @FXML private Label losingSessionsLabel;

    // Data access - SessionDAO uses static methods, no instance needed

    // Date formatter for chart axis
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    @Override
    public void initialize(URL url, ResourceBundle resources) {
        // Set up time range options
        timeRangeCombo.setItems(FXCollections.observableArrayList(
                "All Time",
                "Last 7 Days",
                "Last 30 Days",
                "Last 90 Days",
                "This Month",
                "This Year"
        ));
        timeRangeCombo.setValue("All Time");

        // Set up stakes filter options
        stakesFilterCombo.setItems(FXCollections.observableArrayList(
                "All Stakes",
                "2NL", "5NL", "10NL", "25NL", "50NL", "100NL"
        ));
        stakesFilterCombo.setValue("All Stakes");

        // Add listeners for filter changes
        timeRangeCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());
        stakesFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> refreshData());

        // Initial data load
        refreshData();
    }

    /**
     * Refresh all charts and statistics based on current filters.
     */
    private void refreshData() {
        List<Session> allSessions;
        try {
            allSessions = SessionDAO.getAll();
        } catch (Exception e) {
            // If database error, show empty state
            System.err.println("Error loading sessions: " + e.getMessage());
            allSessions = new ArrayList<>();
        }

        List<Session> filteredSessions = filterSessions(allSessions);

        updateStatsSummary(filteredSessions);
        updateProfitChart(filteredSessions);
        updateStakesProfitChart(filteredSessions);
        updateNotableSessions(filteredSessions);
    }

    /**
     * Filter sessions based on time range and stakes selections.
     */
    private List<Session> filterSessions(List<Session> sessions) {
        List<Session> filtered = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate cutoffDate = null;

        // Determine date cutoff based on time range
        String timeRange = timeRangeCombo.getValue();
        if (timeRange != null) {
            switch (timeRange) {
                case "Last 7 Days" -> cutoffDate = now.minusDays(7);
                case "Last 30 Days" -> cutoffDate = now.minusDays(30);
                case "Last 90 Days" -> cutoffDate = now.minusDays(90);
                case "This Month" -> cutoffDate = now.withDayOfMonth(1);
                case "This Year" -> cutoffDate = now.withDayOfYear(1);
                // "All Time" means no cutoff
            }
        }

        String stakesFilter = stakesFilterCombo.getValue();

        for (Session session : sessions) {
            // Check date filter
            if (cutoffDate != null && session.getDate().isBefore(cutoffDate)) {
                continue;
            }

            // Check stakes filter
            if (stakesFilter != null && !stakesFilter.equals("All Stakes")) {
                if (!stakesFilter.equals(session.getStakes())) {
                    continue;
                }
            }

            filtered.add(session);
        }

        return filtered;
    }

    /**
     * Update the summary statistics row.
     */
    private void updateStatsSummary(List<Session> sessions) {
        if (sessions.isEmpty()) {
            totalSessionsLabel.setText("0");
            totalProfitLabel.setText("$0.00");
            winRateLabel.setText("0%");
            avgSessionLabel.setText("$0.00");
            hoursPlayedLabel.setText("0h");
            hourlyRateLabel.setText("$0.00/hr");
            return;
        }

        int totalSessions = sessions.size();
        double totalProfit = 0;
        int totalMinutes = 0;
        int winningSessions = 0;

        for (Session session : sessions) {
            totalProfit += session.getProfit();
            totalMinutes += session.getDurationMinutes();
            if (session.getProfit() > 0) {
                winningSessions++;
            }
        }

        double winRate = (double) winningSessions / totalSessions * 100;
        double avgSession = totalProfit / totalSessions;
        double hours = totalMinutes / 60.0;
        double hourlyRate = hours > 0 ? totalProfit / hours : 0;

        totalSessionsLabel.setText(String.valueOf(totalSessions));
        totalProfitLabel.setText(formatMoney(totalProfit));
        totalProfitLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
        totalProfitLabel.getStyleClass().add(totalProfit >= 0 ? "profit-positive" : "profit-negative");

        winRateLabel.setText(String.format("%.0f%%", winRate));
        avgSessionLabel.setText(formatMoney(avgSession));
        avgSessionLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
        avgSessionLabel.getStyleClass().add(avgSession >= 0 ? "profit-positive" : "profit-negative");

        hoursPlayedLabel.setText(String.format("%.1fh", hours));
        hourlyRateLabel.setText(String.format("%s/hr", formatMoney(hourlyRate)));
        hourlyRateLabel.getStyleClass().removeAll("profit-positive", "profit-negative");
        hourlyRateLabel.getStyleClass().add(hourlyRate >= 0 ? "profit-positive" : "profit-negative");
    }

    /**
     * Update the profit over time line chart.
     * Shows cumulative profit progression.
     */
    private void updateProfitChart(List<Session> sessions) {
        profitChart.getData().clear();

        if (sessions.isEmpty()) {
            return;
        }

        // Sort sessions by date
        sessions.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        // Create series for cumulative profit
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Profit");

        double cumulativeProfit = 0;
        for (Session session : sessions) {
            cumulativeProfit += session.getProfit();
            String dateStr = session.getDate().format(DATE_FORMAT);
            series.getData().add(new XYChart.Data<>(dateStr, cumulativeProfit));
        }

        profitChart.getData().add(series);

        // Style the line based on final profit
        if (cumulativeProfit >= 0) {
            series.getNode().setStyle("-fx-stroke: #22c55e;");
        } else {
            series.getNode().setStyle("-fx-stroke: #ef4444;");
        }
    }

    /**
     * Update the profit by stakes bar chart.
     */
    private void updateStakesProfitChart(List<Session> sessions) {
        stakesProfitChart.getData().clear();

        if (sessions.isEmpty()) {
            return;
        }

        // Group profit by stakes
        Map<String, Double> profitByStakes = new HashMap<>();
        for (Session session : sessions) {
            String stakes = session.getStakes();
            profitByStakes.merge(stakes, session.getProfit(), Double::sum);
        }

        // Create series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Profit");

        // Add data in a logical order (by stakes level)
        String[] stakesOrder = {"2NL", "5NL", "10NL", "25NL", "50NL", "100NL"};
        for (String stakes : stakesOrder) {
            if (profitByStakes.containsKey(stakes)) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(stakes, profitByStakes.get(stakes));
                series.getData().add(data);
            }
        }

        // Add any other stakes not in our predefined order
        for (String stakes : profitByStakes.keySet()) {
            boolean found = false;
            for (String s : stakesOrder) {
                if (s.equals(stakes)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                series.getData().add(new XYChart.Data<>(stakes, profitByStakes.get(stakes)));
            }
        }

        stakesProfitChart.getData().add(series);

        // Color bars based on profit/loss
        for (XYChart.Data<String, Number> data : series.getData()) {
            // Need to wait for node to be created
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    double value = data.getYValue().doubleValue();
                    if (value >= 0) {
                        newNode.setStyle("-fx-bar-fill: #22c55e;");
                    } else {
                        newNode.setStyle("-fx-bar-fill: #ef4444;");
                    }
                }
            });
        }
    }

    /**
     * Update the notable sessions section (best/worst, win/loss counts).
     */
    private void updateNotableSessions(List<Session> sessions) {
        if (sessions.isEmpty()) {
            bestSessionLabel.setText("--");
            bestSessionDateLabel.setText("");
            worstSessionLabel.setText("--");
            worstSessionDateLabel.setText("");
            winningSessionsLabel.setText("0");
            losingSessionsLabel.setText("0");
            return;
        }

        Session bestSession = sessions.get(0);
        Session worstSession = sessions.get(0);
        int winningCount = 0;
        int losingCount = 0;

        for (Session session : sessions) {
            if (session.getProfit() > bestSession.getProfit()) {
                bestSession = session;
            }
            if (session.getProfit() < worstSession.getProfit()) {
                worstSession = session;
            }
            if (session.getProfit() > 0) {
                winningCount++;
            } else if (session.getProfit() < 0) {
                losingCount++;
            }
        }

        bestSessionLabel.setText(formatMoney(bestSession.getProfit()));
        bestSessionDateLabel.setText(bestSession.getDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) +
                " (" + bestSession.getStakes() + ")");

        worstSessionLabel.setText(formatMoney(worstSession.getProfit()));
        worstSessionDateLabel.setText(worstSession.getDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) +
                " (" + worstSession.getStakes() + ")");

        winningSessionsLabel.setText(String.valueOf(winningCount));
        losingSessionsLabel.setText(String.valueOf(losingCount));
    }

    /**
     * Format a money value as a string.
     */
    private String formatMoney(double amount) {
        if (amount >= 0) {
            return String.format("$%.2f", amount);
        } else {
            return String.format("-$%.2f", Math.abs(amount));
        }
    }
}
