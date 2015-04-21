/**
 * Created by den on 14.02.15.
 *
 * Class for input data : buy array and sell array with structure ["[num]","BS","LM","<Volume>","<Price>"]
 */
public class AuctionData  {
    private int number;
    private char directionAppl;
    private char typeAppl;
    private Integer quantAllp;
    private Integer price;


    public AuctionData(int number,char directionAppl, char typeAppl, Integer quantAllp, Integer price) {
        this.number = number;
        this.directionAppl = directionAppl;
        this.typeAppl = typeAppl;
        this.quantAllp = quantAllp;
        this.price = price;
    }



    public char getDirectionAppl() {
        return directionAppl;
    }

    public char getTypeAppl() {
        return typeAppl;
    }

    public Integer getQuantAllp() {
        return quantAllp;
    }

    public Integer getPrice() {
        return price;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setDirectionAppl(char directionAppl) {
        this.directionAppl = directionAppl;
    }

    public void setTypeAppl(char typeAppl) {
        this.typeAppl = typeAppl;
    }

    public void setQuantAllp(Integer quantAllp) {
        this.quantAllp = quantAllp;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public int getNumber() {
        return number;
    }

    public String toString() {
        return number + " " + directionAppl + " " + typeAppl + " " + quantAllp + " " + price;
    }
}
