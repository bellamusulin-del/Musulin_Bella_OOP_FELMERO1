package carservice;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Reads data.json with FileReader and builds the objects step by step.
 * Missing optional fields get default values, missing required fields
 * throw a DomainValidationException.
 */
public class IntakeParser {

    // Messages about the intakes that were invalid and therefore skipped
    private ArrayList<String> skippedIntakeMessages = new ArrayList<String>();

    public ArrayList<String> getSkippedIntakeMessages() {
        return skippedIntakeMessages;
    }

    // ---------------- STEP 1: read the file ----------------

    public ArrayList<ServiceIntake> parseFile(String filePath) throws DomainValidationException {
        FileReader fileReader = null;
        JsonElement rootElement = null;

        try {
            fileReader = new FileReader(filePath);
            rootElement = new JsonParser().parse(fileReader);
        } catch (FileNotFoundException e) {
            throw new DomainValidationException("File not found: " + filePath, e);
        } catch (JsonParseException e) {
            throw new DomainValidationException("The JSON is not valid: " + e.getMessage(), e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    System.err.println("Could not close the file: " + e.getMessage());
                }
            }
        }

        if (rootElement == null || !rootElement.isJsonObject()) {
            throw new DomainValidationException("The JSON file must contain an object at top level");
        }

        // ---------------- STEP 2: read all intakes ----------------

        JsonArray intakesArray = getArray(rootElement.getAsJsonObject(), "intakes");
        if (intakesArray.size() == 0) {
            throw new DomainValidationException("No intakes found in the JSON file");
        }

        ArrayList<ServiceIntake> allIntakes = new ArrayList<ServiceIntake>();
        skippedIntakeMessages.clear();

        for (int i = 0; i < intakesArray.size(); i++) {
            JsonElement intakeElement = intakesArray.get(i);
            try {
                if (!intakeElement.isJsonObject()) {
                    throw new DomainValidationException("it is not an object");
                }
                ServiceIntake intake = parseIntake(intakeElement.getAsJsonObject());

                // contains() uses equals(), which uses the business key
                if (allIntakes.contains(intake)) {
                    throw new DomainValidationException("duplicate intakeId " + intake.getId());
                }
                allIntakes.add(intake);
            } catch (DomainValidationException e) {
                // One bad intake must not stop the others: we remember the error and continue.
                skippedIntakeMessages.add("Intake number " + (i + 1) + " skipped: " + e.getMessage());
            }
        }

        if (allIntakes.size() == 0) {
            throw new DomainValidationException("No valid intakes found in the JSON file");
        }
        return allIntakes;
    }

    // ---------------- STEP 3: one intake ----------------

    private ServiceIntake parseIntake(JsonObject intakeObject) throws DomainValidationException {
        String intakeId = requireString(intakeObject, "intakeId");
        ServiceIntake intake = new ServiceIntake(intakeId);

        intake.setReceivedAt(readString(intakeObject, "receivedAt", "unknown"));
        intake.setClient(parseClient(getObject(intakeObject, "client")));
        intake.setCar(parseCar(getObject(intakeObject, "car")));

        parseIssues(intakeObject, intake);
        parseDiagnostics(intakeObject, intake);
        parseWorkOrders(intakeObject, intake);

        // invoice (optional)
        JsonObject invoiceObject = getObject(intakeObject, "invoice");
        intake.setCurrency(readString(invoiceObject, "currency", "EUR"));
        intake.setPaid(readBoolean(invoiceObject, "paid", false));

        // meta (optional)
        JsonObject metaObject = getObject(intakeObject, "meta");
        intake.setPriority(readString(metaObject, "priority", "NORMAL"));
        intake.setCourtesyCar(readBoolean(metaObject, "courtesyCar", false));
        // The JSON has no number of days, so we assume the maximum (5) unless "courtesyDays" exists.
        intake.setCourtesyDays(readInt(metaObject, "courtesyDays", CostCalculator.COURTESY_MAX_DAYS));

        return intake;
    }

    private Client parseClient(JsonObject clientObject) throws DomainValidationException {
        String clientId = readString(clientObject, "id", "UNKNOWN");
        String name = readString(clientObject, "name", "Unknown client");
        String phone = readString(clientObject, "phone", "");
        String email = readString(clientObject, "email", "");
        return new Client(clientId, name, phone, email);
    }

    private Car parseCar(JsonObject carObject) throws DomainValidationException {
        String vin = requireString(carObject, "vin"); // a car without VIN is invalid
        String make = readString(carObject, "make", "Unknown");
        String model = readString(carObject, "model", "Unknown");
        int year = readInt(carObject, "year", 0);
        int odometerKm = readInt(carObject, "odometerKm", 0);
        boolean warrantyActive = readBoolean(carObject, "warrantyActive", false);
        return new Car(vin, make, model, year, odometerKm, warrantyActive);
    }

    private void parseIssues(JsonObject intakeObject, ServiceIntake intake) throws DomainValidationException {
        JsonArray issuesArray = getArray(intakeObject, "reportedIssues");
        for (int i = 0; i < issuesArray.size(); i++) {
            JsonObject issueObject = asObject(issuesArray.get(i), "reportedIssues");

            String code = requireString(issueObject, "code");
            Severity severity = Severity.fromText(readString(issueObject, "severity", "LOW"));
            int sinceKm = readInt(issueObject, "sinceKm", 0);

            ArrayList<String> symptoms = new ArrayList<String>();
            JsonArray symptomsArray = getArray(issueObject, "symptoms");
            for (int j = 0; j < symptomsArray.size(); j++) {
                JsonElement symptomElement = symptomsArray.get(j);
                if (symptomElement.isJsonPrimitive()) {
                    symptoms.add(symptomElement.getAsString());
                }
            }

            intake.getIssues().add(new Issue(code, severity, symptoms, sinceKm));
        }
    }

    private void parseDiagnostics(JsonObject intakeObject, ServiceIntake intake) throws DomainValidationException {
        JsonObject diagnosticsObject = getObject(intakeObject, "diagnostics");

        JsonArray obdArray = getArray(diagnosticsObject, "obd");
        for (int i = 0; i < obdArray.size(); i++) {
            JsonObject obdObject = asObject(obdArray.get(i), "obd");
            String dtc = requireString(obdObject, "dtc");
            String status = readString(obdObject, "status", "UNKNOWN");
            intake.getObdCodes().add(new ObdCode(dtc, status));
        }

        JsonArray testsArray = getArray(diagnosticsObject, "tests");
        for (int i = 0; i < testsArray.size(); i++) {
            JsonObject testObject = asObject(testsArray.get(i), "tests");
            String testName = requireString(testObject, "name");
            // Missing result -> we treat the test as NOT ok (safer for a release decision)
            boolean testOk = readBoolean(testObject, "ok", false);
            intake.getTests().add(new DiagnosticTest(testName, testOk));
        }
    }

    private void parseWorkOrders(JsonObject intakeObject, ServiceIntake intake) throws DomainValidationException {
        JsonArray workOrdersArray = getArray(intakeObject, "workOrders");
        for (int i = 0; i < workOrdersArray.size(); i++) {
            JsonObject workOrderObject = asObject(workOrdersArray.get(i), "workOrders");

            String type = requireString(workOrderObject, "type").trim().toUpperCase();
            String workOrderId = intake.getId() + "-WO" + (i + 1);

            // Java 6 cannot use switch on String, so we use if / else if
            WorkOrder workOrder;
            if (type.equals("MECHANICAL")) {
                workOrder = new MechanicalWorkOrder(workOrderId);
            } else if (type.equals("ELECTRICAL")) {
                workOrder = new ElectricalWorkOrder(workOrderId);
            } else {
                throw new DomainValidationException("Unknown work order type: " + type);
            }

            JsonArray tasksArray = getArray(workOrderObject, "tasks");
            for (int j = 0; j < tasksArray.size(); j++) {
                JsonObject taskObject = asObject(tasksArray.get(j), "tasks");
                String taskId = requireString(taskObject, "id");
                String description = readString(taskObject, "desc", "");
                double laborHours = readDouble(taskObject, "laborH", 0.0);
                double hourlyRate = readDouble(taskObject, "hourly", 0.0);
                workOrder.addTask(new Task(taskId, description, laborHours, hourlyRate));
            }

            JsonArray partsArray = getArray(workOrderObject, "parts");
            for (int j = 0; j < partsArray.size(); j++) {
                JsonObject partObject = asObject(partsArray.get(j), "parts");
                String sku = requireString(partObject, "sku");
                String partName = readString(partObject, "name", sku);
                int quantity = readInt(partObject, "qty", 1);
                double unitPrice = readDouble(partObject, "unitPrice", 0.0);
                workOrder.addPart(new Part(sku, partName, quantity, unitPrice));
            }

            intake.getWorkOrders().add(workOrder);
        }
    }

    // ---------------- small helper methods (defensive parsing) ----------------

    /** Returns the field, or null if it is missing or JSON null. */
    private JsonElement getField(JsonObject parent, String fieldName) {
        if (parent.has(fieldName) && !parent.get(fieldName).isJsonNull()) {
            return parent.get(fieldName);
        }
        return null;
    }

    /** Missing object -> empty object, so the defaults are used. */
    private JsonObject getObject(JsonObject parent, String fieldName) throws DomainValidationException {
        JsonElement element = getField(parent, fieldName);
        if (element == null) {
            return new JsonObject();
        }
        return asObject(element, fieldName);
    }

    /** Missing array -> empty array. */
    private JsonArray getArray(JsonObject parent, String fieldName) throws DomainValidationException {
        JsonElement element = getField(parent, fieldName);
        if (element == null) {
            return new JsonArray();
        }
        if (!element.isJsonArray()) {
            throw new DomainValidationException("Field '" + fieldName + "' must be an array");
        }
        return element.getAsJsonArray();
    }

    private JsonObject asObject(JsonElement element, String where) throws DomainValidationException {
        if (!element.isJsonObject()) {
            throw new DomainValidationException("Expected an object inside '" + where + "'");
        }
        return element.getAsJsonObject();
    }

    private String readString(JsonObject parent, String fieldName, String defaultValue)
            throws DomainValidationException {
        JsonElement element = getField(parent, fieldName);
        if (element == null) {
            return defaultValue;
        }
        if (!element.isJsonPrimitive()) {
            throw new DomainValidationException("Field '" + fieldName + "' must be a simple value");
        }
        return element.getAsString();
    }

    private String requireString(JsonObject parent, String fieldName) throws DomainValidationException {
        String value = readString(parent, fieldName, null);
        if (value == null || value.trim().length() == 0) {
            throw new DomainValidationException("Missing required field: " + fieldName);
        }
        return value;
    }

    private double readDouble(JsonObject parent, String fieldName, double defaultValue)
            throws DomainValidationException {
        JsonElement element = getField(parent, fieldName);
        if (element == null) {
            return defaultValue;
        }
        try {
            return element.getAsDouble();
        } catch (RuntimeException e) {
            throw new DomainValidationException("Field '" + fieldName + "' is not a valid number", e);
        }
    }

    private int readInt(JsonObject parent, String fieldName, int defaultValue) throws DomainValidationException {
        JsonElement element = getField(parent, fieldName);
        if (element == null) {
            return defaultValue;
        }
        try {
            return element.getAsInt();
        } catch (RuntimeException e) {
            throw new DomainValidationException("Field '" + fieldName + "' is not a valid integer", e);
        }
    }

    private boolean readBoolean(JsonObject parent, String fieldName, boolean defaultValue)
            throws DomainValidationException {
        JsonElement element = getField(parent, fieldName);
        if (element == null) {
            return defaultValue;
        }
        if (!element.isJsonPrimitive()) {
            throw new DomainValidationException("Field '" + fieldName + "' must be true or false");
        }
        return element.getAsBoolean();
    }
}