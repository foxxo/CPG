import java.awt.Color;


public class Plot {
	
	int xCoord, yCoord; 	//coordinates in grid
	int xPixel, yPixel;		//coordinates in pixel space
	Color shade;			//color on-screen
	String label;			//annotation on-screen (if any)
	
	Plot()
	{}
	
	Plot(int x, int y, int s)
	{
		xCoord = x; yCoord = y;
		shade = Color.BLACK;
		label = "";
		xPixel = xCoord * s;
		yPixel = yCoord * s;
	}
	
	public void changeColor(Color newShade, long pauseTime)
	{
		shade = newShade;
		try{
			Thread.sleep(pauseTime);
		}
		catch( Exception e)
		{
			
		}
	}
	

}
