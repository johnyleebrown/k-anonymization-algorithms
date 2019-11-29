package algorithms.FADS.impl.strategies.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import core.InputRecord;

import algorithms.FADS.impl.strategies.FindKNearestNeighborsStrategy;

public class DefaultFindKNearestNeighborsStrategy implements FindKNearestNeighborsStrategy
{
    @Override
    public List<InputRecord> find(final int k, final InputRecord baseRecord, final List<InputRecord> recordsBuffer)
    {
        if (recordsBuffer.size() < k - 1)
        {
            return Collections.emptyList();
        }

        final List<InputRecord> result = new ArrayList<>(createPrioritizedQueue(k - 1, baseRecord, recordsBuffer));
        result.add(baseRecord);
        return result;
    }

    private Queue<InputRecord> createPrioritizedQueue(final int k, final InputRecord baseRecord, final List<InputRecord> recordsBuffer)
    {
        final Queue<InputRecord> priorityQueue = new PriorityQueue<>(k, getPriorityQueueComparator(baseRecord));
        priorityQueue.addAll(recordsBuffer);
        return priorityQueue;
    }

    private Comparator<InputRecord> getPriorityQueueComparator(final InputRecord baseRecord)
    {
        return new Comparator<InputRecord>()
        {
            @Override
            public int compare(final InputRecord first, final InputRecord second)
            {
                if (getDistance(first, baseRecord) < getDistance(second, baseRecord) || getDistance(first, baseRecord) > getDistance(second, baseRecord))
                {
                    return -1;
                }

                return Integer.compare(first.getTime(), second.getTime());
            }

            private double getDistance(final InputRecord a, final InputRecord b)
            {
                return Math.abs(a.getRawValue() - b.getRawValue());
            }
        };
    }

}
