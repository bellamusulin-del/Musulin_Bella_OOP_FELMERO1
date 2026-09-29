package carservice;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Wizard with 3 steps (Back / Next):
 * 1) choose the intake, 2) select the issues, 3) estimated quote table.
 * (Java 6: JComboBox is not generic, so we cast the selected item.)
 */
public class QuoteWizard extends JFrame {

    private static final long serialVersionUID = 1L;

    private ArrayList<ServiceIntake> intakes;
    private ServiceIntake selectedIntake;
    private CostCalculator calculator = new CostCalculator();

    private int currentStep = 1;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);
    private JButton backButton = new JButton("Back");
    private JButton nextButton = new JButton("Next");

    // step 1
    private JComboBox intakeComboBox;
    private JTextArea intakeInfoArea = new JTextArea(6, 40);

    // step 2
    private JPanel issuesPanel = new JPanel();
    private ArrayList<JCheckBox> issueCheckBoxes = new ArrayList<JCheckBox>();
    private ArrayList<Issue> issuesShownInStepTwo = new ArrayList<Issue>();

    // step 3
    private DefaultTableModel quoteTableModel;
    private JLabel totalsLabel = new JLabel(" ");
    private JLabel releaseLabel = new JLabel(" ");

    public QuoteWizard(ArrayList<ServiceIntake> intakes) {
        super("Car service - estimated quote");
        this.intakes = intakes;

        cardPanel.add(buildStepOnePanel(), "1");
        cardPanel.add(buildStepTwoPanel(), "2");
        cardPanel.add(buildStepThreePanel(), "3");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);

        backButton.addActionListener(new BackClickListener());
        nextButton.addActionListener(new NextClickListener());

        setLayout(new BorderLayout());
        add(cardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        showCurrentStep();
        setSize(760, 420);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // ---------------- building the 3 panels ----------------

    private JPanel buildStepOnePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Step 1: choose the service intake"), BorderLayout.NORTH);

        intakeComboBox = new JComboBox(intakes.toArray());
        intakeComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                showIntakeInfo();
            }
        });

        intakeInfoArea.setEditable(false);
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(intakeComboBox, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(intakeInfoArea), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        showIntakeInfo();
        return panel;
    }

    private JPanel buildStepTwoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Step 2: select the issues you want in the quote"), BorderLayout.NORTH);
        issuesPanel.setLayout(new BoxLayout(issuesPanel, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(issuesPanel), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStepThreePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Step 3: estimated quote"), BorderLayout.NORTH);

        String[] columnNames = { "Work order", "Line", "Kind", "Qty / hours", "Unit price", "Cost" };
        // cells are not editable
        quoteTableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable quoteTable = new JTable(quoteTableModel);
        panel.add(new JScrollPane(quoteTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.add(totalsLabel);
        bottomPanel.add(releaseLabel);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ---------------- step logic ----------------

    private void showIntakeInfo() {
        ServiceIntake chosenIntake = (ServiceIntake) intakeComboBox.getSelectedItem();
        if (chosenIntake == null) {
            return;
        }
        StringBuilder info = new StringBuilder();
        info.append("Client: ").append(chosenIntake.getClient()).append("\n");
        info.append("Car: ").append(chosenIntake.getCar()).append("\n");
        info.append("Reported issues: ").append(chosenIntake.getIssues().size()).append("\n");
        info.append("Courtesy car: ").append(chosenIntake.isCourtesyCar() ? "yes" : "no").append("\n");
        intakeInfoArea.setText(info.toString());
    }

    private void fillIssueCheckBoxes() {
        issuesPanel.removeAll();
        issueCheckBoxes.clear();
        issuesShownInStepTwo = selectedIntake.getIssuesSortedBySeverity(); // HIGH first

        for (Issue issue : issuesShownInStepTwo) {
            JCheckBox checkBox = new JCheckBox(issue.toString(), true); // selected by default
            issueCheckBoxes.add(checkBox);
            issuesPanel.add(checkBox);
        }
        issuesPanel.revalidate();
        issuesPanel.repaint();
    }

    private void fillQuoteTable() {
        quoteTableModel.setRowCount(0);

        // 1) find the categories of the selected issues
        ArrayList<String> selectedCategories = new ArrayList<String>();
        for (int i = 0; i < issueCheckBoxes.size(); i++) {
            if (issueCheckBoxes.get(i).isSelected()) {
                String category = issuesShownInStepTwo.get(i).getRelatedCategory();
                if (category.equals("UNKNOWN")) {
                    // we cannot link this issue, so we include all work orders
                    for (WorkOrder workOrder : selectedIntake.getWorkOrders()) {
                        addCategoryIfMissing(selectedCategories, workOrder.getCategoryName());
                    }
                } else {
                    addCategoryIfMissing(selectedCategories, category);
                }
            }
        }

        // 2) add the lines of the work orders with a selected category
        ArrayList<WorkOrder> includedWorkOrders = new ArrayList<WorkOrder>();
        for (WorkOrder workOrder : selectedIntake.getWorkOrders()) {
            if (selectedCategories.contains(workOrder.getCategoryName())) {
                includedWorkOrders.add(workOrder);
                addWorkOrderRows(workOrder);
            }
        }

        // 3) totals
        double subtotal = CostCalculator.sumCosts(includedWorkOrders);
        double courtesyCost = calculator.calculateCourtesyCost(selectedIntake);
        if (courtesyCost > 0) {
            quoteTableModel.addRow(new Object[] { "-", "Courtesy car", "Fee",
                    Math.min(selectedIntake.getCourtesyDays(), CostCalculator.COURTESY_MAX_DAYS) + " days",
                    CostCalculator.formatMoney(CostCalculator.COURTESY_DAILY_FEE),
                    CostCalculator.formatMoney(courtesyCost) });
        }
        double grandTotal = CostCalculator.roundToTwoDecimals(subtotal + courtesyCost);
        String currency = selectedIntake.getCurrency();
        totalsLabel.setText("Subtotal: " + CostCalculator.formatMoney(subtotal) + " " + currency
                + "   |   Courtesy: " + CostCalculator.formatMoney(courtesyCost)
                + "   |   ESTIMATED TOTAL: " + CostCalculator.formatMoney(grandTotal) + " " + currency);

        if (selectedIntake.isDoNotRelease()) {
            releaseLabel.setText("DO NOT RELEASE: " + selectedIntake.getDoNotReleaseReasons());
            releaseLabel.setForeground(Color.RED);
        } else {
            releaseLabel.setText("Car can be released after the work");
            releaseLabel.setForeground(new Color(0, 120, 0));
        }
    }

    private void addCategoryIfMissing(ArrayList<String> categories, String category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    private void addWorkOrderRows(WorkOrder workOrder) {
        for (Task task : workOrder.getTasks()) {
            quoteTableModel.addRow(new Object[] { workOrder.getCategoryName(), task.getDescription(), "Labor",
                    task.getLaborHours() + " h", CostCalculator.formatMoney(task.getHourlyRate()),
                    CostCalculator.formatMoney(task.calculateCost()) });
        }
        for (Part part : workOrder.getParts()) {
            quoteTableModel.addRow(new Object[] { workOrder.getCategoryName(), part.getName(), "Part",
                    String.valueOf(part.getQuantity()), CostCalculator.formatMoney(part.getUnitPrice()),
                    CostCalculator.formatMoney(part.calculateCost()) });
        }
    }

    private boolean atLeastOneIssueSelected() {
        for (JCheckBox checkBox : issueCheckBoxes) {
            if (checkBox.isSelected()) {
                return true;
            }
        }
        return false;
    }

    private void showCurrentStep() {
        cardLayout.show(cardPanel, String.valueOf(currentStep));
        backButton.setEnabled(currentStep > 1);
        nextButton.setText(currentStep == 3 ? "Close" : "Next");
    }

    // ---------------- button listeners ----------------

    private class NextClickListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (currentStep == 1) {
                selectedIntake = (ServiceIntake) intakeComboBox.getSelectedItem();
                fillIssueCheckBoxes();
                currentStep = 2;
            } else if (currentStep == 2) {
                if (!atLeastOneIssueSelected()) {
                    JOptionPane.showMessageDialog(QuoteWizard.this, "Please select at least one issue.");
                    return;
                }
                fillQuoteTable();
                currentStep = 3;
            } else {
                dispose(); // step 3: Close
                return;
            }
            showCurrentStep();
        }
    }

    private class BackClickListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (currentStep > 1) {
                currentStep--;
                showCurrentStep();
            }
        }
    }
}
