package app.LeaderUtils;

/**
 *This is the ServiceHandler Factory that will create a a thread safe queue handler
 */
public class RequestCoordinator {

    public static Runnable RequestManagerFactory(QueueEntry q) {
        return new QueueHandler(0, q);
    }
}
