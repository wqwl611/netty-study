package wq.wl;

import java.util.concurrent.*;

/**
 * Description:
 *
 * @author: wangliang
 * @time: 2020-08-03
 */
public class ResultFuture<T> implements Future<T> {

    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private T result;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        return result;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.result != null;
    }

    public void setResult(T result) {
        this.result = result;
        countDownLatch.countDown();
    }
}
