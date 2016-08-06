package com.dmc.algo.nostrings;

import com.dmc.algo.Ric;
import com.dmc.d1.cqrs.codegen.Journalable;
import com.dmc.d1.domain.TradeDirection;
import com.dmc.d1.test.domain.BasketConstituent;
import net.openhft.chronicle.core.io.IORuntimeException;
import net.openhft.chronicle.wire.Marshallable;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;

import java.util.function.Supplier;

public class BasketConstituentNoStringJournalable implements Journalable, Marshallable {
  public Ric ric;

  public int adjustedShares;

  public Ric getRic() {
    return ric;
  }

  public int getAdjustedShares() {
    return adjustedShares;
  }

  @Override
  public void readMarshallable(WireIn wireIn) throws IORuntimeException {
    wireIn.read(()-> "ric").asEnum(Ric.class, this, (o, b) -> o.ric = b);

    wireIn.read(()-> "adjustedShares").int32(this, (o, b) -> o.adjustedShares = b);
  }

  @Override
  public void writeMarshallable(WireOut wireOut) {
    wireOut.write(()-> "ric").asEnum(ric);
    wireOut.write(()-> "adjustedShares").int32(adjustedShares);
  }

}
