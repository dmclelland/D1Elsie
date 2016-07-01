package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.util.StateEquals;
import java.lang.String;

public interface Security extends StateEquals<Security> {
  String getName();

  int getAdv20Day();
}
