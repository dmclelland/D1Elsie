package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.cqrs.AnnotatedMethodInvokerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class AbstractCommandHandler<A extends Aggregate> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCommandHandler.class);

    private final AggregateRepository<A> repository;

    private final AnnotatedCommandHandlerInvoker<A, AbstractCommandHandler<A>> annotatedCommandHandlerInvoker;

    protected AbstractCommandHandler(AggregateRepository<A> repository, AnnotatedMethodInvokerStrategy strategy) {
        this.repository = checkNotNull(repository);
        checkNotNull(strategy);
        this.annotatedCommandHandlerInvoker = getMethodInvoker(strategy);
    }

    protected A getAggregate(String id) {
        return repository.find(id);
    }

    protected void createAggregate(A aggregate) {
        repository.create(aggregate);
    }

    private void commitAggregate(String id) {
        repository.commit(id);
    }

    private void rollbackAggregate(String id) {
        repository.rollback(id);
    }

    public void invokeCommand(Command command) {
        try {
            annotatedCommandHandlerInvoker.invoke(command, this);
            commitAggregate(command.getAggregateId());
        } catch (Exception e) {
            //rollback aggregate if any error
            rollbackAggregate(command.getAggregateId());
            LOG.error("Unable to process command {} ", command.toString(), e);
        }
    }


    @SuppressWarnings("rawtypes")
    private AnnotatedCommandHandlerInvoker<A,AbstractCommandHandler<A>> getMethodInvoker(AnnotatedMethodInvokerStrategy strategy) {
        if (AnnotatedMethodInvokerStrategy.GENERATED == strategy) {
            try {
                String className = this.getClass().getSimpleName() + "AnnotatedMethodInvoker";
                Class<?> clazz  = Class.forName("com.dmc.d1.algo.commandhandler." + className);
                return (AnnotatedCommandHandlerInvoker) clazz.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
            }
        } else {
            return new ReflectiveAnnotatedCommandHandlerInvoker(this);
        }
    }
}
