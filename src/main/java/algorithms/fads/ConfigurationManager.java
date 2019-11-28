package algorithms.fads;

public class ConfigurationManager
{
    /**
     * The anonymity set >= 2. When an anonymised value is published it will be for grouping of >= k records.
     *
     * Default k from paper is 100.
     */
    public static final int K = 5;

    /**
     * The time lapse from a record taken as input to it being published, delayConstraint >= k (delta in FADS paper).
     */
    public static final int DELAY_CONSTRAINT = 1000;

    /**
     * The maximum age that a cluster may be reused(t_kc in FADS paper).
     */
    public static final int REUSE_CONSTRAINT = 200;
}
