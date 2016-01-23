import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JViewport;


public class CPGMain{

    Graphics2D renderBuffer;
    transient BufferedImage bufferImage;
    Map myMap;
    
    JFrame creationWindow;
    JPanel mapPanel;
    JPanel mainScreen;
    JPanel buttonPanel;
    JButton createButtons[];
    JButton nextButton;
    
    worldMap mapView;
    boolean globalReady;
    
    int plotSize;

    
    public CPGMain()
    {
    	creationWindow = new JFrame();
    	creationWindow.setSize(990, 925);
        creationWindow.setVisible(true);
        
        plotSize = 5;
        
        mainScreen = new JPanel();
 //       mainScreen.setLayout(new BorderLayout(3,3));
        mainScreen.setSize(990,925);
        
        mapPanel = new JPanel();
        mapPanel.setLayout(new GridBagLayout());
        mapPanel.setSize(980,800);
        mapPanel.setPreferredSize(new Dimension(980, 800));
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setSize(980,30);
        buttonPanel.setPreferredSize(new Dimension(980, 30));
        
        creationWindow.setContentPane(mainScreen);
        creationWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        mainScreen.add(mapPanel);
        mainScreen.add(buttonPanel);
        
        myMap = new Map(160,160, plotSize);
        
         mapView = new worldMap();
         mapView.setSize(800,800);
         mapView.setPreferredSize(new Dimension(800,800));
         mapPanel.add(mapView);
         mapView.setVisible(true);
         createButtons = new JButton[4];
         createButtons[0] = new JButton("Dungeon");
         createButtons[1] = new JButton("Region");
         createButtons[2] = new JButton("Continents");
         createButtons[3] = new JButton("Galaxy");
         
         for(int i = 0; i < 4; i++)
         {
	         createButtons[i].setSize(100,20);
	         createButtons[i].setPreferredSize(new Dimension(100,20));
	         buttonPanel.add(createButtons[i]);
	         createButtons[i].setVisible(true);
         }
         createButtons[0].addActionListener(new ActionListener(){

 			public void actionPerformed(ActionEvent e) {
 				myMap.simpleDungeonMap();
 		         nextButton.setEnabled(false);
 			}
         });
         
         createButtons[1].addActionListener(new ActionListener(){

 			public void actionPerformed(ActionEvent e) {
 				myMap.simpleLandscapeMap();
 		         nextButton.setEnabled(false);
 			}
         });
         createButtons[2].addActionListener(new ActionListener(){

 			public void actionPerformed(ActionEvent e) {
 				myMap.simpleContinentMap();
 		         nextButton.setEnabled(false);
 			}
         });
         createButtons[3].addActionListener(new ActionListener(){

 			public void actionPerformed(ActionEvent e) {
 				myMap.simpleGalaxyMap();
 				nextButton.setEnabled(true);
 			}
         });
         
         nextButton = new JButton("Advance");
         nextButton.setSize(100,20);
         nextButton.setPreferredSize(new Dimension(100,20));
         buttonPanel.add(nextButton);
         nextButton.setVisible(true);
         nextButton.setEnabled(false);
         
         nextButton.addActionListener(new ActionListener(){

 			public void actionPerformed(ActionEvent e) {
 				myMap.nextTurn();
 			}
         });
         
      	creationWindow.setSize(990, 925);
      	creationWindow.revalidate();
         
         bufferImage = mapView.getGraphicsConfiguration().createCompatibleImage(mapView.getWidth(),mapView.getHeight());
         renderBuffer = bufferImage.createGraphics();
         globalReady = true;
         
         mapView.renderThread = new Thread(null, mapView, "AnimationThread");
         mapView.renderThread.setPriority(Thread.MAX_PRIORITY);
         mapView.renderThread.start();
         
         //Plot focusPlot;

         mapView.addMouseListener(new MouseListener() {
        	    @Override
        	    public void mouseClicked(MouseEvent e) {
        	    	int x=e.getX();
        	        int y=e.getY();
        	        x /= plotSize;
        	        y /= plotSize;
        	        
        	        if( myMap.myGrid[x][y] instanceof Star)
        	        {
        	        	myMap.statusReport = myMap.myGrid[x][y].toString();
        	        }
        	    }

				@Override
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
        	});
        	
    }
    
    public static void main(String[] args) {
        new CPGMain();
    }
	
	class worldMap extends JPanel implements Runnable
	{
		Thread renderThread;
		
		
		 public void paintComponent(Graphics g) {
	         Graphics2D g2d = (Graphics2D)g;


	            try {
	    			
	    			if ((g2d != null) && (bufferImage != null))
	    				g2d.drawImage(bufferImage, 0,0, null);
	    	
	    		}
	    		catch (Exception e)
	    			{ System.out.println("Graphics error: " + e.getMessage()); }
	        }
		 
		 public void renderMap()
		 {
			 renderBuffer.setColor(Color.BLACK);
			 renderBuffer.fillRect(0, 0, 800, 800);
			 
			 for(int x = 0; x < myMap.width; x++)
				 for(int y = 0; y < myMap.height; y++)
				 {
					 renderBuffer.setColor(myMap.myGrid[x][y].shade);
					 renderBuffer.fillRect(myMap.myGrid[x][y].xPixel, myMap.myGrid[x][y].yPixel, plotSize, plotSize);
					 if(myMap.myGrid[x][y].label != "")
					 {
						 renderBuffer.setColor(Color.WHITE);
						 renderBuffer.drawString(myMap.myGrid[x][y].label, myMap.myGrid[x][y].xPixel, myMap.myGrid[x][y].yPixel);
					 }
					
				 }
			 
			/* renderBuffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			 renderBuffer.setColor(Color.DARK_GRAY);

			 FontMetrics met = renderBuffer.getFontMetrics(renderBuffer.getFont());
			 int namePixels = met.stringWidth(myMap.statusReport)+10;
				
			renderBuffer.fillRoundRect(25, 50 -15, namePixels, 20, 5, 5);
			renderBuffer.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			*/
			renderBuffer.setColor(Color.WHITE);
			drawString(renderBuffer, myMap.statusReport, 30, 50);
		 }
		 
		
		 
		 
		 public void run() {
				long startTime, diff, sleepDuration;
					startTime = System.currentTimeMillis();
				long period = 12;
				
				while (globalReady)
				{
						renderMap();
					
						paintScreen();
						
						diff = System.currentTimeMillis() - startTime;
						
						sleepDuration = period - diff; 

						if (sleepDuration <= 0) 
							sleepDuration = 9; 
						
						try {Thread.sleep(sleepDuration); }
							catch(InterruptedException ex){}
						
						startTime = System.currentTimeMillis();
					
						
					
				}		
				return;
			}

			
			private void paintScreen()
			{
				
				Graphics2D map;
				
				
				try {
					
					map = (Graphics2D)getGraphics();
					if(map != null && bufferImage != null)
						map.drawImage(bufferImage, 0,0,null);
					
					

					Toolkit.getDefaultToolkit().sync();
					
					map.dispose();
					
					
					
					
				}
				catch (Exception e)
					{ 
						System.out.println("Error in PaintScreen step");
					}
			} 

			
			public void addNotify( )
			{
				super.addNotify( ); 
				renderThread = new Thread(null, this, "Map Display Thread");
				renderThread.setPriority(Thread.MAX_PRIORITY);
				renderThread.start();
			
			}
	
	}
	
	void drawString(Graphics g, String text, int x, int y) {
	    for (String line : text.split("\n"))
	        g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

}
