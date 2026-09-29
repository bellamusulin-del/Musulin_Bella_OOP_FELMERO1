package carservice;

import java.util.ArrayList;
import java.util.Locale;

/** All money calculations are in this class. */
public class CostCalculator {

    public static final double COURTESY_DAILY_FEE = 15.0;
    public static final int COURTESY_MAX_DAYS = 5;

    /**
     * INTERFACE USED IN LOGIC (1): works for tasks, parts and work orders,
     * because all of them implement Billable.
     */
    public static double sumCosts(ArrayList<? extends Billable> items) {
        double total = 0.0;
        for (Billable item : items) {
            total = total + item.calculateCost();
        }
        return roundToTwoDecimals(total);
    }

    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static String formatMoney(double value) {
        // Locale.US so we always get "12.50" and never "12,50"
        return String.format(Locale.US, "%.2f", value);
    }

    /** Courtesy car cost for one intake (uses the meta data of the intake). */
    public double calculateCourtesyCost(ServiceIntake intake) {
        return calculateCourtesyCost(intake.isCourtesyCar(), intake.getCourtesyDays());
    }

    /** OVERLOADED method: same calculation with plain values. Max 5 days. */
    public double calculateCourtesyCost(boolean courtesyCar, int days) {
        if (!courtesyCar) {
            return 0.0;
        }
        int daysToCharge = days;
        if (daysToCharge > COURTESY_MAX_DAYS) {
            daysToCharge = COURTESY_MAX_DAYS;
        }
        if (daysToCharge < 0) {
            daysToCharge = 0;
        }
        return daysToCharge * COURTESY_DAILY_FEE;
    }

    public double calculateTotalLabor(ServiceIntake intake) {
        double totalLabor = 0.0;
        for (WorkOrder workOrder : intake.getWorkOrders()) {
            totalLabor = totalLabor + workOrder.calculateLaborCost();
        }
        return roundToTwoDecimals(totalLabor);
    }

    public double calculateTotalParts(ServiceIntake intake) {
        double totalParts = 0.0;
        for (WorkOrder workOrder : intake.getWorkOrders()) {
            totalParts = totalParts + workOrder.calculatePartsCost();
        }
        return roundToTwoDecimals(totalParts);
    }
}
