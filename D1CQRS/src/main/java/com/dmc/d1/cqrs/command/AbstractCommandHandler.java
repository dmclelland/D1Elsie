package com.dmc.d1.cqrs.command;

import com.dmc.d1.cqrs.Aggregate;
import com.dmc.d1.cqrs.AggregateRepository;
import com.dmc.d1.domain.Id;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by davidclelland on 16/05/2016.
 */
public abstract class AbstractCommandHandler<ID extends Id, A extends Aggregate> {

    protected final AggregateRepository<ID,A> repository;

    protected AbstractCommandHandler(AggregateRepository<ID,A> repository){
        this.repository = checkNotNull(repository);
    }

    protected A getAggregate(ID id){
        return repository.find(id);
    }

    protected void createAggregate(A aggregate){
        repository.create(aggregate);
    }

    protected void commitAggregate(ID id){
        repository.commit(id);
    }

    void rollbackAggregate(ID id){
        repository.rollback(id);
    }


}
