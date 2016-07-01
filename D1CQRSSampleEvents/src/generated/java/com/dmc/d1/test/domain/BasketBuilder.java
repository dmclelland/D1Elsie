package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.util.StateEquals;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import java.lang.Override;
import java.lang.String;
import java.lang.ThreadLocal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BasketBuilder implements StateEquals<Basket> {
  private static final ThreadLocal<BasketBuilder> THREAD_LOCAL = new ThreadLocal<BasketBuilder>() {
    @Override
    protected BasketBuilder initialValue() {
      return new BasketBuilder();
    }
  };

  Map<String, BasketImmutable> basketCache = new HashMap<String, BasketImmutable>();

  private String ric;

  private LocalDate tradeDate;

  private int divisor;

  private Security security;

  private List<BasketConstituent> basketConstituents = new ArrayList();

  private Map<String, BasketConstituent> basketConstituents2 = new HashMap();

  private BasketBuilder() {
  }

  public BasketBuilder ric(String ric) {
    THREAD_LOCAL.get().ric = ric;
    return THREAD_LOCAL.get();
  }

  public BasketBuilder tradeDate(LocalDate tradeDate) {
    THREAD_LOCAL.get().tradeDate = tradeDate;
    return THREAD_LOCAL.get();
  }

  public BasketBuilder divisor(int divisor) {
    THREAD_LOCAL.get().divisor = divisor;
    return THREAD_LOCAL.get();
  }

  public BasketBuilder security(Security security) {
    THREAD_LOCAL.get().security = security;
    return THREAD_LOCAL.get();
  }

  public BasketBuilder basketConstituents(List<BasketConstituent> basketConstituents) {
    THREAD_LOCAL.get().basketConstituents = basketConstituents;
    return THREAD_LOCAL.get();
  }

  public BasketBuilder basketConstituents2(Map<String, BasketConstituent> basketConstituents2) {
    THREAD_LOCAL.get().basketConstituents2 = basketConstituents2;
    return THREAD_LOCAL.get();
  }

  public static BasketBuilder startBuilding() {
    if (THREAD_LOCAL.get() == null) {
      THREAD_LOCAL.set(new BasketBuilder());
    }
    BasketBuilder builder = THREAD_LOCAL.get();
    builder.ric = null;
    builder.tradeDate = null;
    builder.divisor = 0;
    builder.security = null;
    builder.basketConstituents = new ArrayList();
    builder.basketConstituents2 = new HashMap();
    return builder;
  }

  public Basket buildJournalable() {
    BasketJournalable journalable =  BasketJournalable.newInstanceFactory().get();
    journalable.setPooled(false);
    journalable.set(ric,tradeDate,divisor,security,basketConstituents,basketConstituents2);return journalable;
  }

  public Basket buildPooledJournalable() {
    BasketJournalable journalable = ThreadLocalObjectPool.allocateObject(BasketJournalable.CLASS_NAME);
    journalable.setPooled(true);
    journalable.set(ric,tradeDate,divisor,security,basketConstituents,basketConstituents2);return journalable;
  }

  public Basket buildImmutable() {
    BasketImmutable immutable = basketCache.get(ric);
    if (immutable == null || !this.stateEquals(immutable)) {immutable = new BasketImmutable(ric,tradeDate,divisor,security,basketConstituents,basketConstituents2);basketCache.put(ric, immutable);}return immutable;
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

  public static BasketBuilder copyBuilder(Basket basket) {
    BasketBuilder builder = THREAD_LOCAL.get();
    builder.ric(basket.getRic());
    builder.tradeDate(basket.getTradeDate());
    builder.divisor(basket.getDivisor());
    builder.security(basket.getSecurity());
    builder.basketConstituents(basket.getBasketConstituents());
    builder.basketConstituents2(basket.getBasketConstituents2());
    return builder;
  }
}
