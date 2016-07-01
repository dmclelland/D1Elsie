package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.codegen.Journalable;
import com.dmc.d1.cqrs.util.Poolable;
import com.dmc.d1.cqrs.util.Resettable;
import com.dmc.d1.cqrs.util.StateEquals;
import com.dmc.d1.cqrs.util.ThreadLocalObjectPool;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

class BasketJournalable implements Basket, Journalable, Resettable, Marshallable, Poolable {
  static final String CLASS_NAME = "com.dmc.d1.test.domain.Basket";

  private static final Supplier<BasketJournalable> SUPPLIER = BasketJournalable::new;

  private boolean pooled;

  private String ric;

  private LocalDate tradeDate;

  private int divisor;

  private Security security;

  private List<BasketConstituent> basketConstituents = new ArrayList();

  private Map<String, BasketConstituent> basketConstituents2 = new HashMap();

  private BasketJournalable() {
    ;
  }

  static Supplier<BasketJournalable> newInstanceFactory() {
    return SUPPLIER;
  }

  public void setPooled(boolean pooled) {
    this.pooled = pooled;
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

  private Security journalableSecurity(Security security) {
    return security instanceof Journalable ? security :pooled ?  SecurityBuilder.copyBuilder(security).buildPooledJournalable() : SecurityBuilder.copyBuilder(security).buildJournalable();
  }

  @Override
  public Security getSecurity() {
    return security;
  }

  private List<BasketConstituent> journalableBasketConstituents(List<BasketConstituent> basketConstituents) {
    if (!basketConstituents.isEmpty() && basketConstituents.get(0) instanceof Journalable) {
      return basketConstituents;
    }
    List<BasketConstituent> newList = new ArrayList();
    basketConstituents.forEach((val) -> newList.add(val instanceof Journalable ? val :  pooled ?  BasketConstituentBuilder.copyBuilder(val).buildPooledJournalable() : BasketConstituentBuilder.copyBuilder(val).buildJournalable()));
    return newList;
  }

  @Override
  public List<BasketConstituent> getBasketConstituents() {
    return basketConstituents;
  }

  private Map<String, BasketConstituent> journalableBasketConstituents2(Map<String, BasketConstituent> basketConstituents2) {
    if (!basketConstituents2.isEmpty() && basketConstituents2.values().iterator().next() instanceof Journalable) {
      return basketConstituents2;
    }
    Map<String, BasketConstituent> newMap = new HashMap();
    basketConstituents2.forEach((key, val) -> newMap.put(key, val instanceof Journalable ? val : pooled ?  BasketConstituentBuilder.copyBuilder(val).buildPooledJournalable() : BasketConstituentBuilder.copyBuilder(val).buildJournalable()));
    return newMap;
  }

  @Override
  public Map<String, BasketConstituent> getBasketConstituents2() {
    return basketConstituents2;
  }

  @Override
  public String getClassName() {
    return CLASS_NAME;
  }

  @Override
  public void readMarshallable(WireIn wireIn) throws IORuntimeException {
    wireIn.read(()-> "ric").text(this, (o, b) -> o.ric = b);
    wireIn.read(()-> "tradeDate").date(this, (o, b) -> o.tradeDate = b);
    wireIn.read(()-> "divisor").int32(this, (o, b) -> o.divisor = b);
    Security security = ThreadLocalObjectPool.allocateObject(Security.class.getName());
    wireIn.read(() -> "security").object(security, Security.class);
    this.security=security;
    wireIn.read(()-> "basketConstituents").sequence(this.basketConstituents,(l,v) -> {while (v.hasNextSequenceItem()){BasketConstituent e = ThreadLocalObjectPool.allocateObject(BasketConstituent.class.getName());l.add(v.object(e, BasketConstituent.class));} });
    wireIn.read(()-> "basketConstituents2").sequence(this.basketConstituents2,(l,v) -> {while (v.hasNextSequenceItem()){BasketConstituent e = ThreadLocalObjectPool.allocateObject(BasketConstituent.class.getName());BasketConstituent o = v.object(e,BasketConstituent.class);l.put(o.getRic(), o);} });
  }

  @Override
  public void writeMarshallable(WireOut wireOut) {
    wireOut.write(()-> "ric").text(ric);
    wireOut.write(()-> "tradeDate").date(tradeDate);
    wireOut.write(()-> "divisor").int32(divisor);
    wireOut.write(()-> "security").object(Security.class, this.security);
    wireOut.write(() -> "basketConstituents").sequence(this.basketConstituents);
    wireOut.write(() -> "basketConstituents2").sequence(this.basketConstituents2.values());
  }

  void set(String ric, LocalDate tradeDate, int divisor, Security security, List<BasketConstituent> basketConstituents, Map<String, BasketConstituent> basketConstituents2) {
    this.ric = ric;
    this.tradeDate = tradeDate;
    this.divisor = divisor;
    this.security = journalableSecurity(security);
    this.basketConstituents = journalableBasketConstituents(basketConstituents);
    this.basketConstituents2 = journalableBasketConstituents2(basketConstituents2);
  }

  public void reset() {
    this.ric = null;
    this.tradeDate = null;
    this.divisor = 0;
    this.security = null;
    this.basketConstituents.clear();
    this.basketConstituents2.clear();
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasketJournalable that = (BasketJournalable)o;
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
}
