package com.dmc.d1.cqrs;

import com.dmc.d1.domain.AssetType;
import com.dmc.d1.sample.domain.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Created By davidclelland on 29/06/2016.
 */
class TestBasketBuilder {

    static Basket createBasket(int rnd) {

        String ric = securities[Math.abs(rnd) % 4];

        return BasketBuilder.startBuilding()
                .tradeDate(LocalDate.now())
                .divisor(divisor(rnd))
                .ric(ric)
                .security(security(rnd))
                .basketConstituents(constituents(ric))
                .buildJournalable();
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

    static int noOfAssetTypes = AssetType.values().length;

    static AssetType assetType(int rnd) {

        return AssetType.values()[Math.abs(rnd) % noOfAssetTypes];
    }


    static String ric(int rnd) {

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

    static Map<String, List<BasketConstituent>> constituentsMap = new HashMap<>();

    static String[] constituents = new String[100];

    static{
        for (int i = 0; i < constituents.length; i++) {
            constituents[i] = "ric" + i;
        }
    }

    static List<BasketConstituent> constituents(String ric) {
        if (constituentsMap.containsKey(ric))
            return constituentsMap.get(ric);

        int rnd = RANDOM.nextInt(99) + 1;
        List<BasketConstituent> lst = new ArrayList<>();
        for (int i = 1; i <= rnd; i++) {
            String constituentRic = constituents[i];
            lst.add(BasketConstituentBuilder.startBuilding().adjustedShares(i).ric(constituentRic).buildJournalable());
        }

        constituentsMap.put(ric, lst);

        return lst;

    }


}