package at.yawk.filemanager;

/**
 * @author Jonas Konrad (yawkat)
 */
public interface Sink<T> {
    void digest(T element);
}
