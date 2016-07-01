package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.codegen.Immutable;
import com.dmc.d1.cqrs.codegen.Mutable;
import com.dmc.d1.cqrs.util.StateEquals;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BasketMutable implements Basket, Mutable {
  static final String CLASS_NAME = "com.dmc.d1.test.domain.Basket";

  private final String ric;

  private final LocalDate tradeDate;

  private final int divisor;

  private final Security security;

  private final List<BasketConstituent> basketConstituents;

  private final Map<String, BasketConstituent> basketConstituents2;

  public BasketMutable(String ric, LocalDate tradeDate, int divisor, Security security, List<BasketConstituent> basketConstituents, Map<String, BasketConstituent> basketConstituents2) {
    this.ric = ric;
    this.tradeDate = tradeDate;
    this.divisor = divisor;
    this.security = immutableSecurity(security);
    this.basketConstituents = immutableBasketConstituents(basketConstituents);
    this.basketConstituents2 = immutableBasketConstituents2(basketConstituents2);
  }

  @Override
  public String getRic() {
    return ric;
  }

  @Override
  public LocalDate getTradeDate() {
    return tradeDate;
  }

  @Override
  public int getDivisor() {
    return divisor;
  }

  private Security immutableSecurity(Security security) {
    return security instanceof Immutable ? security : SecurityBuilder.copyBuilder(security).buildImmutable();
  }

  @Override
  public Security getSecurity() {
    return security;
  }

  private List<BasketConstituent> immutableBasketConstituents(List<BasketConstituent> basketConstituents) {
    List<BasketConstituent> newList = new ArrayList();
    basketConstituents.forEach((val) -> newList.add(val instanceof Immutable ? val : BasketConstituentBuilder.copyBuilder(val).buildImmutable()));
    return newList;
  }

  @Override
  public List<BasketConstituent> getBasketConstituents() {
    return new ArrayList(basketConstituents);
  }

  private Map<String, BasketConstituent> immutableBasketConstituents2(Map<String, BasketConstituent> basketConstituents2) {
    Map<String, BasketConstituent> newMap = new HashMap();
    basketConstituents2.forEach((key, val) -> newMap.put(key, val instanceof Immutable ? val : BasketConstituentBuilder.copyBuilder(val).buildImmutable()));
    return newMap;
  }

  @Override
  public Map<String, BasketConstituent> getBasketConstituents2() {
    return new HashMap(basketConstituents2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasketMutable that = (BasketMutable)o;
    if (ric != null ? !ric.equals(that.ric) : that.ric != null) return false;
    if (tradeDate != null ? !tradeDate.equals(that.tradeDate) : that.tradeDate != null) return false;
    if (divisor!=that.divisor) return false;
    if (security != null ? !security.equals(that.security) : that.security != null) return false;
    if (basketConstituents != null ? !basketConstituents.equals(that.basketConstituents) : that.basketConstituents != null) return false;
    if (basketConstituents2 != null ? !basketConstituents2.equals(that.basketConstituents2) : that.basketConstituents2 != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = ric != null ? ric.hashCode() : 0;
    result = 31 * result + (tradeDate != null ? tradeDate.hashCode() : 0);
    result = 31 * result + divisor;
    result = 31 * result + (security != null ? security.hashCode() : 0);
    result = 31 * result + (basketConstituents != null ? basketConstituents.hashCode() : 0);
    result = 31 * result + (basketConstituents2 != null ? basketConstituents2.hashCode() : 0);
    return result;
  }

  @Override
  public boolean stateEquals(Basket o) {
    if (ric != null ? !ric.equals(o.getRic()) : o.getRic() != null) return false;
    if (tradeDate != null ? !tradeDate.equals(o.getTradeDate()) : o.getTradeDate() != null) return false;
    if (o.getDivisor()!=this.divisor) return false;
    if (security != null ? !security.stateEquals(o.getSecurity()) : o.getSecurity() != null) return false;
    if (basketConstituents != null ? !StateEquals.listStateEquals(basketConstituents,o.getBasketConstituents()) : o.getBasketConstituents() != null) return false;
    if (basketConstituents2 != null ? !StateEquals.mapStateEquals(basketConstituents2,o.getBasketConstituents2()) : o.getBasketConstituents2() != null) return false;
    return true;
  }
}
