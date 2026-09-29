package carservice;

import java.util.ArrayList;
import java.util.Collections;

/** One car service intake (one entry of the "intakes" array). */
public class ServiceIntake extends BaseEntity {

    private String receivedAt; // kept as text (no java.time in Java 6)
    private Client client;
    private Car car;
    private ArrayList<Issue> issues = new ArrayList<Issue>();
    private ArrayList<ObdCode> obdCodes = new ArrayList<ObdCode>();
    private ArrayList<DiagnosticTest> tests = new ArrayList<DiagnosticTest>();
    private ArrayList<WorkOrder> workOrders = new ArrayList<WorkOrder>();
    private String currency = "EUR";
    private boolean paid = false;
    private String priority = "NORMAL";
    private boolean courtesyCar = false;
    private int courtesyDays = CostCalculator.COURTESY_MAX_DAYS;

    public ServiceIntake(String intakeId) throws DomainValidationException {
        super(intakeId);
    }

    @Override
    public String businessKey() {
        return id; // intakeId
    }

    // ---------- business logic ----------

    public boolean hasObdCode(String dtcToFind) {
        for (ObdCode obdCode : obdCodes) {
            if (obdCode.getDtc().equalsIgnoreCase(dtcToFind)) {
                return true; // any status (PENDING, HISTORY, ...) counts
            }
        }
        return false;
    }

    /** Oil pressure is "not OK" only if a test named "Oil pressure" failed. */
    public boolean isOilPressureNotOk() {
        for (DiagnosticTest test : tests) {
            if (test.getName().equalsIgnoreCase("Oil pressure") && !test.isOk()) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getDoNotReleaseReasons() {
        ArrayList<String> reasons = new ArrayList<String>();
        if (hasObdCode("P0420")) {
            reasons.add("OBD code P0420 present");
        }
        if (isOilPressureNotOk()) {
            reasons.add("Oil pressure test not OK");
        }
        return reasons;
    }

    public boolean isDoNotRelease() {
        return getDoNotReleaseReasons().size() > 0;
    }

    /** Returns a sorted COPY of the issues (HIGH first). */
    public ArrayList<Issue> getIssuesSortedBySeverity() {
        ArrayList<Issue> sortedIssues = new ArrayList<Issue>(issues);
        Collections.sort(sortedIssues, new SeverityComparator());
        return sortedIssues;
    }

    /** Subtotal of all work orders (labor + parts). */
    public double calculateSubtotal() {
        return CostCalculator.sumCosts(workOrders);
    }

    // ---------- getters and setters ----------

    public String getReceivedAt() { return receivedAt; }
    public void setReceivedAt(String receivedAt) { this.receivedAt = receivedAt; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }
    public ArrayList<Issue> getIssues() { return issues; }
    public ArrayList<ObdCode> getObdCodes() { return obdCodes; }
    public ArrayList<DiagnosticTest> getTests() { return tests; }
    public ArrayList<WorkOrder> getWorkOrders() { return workOrders; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCourtesyCar() { return courtesyCar; }
    public void setCourtesyCar(boolean courtesyCar) { this.courtesyCar = courtesyCar; }
    public int getCourtesyDays() { return courtesyDays; }
    public void setCourtesyDays(int courtesyDays) { this.courtesyDays = courtesyDays; }

    @Override
    public String toString() {
        String clientName = (client == null) ? "?" : client.getName();
        String carText = (car == null) ? "?" : car.getMake() + " " + car.getModel();
        return id + " - " + carText + " - " + clientName;
    }
}
