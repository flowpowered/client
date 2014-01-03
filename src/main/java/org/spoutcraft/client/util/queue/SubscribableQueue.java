package org.spoutcraft.client.util.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A concurrent queue to which multiple threads can subscribe. Each subscribed thread gets its own copy of the queue, but not of it contents. Items added to the queue (via either {@link #add(Object)},
 * {@link #addAll(java.util.Collection)} or {@link #offer(Object)}) will be added to all the thread queues, but any removal operation is confined to the thread's queue. A thread needs to subscribe to
 * the queue before being able to use it, using {@link #subscribe()}. The queue to use is selected from the calling thread, using {@link Thread#currentThread()}. Once done, a thread should unsubscribe
 * using {@link #unsubscribe()}.
 * <p/>
 * When a thread constructs a new {@link org.spoutcraft.client.util.queue.SubscribableQueue}, it can declared itself as the publisher thread, and the operation of the following methods is limited to
 * that thread only: {@link #unsubscribeAll()}. The following operations operate on all queues if called by the publisher, else on the thread's own queue: {@link #add(Object)}, {@link
 * #addAll(java.util.Collection)}, {@link #offer(Object)}.
 * <p/>
 * Call to methods can be expensive because of the cost of {@link Thread#currentThread()} on certain platforms. Using mass removal or addition operations ({@link #removeAll(java.util.Collection)},
 * {@link #addAll(java.util.Collection)}) is recommended when multiple items need to be removed or added.
 */
public class SubscribableQueue<T> implements Queue<T> {
    private final Map<Object, Queue<T>> queues = new ConcurrentHashMap<>();
    private final AtomicLong publisherThreadID = new AtomicLong();

    /**
     * Constructs a new subscribable queue, making the thread the publisher.
     */
    public SubscribableQueue() {
        this(true);
    }

    /**
     * Constructs a new subscribable queue.
     *
     * @param becomePublisher Whether or not to become the publisher
     */
    public SubscribableQueue(boolean becomePublisher) {
        if (becomePublisher) {
            publisherThreadID.set(Thread.currentThread().getId());
        } else {
            publisherThreadID.set(-1);
        }
    }

    /**
     * Attempts to make the thread the publisher, returning true if the attempt succeeded. This is only possible if there's no publisher.
     *
     * @return Whether or not the thread became the publisher
     */
    public boolean becomePublisher() {
        if (publisherThreadID.get() != -1) {
            return false;
        }
        publisherThreadID.set(Thread.currentThread().getId());
        return true;
    }

    /**
     * Makes the publisher thread not be the publisher any more. Can only be called by the publisher thread.
     */
    public void quitPublisher() {
        checkPublisherThread();
        publisherThreadID.set(-1);
    }

    /**
     * Subscribes the thread to the queue.
     */
    public void subscribe() {
        queues.put(Thread.currentThread().getId(), new ConcurrentLinkedQueue<T>());
    }

    /**
     * Unsubscribes the thread from the queue.
     */
    public void unsubscribe() {
        queues.remove(Thread.currentThread().getId());
    }

    /**
     * Unsubscribes all threads from all queues, deleting all the content. Can only be performed by the publisher thread.
     */
    public void unsubscribeAll() {
        checkPublisherThread();
        queues.clear();
    }

    @Override
    public boolean add(T t) {
        checkNotNull(t);
        if (isPublisherThread()) {
            boolean changed = false;
            for (Queue<T> queue : queues.values()) {
                if (queue.add(t)) {
                    changed = true;
                }
            }
            return changed;
        }
        return getCurrentThreadQueue().add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        checkNotNull(c);
        if (isPublisherThread()) {
            boolean changed = false;
            for (Queue<T> queue : queues.values()) {
                if (queue.addAll(c)) {
                    changed = true;
                }
            }
            return changed;
        }
        return getCurrentThreadQueue().addAll(c);
    }

    @Override
    public boolean offer(T t) {
        checkNotNull(t);
        if (isPublisherThread()) {
            boolean changed = false;
            for (Queue<T> queue : queues.values()) {
                if (queue.offer(t)) {
                    changed = true;
                }
            }
            return changed;
        }
        return getCurrentThreadQueue().offer(t);
    }

    @Override
    public T remove() {
        return getCurrentThreadQueue().remove();
    }

    @Override
    public T poll() {
        return getCurrentThreadQueue().poll();
    }

    @Override
    public T element() {
        return getCurrentThreadQueue().element();
    }

    @Override
    public T peek() {
        return getCurrentThreadQueue().peek();
    }

    @Override
    public int size() {
        return getCurrentThreadQueue().size();
    }

    @Override
    public boolean isEmpty() {
        return getCurrentThreadQueue().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getCurrentThreadQueue().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return getCurrentThreadQueue().iterator();
    }

    @Override
    public Object[] toArray() {
        return getCurrentThreadQueue().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        checkNotNull(a);
        return getCurrentThreadQueue().toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return getCurrentThreadQueue().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        checkNotNull(c);
        return getCurrentThreadQueue().containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkNotNull(c);
        return getCurrentThreadQueue().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        checkNotNull(c);
        return getCurrentThreadQueue().retainAll(c);
    }

    @Override
    public void clear() {
        getCurrentThreadQueue().clear();
    }

    private Queue<T> getCurrentThreadQueue() {
        final Queue<T> queue = queues.get(Thread.currentThread().getId());
        if (queue == null) {
            throw new IllegalArgumentException("The calling thread is not subscribed to the queue");
        }
        return queue;
    }

    private void checkNotNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }

    private void checkPublisherThread() {
        if (!isPublisherThread()) {
            throw new IllegalStateException("This operation can only be performed by the publisher thread");
        }
    }

    private boolean isPublisherThread() {
        return Thread.currentThread().getId() == publisherThreadID.get();
    }
}
