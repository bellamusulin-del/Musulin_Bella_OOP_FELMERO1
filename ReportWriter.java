package carservice;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/** Builds the console summary and the full report.txt text. */
public class ReportWriter {

    private CostCalculator calculator = new CostCalculator();

    // ---------------- short summary for the console ----------------

    public String buildSummary(ArrayList<ServiceIntake> intakes, ArrayList<String> skippedMessages) {
        StringBuilder text = new StringBuilder();
        text.append("=== CAR SERVICE SUMMARY ===\n");
        for (ServiceIntake intake : intakes) {
            double subtotal = intake.calculateSubtotal();
            double courtesyCost = calculator.calculateCourtesyCost(intake);

            text.append("Intake ").append(intake.getId()).append(" - ").append(intake.getCar().getMake())
                    .append(" ").append(intake.getCar().getModel()).append("\n");
            text.append("  Subtotal: ").append(CostCalculator.formatMoney(subtotal)).append(" ")
                    .append(intake.getCurrency()).append("\n");
            text.append("  Courtesy car: ").append(CostCalculator.formatMoney(courtesyCost)).append("\n");
            text.append("  Total: ").append(CostCalculator.formatMoney(subtotal + courtesyCost)).append("\n");
            if (intake.isDoNotRelease()) {
                text.append("  STATUS: DO NOT RELEASE\n");
            } else {
                text.append("  STATUS: OK to release\n");
            }
            text.append("  Top issue: ");
            ArrayList<Issue> sortedIssues = intake.getIssuesSortedBySeverity();
            if (sortedIssues.size() > 0) {
                text.append(sortedIssues.get(0).toString());
            } else {
                text.append("none");
            }
            text.append("\n");
        }
        appendSkippedIntakes(text, skippedMessages);
        return text.toString();
    }

    private void appendSkippedIntakes(StringBuilder text, ArrayList<String> skippedMessages) {
        if (skippedMessages.size() == 0) {
            return;
        }
        text.append("Invalid intakes (not processed):\n");
        for (String message : skippedMessages) {
            text.append("  - ").append(message).append("\n");
        }
    }

    // ---------------- full report ----------------

    public String buildFullReport(ArrayList<ServiceIntake> intakes, ArrayList<String> skippedMessages) {
        StringBuilder text = new StringBuilder();
        text.append("CAR SERVICE INTAKE REPORT\n");
        text.append("=========================\n\n");

        for (ServiceIntake intake : intakes) {
            appendIntake(text, intake);
        }
        appendSkippedIntakes(text, skippedMessages);
        text.append("\nEntities created while parsing: ").append(BaseEntity.getEntityCounter()).append("\n");
        return text.toString();
    }

    private void appendIntake(StringBuilder text, ServiceIntake intake) {
        text.append("Intake: ").append(intake.getId()).append("  (received ")
                .append(intake.getReceivedAt()).append(")\n");
        text.append("Client: ").append(intake.getClient()).append("\n");
        text.append("Car: ").append(intake.getCar()).append("\n");
        text.append("Priority: ").append(intake.getPriority()).append("\n\n");

        // --- issues sorted by severity ---
        text.append("Issues by severity:\n");
        ArrayList<Issue> sortedIssues = intake.getIssuesSortedBySeverity();
        for (int i = 0; i < sortedIssues.size(); i++) {
            text.append("  ").append(i + 1).append(". ").append(sortedIssues.get(i)).append("\n");
        }
        text.append("\n");

        // --- diagnostics ---
        text.append("OBD codes:\n");
        for (ObdCode obdCode : intake.getObdCodes()) {
            text.append("  ").append(obdCode).append("\n");
        }
        text.append("Tests:\n");
        for (DiagnosticTest test : intake.getTests()) {
            text.append("  ").append(test).append("\n");
        }
        text.append("\n");

        // --- work orders ---
        for (WorkOrder workOrder : intake.getWorkOrders()) {
            appendWorkOrder(text, workOrder);
        }
        text.append("Work order ids: ").append(describeIds(intake.getWorkOrders())).append("\n\n");

        // --- totals ---
        double totalLabor = calculator.calculateTotalLabor(intake);
        double totalParts = calculator.calculateTotalParts(intake);
        double subtotal = intake.calculateSubtotal();
        double courtesyCost = calculator.calculateCourtesyCost(intake);
        double grandTotal = CostCalculator.roundToTwoDecimals(subtotal + courtesyCost);
        String currency = intake.getCurrency();

        text.append("Totals:\n");
        text.append("  Labor total:  ").append(CostCalculator.formatMoney(totalLabor)).append(" ").append(currency).append("\n");
        text.append("  Parts total:  ").append(CostCalculator.formatMoney(totalParts)).append(" ").append(currency).append("\n");
        text.append("  Subtotal:     ").append(CostCalculator.formatMoney(subtotal)).append(" ").append(currency).append("\n");
        if (intake.isCourtesyCar()) {
            int chargedDays = Math.min(intake.getCourtesyDays(), CostCalculator.COURTESY_MAX_DAYS);
            text.append("  Courtesy car: ").append(chargedDays).append(" days x ")
                    .append(CostCalculator.formatMoney(CostCalculator.COURTESY_DAILY_FEE)).append(" = ")
                    .append(CostCalculator.formatMoney(courtesyCost)).append(" ").append(currency).append("\n");
        } else {
            text.append("  Courtesy car: not requested\n");
        }
        text.append("  GRAND TOTAL:  ").append(CostCalculator.formatMoney(grandTotal)).append(" ").append(currency).append("\n\n");

        // --- release status ---
        if (intake.isDoNotRelease()) {
            text.append("RELEASE STATUS: DO NOT RELEASE\n");
            for (String reason : intake.getDoNotReleaseReasons()) {
                text.append("  - ").append(reason).append("\n");
            }
        } else {
            text.append("RELEASE STATUS: OK to release\n");
        }
        text.append("Invoice paid: ").append(intake.isPaid() ? "yes" : "no").append("\n");
        text.append("\n-------------------------------------------\n\n");
    }

    private void appendWorkOrder(StringBuilder text, WorkOrder workOrder) {
        text.append(workOrder.getCategoryName()).append(" work order ").append(workOrder.getId()).append("\n");
        for (Task task : workOrder.getTasks()) {
            text.append("  Task ").append(task.getId()).append(" ").append(task.getDescription()).append(": ")
                    .append(task.getLaborHours()).append(" h x ")
                    .append(CostCalculator.formatMoney(task.getHourlyRate())).append(" = ")
                    .append(CostCalculator.formatMoney(task.calculateCost())).append("\n");
        }
        for (Part part : workOrder.getParts()) {
            text.append("  Part ").append(part.getId()).append(" ").append(part.getName()).append(": ")
                    .append(part.getQuantity()).append(" x ")
                    .append(CostCalculator.formatMoney(part.getUnitPrice())).append(" = ")
                    .append(CostCalculator.formatMoney(part.calculateCost())).append("\n");
        }
        text.append("  Labor: ").append(CostCalculator.formatMoney(workOrder.calculateLaborCost()))
                .append("  Parts: ").append(CostCalculator.formatMoney(workOrder.calculatePartsCost()))
                .append("  Work order total: ").append(CostCalculator.formatMoney(workOrder.calculateCost()))
                .append("\n\n");
    }

    /** INTERFACE USED IN LOGIC (2): works with any list of Identifiable objects. */
    private String describeIds(ArrayList<? extends Identifiable> items) {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                ids.append(", ");
            }
            ids.append(items.get(i).getId());
        }
        return ids.toString();
    }

    // ---------------- write to file ----------------

    public void writeReportToFile(ArrayList<ServiceIntake> intakes, ArrayList<String> skippedMessages,
            String filePath) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(filePath));
            writer.print(buildFullReport(intakes, skippedMessages));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
