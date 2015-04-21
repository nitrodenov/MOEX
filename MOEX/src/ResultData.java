/**
 * Created by den on 15.02.15.
 *
 * The resultarray.
 */
public class ResultData {
    private int buyOpInd;
    private int sellOpInd;
    private int quant;
    private int value;

    public ResultData(int buyOpInd, int sellOpInd, int quant, int value) {
        this.buyOpInd = buyOpInd;
        this.sellOpInd = sellOpInd;
        this.quant = quant;
        this.value = value;
    }


    public int getBuyOpInd() {
        return buyOpInd;
    }

    public int getSellOpInd() {
        return sellOpInd;
    }

    public int getQuant() {
        return quant;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return buyOpInd + " " + sellOpInd + " " + quant + " " + value;
    }
}
