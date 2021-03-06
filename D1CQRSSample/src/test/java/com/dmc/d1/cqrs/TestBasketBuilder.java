package com.dmc.d1.cqrs;

import com.dmc.d1.domain.AssetType;
import com.dmc.d1.domain.IndexRic;
import com.dmc.d1.domain.StockRic;
import com.dmc.d1.sample.domain.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Created By davidclelland on 29/06/2016.
 */
class TestBasketBuilder {

    static Basket createBasket(int rnd, int maxNoOfConstituents) {

        if (maxNoOfConstituents > 500)
            maxNoOfConstituents = 500;

        IndexRic ric = securities[Math.abs(rnd) % 4];

        return BasketBuilder.startBuilding()
                .tradeDate(LocalDate.now())
                .divisor(divisor(rnd))
                .ric(ric)
                .security(security(rnd))
                .basketConstituents(constituents(ric, maxNoOfConstituents))
                .buildJournalable();
    }

    static Basket2 createBasket2(int rnd, int maxNoOfConstituents) {

        IndexRic ric = securities[Math.abs(rnd) % 4];

        return Basket2Builder.startBuilding()
                .tradeDate(LocalDate.now())
                .divisor(divisor(rnd))
                .ric(ric)
                .security(security(rnd))
                .basketConstituents2(constituents2(ric, maxNoOfConstituents))
                .lastUpdated(LocalDate.now())
                .buildJournalable();
    }


    static int[] divisors = new int[4];

    static {
        divisors[0] = 50000;
        divisors[1] = 1000;
        divisors[2] = 5000;
        divisors[3] = 10000;

    }


    static IndexRic[] securities = new IndexRic[4];

    static {
        securities[0] = IndexRic.X8PS;
        securities[1] = IndexRic.X5PS;
        securities[2] = IndexRic.X6PS;
        securities[3] = IndexRic.X7PS;
    }

    static int noOfAssetTypes = AssetType.values().length;

    static AssetType assetType(int rnd) {

        return AssetType.values()[Math.abs(rnd) % noOfAssetTypes];
    }


    static IndexRic ric(int rnd) {

        return securities[Math.abs(rnd) % 4];
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

    static Random RANDOM = new Random();

    //static Map<IndexRic, List<BasketConstituent>> constituentsMap = new HashMap<>();


    //static Map<IndexRic, Map<StockRic, BasketConstituent2>> constituentsMap2 = new HashMap<>();


    static StockRic[] constituents = StockRic.values();


    static List<BasketConstituent> constituents(IndexRic ric, int noOfConstituents) {
        //if (constituentsMap.containsKey(ric))
        //return constituentsMap.get(ric);

        int rnd = RANDOM.nextInt(noOfConstituents - 1) + 1;
        List<BasketConstituent> lst = new ArrayList<>();
        for (int i = 1; i <= rnd; i++) {
            StockRic constituentRic = constituents[i];
            lst.add(BasketConstituentBuilder.startBuilding().adjustedShares(i).ric(constituentRic).buildJournalable());
        }

        //constituentsMap.put(ric, lst);

        return lst;

    }


    static Map<StockRic, BasketConstituent2> constituents2(IndexRic ric, int noOfConstituents) {
//        if (constituentsMap2.containsKey(ric))
//            return constituentsMap2.get(ric);

        LocalDate now = LocalDate.now();
        int rnd = RANDOM.nextInt(noOfConstituents - 1) + 1;
        Map<StockRic, BasketConstituent2> map = new HashMap<>();
        for (int i = 1; i <= rnd; i++) {
            StockRic constituentRic = constituents[i];
            map.put(constituentRic, BasketConstituent2Builder.startBuilding()
                    .initialAdjustedShares(i)
                    .adjustedShares(i)
                    .initialAdjustedShares(i)
                    .ric(constituentRic)
                    .lastUpdated(now)
                    .buildJournalable());
        }

        //constituentsMap2.put(ric, map);

        return map;
    }


}
