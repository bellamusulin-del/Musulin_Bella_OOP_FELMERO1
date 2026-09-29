package carservice;

/** A part used in a work order. The id is the SKU. Cost = qty * unit price. */
public class Part extends BaseEntity implements Billable {

    private String name;
    private int quantity;
    private double unitPrice;

    public Part(String sku, String name, int quantity, double unitPrice) throws DomainValidationException {
        super(sku);
        if (quantity < 0 || unitPrice < 0) {
            throw new DomainValidationException("Negative quantity or price for part " + sku);
        }
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // OVERLOADED constructor: quantity is 1 by default
    public Part(String sku, String name, double unitPrice) throws DomainValidationException {
        this(sku, name, 1, unitPrice);
    }

    @Override
    public String businessKey() {
        return id; // SKU
    }

    public double calculateCost() {
        return quantity * unitPrice;
    }

    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }

    @Override
    public String toString() {
        return "Part " + id + " " + name + ": " + quantity + " x " + unitPrice;
    }
}

