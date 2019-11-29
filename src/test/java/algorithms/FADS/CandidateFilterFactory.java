package algorithms.FADS;

import algorithms.FADS.impl.FadsStreamingKFilter;
import core.StreamingKFilter;

public class CandidateFilterFactory {

    /**
     * Use this method to instantiate your implementation, along with any other objects you need to create your filter.
     * <p>
     * The static constant {@value StreamingKFilterTestSuite#K} sets the k-anonymity value for the test suite should your filter require this.
     *
     * @return an instance of a {@link StreamingKFilter}
     */
    public static StreamingKFilter getStreamingKFilter() {
        return new FadsStreamingKFilter(); // Return your filter here.
    }
}
