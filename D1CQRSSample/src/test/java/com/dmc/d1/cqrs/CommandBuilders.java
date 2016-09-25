package com.dmc.d1.cqrs;

import com.dmc.d1.cqrs.command.Command;
import com.dmc.d1.cqrs.sample.aggregate.ComplexMutableAggregate;
import com.dmc.d1.cqrs.sample.command.CreateComplexAggregateCommand;
import com.dmc.d1.cqrs.sample.command.CreateMutableComplexAggregateCommand;
import com.dmc.d1.cqrs.sample.command.UpdateComplexAggregateCommand;
import com.dmc.d1.cqrs.sample.domain.MyId;
import com.dmc.d1.sample.domain.Basket;
import com.dmc.d1.sample.domain.Basket2;
import com.dmc.d1.sample.domain.BasketConstituent2;
import com.dmc.d1.sample.event.UpdateBasketConstituentEventBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Created By davidclelland on 25/09/2016.
 */
class CommandBuilders {

    static class CreateComplexAggregateCommandSupplier implements Supplier<Command> {
        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

        @Override
        public Command get() {
            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);
            Basket basket = TestBasketBuilder.createBasket(rnd);
            Command command = new CreateComplexAggregateCommand(id, basket);
            return command;
        }

        private int xorShift(int x) {
            x ^= x << 6;
            x ^= x >>> 21;
            x ^= (x << 7);
            return x;
        }
    }


    static class CreateMutableComplexAggregateCommandSupplier implements Supplier<Command> {
        int rnd = ((this.hashCode() ^ (int) System.nanoTime()));

        @Override
        public Command get() {
            rnd = xorShift(rnd);
            MyId id = MyId.from("" + rnd);
            Basket2 basket = TestBasketBuilder.createBasket2(rnd);
            Command command = new CreateMutableComplexAggregateCommand(id, basket);
            return command;
        }

        private int xorShift(int x) {
            x ^= x << 6;
            x ^= x >>> 21;
            x ^= (x << 7);
            return x;
        }
    }

    static class UpdateBasketConstituentEventSupplier implements Supplier<Command> {

        List<ComplexMutableAggregate> aggregates;
        int pos = -1;

        UpdateBasketConstituentEventSupplier(List<ComplexMutableAggregate> aggregates) {
            this.aggregates = aggregates;
        }

        @Override
        public Command get() {

            pos = ++pos < aggregates.size() ? pos : 0;

            ComplexMutableAggregate aggregate = aggregates.get(pos);

            Basket2 basket2 = aggregate.getBasket();

            BasketConstituent2 constituent2 = basket2.getBasketConstituents().get(
                    ThreadLocalRandom.current().nextInt(basket2.getBasketConstituents().size()));

            int adjustedShares = constituent2.getAdjustedShares()+1;

            return new UpdateComplexAggregateCommand(MyId.from(aggregate.getId()),
                    constituent2.getRic(),adjustedShares);
        }
    }
}
