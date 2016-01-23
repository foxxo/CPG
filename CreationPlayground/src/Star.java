import java.awt.Color;
import java.util.Random;


public class Star extends Plot
{
	
	enum starType {BLACKHOLE, NEBULA, NEUTRON, BLUEGIANT, BLUE, REDGIANT, WHITE, YELLOW, ORANGE, REDDWARF, WHITEDWARF};
	starType myType;
	Orbital bodies[];
	int maxBodies = 6;
	
	Star(int x, int y, int s) 
	{
		super(x, y, s);
		myType = null;
		bodies = new Orbital[maxBodies];
	}
	
	Star(starType t, int x, int y, int s)
	{
		super(x, y, s);
		myType = t;
		bodies = new Orbital[maxBodies];
	}
	
	void generatePlanets()
	{
		
		if(myType.ordinal() >= 3)
		{
			Random randGen = new Random();
			for(int orbit = 0; orbit < maxBodies; orbit++)
			{
				if(myType == starType.REDGIANT && orbit < 3)	//Red giants have absorbed or burned off inner planets.
					continue;
				if(randGen.nextInt(3) > 0)
				{
					bodies[orbit] = new Planet(this, orbit);
					((Planet)(bodies[orbit])).createConditions();
					
					
				}
				
			}
		}
		
	}
	
	public String toString()
	{
		String text = "";
		if(myType == null)
			return text;

		text += myType.toString() + " STAR" + "\n"
			+"Planets:\n";
		for(int o = 0; o < maxBodies; o++)
		{
			text += "  " + (o +1) + ": ";
			if(bodies[o] != null)
			{
				text += ((Planet)bodies[o]).toString();
			}
			else
				text += "---------\n";
		}
		
		
		return text;
	}

}
