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

class SecurityJournalable implements Security, Journalable, Resettable, Marshallable, Poolable {
  static final String CLASS_NAME = "com.dmc.d1.test.domain.Security";

  private static final Supplier<SecurityJournalable> SUPPLIER = SecurityJournalable::new;

  private boolean pooled;

  private String name;

  private int adv20Day;

  private SecurityJournalable() {
    ;
  }

  static Supplier<SecurityJournalable> newInstanceFactory() {
    return SUPPLIER;
  }

  public void setPooled(boolean pooled) {
    this.pooled = pooled;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getAdv20Day() {
    return adv20Day;
  }

  @Override
  public String getClassName() {
    return CLASS_NAME;
  }

  @Override
  public void readMarshallable(WireIn wireIn) throws IORuntimeException {
    wireIn.read(()-> "name").text(this, (o, b) -> o.name = b);
    wireIn.read(()-> "adv20Day").int32(this, (o, b) -> o.adv20Day = b);
  }

  @Override
  public void writeMarshallable(WireOut wireOut) {
    wireOut.write(()-> "name").text(name);
    wireOut.write(()-> "adv20Day").int32(adv20Day);
  }

  void set(String name, int adv20Day) {
    this.name = name;
    this.adv20Day = adv20Day;
  }

  public void reset() {
    this.name = null;
    this.adv20Day = 0;
  }

  @Override
  public boolean stateEquals(Security o) {
    if (name != null ? !name.equals(o.getName()) : o.getName() != null) return false;
    if (o.getAdv20Day()!=this.adv20Day) return false;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SecurityJournalable that = (SecurityJournalable)o;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (adv20Day!=that.adv20Day) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + adv20Day;
    return result;
  }
}