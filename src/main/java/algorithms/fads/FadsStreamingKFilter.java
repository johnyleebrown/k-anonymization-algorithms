package algorithms.fads;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import core.InputRecord;
import core.OutputRecord;
import core.StreamingKFilter;
import entities.Cluster;
import strategies.impl.DefaultFindKNearestNeighborsStrategy;

import static algorithms.fads.ConfigurationManager.DELAY_CONSTRAINT;
import static algorithms.fads.ConfigurationManager.K;
import static algorithms.fads.ConfigurationManager.REUSE_CONSTRAINT;

/**
 * This implementation is based on the paper "Fast clustering-based anonymization approaches with time constraints for data streams"
 * by Kun Guo and Qishan Zhang.
 *
 * @see <a href="https://www.sciencedirect.com/science/article/abs/pii/S0950705113000877">Reference</a>.
 */
public class FadsStreamingKFilter implements StreamingKFilter
{
    private final List<InputRecord> recordsBuffer = new ArrayList<>(); // Set_tp in FADS paper
    private final List<Cluster> reusableClusters = new ArrayList<>(); // Set_kc in FADS paper
    private Collection<OutputRecord> publishableRecords = new ArrayDeque<>();
    private int currentTime;

    public FadsStreamingKFilter()
    {
    }

    @Override
    public void processNewRecord(final InputRecord input)
    {
        currentTime = input.getTime();
        recordsBuffer.add(input);

        cleanupClusters();

        while (recordsBuffer.size() >= DELAY_CONSTRAINT)
        {
            publishRecord(recordsBuffer.remove(0));
        }
    }

    private void cleanupClusters()
    {
        reusableClusters.removeIf(cluster -> cluster.getAge(currentTime) >= REUSE_CONSTRAINT);
    }

    @Override
    public Collection<OutputRecord> returnPublishableRecords()
    {
        if (publishableRecords.isEmpty())
        {
            return Collections.emptyList();
        }

        Collection<OutputRecord> publishedRecords = publishableRecords;
        publishableRecords = new ArrayDeque<>();
        return publishedRecords;
    }

    private void publishRecord(final InputRecord record)
    {
        final Optional<Cluster> reusableCluster = getLeastInfoLossReusableCluster(record);
        final List<InputRecord> closestRecords = getClosestRecords(record, recordsBuffer);
        final Cluster newCluster = new Cluster(closestRecords, currentTime);

        if (reusableCluster.isPresent() && reusableCluster.get().preferThan(newCluster, record))
        {
            addRecordToPublish(reusableCluster.get(), record);
            return;
        }

        reusableClusters.add(newCluster);

        for (final InputRecord newClusterRecord : closestRecords)
        {
            addRecordToPublish(newCluster, newClusterRecord);
        }

        recordsBuffer.removeAll(closestRecords);
    }

    private void addRecordToPublish(final Cluster cluster, final InputRecord record)
    {
        publishableRecords.add(new OutputRecord(record, currentTime, cluster.getAnonymisedValue()));
    }

    private Optional<Cluster> getLeastInfoLossReusableCluster(final InputRecord record)
    {
        return reusableClusters.stream()
                .filter(cluster -> cluster.covers(record))
                .min(Comparator.comparing(cluster -> cluster.getInfoLoss(record)));
    }

    private List<InputRecord> getClosestRecords(final InputRecord record, final List<InputRecord> recordsBuffer)
    {
        return new DefaultFindKNearestNeighborsStrategy().find(K, record, recordsBuffer);
    }
}
