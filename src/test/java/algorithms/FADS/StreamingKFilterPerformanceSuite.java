package algorithms.FADS;

import algorithms.FADS.measures.RmseDistortion;
import algorithms.FADS.measures.RmseLatency;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import core.InputRecord;
import core.OutputRecord;
import core.StreamingKFilter;

/**
 * A test suite designed to performance test a given {@link StreamingKFilter} implementation with a range of data distributions.
 * <p>
 * This suite will run the filter with some different data distributions whilst making random calls to publish records. It will then gather some statistics on the published records and print these out.
 */
public class StreamingKFilterPerformanceSuite {

    public static void main(String[] args) {
        double[] normalDistribution = new NormalDistribution(50, 0.5).sample(10_000);
        List<Double> normalList = convertArrayToList(normalDistribution);

        List<Double> normalListScaledAndRounded = scaleAndRoundToWholeNumbers(normalList);

        double[] uniformDistribution = new UniformRealDistribution(-100, 100).sample(10_000);
        List<Double> uniformList = convertArrayToList(uniformDistribution);

        double[] uniformDistributionTwo = new UniformRealDistribution(100_000, 1_000_000).sample(10_000);
        List<Double> uniformListTwo = convertArrayToList(uniformDistributionTwo);

        printHeaderLineForTestResults();
        runForDistributionAndPrintResults("Normal Distribution around 50", normalList);
        runForDistributionAndPrintResults("Normal Distribution around 50,000, rounded to whole numbers", normalListScaledAndRounded);
        runForDistributionAndPrintResults("Uniform distribution, positive and negative", uniformList);
        runForDistributionAndPrintResults("Uniform distribution over a large range", uniformListTwo);
    }

    private static void printHeaderLineForTestResults() {
        System.out.format("| %65s | %20s | %20s | %20s |%n",
                "Distribution Description", "RMSE Distortion", "RMSE Latency", "Unpublished records");
    }

    private static void runForDistributionAndPrintResults(String distributionName, List<Double> dataDistribution) {
        Collection<OutputRecord> publishableRecords = new ArrayList<>(dataDistribution.size());
        StreamingKFilter streamingKFilter = CandidateFilterFactory.getStreamingKFilter();

        for (int time = 0; time < dataDistribution.size(); time++) {
            streamingKFilter.processNewRecord(new InputRecord(time, dataDistribution.get(time)));

            // Send a request to publish at random intervals
            if ((time % getRandomIntFromRange(1, dataDistribution.size() / 100)) == 0) {
                publishableRecords.addAll(streamingKFilter.returnPublishableRecords());
            }
        }

        publishableRecords.addAll(streamingKFilter.returnPublishableRecords());

        if (publishableRecords.size() == 0) {
            System.out.println("No records published for " + distributionName);
        } else {
            double rangeOfData = Collections.max(dataDistribution) - Collections.min(dataDistribution);

            double rmseDistortion = RmseDistortion.measureDataDistortion(publishableRecords, rangeOfData);
            double rmseLatency = RmseLatency.measureLatency(publishableRecords);
            int remainingRecords = dataDistribution.size() - publishableRecords.size();

            System.out.format(
                    "| %65s | %20.5f | %20.5f | %20d |%n",
                    distributionName,
                    rmseDistortion,
                    rmseLatency,
                    remainingRecords);
        }
    }

    private static List<Double> scaleAndRoundToWholeNumbers(List<Double> normalList) {
        return normalList.stream().map(aDouble -> (double) Math.round(aDouble * 1000)).collect(Collectors.toList());
    }

    private static List<Double> convertArrayToList(double[] distribution) {
        return DoubleStream.of(distribution).boxed().collect(Collectors.toList());
    }

    private static Integer getRandomIntFromRange(int min, int max) {
        int randomInt = new Random().nextInt(max - min);
        return randomInt + min;
    }
}