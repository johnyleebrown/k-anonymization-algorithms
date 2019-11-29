package algorithms.FADS.impl.strategies;

import java.util.List;

import core.InputRecord;

/**
 * A strategy to find k nearest neighbors.
 */
public interface FindKNearestNeighborsStrategy
{
    /**
     * USing priority queue to get k - 1 closest items.
     *
     * @param k             the number of closest records that should be found
     * @param record        a record
     * @param recordsBuffer all records to pick from
     * @return a list of closest records
     */
    List<InputRecord> find(int k, InputRecord record, List<InputRecord> recordsBuffer);
}
