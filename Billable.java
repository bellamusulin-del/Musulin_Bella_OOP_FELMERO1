package carservice;

/** Interface 2: everything that has a cost (task, part, work order). */
public interface Billable {
    double calculateCost();
}
