package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.domain.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class AbstractCommandHandler<ID extends Id, A extends Aggregate> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCommandHandler.class);

    protected final AggregateRepository<ID, A> repository;

    private final AnnotatedMethodInvoker annotatedMethodInvoker;

    protected AbstractCommandHandler(AggregateRepository<ID, A> repository, AnnotatedMethodInvokerStrategy strategy) {
        this.repository = checkNotNull(repository);

        checkNotNull(strategy);
        this.annotatedMethodInvoker = getMethodInvoker(strategy);
    }


    private AnnotatedMethodInvoker getMethodInvoker(AnnotatedMethodInvokerStrategy strategy) {
        if (AnnotatedMethodInvokerStrategy.GENERATED == strategy) {
            try {
                String className = this.getClass().getSimpleName() + "AnnotatedMethodInvoker";
                Class<?> clazz  = Class.forName("com.dmc.d1.algo.command." + className);
                return (AnnotatedMethodInvoker) clazz.newInstance();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
            } catch (InstantiationException e) {
                throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
            }
        } else {
            return new ReflectiveAnnotatedMethodInvoker(this);
        }
    }


    protected A getAggregate(ID id) {
        return repository.find(id);
    }

    protected void createAggregate(A aggregate) {
        repository.create(aggregate);
    }

    protected void commitAggregate(ID id) {
        repository.commit(id);
    }

    void rollbackAggregate(ID id) {
        repository.rollback(id);
    }

    public void invokeCommand(Command<ID> command) {
        try {
            annotatedMethodInvoker.invoke(command, this);
            commitAggregate(command.getAggregateId());
        } catch (Exception e) {
            //rollback aggregate if any error
            rollbackAggregate(command.getAggregateId());
            LOG.error("Unable to process command {} ", command.toString(), e);
        }
    }


}
