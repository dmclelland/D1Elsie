package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.util.StateEquals;
import java.lang.String;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface Basket extends StateEquals<Basket> {
  String getRic();

  LocalDate getTradeDate();

  int getDivisor();

  Security getSecurity();

  List<BasketConstituent> getBasketConstituents();

  Map<String, BasketConstituent> getBasketConstituents2();
}
