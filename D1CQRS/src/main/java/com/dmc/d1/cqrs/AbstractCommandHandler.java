package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.domain.Id;
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

    protected AbstractCommandHandler(AggregateRepository<A> repository) {
        this.repository = checkNotNull(repository);
        this.annotatedCommandHandlerInvoker = getMethodInvoker();
    }

    protected A getAggregate(String id) {
        return repository.find(id);
    }

    protected void createAggregate(A aggregate) {
        repository.create(aggregate);
        UnitOfWork.add(aggregate);
    }

    public void invokeCommand(Command command) {
        try {
            //if the command to invoke actually creates the aggregate then
            //obviously there won't be an aggregate at this point
            //in this case the aggregate gets added to the unit or work in the
            //createAggregate method
            Aggregate aggregate = getAggregate(command.getAggregateId());

            if(aggregate!=null)
                UnitOfWork.add(aggregate);

            annotatedCommandHandlerInvoker.invoke(command, this);
            UnitOfWork.commit();
        } catch (Throwable e) {
            //rollback aggregate if any error
            UnitOfWork.rollback();
            LOG.error("Unable to process command {} ", command.toString(), e);
        }
    }

    @SuppressWarnings("rawtypes")
    private AnnotatedCommandHandlerInvoker<A, AbstractCommandHandler<A>> getMethodInvoker() {
        try {
            String className = this.getClass().getSimpleName() + "AnnotatedMethodInvoker";
            Class<?> clazz = Class.forName("com.dmc.d1.algo.commandhandler." + className);
            return (AnnotatedCommandHandlerInvoker) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to resolve annotated method invoker for " + this.getClass().getSimpleName(), e);
        }
    }
}
