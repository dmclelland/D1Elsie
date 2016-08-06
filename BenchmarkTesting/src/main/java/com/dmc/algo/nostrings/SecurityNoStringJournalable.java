package com.dmc.algo.nostrings;

import com.dmc.algo.Ric;
import com.dmc.d1.cqrs.codegen.Journalable;
import com.dmc.d1.domain.AssetType;
import com.dmc.d1.test.domain.Security;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

import java.util.function.Supplier;

public class SecurityNoStringJournalable implements Journalable, Marshallable {

  public SecurityNoStringJournalable(){};

  public Ric ric;

  public int adv20Day;

  public AssetType assetType;

  public Ric getName() {
    return ric;
  }


  public int getAdv20Day() {
    return adv20Day;
  }


  public AssetType getAssetType() {
    return assetType;
  }

  @Override
  public void readMarshallable(WireIn wireIn) throws IORuntimeException {
    wireIn.read(()-> "ric").asEnum(Ric.class, this, (o, b) -> o.ric = b);
    wireIn.read(()-> "adv20Day").int32(this, (o, b) -> o.adv20Day = b);
    wireIn.read(()-> "assetType").asEnum(AssetType.class, this, (o, b) -> o.assetType = b);
  }

  @Override
  public void writeMarshallable(WireOut wireOut) {
    wireOut.write(()-> "ric").asEnum(ric);
    wireOut.write(()-> "adv20Day").int32(adv20Day);
    wireOut.write(()-> "assetType").asEnum(this.assetType);
  }


}
