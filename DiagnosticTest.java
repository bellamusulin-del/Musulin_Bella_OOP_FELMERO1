package carservice;

/** A simple data class for one diagnostic test result. */
public class DiagnosticTest {

    private String name;
    private boolean ok;

    public DiagnosticTest(String name, boolean ok) {
        this.name = name;
        this.ok = ok;
    }

    public String getName() { return name; }
    public boolean isOk() { return ok; }

    @Override
    public String toString() {
        return name + ": " + (ok ? "OK" : "NOT OK");
    }
}