package algorithms.fads;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import core.InputRecord;
import core.OutputRecord;
import core.StreamingKFilter;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * A suite of tests designed to verify that a given implementation of a {@link StreamingKFilter} respects K-Anonymity for a fixed K value of {@link #K}, as well as some other basic requirements.
 * <p>
 * Do not make changes to any of the tests below, but feel free to add your own tests to a new class.
 */
@DisplayName("K-Anonymity Test Suite")
public class StreamingKFilterTestSuite {

    // For the purpose of these tests, we will set K to 5.
    public static final int K = 5;

    private StreamingKFilter streamingKFilter;
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        streamingKFilter = CandidateFilterFactory.getStreamingKFilter();
    }

    @Test
    @DisplayName("Streaming Filter respects K-Anonymity for < K inputs")
    void givenCandidateFilter_whenFewerThanKValuesProvided_thenWeRespectKAnonymity() {
        streamingKFilter.processNewRecord(new InputRecord(1, 10.0));

        assertThat("No records should be published if fewer than K have been supplied.",
                streamingKFilter.returnPublishableRecords(), empty());
    }

    @ParameterizedTest(name = "Running with a \"{0}\"")
    @SuppressWarnings("unused") // name arg is used for the Parameterized Test name.
    @MethodSource("dataDistributionProvider")
    @DisplayName("Streaming Filter respects K-anonymity for > K records")
    void givenCandidateFilter_whenMoreThanKRecordsProvided_thenWeRespectKAnonymity(String name, List<Double> values) {
        for (int i = 1; i <= values.size(); i++) {
            streamingKFilter.processNewRecord(new InputRecord(i, values.get(i - 1)));
        }

        Collection<OutputRecord> publishableRecords = streamingKFilter.returnPublishableRecords();

        Map<Double, List<OutputRecord>> anonymisedBuckets = groupByAnonymisedValue(publishableRecords);

        printBadBuckets(anonymisedBuckets);
        failBadBuckets(anonymisedBuckets);
    }

    @Test
    @DisplayName("Streaming Filter does not publish duplicate records")
    void givenCandidateFilter_whenWeRequestToPublishTwiceWithoutProvidingNewRecords_thenOutputsAreNotRepeated() {
        int numberOfRecords = 1000;
        int min = 0;
        int max = 10;

        for (int time = 1; time <= numberOfRecords; time++) {
            streamingKFilter.processNewRecord(new InputRecord(time, getRandomDoubleInRange(min, max)));
        }

        Collection<OutputRecord> firstRequestForRecords = streamingKFilter.returnPublishableRecords();
        Collection<OutputRecord> secondRequestForRecords = streamingKFilter.returnPublishableRecords();

        List<Integer> inputTimesFromFirstRequest = getInputTimesFromOutputRecords(firstRequestForRecords);
        List<Integer> inputTimesFromSecondRequest = getInputTimesFromOutputRecords(secondRequestForRecords);

        inputTimesFromFirstRequest.forEach(inputTimeFromFirstRequest ->
                assertThat("No one input record should be output more than once.",
                        inputTimeFromFirstRequest, is(not(in(inputTimesFromSecondRequest)))));
    }

    @Test
    @DisplayName("Streaming Filter can cope with frequent requests to publish")
    void givenCandidateFilter_whenWeFrequentlyRequestData_thenWeRespectKAnonymity() {
        int numberOfRecords = 10000;
        int min = 0;
        int max = 100;

        Collection<OutputRecord> publishableRecords = new ArrayList<>();

        for (int time = 1; time <= numberOfRecords; time++) {
            streamingKFilter.processNewRecord(new InputRecord(time, getRandomDoubleInRange(min, max)));

            // Send a request to publish every 10 seconds.
            if ((time % 10) == 0) {
                publishableRecords.addAll(streamingKFilter.returnPublishableRecords());
            }
        }

        publishableRecords.addAll(streamingKFilter.returnPublishableRecords());

        assertThat("We publish at most " + numberOfRecords + " records.",
                publishableRecords.size(), lessThanOrEqualTo(numberOfRecords));

        Map<Double, List<OutputRecord>> anonymisedBuckets = groupByAnonymisedValue(publishableRecords);

        printBadBuckets(anonymisedBuckets);
        failBadBuckets(anonymisedBuckets);
    }

    @Test
    @DisplayName("Streaming Filter correctly sets output time")
    void givenCandidateFilter_whenWeRequestData_thenPublishedTimeIsCorrectlySet() {
        int numberOfRecords = 1000;
        int min = 0;
        int max = 10;

        Set<Integer> inputTimesForRecords = new HashSet<>(numberOfRecords);

        for (int time = 1; time <= numberOfRecords; time++) {
            streamingKFilter.processNewRecord(new InputRecord(time, getRandomDoubleInRange(min, max)));
            inputTimesForRecords.add(time);
        }

        Collection<OutputRecord> publishableRecords = streamingKFilter.returnPublishableRecords();

        publishableRecords.forEach(outputRecord ->
                assertThat("Records can't be published with an output time earlier than the records input time.",
                        outputRecord.getOutputTime(), greaterThanOrEqualTo(outputRecord.getInputTime())));

        List<Integer> outputTimesFromOutputRecords = getOutputTimesFromOutputRecords(publishableRecords);

        outputTimesFromOutputRecords.forEach(outputTime ->
                assertThat("Record output time should always be based off of a record input time.",
                        inputTimesForRecords, hasItem(outputTime)));
    }

    private void failBadBuckets(Map<Double, List<OutputRecord>> anonymisedBuckets) {
        anonymisedBuckets.forEach((anonValue, anonBucket) -> assertThat("The bucket for anonymised value " + anonValue + " is not K-Anonymous", anonBucket.size(), greaterThanOrEqualTo(K)));
    }

    private void printBadBuckets(Map<Double, List<OutputRecord>> anonymisedBuckets) {
        anonymisedBuckets.entrySet()
                .stream()
                .filter(bucket -> bucket.getValue().size() < K)
                .forEach(this::printOutBucketStats);
    }

    private void printOutBucketStats(Map.Entry<Double, List<OutputRecord>> bucket) {
        String bucketStats = String.format("The bucket with anonymised value [%s] and the following Output Records does not meet the K requirements. \n %s \n",
                bucket.getKey(),
                bucket.getValue().stream()
                        .map(OutputRecord::toString)
                        .collect(Collectors.joining(",\n ")));

        System.out.println(bucketStats);
    }

    private List<Integer> getInputTimesFromOutputRecords(Collection<OutputRecord> firstRequestForRecords) {
        return firstRequestForRecords.stream().map(OutputRecord::getInputTime).collect(Collectors.toList());
    }

    private List<Integer> getOutputTimesFromOutputRecords(Collection<OutputRecord> firstRequestForRecords) {
        return firstRequestForRecords.stream().map(OutputRecord::getOutputTime).collect(Collectors.toList());
    }

    private Double getRandomDoubleInRange(int min, int max) {
        return random.nextDouble() * (max - min) + min;
    }

    private Map<Double, List<OutputRecord>> groupByAnonymisedValue(Collection<OutputRecord> outputRecords) {
        return outputRecords.stream().collect(Collectors.groupingBy(OutputRecord::getAnonymisedValue));
    }

    private static Stream<Arguments> dataDistributionProvider() {
        int numberOfRecords = 100_000;

        double[] normalDistribution = new NormalDistribution(50, 0.5).sample(numberOfRecords);
        List<Double> normalList = DoubleStream.of(normalDistribution).boxed().collect(Collectors.toList());

        int[] poissonDistribution = new PoissonDistribution(50, 10).sample(numberOfRecords);
        List<Double> poissonList = DoubleStream.of(Arrays.stream(poissonDistribution).asDoubleStream().toArray()).boxed().collect(Collectors.toList());

        double[] uniformDistribution = new UniformRealDistribution(0, 1000).sample(numberOfRecords);
        List<Double> uniformList = DoubleStream.of(uniformDistribution).boxed().collect(Collectors.toList());

        return Stream.of(
                arguments("Normal Distribution", normalList),
                arguments("Poisson Distribution", poissonList),
                arguments("Uniform Distribution", uniformList)
        );
    }
}
