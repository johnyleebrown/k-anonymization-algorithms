package algorithms.FADS.impl.entities;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Range;
import core.InputRecord;

public class Cluster
{
    private final double anonymisedValue;
    private final int creationTime;
    private final Range<Double> range;

    public Cluster(final List<InputRecord> records, final int creationTime)
    {
        this.anonymisedValue = records.stream().mapToDouble(InputRecord::getRawValue).average().getAsDouble();
        this.creationTime = creationTime;
        this.range = Range.encloseAll(records.stream().map(InputRecord::getRawValue).collect(Collectors.toList()));
    }

    public double getAnonymisedValue()
    {
        return anonymisedValue;
    }

    public double getAge(final int currentTime)
    {
        return currentTime - creationTime;
    }

    public boolean covers(final InputRecord record)
    {
        return range.contains(record.getRawValue());
    }

    public double getInfoLoss(final InputRecord record)
    {
        return Math.abs(anonymisedValue - record.getRawValue());
    }

    public boolean preferThan(final Cluster other, final InputRecord record)
    {
        final double selfInfoLoss = getInfoLoss(record);
        final double otherInfoLoss = other.getInfoLoss(record);

        if (selfInfoLoss < otherInfoLoss)
        {
            return true;
        }
        else if (selfInfoLoss > otherInfoLoss)
        {
            return false;
        }

        return creationTime < other.creationTime;
    }
}
