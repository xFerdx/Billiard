import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class Game extends JFrame{

    public static void main(String[] args) {
        new Game();
    }

    final int WindowWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    final int WindowHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
    static int sleepTime = 1;
    final int numberBallRows = 5;
    ArrayList<Ball> balls = new ArrayList<Ball>();
    final int playerNumber = 5;
    int player = 1;
    int rightBorder = 1100;
    int leftBorder = 100;
    int upperBorder = 100;
    int lowerBorder = 600;
    int grappledBall = -1;
    long newSoundTime = System.nanoTime();
    Ball[] holes;
    int holeSize = 200/numberBallRows;

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


            if(System.nanoTime()-newSoundTime>1e9) {
                newSoundTime=System.nanoTime();
                try {
                    System.out.println("da soundsdsds");
                    playSound(".\\plop.wav", -80.0f);
                    playSound(".\\suck.wav", -80.0f);
                } catch (Exception e) {
                    System.out.println("error with sound");
                }
            }



            checkCollisions();
            checkBallIn();



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

        int size = 150/numberBallRows;

        balls.add(new Ball(400,300,2,-1,5,0f,size));



        for (int i = 0; i < numberBallRows; i++) {
            for (int j = 0; j < i+1; j++) {
                balls.add(new Ball((float) ((leftBorder+rightBorder)/3*2+Math.sqrt(Math.pow(size,2)-Math.pow(0.5*size,2))*i)+i,
                        (lowerBorder + upperBorder) /2f -  size /2f - size /2f *i+size*j+j-numberBallRows/2f,
                        i==Math.floor(numberBallRows /2f) && j==(i+1)/2?3:balls.size() % 2,0,0,0,size));
            }
        }




        holes =  new Ball[6];
        holes[0] = new Ball(leftBorder-holeSize/3f,upperBorder-holeSize/3f,0,0,0,0,holeSize);
        holes[1] = new Ball(leftBorder-holeSize/3f,lowerBorder-2*holeSize/3f,0,0,0,0,holeSize);
        holes[2] = new Ball((float) (leftBorder + rightBorder) /2-holeSize/2f,upperBorder-holeSize/2f,0,0,0,0,holeSize);
        holes[3] = new Ball((float) (leftBorder + rightBorder) /2-holeSize/2f,lowerBorder-holeSize/2f,0,0,0,0,holeSize);
        holes[4] = new Ball(rightBorder-2*holeSize/3f,upperBorder-holeSize/3f,0,0,0,0,holeSize);
        holes[5] = new Ball(rightBorder-2*holeSize/3f,lowerBorder-2*holeSize/3f,0,0,0,0,holeSize);




    }



    public void replay(){

    }



    public void checkCollisions(){
        for (int i = 0; i < balls.size()-1; i++) {
            for (int j = i+1; j < balls.size(); j++) {
                Ball ballI = balls.get(i);
                Ball ballJ = balls.get(j);
                if(getDistance(ballI.xPos+ballI.size/2f,ballJ.xPos+ballJ.size/2f,ballI.yPos+ballI.size/2f,balls.get(j).yPos+ballJ.size/2f)
                           <= balls.get(i).size/2f+ballJ.size /2f) {
                    calcNewVecs(ballI, ballJ);
                    try {
                        if(System.nanoTime()>newSoundTime)
                            playSound(".\\plop.wav",6.25f*(ballI.Speed+ballJ.Speed)-50);
                    } catch (Exception e) {
                        System.out.println("error with sound");
                    }
                }
            }
        }
    }

    public void checkBallIn(){
        for (int i = 0; i < balls.size(); i++) {
            Ball ball = balls.get(i);
            for (Ball hole : holes) {
                if (getDistance(ball.xPos + ball.size / 2f, hole.xPos + hole.size / 2f, ball.yPos + ball.size / 2f, hole.yPos + hole.size / 2f)
                        <= (double) hole.size / 2f) {
                    try {
                        System.out.println("ball in");
                        playSound(".\\suck.wav", 0f);
                    } catch (Exception e) {
                        System.out.println("error with suck sound");
                    }
                    balls.remove(i);
                }
            }
        }
    }



    public void calcNewVecs(Ball b1, Ball b2){
        float oldX1 = b1.xVec;
        float oldY1 = b1.yVec;
        float oldX2 = b2.xVec;
        float oldY2 = b2.yVec;
        float smallValue = 1e-5f;

        b1.xVec = (b1.xPos + (float) b1.size / 2)-(b2.xPos + (float) b2.size / 2);
        b1.yVec = (b1.yPos + (float) b1.size / 2)-(b2.yPos + (float) b2.size / 2);
        b1.normVec();
        float u = (float) Math.toDegrees(Math.acos(Math.min(Math.abs(oldX1)*Math.abs(b1.xVec)+Math.abs(oldY1)*Math.abs(b1.yVec),1)));
        if(b1.Speed==0)u = 0;
        b1.xVec=((90-u)*b1.xVec+u*oldX1)+((90-u)*b1.xVec*b2.Speed+u*oldX1*b1.Speed)/(b1.Speed+b2.Speed+smallValue);
        b1.yVec=((90-u)*b1.yVec+u*oldY1)+((90-u)*b1.yVec*b2.Speed+u*oldY1*b1.Speed)/(b1.Speed+b2.Speed+smallValue);
        b1.normVec();

        b2.xVec = (b2.xPos + (float) b2.size / 2)-(b1.xPos + (float) b1.size / 2) + b2.xVec;
        b2.yVec = (b2.yPos + (float) b2.size / 2)-(b1.yPos + (float) b1.size / 2) + b2.yVec;
        b2.normVec();
        System.out.println(oldX2+" "+ b2.xVec+" "+oldY2+" "+b2.yVec+" "+b2.Speed);
        float u2 = (float) Math.toDegrees(Math.acos(Math.min(Math.abs(oldX2)*Math.abs(b2.xVec)+Math.abs(oldY2)*Math.abs(b2.yVec),1)));
        if(b2.Speed==0)u2 = 0;
        b2.xVec=((90-u2)*b2.xVec+u2*oldX2)+((90-u2)*b2.xVec*b1.Speed+u2*oldX2*b2.Speed)/(b1.Speed+b2.Speed+smallValue);
        b2.yVec=((90-u2)*b2.yVec+u2*oldY2)+((90-u2)*b2.yVec*b1.Speed+u2*oldY2*b2.Speed)/(b1.Speed+b2.Speed+smallValue);
        b2.normVec();



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
        for (int i = 0; i < balls.size(); i++) {
            if(Math.pow(x-(balls.get(i).xPos+balls.get(i).size/2f+7),2)+Math.pow(y-(balls.get(i).yPos+balls.get(i).size/2f+30),2)<=Math.pow(balls.get(i).size/2f,2)){
                grappledBall = i;
                System.out.println("clicked: "+i);
                break;
            }
        }
    }

    public double getDistance(float x1, float x2, float y1, float y2){
        return Math.sqrt(Math.pow(x1-x2,2)+Math.pow(y1-y2,2));
    }

    public void clickReleased(){
        if (grappledBall == -1)return;

        Ball gb = balls.get(grappledBall);

        float x = MouseInfo.getPointerInfo().getLocation().x;
        float y = MouseInfo.getPointerInfo().getLocation().y - 20;

        if(getDistance(x,gb.xPos+gb.size/2f,y,gb.yPos+gb.size/2f)<=gb.size/2f){
            grappledBall = -1;
            return;}
        gb.xVec=gb.xPos+gb.size/2f-x;
        gb.yVec=gb.yPos+gb.size/2f-y;
        gb.Speed= Math.min((float) getDistance(x,gb.xPos+gb.size/2f,y,gb.yPos+gb.size/2f)/100,5);
        gb.normVec();
        grappledBall = -1;
        System.out.println("released: " + grappledBall);
    }


    class DrawPanel extends JPanel {
        public void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WindowWidth, WindowHeight);


            for (Ball ball : balls) {
                g.setColor(Color.WHITE);
                g.drawOval((int) ball.xPos, (int) ball.yPos, ball.size, ball.size);
                if (ball.team == 0) g.setColor(Color.RED);
                else if (ball.team == 1) g.setColor(Color.BLUE);
                else if (ball.team == 2) g.setColor(Color.WHITE);
                else if (ball.team == 3) g.setColor(Color.BLACK);
                g.fillOval((int) ball.xPos, (int) ball.yPos, ball.size, ball.size);
                g.setColor(Color.YELLOW);
                g.drawLine((int) (ball.xPos+ball.size/2), (int) (ball.yPos+ball.size/2), (int) (ball.xPos+ball.size/2+(ball.Speed*50)*ball.xVec), (int) (ball.yPos+ball.size/2+(ball.Speed*50)*ball.yVec));
                g.setColor(Color.MAGENTA);
                if(grappledBall !=-1) {
                    float mx = MouseInfo.getPointerInfo().getLocation().x;
                    float my = MouseInfo.getPointerInfo().getLocation().y - 20;
                    float x1 = balls.get(grappledBall).xPos + balls.get(grappledBall).size / 2f;
                    float y1 = balls.get(grappledBall).yPos + balls.get(grappledBall).size / 2f;
                    float vx = (x1-mx);
                    float vy = (y1-my);

                    float minK = calcEndPos(x1,y1,vx,vy);

                    g.drawLine((int) mx,(int) my,(int) (x1+vx*minK),(int) (y1+vy*minK));
                    g.drawOval((int) (x1+vx*minK)-ball.size/2,(int) (y1+vy*minK)-ball.size/2,ball.size,ball.size);
                }
            }



            g.setColor(Color.GREEN);
            g.drawLine(leftBorder,upperBorder,rightBorder,upperBorder);
            g.drawLine(leftBorder,lowerBorder,rightBorder,lowerBorder);
            g.drawLine(leftBorder,upperBorder,leftBorder,lowerBorder);
            g.drawLine(rightBorder,upperBorder,rightBorder,lowerBorder);


            for (Ball hole:holes) {
                g.setColor(Color.BLACK);
                g.fillOval((int) hole.xPos, (int) hole.yPos, hole.size, hole.size);
                g.setColor(Color.GREEN);
                g.drawOval((int) hole.xPos, (int) hole.yPos, hole.size, hole.size);
            }




        }
    }

    public float calcEndPos(float x1,float y1, float vx, float vy){
        float minK = Float.MAX_VALUE;

        for (int i = 0; i < balls.size(); i++) {
            if(i== grappledBall)continue;
            float x2 = balls.get(i).xPos + balls.get(i).size / 2f;
            float y2 = balls.get(i).yPos + balls.get(i).size / 2f;

            float q  = (float) ((Math.pow(x2-x1,2)+Math.pow(y2-y1,2)-(Math.pow(balls.get(grappledBall).size/2f+balls.get(i).size/2f,2)))/(Math.pow(vx,2)+Math.pow(vy,2)));
            float p = (float) ((2*(x2-x1)*vx+2*(y2-y1)*vy)/(Math.pow(vx,2)+Math.pow(vy,2)));

            float k1 = (float) -(-p/2 + Math.sqrt(Math.pow(p/2,2)-q));
            float k2 = (float) -(-p/2 - Math.sqrt(Math.pow(p/2,2)-q));

            if(Float.isNaN(k1)||k1<0)k1 = Float.MAX_VALUE;
            if(Float.isNaN(k2)||k2<0)k2 = Float.MAX_VALUE;


            minK = Math.min(Math.min(k1,k2),minK);
        }
        return Math.min(minK,1000);
    }

    public void playSound(String fileName, float volume) throws Exception {
        if(volume!=-80.0f)this.newSoundTime+=(long)2e8;
        File url = new File(fileName);
        Clip clip = AudioSystem.getClip();
        AudioInputStream ais = AudioSystem.getAudioInputStream(url);
        clip.open(ais);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gainControl.setValue(volume);
        clip.start();
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
    public int size = 30;

    public Ball(float xPos, float yPos, int team, int xVec, int yVec, float Speed, int size){
        this.xPos=xPos;
        this.yPos=yPos;
        this.team=team;
        this.xVec=xVec;
        this.yVec=yVec;
        this.Speed=Speed;
        this.size=size;
        normVec();
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

    public void normVec(){
        if(xVec==0 && yVec==0)return;
        float z = (float) Math.sqrt(xVec*xVec+yVec*yVec);
        xVec/=z;
        yVec/=z;
    }

    public void normalSpeedReducer(){
        Speed*= (float) Math.pow(2.71,-0.0009*Game.sleepTime);
        if(Speed<0.003)Speed=0;
    }


}