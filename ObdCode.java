package carservice;

/** A simple data class for one OBD trouble code. */
public class ObdCode {

    private String dtc;
    private String status;

    public ObdCode(String dtc, String status) {
        this.dtc = dtc;
        this.status = status;
    }

    public String getDtc() { return dtc; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return dtc + " (" + status + ")";
    }
}
