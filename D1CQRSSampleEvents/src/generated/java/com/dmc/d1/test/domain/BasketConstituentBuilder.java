package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.util.StateEquals;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import java.lang.Override;
import java.lang.String;
import java.lang.ThreadLocal;
import java.util.HashMap;
import java.util.Map;

public class BasketConstituentBuilder implements StateEquals<BasketConstituent> {
  private static final ThreadLocal<BasketConstituentBuilder> THREAD_LOCAL = new ThreadLocal<BasketConstituentBuilder>() {
    @Override
    protected BasketConstituentBuilder initialValue() {
      return new BasketConstituentBuilder();
    }
  };

  Map<String, BasketConstituentImmutable> basketConstituentCache = new HashMap<String, BasketConstituentImmutable>();

  private String ric;

  private int adjustedShares;

  private BasketConstituentBuilder() {
  }

  public BasketConstituentBuilder ric(String ric) {
    THREAD_LOCAL.get().ric = ric;
    return THREAD_LOCAL.get();
  }

  public BasketConstituentBuilder adjustedShares(int adjustedShares) {
    THREAD_LOCAL.get().adjustedShares = adjustedShares;
    return THREAD_LOCAL.get();
  }

  public static BasketConstituentBuilder startBuilding() {
    if (THREAD_LOCAL.get() == null) {
      THREAD_LOCAL.set(new BasketConstituentBuilder());
    }
    BasketConstituentBuilder builder = THREAD_LOCAL.get();
    builder.ric = null;
    builder.adjustedShares = 0;
    return builder;
  }

  public BasketConstituent buildJournalable() {
    BasketConstituentJournalable journalable =  BasketConstituentJournalable.newInstanceFactory().get();
    journalable.setPooled(false);
    journalable.set(ric,adjustedShares);return journalable;
  }

  public BasketConstituent buildPooledJournalable() {
    BasketConstituentJournalable journalable = ThreadLocalObjectPool.allocateObject(BasketConstituentJournalable.CLASS_NAME);
    journalable.setPooled(true);
    journalable.set(ric,adjustedShares);return journalable;
  }

  public BasketConstituent buildImmutable() {
    BasketConstituentImmutable immutable = basketConstituentCache.get(ric);
    if (immutable == null || !this.stateEquals(immutable)) {immutable = new BasketConstituentImmutable(ric,adjustedShares);basketConstituentCache.put(ric, immutable);}return immutable;
  }

  @Override
  public boolean stateEquals(BasketConstituent o) {
    if (ric != null ? !ric.equals(o.getRic()) : o.getRic() != null) return false;
    if (o.getAdjustedShares()!=this.adjustedShares) return false;
    return true;
  }

  public static BasketConstituentBuilder copyBuilder(BasketConstituent basketConstituent) {
    BasketConstituentBuilder builder = THREAD_LOCAL.get();
    builder.ric(basketConstituent.getRic());
    builder.adjustedShares(basketConstituent.getAdjustedShares());
    return builder;
  }
}
