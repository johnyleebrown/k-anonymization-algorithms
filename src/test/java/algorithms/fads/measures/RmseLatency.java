package algorithms.fads.measures;

import core.OutputRecord;

import java.util.Collection;

import static algorithms.fads.measures.RmseDistortion.squareDiff;

/**
 * Defines a measure of latency using Root Mean Square Error.
 */
public class RmseLatency {

    /**
     * Returns the latency measured by RMSE.
     *
     * @param outputRecords the output records
     * @return the latency measured by RMSE
     */
    public static double measureLatency(Collection<OutputRecord> outputRecords) {
        double diffSum = outputRecords.stream()
                .mapToDouble(RmseLatency::latencyRmse)
                .sum();

        return Math.sqrt(Math.abs(diffSum / outputRecords.size()));
    }

    // computes the RMSE for a single record
    private static double latencyRmse(OutputRecord record) {
        int outputTime = record.getOutputTime();
        int inputTime = record.getInputTime();
        return squareDiff(outputTime, inputTime);
    }
}
