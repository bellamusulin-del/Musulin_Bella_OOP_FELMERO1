package carservice;

/** Severity of a reported issue. A lower rank number means more important. */
public enum Severity {
    HIGH(1),
    MEDIUM(2),
    LOW(3);

    private final int rank;

    private Severity(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    /**
     * Converts text from the JSON to a Severity.
     * Missing text -> default LOW. Unknown text -> exception.
     */
    public static Severity fromText(String text) throws DomainValidationException {
        if (text == null || text.trim().length() == 0) {
            return LOW; // default value
        }
        String cleanedText = text.trim().toUpperCase();
        Severity[] allValues = Severity.values();
        for (int i = 0; i < allValues.length; i++) {
            if (allValues[i].name().equals(cleanedText)) {
                return allValues[i];
            }
        }
        throw new DomainValidationException("Unknown severity: " + text);
    }
}

