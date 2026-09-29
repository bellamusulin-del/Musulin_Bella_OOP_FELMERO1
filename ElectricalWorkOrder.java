package carservice;

public class ElectricalWorkOrder extends WorkOrder {

    public ElectricalWorkOrder(String id) throws DomainValidationException {
        super(id);
    }

    @Override
    public String getCategoryName() {
        return "ELECTRICAL";
    }
}