package carservice;

/** A labor task inside a work order. Cost = hours * hourly rate. */
public class Task extends BaseEntity implements Billable {

    private String description;
    private double laborHours;
    private double hourlyRate;

    public Task(String id, String description, double laborHours, double hourlyRate)
            throws DomainValidationException {
        super(id);
        if (laborHours < 0 || hourlyRate < 0) {
            throw new DomainValidationException("Negative hours or rate in task " + id);
        }
        this.description = description;
        this.laborHours = laborHours;
        this.hourlyRate = hourlyRate;
    }

    // OVERLOADED constructor: a task without hours/rate (cost 0)
    public Task(String id, String description) throws DomainValidationException {
        this(id, description, 0.0, 0.0);
    }

    @Override
    public String businessKey() {
        return id;
    }

    // Implementation of the Billable interface
    public double calculateCost() {
        return laborHours * hourlyRate;
    }

    public String getDescription() { return description; }
    public double getLaborHours() { return laborHours; }
    public double getHourlyRate() { return hourlyRate; }

    @Override
    public String toString() {
        return "Task " + id + " " + description + ": " + laborHours + " h x " + hourlyRate;
    }
}
