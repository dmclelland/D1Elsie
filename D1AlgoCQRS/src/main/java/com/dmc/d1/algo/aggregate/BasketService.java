package com.dmc.d1.algo.aggregate;

import com.dmc.d1.domain.InstrumentId;

/**
 * Created By davidclelland on 02/07/2016.
 */
public interface BasketService {

    Basket createBasket(InstrumentId id, int qty);



}
