package demo1;

import lombok.Data;

import java.awt.image.BufferedImage;

@Data
public class Cell {
    private int row;
    private int col;
    private BufferedImage image;

    public Cell(int row, int col, BufferedImage image) {
        this.row=row;
        this.col=col;
        this.image=image;
    }

    public void left(){
        col--;
    }
    public void right(){col++;}

    public void drop(){
        row++;
    }
}

