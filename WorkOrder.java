package carservice;

import java.util.ArrayList;

/**
 * Abstract work order. The concrete types (Mechanical, Electrical)
 * come from the "type" field of the JSON.
 */
public abstract class WorkOrder extends BaseEntity implements Billable {

    protected ArrayList<Task> tasks;
    protected ArrayList<Part> parts;

    public WorkOrder(String id) throws DomainValidationException {
        super(id);
        this.tasks = new ArrayList<Task>();
        this.parts = new ArrayList<Part>();
    }

    /** Each subclass returns its own category name (polymorphism). */
    public abstract String getCategoryName();

    @Override
    public String businessKey() {
        return id;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void addPart(Part part) {
        parts.add(part);
    }

    public ArrayList<Task> getTasks() { return tasks; }
    public ArrayList<Part> getParts() { return parts; }

    public double calculateLaborCost() {
        return CostCalculator.sumCosts(tasks);
    }

    public double calculatePartsCost() {
        return CostCalculator.sumCosts(parts);
    }

    // Implementation of Billable: labor + parts
    public double calculateCost() {
        return CostCalculator.roundToTwoDecimals(calculateLaborCost() + calculatePartsCost());
    }

    @Override
    public String toString() {
        return getCategoryName() + " work order " + id + " (" + tasks.size() + " tasks, "
                + parts.size() + " parts)";
    }
}
