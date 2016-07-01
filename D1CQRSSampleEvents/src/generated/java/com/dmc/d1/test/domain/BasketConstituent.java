package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.util.StateEquals;
import java.lang.String;

public interface BasketConstituent extends StateEquals<BasketConstituent> {
  String getRic();

  int getAdjustedShares();
}
