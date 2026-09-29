package carservice;

public class Car extends BaseEntity {

    private String make;
    private String model;
    private int year;
    private int odometerKm;
    private boolean warrantyActive;

    // The id of a car is its VIN.
    public Car(String vin, String make, String model, int year, int odometerKm, boolean warrantyActive)
            throws DomainValidationException {
        super(vin);
        if (odometerKm < 0) {
            throw new DomainValidationException("Odometer cannot be negative for car " + vin);
        }
        this.make = make;
        this.model = model;
        this.year = year;
        this.odometerKm = odometerKm;
        this.warrantyActive = warrantyActive;
    }

    @Override
    public String businessKey() {
        return id; // VIN
    }

    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public int getOdometerKm() { return odometerKm; }
    public boolean isWarrantyActive() { return warrantyActive; }

    @Override
    public String toString() {
        return make + " " + model + " " + year + " (VIN " + id + ", " + odometerKm + " km, warranty: "
                + (warrantyActive ? "yes" : "no") + ")";
    }
}