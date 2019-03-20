package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TradingAlgo {

    /*
     * input file path for trade data
     */
    public static String INPUT_FILE_PATH = "/home/naila/Downloads/input.csv";
    /*
     * Folder path for writing output files
     */
    public static String OUTPUT_FOLDER_PATH = "/home/naila/Desktop/";
    /*
     * delimeter for creating csv's
     */
    public static String DELIMETER = ",";
    /*
     * Date format of input file
     */
    // Date format to be changed according to date format in the input file
    public static String DATE_FORMAT = "dd-MMM-yyyy";

    public static void main(final String args[]) {

        Map<String, Double> dateToBuyOrderValueMap = new HashMap<>();
        Map<String, Double> dateToSellOrderValueMap = new HashMap<>();
        Map<String, Map<String, userTransaction>> dateToUserToBuyTransMap = new HashMap<>();
        Map<String, Map<String, userTransaction>> dateToUserToSellTransMap = new HashMap<>();

        try {

            /*
             * tradeList after reading data from CSV file with input file path
             * = INPUT_FILE_PATH
             */
            List<TradeEntity> tradeList = readCSV(TradingAlgo.INPUT_FILE_PATH);

            for (TradeEntity trade : tradeList) {

                String finalSettlementDate = getFinalSettlementDay(trade.settDate, trade.currency);
                double transactionPrice = trade.units * trade.price * trade.rate;

                if (trade.isbuy) {

                    double priceToBuy = 0;
                    if (dateToBuyOrderValueMap.get(finalSettlementDate) != null) {
                        priceToBuy = dateToBuyOrderValueMap.get(finalSettlementDate);
                    }
                    dateToBuyOrderValueMap.put(finalSettlementDate, priceToBuy + transactionPrice);

                    updateUserTransactionMap(dateToUserToBuyTransMap, finalSettlementDate, trade.name, transactionPrice);

                } else {

                    double priceToSell = 0;
                    if (dateToSellOrderValueMap.get(finalSettlementDate) != null) {
                        priceToSell = dateToSellOrderValueMap.get(finalSettlementDate);
                    }
                    dateToSellOrderValueMap.put(finalSettlementDate, priceToSell + transactionPrice);

                    updateUserTransactionMap(dateToUserToSellTransMap, finalSettlementDate, trade.name, transactionPrice);
                }

            }

            
            PrintDataInCSV(dateToBuyOrderValueMap, TradingAlgo.OUTPUT_FOLDER_PATH + "buy");
            PrintDataInCSV(dateToSellOrderValueMap, TradingAlgo.OUTPUT_FOLDER_PATH + "sell");
            PrintDataInCSVForUser(dateToUserToBuyTransMap, TradingAlgo.OUTPUT_FOLDER_PATH + "buyUserData");
            PrintDataInCSVForUser(dateToUserToSellTransMap, TradingAlgo.OUTPUT_FOLDER_PATH + "sellUserData");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void updateUserTransactionMap(final Map<String, Map<String, userTransaction>> transacData, final String date,
        final String name, final double price) {

        transacData.computeIfAbsent(date, k -> new HashMap<>()).computeIfAbsent(name, k -> new userTransaction(name, 0.0));

        double existingValue = transacData.get(date).get(name).value;
        double modifiedValue = existingValue + price;

        transacData.get(date).get(name).value = modifiedValue;
    }

    /**
     * this method will return the final settlement date if the settlement date
     * is working day then it will return the same day otherwise next working
     * day
     *
     * @param dateInString
     * @param currency
     * @return
     * @throws Exception
     */
    public static String getFinalSettlementDay(final String dateInString, final String currency) throws Exception {

        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat(TradingAlgo.DATE_FORMAT);
            Calendar cal = Calendar.getInstance();

            Date date = dateFormat.parse(dateInString);

            /*
             * 0 = SUnday, 1= Monday
             */
            int dayofWeek = date.getDay();

            int dayToBeadded = 0;

            if (currency.equalsIgnoreCase("AED") || currency.equalsIgnoreCase("SAR")) {

                if (dayofWeek == 5 || dayofWeek == 6) {
                    dayToBeadded = 7 - dayofWeek;
                }
            } else {

                if (dayofWeek == 6) {
                    dayToBeadded = 2;
                } else if (dayofWeek == 6) {
                    dayToBeadded = 1;
                }
            }

            cal.setTime(date);
            cal.add(Calendar.DATE, dayToBeadded);

            String workingDay = dateFormat.format(cal.getTime());

            return workingDay;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }

    /**
     * printing data into CSV file for total buy and sell values
     *
     * @param data
     * @param filePath
     */
    public static void PrintDataInCSV(final Map<String, Double> data, final String filePath) {

        List<String> lineList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : data.entrySet()) {

            String line = entry.getKey() + TradingAlgo.DELIMETER + entry.getValue();
            lineList.add(line);
        }

        writeCSV(lineList, filePath);
    }

    /**
     * this method is sorting the user transaction value as well as printing
     * them into CSV file
     *
     * @param data
     * @param filePath
     */
    public static void PrintDataInCSVForUser(final Map<String, Map<String, userTransaction>> data, final String filePath) {

        List<String> lineList = new ArrayList<>();

        for (Map.Entry<String, Map<String, userTransaction>> entry : data.entrySet()) {

            String date = entry.getKey();
            List<userTransaction> list = new ArrayList<>();
            for (Map.Entry<String, userTransaction> innerEntry : entry.getValue().entrySet()) {

                userTransaction transaction = innerEntry.getValue();
                list.add(transaction);
            }

            Collections.sort(list, new sort());

            int rank = 1;
            for (userTransaction transac : list) {
                String line = date + TradingAlgo.DELIMETER + rank++ + TradingAlgo.DELIMETER + transac.name + TradingAlgo.DELIMETER
                    + transac.value;
                lineList.add(line);
            }

        }

        writeCSV(lineList, filePath);
    }

    public static List<TradeEntity> readCSV(final String filePath) throws Exception {

        List<TradeEntity> tradeList = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader(filePath));

            while (true) {

                String line = br.readLine();

                if (line == null) {
                    break;
                }

                String[] data = line.split(",");

                TradeEntity entity = new TradeEntity();

                entity.name = data[0];
                entity.isbuy = false;
                if (data[1].equalsIgnoreCase("B")) {
                    entity.isbuy = true;
                }
                entity.rate = Double.parseDouble(data[2]);
                entity.currency = data[3];
                entity.instDate = data[4];
                entity.settDate = data[5];
                entity.units = Double.parseDouble(data[6]);
                entity.price = Double.parseDouble(data[7]);

                tradeList.add(entity);
            }

            br.close();
            return tradeList;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }

    }

    public static void writeCSV(final List<String> data, final String fileName) {

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

            for (String line : data) {

                bw.write(line);
                bw.newLine();
            }

            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class TradeEntity {

        public String name;
        public boolean isbuy;
        public double rate;
        public String currency;
        public String instDate;
        public String settDate;
        public double units;
        public double price;
    }

    static class userTransaction {

        public userTransaction(final String name, final Double value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String name;
        public Double value;
    }

    /**
     * comparator method for sorting the transactions
     *
     * @author naila
     *
     */
    static class sort implements Comparator<userTransaction> {

        @Override
        public int compare(final userTransaction a, final userTransaction b) {

            if (a == null || a.value == null) {
                return -1;
            }

            if (b == null || b.value == null) {
                return 1;
            }

            return (int) Math.ceil(b.value - a.value);
        }
    }
}
