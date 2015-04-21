import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.*;

/**
 * Created by den on 14.02.15.
 */
public class Moex {

    public static final String INPUT_FILE_NAME = "moex.csv";
    public static final String OUTPUT_FILE_NAME = "outputData.csv";
    private boolean falseAuction = false;

    private List<AuctionData> buyAuctionList = new ArrayList<AuctionData>();
    private List<AuctionData> sellAuctionList = new ArrayList<AuctionData>();
    private int auctPrice = 0;

    //Comparator for buy array
    private static class AuctionBuyDataComparator implements Comparator<AuctionData> {

        @Override
        public int compare(AuctionData ad1, AuctionData ad2) {
            if (ad1.getTypeAppl() == 'M' && ad2.getTypeAppl() == 'L') return -1;
            else if (ad1.getTypeAppl() == 'L' && ad2.getTypeAppl() == 'M') return 1;
            else if (ad1.getTypeAppl() == 'L' && ad2.getTypeAppl() == 'L' && ad1.getPrice() < ad2.getPrice()) return 1;
            else if (ad1.getTypeAppl() == 'L' && ad2.getTypeAppl() == 'L' && ad1.getPrice() > ad2.getPrice()) return -1;
            return 0;
        }
    }

    //Comparator for sell array
    private static class AuctionSellDataComparator implements Comparator<AuctionData> {

        @Override
        public int compare(AuctionData ad1, AuctionData ad2) {
            if (ad1.getTypeAppl() == 'M' && ad2.getTypeAppl() == 'L') return -1;
            else if (ad1.getTypeAppl() == 'L' && ad2.getTypeAppl() == 'M') return 1;
            else if (ad1.getTypeAppl() == 'L' && ad2.getTypeAppl() == 'L' && ad1.getPrice() > ad2.getPrice()) return 1;
            else if (ad1.getTypeAppl() == 'L' && ad2.getTypeAppl() == 'L' && ad1.getPrice() < ad2.getPrice()) return -1;
            return 0;
        }
    }

    private void writeToCsv(ArrayList<ResultData> resultData) {
        FileWriter fileWriter = null;

        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n").withDelimiter(';');
        try {
            fileWriter = new FileWriter(OUTPUT_FILE_NAME);
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            if (falseAuction)
                csvFilePrinter.printRecord("FAILED");
            else  {
                csvFilePrinter.printRecord("OK", auctPrice, countTotalValue(resultData));
                for (ResultData rd : resultData) {
                    csvFilePrinter.printRecord(rd.getBuyOpInd(), rd.getSellOpInd(), rd.getQuant(), rd.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        try {
            fileWriter.flush();
            fileWriter.close();
            csvFilePrinter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    }

    // This method count the auction value of the result array. The value of one sell/buy operation is price * quantity.
    private long countTotalValue(ArrayList<ResultData> resultAuctionList) {
        long total = 0;
        for (ResultData element : resultAuctionList) {
            total += element.getValue();
        }
        return total;
    }

    private void readFromCsv() {
        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader("[num]","BS","LM","<Volume>","<Price>").withSkipHeaderRecord().withDelimiter(';');
        try {
            fileReader = new FileReader(INPUT_FILE_NAME);
            csvFileParser = new CSVParser(fileReader, csvFileFormat);
            List csvRecords = csvFileParser.getRecords();
            if (csvRecords.isEmpty()) {
                falseAuction = true;
                return;
            }
            int number = 1;
            for (Object record : csvRecords) {
                CSVRecord csvRec = (CSVRecord)record;
                if (csvRec.get(1).charAt(0) == 'B') {
                    if (csvRec.get(4).isEmpty())
                        buyAuctionList.add(new AuctionData(number, csvRec.get(1).charAt(0), csvRec.get(2).charAt(0), Integer.parseInt(csvRec.get(3)), null));
                    else
                        buyAuctionList.add(new AuctionData(number, csvRec.get(1).charAt(0), csvRec.get(2).charAt(0), Integer.parseInt(csvRec.get(3)), Integer.parseInt(csvRec.get(4))));
                }
                else {
                    if (csvRec.get(4).isEmpty())
                        sellAuctionList.add(new AuctionData(number, csvRec.get(1).charAt(0), csvRec.get(2).charAt(0), Integer.parseInt(csvRec.get(3)), null));
                    else
                        sellAuctionList.add(new AuctionData(number, csvRec.get(1).charAt(0), csvRec.get(2).charAt(0), Integer.parseInt(csvRec.get(3)), Integer.parseInt(csvRec.get(4))));
                }
                number++;
            }
            Collections.sort(buyAuctionList, new AuctionBuyDataComparator());
            Collections.sort(sellAuctionList, new AuctionSellDataComparator());
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Merge the buy array and sell array to result array.
    private ArrayList<ResultData> merge(Integer price) {
        if (price == null) {
            falseAuction = true;
            return null;
        }
        ArrayList<ResultData> resultAuctionList = new ArrayList<ResultData>();
        int i = 0, j = 0;
        AuctionData buyAD = new AuctionData(buyAuctionList.get(i).getNumber(), buyAuctionList.get(i).getDirectionAppl(),
                buyAuctionList.get(i).getTypeAppl(), buyAuctionList.get(i).getQuantAllp(), buyAuctionList.get(i).getPrice());
        AuctionData sellAD = new AuctionData(sellAuctionList.get(j).getNumber(),sellAuctionList.get(j).getDirectionAppl(),
                sellAuctionList.get(j).getTypeAppl(),sellAuctionList.get(j).getQuantAllp(),sellAuctionList.get(j).getPrice());
        while(true) {
            if (i == buyAuctionList.size() || j == sellAuctionList.size())
                break;

            if (buyAD.getPrice() == null || buyAD.getTypeAppl() == 'M')
                buyAD.setPrice(price);
            if (sellAD.getPrice() == null || sellAD.getTypeAppl() == 'M')
                sellAD.setPrice(price);

            if (buyAD.getPrice() < price && sellAD.getPrice() <= price) {
                i++;
                if (i == buyAuctionList.size()) continue;
                buyAD = new AuctionData(buyAuctionList.get(i).getNumber(), buyAuctionList.get(i).getDirectionAppl(),
                        buyAuctionList.get(i).getTypeAppl(), buyAuctionList.get(i).getQuantAllp(), buyAuctionList.get(i).getPrice());
                continue;
            }
            else if (sellAD.getPrice() > price && buyAD.getPrice() >= price) {
                j++;
                if (j == sellAuctionList.size()) continue;
                sellAD = new AuctionData(sellAuctionList.get(j).getNumber(),sellAuctionList.get(j).getDirectionAppl(),
                        sellAuctionList.get(j).getTypeAppl(),sellAuctionList.get(j).getQuantAllp(),sellAuctionList.get(j).getPrice());
                continue;
            }
            else if (buyAD.getPrice() < price && sellAD.getPrice() > price) {
                i++;
                j++;
                if (i == buyAuctionList.size() && j == sellAuctionList.size()) continue;
                buyAD = new AuctionData(buyAuctionList.get(i).getNumber(), buyAuctionList.get(i).getDirectionAppl(),
                        buyAuctionList.get(i).getTypeAppl(), buyAuctionList.get(i).getQuantAllp(), buyAuctionList.get(i).getPrice());
                sellAD = new AuctionData(sellAuctionList.get(j).getNumber(),sellAuctionList.get(j).getDirectionAppl(),
                        sellAuctionList.get(j).getTypeAppl(),sellAuctionList.get(j).getQuantAllp(),sellAuctionList.get(j).getPrice());
                continue;
            }

            int dif = Math.abs(buyAD.getQuantAllp() - sellAD.getQuantAllp());
            if (buyAD.getQuantAllp() > sellAD.getQuantAllp()) {
                buyAD.setQuantAllp(dif);
                resultAuctionList.add(new ResultData(buyAD.getNumber(), sellAD.getNumber(), sellAD.getQuantAllp(), sellAD.getQuantAllp()*price ));
                j++;
                if (j == sellAuctionList.size()) continue;
                sellAD = new AuctionData(sellAuctionList.get(j).getNumber(),sellAuctionList.get(j).getDirectionAppl(),
                        sellAuctionList.get(j).getTypeAppl(),sellAuctionList.get(j).getQuantAllp(),sellAuctionList.get(j).getPrice());

            }
            else if (buyAD.getQuantAllp() < sellAD.getQuantAllp()) {
                sellAD.setQuantAllp(dif);
                resultAuctionList.add(new ResultData(buyAD.getNumber(), sellAD.getNumber(), buyAD.getQuantAllp(), buyAD.getQuantAllp()*price ));
                i++;
                if (i == buyAuctionList.size()) continue;
                buyAD = new AuctionData(buyAuctionList.get(i).getNumber(), buyAuctionList.get(i).getDirectionAppl(),
                        buyAuctionList.get(i).getTypeAppl(), buyAuctionList.get(i).getQuantAllp(), buyAuctionList.get(i).getPrice());
            }
            else {
                resultAuctionList.add(new ResultData(buyAD.getNumber(), sellAD.getNumber(), buyAD.getQuantAllp(), buyAD.getQuantAllp()*price ));
                i++;
                j++;
                if (i == buyAuctionList.size() && j == sellAuctionList.size()) continue;
                buyAD = new AuctionData(buyAuctionList.get(i).getNumber(), buyAuctionList.get(i).getDirectionAppl(),
                        buyAuctionList.get(i).getTypeAppl(), buyAuctionList.get(i).getQuantAllp(), buyAuctionList.get(i).getPrice());
                sellAD = new AuctionData(sellAuctionList.get(j).getNumber(),sellAuctionList.get(j).getDirectionAppl(),
                        sellAuctionList.get(j).getTypeAppl(),sellAuctionList.get(j).getQuantAllp(),sellAuctionList.get(j).getPrice());
            }
            if (resultAuctionList.isEmpty())
                falseAuction = true;
        }
        return resultAuctionList;

    }

    private boolean inPriceSet(Set<Integer> priceSet, int price) {
        for (Integer el : priceSet){
            if (el.equals(price))
                return true;
        }
        return false;
    }

    //Count price for auction with max value. return the result array.
    public ArrayList<ResultData> countResultWithMaxPrice() {
        Set<Integer> priceSet = new HashSet<Integer>();
        ArrayList<ResultData> max = null;
        int i = 0, j = 0;
        int tempPrice = 0;
        while (i < buyAuctionList.size() || j < sellAuctionList.size()) {
            ArrayList<ResultData> temp = null;
            if (i < buyAuctionList.size()) {
                if (max == null) {
                    if (buyAuctionList.get(i).getTypeAppl() == 'L') {
                        if (inPriceSet(priceSet, buyAuctionList.get(i).getPrice())){
                            i++;
                            continue;
                        }
                        max = merge(buyAuctionList.get(i).getPrice());
                        auctPrice = buyAuctionList.get(i).getPrice();
                        priceSet.add(buyAuctionList.get(i).getPrice());
                        i++;
                        continue;
                    }
                    else {
                        i++;
                        continue;
                    }
                }
                else {
                    if (buyAuctionList.get(i).getTypeAppl() == 'L') {
                        if (inPriceSet(priceSet, buyAuctionList.get(i).getPrice())) {
                            i++;
                            continue;
                        }
                        tempPrice = buyAuctionList.get(i).getPrice();
                        temp = merge(tempPrice);
                        priceSet.add(buyAuctionList.get(i).getPrice());
                        i++;
                        if (countTotalValue(max) < countTotalValue(temp)) {
                            max = temp;
                            auctPrice = tempPrice;
                            continue;
                        }
                    }
                    else {
                        i++;
                        continue;
                    }
                }
            }
            else {
                if (max == null) {
                    if (sellAuctionList.get(j).getTypeAppl() == 'L') {
                        if (inPriceSet(priceSet, sellAuctionList.get(j).getPrice())) {
                            j++;
                            continue;
                        }
                        max = merge(sellAuctionList.get(j).getPrice());
                        auctPrice = sellAuctionList.get(j).getPrice();
                        priceSet.add(sellAuctionList.get(j).getPrice());
                        j++;
                        continue;
                    }
                    else {
                        j++;
                        continue;
                    }
                }
                else {
                    if (sellAuctionList.get(j).getTypeAppl() == 'L') {
                        if (inPriceSet(priceSet, sellAuctionList.get(j).getPrice())) {
                            j++;
                            continue;
                        }
                        temp = merge(sellAuctionList.get(j).getPrice());
                        tempPrice = sellAuctionList.get(j).getPrice();
                        priceSet.add(sellAuctionList.get(j).getPrice());
                        j++;
                        if (countTotalValue(max) < countTotalValue(temp)) {
                            max = temp;
                            auctPrice = tempPrice;
                            continue;
                        }
                    }
                    else {
                        j++;
                        continue;
                    }
                }
            }

        }
        return max;
    }

    public static void main(String[] args) {
        Moex moex = new Moex();
        moex.readFromCsv();
        ArrayList<ResultData> max = moex.countResultWithMaxPrice();
        moex.writeToCsv(max);


    }
}
