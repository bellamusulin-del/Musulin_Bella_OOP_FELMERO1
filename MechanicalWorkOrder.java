package carservice;

public class MechanicalWorkOrder extends WorkOrder {

    public MechanicalWorkOrder(String id) throws DomainValidationException {
        super(id);
    }

    @Override
    public String getCategoryName() {
        return "MECHANICAL";
    }
}

