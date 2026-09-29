package carservice;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        String jsonFilePath = "data.json";
        if (args.length > 0) {
            jsonFilePath = args[0];
        }

        // ---- 1) parse ----
        IntakeParser parser = new IntakeParser();
        ArrayList<ServiceIntake> parsedIntakes;
        try {
            parsedIntakes = parser.parseFile(jsonFilePath);
        } catch (DomainValidationException e) {
            System.err.println("Invalid data: " + e.getMessage());
            return;
        }

        // ---- 2) console summary + report.txt ----
        ReportWriter reportWriter = new ReportWriter();
        System.out.println(reportWriter.buildSummary(parsedIntakes, parser.getSkippedIntakeMessages()));

        try {
            reportWriter.writeReportToFile(parsedIntakes, parser.getSkippedIntakeMessages(), "report.txt");
            System.out.println("report.txt written.");
        } catch (IOException e) {
            System.err.println("Could not write report.txt: " + e.getMessage());
        }

        // ---- 3) GUI wizard ----
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("No display available, GUI skipped.");
            return;
        }
        final ArrayList<ServiceIntake> intakesForGui = parsedIntakes;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                QuoteWizard wizard = new QuoteWizard(intakesForGui);
                wizard.setVisible(true);
            }
        });
    }
}
