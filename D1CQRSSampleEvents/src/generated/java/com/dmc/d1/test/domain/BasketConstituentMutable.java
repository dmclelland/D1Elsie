package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.codegen.Mutable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;

class BasketConstituentMutable implements BasketConstituent, Mutable {
  static final String CLASS_NAME = "com.dmc.d1.test.domain.BasketConstituent";

  private final String ric;

  private final int adjustedShares;

  public BasketConstituentMutable(String ric, int adjustedShares) {
    this.ric = ric;
    this.adjustedShares = adjustedShares;
  }

  @Override
  public String getRic() {
    return ric;
  }

  @Override
  public int getAdjustedShares() {
    return adjustedShares;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasketConstituentMutable that = (BasketConstituentMutable)o;
    if (ric != null ? !ric.equals(that.ric) : that.ric != null) return false;
    if (adjustedShares!=that.adjustedShares) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = ric != null ? ric.hashCode() : 0;
    result = 31 * result + adjustedShares;
    return result;
  }

  @Override
  public boolean stateEquals(BasketConstituent o) {
    if (ric != null ? !ric.equals(o.getRic()) : o.getRic() != null) return false;
    if (o.getAdjustedShares()!=this.adjustedShares) return false;
    return true;
  }
}
