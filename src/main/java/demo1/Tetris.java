package demo1;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tetris extends JPanel {
    //正在下落
    private  Tetromino currentOne =Tetromino.randomOne();
    //下一个
    private  Tetromino nextOne =Tetromino.randomOne();
    private Cell[][] wall =new Cell[18][9];
    //单元格
    private static final int CELL_SIZE=48;
    //申明分数池
    int[] scores_pool={0,1,2,5,10};
    //当前分数
    private  int totalScore=0;
    //当前消除的行数
    private int totalLine=0;
    //游戏的状态
    public static final int PLAYING=0;
    public static final int PAUSE=1;
    public static final int GAMEOVER=2;
    //声明状态值
    private int game_state;
    //显示游戏状态
    String[] show_state={"P[pause]","C[continue]","S[replay]"};

    public static BufferedImage I;
    public static BufferedImage J;
    public static BufferedImage L;
    public static BufferedImage O;
    public static BufferedImage S;
    public static BufferedImage T;
    public static BufferedImage Z;
    public static BufferedImage backImage;
    static {
        try {
            I= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/I.png"));
            J= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/J.png"));
            L= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/L.png"));
            O= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/O.png"));
            S= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/S.png"));
            T= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/T.png"));
            Z= ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/Z.png"));
            backImage =ImageIO.read(new File("D:/桌面/Tetris/src/main/java/images/background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void paint(Graphics g){
        g.drawImage(backImage,0,0,null);
        //平移
        g.translate(17,13);
        //主区域
        paintWall(g);
        //绘制在下落的
        paintCurrentOne(g);
        //绘制下一个
        paintNextone(g);
        //绘制得分
        paintScore(g);
        //绘制状态
        paintState(g);
    }

    //按键控制
    public void start(){
        game_state=PLAYING;
        KeyListener listener=new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
               int code =e.getKeyCode();
               switch (code) {
                   case KeyEvent.VK_DOWN:
                       sortDropAction();
                       break;
                   case KeyEvent.VK_LEFT:
                       moveleftAction();
                       break;
                   case KeyEvent.VK_RIGHT:
                       moverightAction();
                       break;
                   case KeyEvent.VK_UP:
                       rotateRightAction();
                       break;
                   case KeyEvent.VK_SPACE:
                       handDropAction();
                       break;
                   case KeyEvent.VK_P:
                       if (game_state == PLAYING) {
                           game_state = PAUSE;
                       }
                       break;
                   case KeyEvent.VK_C:
                       if (game_state == PAUSE)
                           game_state = PLAYING;
                       break;
                   case KeyEvent.VK_S:
                       //重开
                       game_state = PLAYING;
                       wall = new Cell[18][9];
                       currentOne = Tetromino.randomOne();
                       nextOne = Tetromino.randomOne();
                       totalLine = 0;
                       totalScore = 0;
                       break;
               }
            }
        };
        //将窗口设为焦点
        this.addKeyListener(listener);
        this.requestFocus();
        while (true){
            //判断当前的状态，在游戏中时自由下落
            if(game_state==PLAYING){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(canDrop()){
                    currentOne.softdorp();
                }else {
                    landToWall();
                    destroyLine();
                    if(isGameOver()){
                        game_state=GAMEOVER;
                    }else {
                        currentOne=nextOne;
                        nextOne=Tetromino.randomOne();

                    }
                }
            }
            repaint();
        }

    }

    //创建顺时针旋转方法
    public void rotateRightAction(){
        currentOne.rotateRight();
        if(outofBounds()||coincide()){//判断是否重合与越界
            currentOne.rotateLeft();
        }
    }

    //瞬间下落
    public void handDropAction(){
        while (true) {
            if (canDrop()) {
                currentOne.softdorp();
            } else {
                break;

            }
        }
       //判断是否结束，没有的话嵌入墙中
        landToWall();
        destroyLine();
        if(isGameOver()){
            game_state=GAMEOVER;
        }else {
            currentOne=nextOne;
            nextOne=Tetromino.randomOne();
        }

    }
    //按键一次下落一格
    public void sortDropAction(){
        if(canDrop()){
            currentOne.softdorp();
        }else {
            //放入墙里面
            landToWall();
            //判断能否消行
            destroyLine();
            if(isGameOver()){
                game_state=GAMEOVER;
            }else{
                //游戏没有结束
                currentOne=nextOne;
                nextOne=Tetromino.randomOne();
            }
        }
    }

    //嵌入格子内
    private void landToWall() {
        Cell[] cells=currentOne.cells;
        for(Cell cell:cells){
            int row =cell.getRow();
            int col =cell.getCol();
            wall[row][col]=cell;

        }
    }

    //判断是否能下落
    public boolean canDrop(){
        Cell[] cells=currentOne.cells;
        for(Cell cell:cells){
            int row =cell.getRow();
            int col =cell.getCol();
            if(row==wall.length-1){
                return false;
            } else if(wall[row+1][col]!=null){
                return false;
            }
        }
        return true;
    }
    //消行
    public void destroyLine(){
        //统计消除总行数
        int line=0;
        Cell[] cells=currentOne.cells;
        for(Cell cell:cells){
            int row=cell.getRow();
            if(isFullLine(row)){
                line++;
                for (int i=row;i>0;i--){
                    System.arraycopy(wall[i-1],0,wall[i],0,wall[0].length);
                }
                wall[0]=new Cell[9];
            }
        }

        //在分数池中获取分数，累加到分数中。
        totalScore+=scores_pool[line];
        totalLine+=line;//消除总行数
    }
    //判断当前行是否满
    public boolean isFullLine(int row){
        Cell[] cells=wall[row];
        for(Cell cell:cells){
            if(cell==null){
                return false;
            }
        }
        return true;
    }

    //判断是否结束】
    public boolean isGameOver(){
        Cell[] cells=nextOne.cells;
        for( Cell cell:cells){
            int row=cell.getRow();
            int col =cell.getCol();
            if(wall[row][col]!=null) {
                return true;
            }
        }
        return false;
    }
    private void paintState(Graphics g) {
        if(game_state==PLAYING){
            g.drawString(show_state[PLAYING],500,666 );
        } else if (game_state==PAUSE) {
            g.drawString(show_state[1],500,666 );
        } else if (game_state==GAMEOVER) {
            g.drawString(show_state[2],500,666 );
            g.setColor(Color.red);
            g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,60));
            g.drawString("GAMEOVER",30,400);

        }
    }

    private void paintScore(Graphics g) {
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,30));
        g.drawString("SCORES:"+totalScore,500,250);
        g.drawString("LINES:"+ totalLine,500,430);

    }

    private void paintNextone(Graphics g) {
        Cell[] cells=nextOne.cells;
        for(Cell cell :cells){
            int x=cell.getCol()*CELL_SIZE+380;
            int y=cell.getRow()*CELL_SIZE+23;
            g.drawImage(cell.getImage(), x,y,null);
        }
    }

    private void paintCurrentOne(Graphics g) {
        Cell[] cells =currentOne.cells;
        for(Cell cell:cells){
            int x= cell.getCol()*CELL_SIZE;
            int y= cell.getRow()*CELL_SIZE;
            g.drawImage(cell.getImage(),x,y,null);
        }
    }

    public void paintWall(Graphics g){
        for(int i=0;i<wall.length;i++){
            for(int j=0;j<wall[i].length;j++){
                int x=j*CELL_SIZE;
                int y=i*CELL_SIZE;
                Cell cell=wall[i][j];
                //判读是否有单元格，没有就划线，有就填入
                if(cell==null){
                    g.drawRect(x,y,CELL_SIZE,CELL_SIZE);
                }else{
                   g.drawImage(cell.getImage(),x,y,null);
                }
            }
        }
    }

    //判断是否出界
    public boolean outofBounds(){
        Cell[] cells=currentOne.cells;
        for(Cell cell:cells){
            int col=cell.getCol();
            int row =cell.getRow();
            if(row<0||row>wall.length-1||col<0||col>wall[0].length-1){
                return true;
            }
        }
        return false;
    }
    //判断是否重合
    public boolean coincide(){
        Cell[] cells=currentOne.cells;
        for(Cell cell:cells){
            int col=cell.getCol();
            int row =cell.getRow();
            if(wall[row][col]!=null){
                return true;
            }
        }
        return false;
    }

    //按键左移
    public void moveleftAction(){
        currentOne.moveleft();
        //判断是否越界以及重合
        if(outofBounds()||coincide()){
            currentOne.moveright();
        }
    }
    //按键右
    public void moverightAction(){
        currentOne.moveright();
        if(outofBounds()||coincide()){
            currentOne.moveright();
        }
    }

    public static void main(String[] args) {
        JFrame frame =new JFrame("俄罗斯方块");
        Tetris panel =new Tetris();
        frame.add(panel);
        frame.setVisible(true);
        frame.setSize(810,940);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.start();

    }
}
