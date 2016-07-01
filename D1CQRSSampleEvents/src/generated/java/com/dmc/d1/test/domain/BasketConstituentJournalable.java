package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.codegen.Journalable;
import com.dmc.d1.cqrs.util.Poolable;
import com.dmc.d1.cqrs.util.Resettable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.function.Supplier;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

class BasketConstituentJournalable implements BasketConstituent, Journalable, Resettable, Marshallable, Poolable {
  static final String CLASS_NAME = "com.dmc.d1.test.domain.BasketConstituent";

  private static final Supplier<BasketConstituentJournalable> SUPPLIER = BasketConstituentJournalable::new;

  private boolean pooled;

  private String ric;

  private int adjustedShares;

  private BasketConstituentJournalable() {
    ;
  }

  static Supplier<BasketConstituentJournalable> newInstanceFactory() {
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
  public int getAdjustedShares() {
    return adjustedShares;
  }

  @Override
  public String getClassName() {
    return CLASS_NAME;
  }

  @Override
  public void readMarshallable(WireIn wireIn) throws IORuntimeException {
    wireIn.read(()-> "ric").text(this, (o, b) -> o.ric = b);
    wireIn.read(()-> "adjustedShares").int32(this, (o, b) -> o.adjustedShares = b);
  }

  @Override
  public void writeMarshallable(WireOut wireOut) {
    wireOut.write(()-> "ric").text(ric);
    wireOut.write(()-> "adjustedShares").int32(adjustedShares);
  }

  void set(String ric, int adjustedShares) {
    this.ric = ric;
    this.adjustedShares = adjustedShares;
  }

  public void reset() {
    this.ric = null;
    this.adjustedShares = 0;
  }

  @Override
  public boolean stateEquals(BasketConstituent o) {
    if (ric != null ? !ric.equals(o.getRic()) : o.getRic() != null) return false;
    if (o.getAdjustedShares()!=this.adjustedShares) return false;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BasketConstituentJournalable that = (BasketConstituentJournalable)o;
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
}
