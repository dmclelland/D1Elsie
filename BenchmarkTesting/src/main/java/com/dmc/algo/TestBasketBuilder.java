package com.dmc.algo;

import com.dmc.algo.nostrings.BasketConstituentNoStringJournalable;
import com.dmc.algo.nostrings.BasketNoStringJournalable;
import com.dmc.algo.nostrings.SecurityNoStringJournalable;
import com.dmc.d1.domain.AssetType;
import com.dmc.d1.sample.domain.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Created By davidclelland on 29/06/2016.
 */
class TestBasketBuilder {

    static Basket createBasket(int rnd, int basketSize) {

        String ric = securities[Math.abs(rnd) % 4];

        return BasketBuilder.startBuilding()
                .tradeDate(LocalDate.now())
                .divisor(divisor(rnd))
                .ric(ric)
                .security(security(rnd))
                .basketConstituents(constituents(ric, basketSize))
                .buildJournalable();
    }


    static BasketNoStringJournalable createBasketNoStrings(int rnd, int basketSize) {

        Ric ric = securitiesNoStrings[Math.abs(rnd) % 4];

        BasketNoStringJournalable b = new BasketNoStringJournalable();
        b.ric = ric;
        b.divisor = divisor(rnd);
        b.security = securityNoString(rnd);
        b.tradeDate = LocalDate.now();
        b.basketConstituents = constituentsNoString(ric, basketSize);


        return b;
    }


    static int[] divisors = new int[4];

    static {
        divisors[0] = 50000;
        divisors[1] = 1000;
        divisors[2] = 5000;
        divisors[3] = 10000;

    }


    static String[] securities = new String[4];

    static {
        securities[0] = "X8PS.DE";
        securities[1] = "X5PS.DE";
        securities[2] = "X6PS.DE";
        securities[3] = "X7PS.DE";
    }


    static Ric[] securitiesNoStrings = new Ric[4];

    static {
        securitiesNoStrings[0] = Ric.RIC1;
        securitiesNoStrings[1] = Ric.RIC2;
        securitiesNoStrings[2] = Ric.RIC3;
        ;
        securitiesNoStrings[3] = Ric.RIC4;
    }


    static int noOfAssetTypes = AssetType.values().length;

    static AssetType assetType(int rnd) {

        return AssetType.values()[Math.abs(rnd) % noOfAssetTypes];
    }


    static String ric(int rnd) {

        return securities[Math.abs(rnd) % 4];
    }

    static Ric ricNoString(int rnd) {

        return securitiesNoStrings[Math.abs(rnd) % 4];
    }


    static int divisor(int rnd) {

        return divisors[Math.abs(rnd) % 4];
    }


    static Security security(int rnd) {

        return SecurityBuilder.startBuilding()
                .name(ric(rnd))
                .adv20Day(12000)
                .assetType(assetType(rnd))
                .buildJournalable();
    }

    static SecurityNoStringJournalable securityNoString(int rnd) {

        SecurityNoStringJournalable securityNoStringJournalable = new SecurityNoStringJournalable();
        securityNoStringJournalable.ric = ricNoString(rnd);

        securityNoStringJournalable.adv20Day = 12000;
        securityNoStringJournalable.assetType = assetType(rnd);


        return securityNoStringJournalable;
    }


    static Random RANDOM = new Random();

    static Map<String, List<BasketConstituent>> constituentsMap = new HashMap<>();

    static Map<Ric, List<BasketConstituentNoStringJournalable>> constituentsMapNoStrings = new HashMap<>();

    static String[] constituents = new String[10000];

    static {
        for (int i = 0; i < constituents.length; i++) {
            constituents[i] = "ric" + i;
        }
    }


    static List<BasketConstituent> constituents(String ric, int basketSize) {
        if (constituentsMap.containsKey(ric))
            return constituentsMap.get(ric);

        int rnd = RANDOM.nextInt(basketSize - 1) + 1;
        List<BasketConstituent> lst = new ArrayList<>();
        for (int i = 1; i <= rnd; i++) {
            String constituentRic = constituents[i];
            lst.add(BasketConstituentBuilder.startBuilding().adjustedShares(i).ric(constituentRic).buildJournalable());
        }
        constituentsMap.put(ric, lst);
        return lst;
    }

    static List<BasketConstituentNoStringJournalable> constituentsNoString(Ric ric, int basketSize) {
        if (constituentsMapNoStrings.containsKey(ric))
            return constituentsMapNoStrings.get(ric);

        int rnd = RANDOM.nextInt(basketSize - 1) + 1;
        List<BasketConstituentNoStringJournalable> lst = new ArrayList<>();
        for (int i = 1; i <= rnd; i++) {
            Ric constituentRic = Ric.values()[rnd];
            BasketConstituentNoStringJournalable c = new BasketConstituentNoStringJournalable();
            c.adjustedShares = i;
            c.ric = constituentRic;

            lst.add(c);
        }

        constituentsMapNoStrings.put(ric, lst);

        return lst;

    }

}
