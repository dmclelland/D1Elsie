package com.dmc.d1.test.domain;

import com.dmc.d1.cqrs.codegen.Mutable;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;

class SecurityMutable implements Security, Mutable {
  static final String CLASS_NAME = "com.dmc.d1.test.domain.Security";

  private final String name;

  private final int adv20Day;

  public SecurityMutable(String name, int adv20Day) {
    this.name = name;
    this.adv20Day = adv20Day;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SecurityMutable that = (SecurityMutable)o;
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

  @Override
  public boolean stateEquals(Security o) {
    if (name != null ? !name.equals(o.getName()) : o.getName() != null) return false;
    if (o.getAdv20Day()!=this.adv20Day) return false;
    return true;
  }
}
