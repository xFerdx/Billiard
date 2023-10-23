import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JFrame{

    public static void main(String[] args) {
        new Game();
    }

    final int WindowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    final int WindowHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    static int sleepTime = 1;
    final int numberBalls = 17;
    Ball[] balls = new Ball[numberBalls];
    final int playerNumber = 5;
    int player = 1;
    int rightBorder = 1100;
    int leftBorder = 100;
    int upperBorder = 100;
    int lowerBorder = 600;
    int grappedBall = -1;

    Game(){
        setContentPane(new DrawPanel());
        setSize(WindowWidth, WindowHeight);
        setTitle("Billiard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        this.addMouseListener(new MouseEventDemo());
        start();
    }



    public void start(){
        init();
        while(true) {

            for (Ball ball : balls) {
                ball.calcPos();
                ball.checkBorder(rightBorder, leftBorder, upperBorder, lowerBorder);
                ball.normalSpeedReducer();
            }

            checkCollisions();


            for (int i = 0;i<balls.length;i++) {
                System.out.println(i+" " + balls[i].xPos + " " + balls[i].yPos + " " + balls[i].xVec + " " + balls[i].yVec + " " + balls[i].Speed);
            }

            repaint();


            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        //System.out.println();
        //replay();

    }



    public void init(){
        balls[0] = new Ball(105,305,0,11,20,0f,30);
        balls[1] = new Ball(400,300,1,-1,5,0f,30);


        int number = 5;
        int size = 150/number;

        int z = 2;
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < i+1; j++) {
                balls[z] = new Ball((float) ((leftBorder+rightBorder)/3*2+Math.sqrt(Math.pow(size,2)-Math.pow(0.5*size,2))*i)+i, (lowerBorder + upperBorder) /2f -  size /2f - size /2f *i+size*j+j-number/2f,(j+i) % 2,0,0,0,size);
                z++;
            }
        }


    }



    public void replay(){

    }



    public void checkCollisions(){
        for (int i = 0; i < balls.length-1; i++) {
            for (int j = i+1; j < balls.length; j++) {
                if(Math.sqrt(Math.pow((balls[i].xPos+ (double) balls[i].size /2f)-(balls[j].xPos+ (double) balls[j].size /2f),2)
                        +Math.pow((balls[i].yPos+ (double) balls[i].size /2f)-(balls[j].yPos+ (double) balls[j].size /2f),2))
                        <= (double) balls[i].size /2f + (double) balls[j].size /2f) {
                    System.out.println(i+" "+j);
                    calcNewVecs(balls[i], balls[j]);
                }
            }
        }
    }

    public void calcNewVecs(Ball b1, Ball b2){
        float oldX1 = b1.xVec;
        float oldY1 = b1.yVec;
        float oldX2 = b2.xVec;
        float oldY2 = b2.yVec;

        b1.xVec = (b1.xPos + (float) b1.size / 2)-(b2.xPos + (float) b2.size / 2);
        b1.yVec = (b1.yPos + (float) b1.size / 2)-(b2.yPos + (float) b2.size / 2);
        b1.normateVec();
        float u = (float) Math.toDegrees(Math.acos(Math.min(Math.abs(oldX1)*Math.abs(b1.xVec)+Math.abs(oldY1)*Math.abs(b1.yVec),1)));
        if(b1.Speed==0)u = 0;
        b1.xVec=((90-u)*b1.xVec+u*oldX1)+((90-u)*b1.xVec*b2.Speed+u*oldX1*b1.Speed)/(b1.Speed+b2.Speed+0.000001f);
        b1.yVec=((90-u)*b1.yVec+u*oldY1)+((90-u)*b1.yVec*b2.Speed+u*oldY1*b1.Speed)/(b1.Speed+b2.Speed+0.000001f);
        b1.normateVec();

        b2.xVec = (b2.xPos + (float) b2.size / 2)-(b1.xPos + (float) b1.size / 2) + b2.xVec;
        b2.yVec = (b2.yPos + (float) b2.size / 2)-(b1.yPos + (float) b1.size / 2) + b2.yVec;
        b2.normateVec();
        System.out.println(oldX2+" "+ b2.xVec+" "+oldY2+" "+b2.yVec);
        float u2 = (float) Math.toDegrees(Math.acos(Math.min(Math.abs(oldX2)*Math.abs(b2.xVec)+Math.abs(oldY2)*Math.abs(b2.yVec),1)));
        if(b2.Speed==0)u2 = 0;
        b2.xVec=((90-u2)*b2.xVec+u2*oldX2)+((90-u2)*b2.xVec*b1.Speed+u2*oldX2*b2.Speed)/(b1.Speed+b2.Speed+0.000001f);
        b2.yVec=((90-u2)*b2.yVec+u2*oldY2)+((90-u2)*b2.yVec*b1.Speed+u2*oldY2*b2.Speed)/(b1.Speed+b2.Speed+0.000001f);
        b2.normateVec();



        float OldSpeed1 = b1.Speed;
        float OldSpeed2 = b2.Speed;

        float w = (float) Math.toDegrees(Math.acos(Math.min((Math.abs(oldX1)*Math.abs(oldX2)+Math.abs(oldY1)*Math.abs(oldY2)),1)));
        if(OldSpeed1==0 || OldSpeed2==0)
            w=0;
        b1.Speed = (u*OldSpeed1+(90-u)*OldSpeed2)/90;
        b2.Speed = (u2*OldSpeed2+(90-u2)*OldSpeed1)/90;

        b1.calcPos();
        b2.calcPos();
        b1.calcPos();
        b2.calcPos();
    }

    public void calcNewVecs2(Ball b1, Ball b2){
        float vx1 = b1.xVec*b1.Speed;
        float vy1 = b1.yVec*b1.Speed;
        float vx2 = b2.xVec*b2.Speed;
        float vy2 = b2.yVec*b2.Speed;

        float m = (b1.xPos-b2.xPos)/(b1.yPos-b2.yPos);

        b1.xVec = (1/(m*m+1))*(vx1+m*vy1+m*m*vx2-m*vy2);
        b1.yVec = (1/(m*m+1))*(m*vx1+m*m*vy1-m*vx2*vy2);
        b2.xVec = (1/(m*m+1))*(vx2+m*vy2+m*m*vx1-m*vy1);
        b2.yVec = (1/(m*m+1))*(m*vx2+m*m*vy2-m*vx1*vy1);
        float z1 = (float) Math.sqrt(b1.xVec*b1.xVec+b1.yVec*b1.yVec)+0.0001f;
        float z2 = (float) Math.sqrt(b2.xVec*b2.xVec+b2.yVec*b2.yVec)+0.0001f;

        b1.Speed*=z1;
        b2.Speed*=z2;
        b1.xVec/=z1;
        b1.yVec/=z1;
        b2.xVec/=z2;
        b2.yVec/=z2;

    }






    public void click(int x, int y){
        System.out.println("clicked, ball[0]:"+x+" "+balls[0].xPos+" "+y+" "+balls[0].yPos);
        for (int i = 0; i < balls.length; i++) {
            if(Math.pow(x-(balls[i].xPos+balls[i].size/2f+7),2)+Math.pow(y-(balls[i].yPos+balls[i].size/2f+30),2)<=Math.pow(balls[i].size/2f,2)){
                grappedBall = i;
                System.out.println("clicked: "+i);
                break;
            }
        }
    }

    public void clickReleased(){
        if (grappedBall == -1)return;

        float x = MouseInfo.getPointerInfo().getLocation().x;
        float y = MouseInfo.getPointerInfo().getLocation().y - 20;

        balls[grappedBall].xVec=balls[grappedBall].xPos+balls[grappedBall].size/2f-x;
        balls[grappedBall].yVec=balls[grappedBall].yPos+balls[grappedBall].size/2f-y;
        balls[grappedBall].Speed= (float) Math.sqrt(Math.pow(balls[grappedBall].xVec,2)+Math.pow(balls[grappedBall].yVec,2))/100;
        balls[grappedBall].normateVec();
        grappedBall = -1;
        System.out.println("released: " + grappedBall);
    }


    class DrawPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WindowWidth, WindowHeight);




            for (Ball ball : balls) {
                g.setColor(Color.WHITE);
                g.drawOval((int) ball.xPos, (int) ball.yPos, ball.size, ball.size);
                if (ball.team == 0) g.setColor(Color.RED);
                if (ball.team == 1) g.setColor(Color.BLUE);
                g.fillOval((int) ball.xPos, (int) ball.yPos, ball.size, ball.size);
                g.setColor(Color.YELLOW);
                g.drawLine((int) (ball.xPos+ball.size/2), (int) (ball.yPos+ball.size/2), (int) (ball.xPos+ball.size/2+(ball.Speed*50)*ball.xVec), (int) (ball.yPos+ball.size/2+(ball.Speed*50)*ball.yVec));
                g.setColor(Color.MAGENTA);
                if(grappedBall!=-1) {
                    float mx = MouseInfo.getPointerInfo().getLocation().x;
                    float my = MouseInfo.getPointerInfo().getLocation().y - 20;
                    float x1 = balls[grappedBall].xPos + balls[grappedBall].size / 2f;
                    float y1 = balls[grappedBall].yPos + balls[grappedBall].size / 2f;
                    float vx = (x1-mx);
                    float vy = (y1-my);

                    float minK = calcEndPos(x1,y1,vx,vy);

                    g.drawLine((int) mx,(int) my,(int) (x1+vx*minK),(int) (y1+vy*minK));
                    g.drawOval((int) (x1+vx*minK)-15,(int) (y1+vy*minK)-15,30,30);
                }
            }



            g.setColor(Color.GREEN);
            g.drawLine(leftBorder,upperBorder,rightBorder,upperBorder);
            g.drawLine(leftBorder,lowerBorder,rightBorder,lowerBorder);
            g.drawLine(leftBorder,upperBorder,leftBorder,lowerBorder);
            g.drawLine(rightBorder,upperBorder,rightBorder,lowerBorder);



        }
    }

    public float calcEndPos(float x1,float y1, float vx, float vy){
        float minK = Float.MAX_VALUE;

        for (int i = 0; i < balls.length; i++) {
            if(i==grappedBall)continue;
            float x2 = balls[i].xPos + balls[i].size / 2f;
            float y2 = balls[i].yPos + balls[i].size / 2f;

            float q  = (float) ((Math.pow(x2-x1,2)+Math.pow(y2-y1,2)-(Math.pow(balls[grappedBall].size/2f+balls[i].size/2f,2)))/(Math.pow(vx,2)+Math.pow(vy,2)));
            float p = (float) ((2*(x2-x1)*vx+2*(y2-y1)*vy)/(Math.pow(vx,2)+Math.pow(vy,2)));

            float k1 = (float) -(-p/2 + Math.sqrt(Math.pow(p/2,2)-q));
            float k2 = (float) -(-p/2 - Math.sqrt(Math.pow(p/2,2)-q));

            if(Float.isNaN(k1)||k1<0)k1 = Float.MAX_VALUE;
            if(Float.isNaN(k2)||k2<0)k2 = Float.MAX_VALUE;


            minK = Math.min(Math.min(k1,k2),minK);
        }
        return Math.min(minK,1000);


    }







    class MouseEventDemo implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {

        }
        public void mousePressed(MouseEvent e) {
            click(e.getX(),e.getY());
        }
        public void mouseReleased(MouseEvent e) {
            clickReleased();
        }
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}

    }
}

class Ball{
    public int team;
    public float Speed = 0;
    public float xVec = 0;
    public float yVec = 0;
    public float xPos = 500;
    public float yPos = 500;
    int size = 30;

    public Ball(){

    }

    public Ball(float xPos, float yPos, int team){
        this.xPos=xPos;
        this.yPos=yPos;
        this.team=team;
    }

    public Ball(float xPos, float yPos, int team, int xVec, int yVec, float Speed, int size){
        this.xPos=xPos;
        this.yPos=yPos;
        this.team=team;
        this.xVec=xVec;
        this.yVec=yVec;
        this.Speed=Speed;
        this.size=size;
        normateVec();
    }

    public void calcPos(){
        xPos+=xVec*Speed;
        yPos+=yVec*Speed;
    }

    public void checkBorder(int right, int left, int upper, int lower){
        if(xPos + size >= right){xPos=right-size; xVec*= -1;}
        if(xPos <= left){xPos=left; xVec*= -1;}
        if(yPos + size >= lower){yPos=lower-size; yVec*= -1;}
        if(yPos <= upper){yPos=upper; yVec*= -1;}
    }

    public void normateVec(){
        if(xVec==0 && yVec==0)return;
        float z = (float) Math.sqrt(xVec*xVec+yVec*yVec);
        xVec/=z;
        yVec/=z;
    }

    public void normalSpeedReducer(){
        Speed*=Math.pow(2.71,-0.0005*Game.sleepTime);
        if(Speed<0.001)Speed=0;
    }


}