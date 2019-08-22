package me.exrates.externalservice.utils;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Util component to perform task after spring transaction is successfully committed
 */
@Component
public class TransactionAfterCommitExecutor extends TransactionSynchronizationAdapter implements Executor {

    private static final ThreadLocal<List<Runnable>> runnable = new ThreadLocal<>();

    @Override
    public void execute(final Runnable command) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            command.run();
            return;
        }
        List<Runnable> threadRunnable = runnable.get();
        if (threadRunnable == null) {
            threadRunnable = new ArrayList<>();
            runnable.set(threadRunnable);
            TransactionSynchronizationManager.registerSynchronization(this);
        }
        threadRunnable.add(command);
    }

    @Override
    public void afterCommit() {
        runnable.get().forEach(Runnable::run);
    }

    @Override
    public void afterCompletion(final int status) {
        runnable.remove();
    }
}