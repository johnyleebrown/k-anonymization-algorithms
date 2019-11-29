package algorithms.FADS.measures;

import core.OutputRecord;

import java.util.Collection;

/**
 * Defines a measure of data distortion using Root Mean Square Error.
 */
public class RmseDistortion {

    /**
     * Returns the normalised data distortion measured by RMSE.
     *
     * @param outputRecords the output records
     * @param rangeOfData the range of that data
     * @return the data distortion measured by RMSE
     */
    public static double measureDataDistortion(Collection<OutputRecord> outputRecords, double rangeOfData) {
        double diffSum = outputRecords.stream()
                .mapToDouble(RmseDistortion::dataDistortionRmse)
                .sum();

        double rmse = Math.sqrt(Math.abs(diffSum / outputRecords.size()));
        return rmse / rangeOfData;
    }

    // computes the RMSE for a single record
    private static double dataDistortionRmse(OutputRecord record) {
        double anonymisedValue = record.getAnonymisedValue();
        double rawValue = record.getRawValue();
        return squareDiff(anonymisedValue, rawValue);
    }

    static double squareDiff(double x, double y) {
        return Math.pow(x - y, 2);
    }
}
