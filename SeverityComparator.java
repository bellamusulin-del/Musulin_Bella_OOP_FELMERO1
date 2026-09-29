package carservice;

import java.util.Comparator;

/** Sorts issues HIGH -> MEDIUM -> LOW. If equal, sorts by code. */
public class SeverityComparator implements Comparator<Issue> {

    public int compare(Issue firstIssue, Issue secondIssue) {
        int firstRank = firstIssue.getSeverity().getRank();
        int secondRank = secondIssue.getSeverity().getRank();
        if (firstRank != secondRank) {
            return firstRank - secondRank;
        }
        return firstIssue.getCode().compareTo(secondIssue.getCode());
    }
}
