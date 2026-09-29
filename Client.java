package carservice;

public class Client extends BaseEntity {

    private String name;
    private String phone;
    private String email;

    public Client(String id, String name, String phone, String email) throws DomainValidationException {
        super(id);
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public String businessKey() {
        return id;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return name + " (" + id + ", " + phone + ", " + email + ")";
    }
}
