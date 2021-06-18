package eu.smesec.core.cache;

import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

//import org.glassfish.jersey.logging.LoggingFeature;

public abstract class Cache {
    protected static Logger logger = Logger.getLogger(Cache.class.getName());

    protected Path path;
    protected ReentrantReadWriteLock.ReadLock readLock;
    protected ReentrantReadWriteLock.WriteLock writeLock;

    /**
     * Cache base class constructor.
     *
     * @param path Cache directory path
     */
    public Cache(Path path) {
        this.path = path;
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }

    public Path getPath() {
        return path;
    }
}
