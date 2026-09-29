package carservice;

import java.util.ArrayList;

/** One issue reported by the client. */
public class Issue extends BaseEntity {

    private Severity severity;
    private ArrayList<String> symptoms;
    private int sinceKm;

    // Full constructor
    public Issue(String code, Severity severity, ArrayList<String> symptoms, int sinceKm)
            throws DomainValidationException {
        super(code);
        this.severity = severity;
        this.symptoms = symptoms;
        this.sinceKm = sinceKm;
    }

    // OVERLOADED constructor: only code and severity, the rest gets defaults
    public Issue(String code, Severity severity) throws DomainValidationException {
        this(code, severity, new ArrayList<String>(), 0);
    }

    @Override
    public String businessKey() {
        return id; // the issue code
    }

    public String getCode() { return id; }
    public Severity getSeverity() { return severity; }
    public ArrayList<String> getSymptoms() { return symptoms; }
    public int getSinceKm() { return sinceKm; }

    /**
     * ASSUMPTION: the JSON does not link issues to work orders, so we link
     * them by the prefix of the issue code:
     * ENG / SUS / BRAKE / AC -> MECHANICAL, INF / ELE / SENSOR -> ELECTRICAL.
     * Any other prefix -> UNKNOWN (the wizard then includes all work orders).
     */
    public String getRelatedCategory() {
        if (id.startsWith("ENG") || id.startsWith("SUS") || id.startsWith("BRAKE") || id.startsWith("AC")) {
            return "MECHANICAL";
        }
        if (id.startsWith("INF") || id.startsWith("ELE") || id.startsWith("SENSOR")) {
            return "ELECTRICAL";
        }
        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + id + " " + symptoms + " (since " + sinceKm + " km)";
    }
}