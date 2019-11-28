FADS algorithm can be closely compared to CASTLE(the first anonymization algorithm for data streams). The algorithm FADS takes into account the time constraints on tuple publication and cluster reuse during data stream anonymization.
<p>
FADS vs CASTLE (the main reasons)

- The algorithm does no restrict the size of the k-anonymized cluster set to grow with the size of the stream, so it can increase time complexity to O($n^2$). The algorithm does no restrict the size of cluster itself also, so it can increase time complexity to O($n^2$). FADS implements restrictions which gives us linear time complexity.
- Space complexity suffers for same reasons. Space complexity in FADS is restricted by constant C.
<p>
### Strategy

1. Read the input record from the stream and place it to buffer.
2. When a record stays longer than the delay constraint, it is published.
3. If the publish is processed, the record will be placed in set for reuse.
4. When no more records arrive, step 2 and 3 will be repeated for records left in set to clear it out.

