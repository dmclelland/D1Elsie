package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.util.StateEquals;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import com.dmc.d1.domain.AssetType;
import java.lang.Override;
import java.lang.String;
import java.lang.ThreadLocal;
import java.util.HashMap;
import java.util.Map;

public class SecurityBuilder implements StateEquals<Security> {
  private static final ThreadLocal<SecurityBuilder> THREAD_LOCAL = new ThreadLocal<SecurityBuilder>() {
    @Override
    protected SecurityBuilder initialValue() {
      return new SecurityBuilder();
    }
  };

  Map<String, SecurityImmutable> securityCache = new HashMap<String, SecurityImmutable>();

  private String name;

  private int adv20Day;

  private AssetType assetType;

  private SecurityBuilder() {
  }

  public SecurityBuilder name(String name) {
    THREAD_LOCAL.get().name = name;
    return THREAD_LOCAL.get();
  }

  public SecurityBuilder adv20Day(int adv20Day) {
    THREAD_LOCAL.get().adv20Day = adv20Day;
    return THREAD_LOCAL.get();
  }

  public SecurityBuilder assetType(AssetType assetType) {
    THREAD_LOCAL.get().assetType = assetType;
    return THREAD_LOCAL.get();
  }

  public static SecurityBuilder startBuilding() {
    if (THREAD_LOCAL.get() == null) {
      THREAD_LOCAL.set(new SecurityBuilder());
    }
    SecurityBuilder builder = THREAD_LOCAL.get();
    builder.name = null;
    builder.adv20Day = 0;
    builder.assetType = null;
    return builder;
  }

  public Security buildJournalable() {
    SecurityJournalable journalable =  SecurityJournalable.newInstanceFactory().get();
    journalable.setPooled(false);
    journalable.set(name,adv20Day,assetType);return journalable;
  }

  public Security buildPooledJournalable() {
    SecurityJournalable journalable = ThreadLocalObjectPool.allocateObject(SecurityJournalable.CLASS_NAME);
    journalable.setPooled(true);
    journalable.set(name,adv20Day,assetType);return journalable;
  }

  public Security buildImmutable() {
    SecurityImmutable immutable = securityCache.get(name);
    if (immutable == null || !this.stateEquals(immutable)) {immutable = new SecurityImmutable(name,adv20Day,assetType);securityCache.put(name, immutable);}return immutable;
  }

  @Override
  public boolean stateEquals(Security o) {
    if (name != null ? !name.equals(o.getName()) : o.getName() != null) return false;
    if (o.getAdv20Day()!=this.adv20Day) return false;
    if (assetType != null ? assetType!=o.getAssetType() : o.getAssetType() != null) return false;
    return true;
  }

  public static SecurityBuilder copyBuilder(Security security) {
    SecurityBuilder builder = THREAD_LOCAL.get();
    builder.name(security.getName());
    builder.adv20Day(security.getAdv20Day());
    builder.assetType(security.getAssetType());
    return builder;
  }
}
