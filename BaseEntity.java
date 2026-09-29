package carservice;

/**
 * Abstract base class of the whole model.
 * Every entity has an id and a business key.
 */
public abstract class BaseEntity implements Identifiable {

    // STATIC COUNTER: counts how many valid entities were created in total.
    private static int entityCounter = 0;

    protected String id;

    public BaseEntity(String id) throws DomainValidationException {
        if (id == null || id.trim().length() == 0) {
            throw new DomainValidationException("Entity id must not be empty");
        }
        this.id = id.trim();
        entityCounter++;
    }

    public String getId() {
        return id;
    }

    public static int getEntityCounter() {
        return entityCounter;
    }

    /** Each subclass says what makes it unique (VIN, intake id, sku, ...). */
    public abstract String businessKey();

    /**
     * equals() is based ONLY on the business key (and the class).
     * Two objects of the same class with the same business key are equal,
     * even if other fields are different.
     */
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null) {
            return false;
        }
        if (getClass() != otherObject.getClass()) {
            return false;
        }
        BaseEntity otherEntity = (BaseEntity) otherObject;
        return businessKey().equals(otherEntity.businessKey());
    }

    /**
     * hashCode() uses the same business key as equals(),
     * so equal objects always have the same hash code.
     */
    @Override
    public int hashCode() {
        return businessKey().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + businessKey() + "]";
    }
}
