import java.util.ArrayList;
import java.util.Random;


public class Planet extends Orbital{

	static enum atmoGas {NONE, CARBONDIOXIDE, NITROGEN, OXYGEN, WATERVAPOR, METHANE};
	static enum planetSize {TINY, SMALL, MEDIUM, LARGE, HUGE};
	static enum planetEco {NONE, MICROBE, LICHEN, PLANT, ANIMAL, LUSH};
	static enum planetPressure {NONE, WISPY, THIN, NORMAL, THICK, SUPER};
	static enum planetTemp {FRIGID, COLD, MILD, WARM, HOT, HELLISH};
	static enum planetMin {NONE, NEGLIGIBLE, POOR, NORMAL, RICH, ULTRA};
	static enum planetGravity {LOW, NORMAL, HIGH};
	
	static final int maxTemp = 50, maxMin = 5, maxPress = 5, maxEco = 5, maxGrav = 2, maxWater = 5;
	static final int minTemp = 0, minMin = 0, minPress = 0, minEco = 0, minGrav = 0, minWater = 0;
	
	atmoGas atmosphere[];		//chemical composition of the atmosphere
	planetMin mineralBase;		//base level of minerals on the planet
	planetMin mineralAvail;		//how freely available those minerals are; affected by ecosystem, techs & developments
	planetEco ecology;			//complexity of the local ecosystem
	planetSize size;			//size of the body
	planetPressure pressure;	//how dense the atmosphere is
	planetTemp temperature;		//overall surface temperature; affected by atmospheric pressure & composition, solar heating, techs & developments
	int solarHeating, currentTemp, targetTemp, tempROC;			//heat delivered via insolation; based on star type and orbital position.
	planetGravity gravity;		//based primarily on size
	int water;			
	
	ArrayList<Map.planetaryEffect> localEffects;
	
	Planet(Star p, int x)
	{
		super(p,x);
		atmosphere = new atmoGas[5];
		for(int i = 0; i < 5; i++)
			atmosphere[i] = atmoGas.NONE;
		localEffects = new ArrayList<Map.planetaryEffect>();
	}
	
	void createConditions()		//Create the initial conditions on the planet.
	{
		Random randGen = new Random();
		
		localEffects.add(
				new Map.planetaryEffect() {
					
					int start;
					@Override
					public boolean checkExpiry() {
						
							return false;
					}
					
					@Override
					public void applyInitial() {		//Determine target temperature based on solar class and orbital position
						int heat = myParent.myType.ordinal();
						heat = 5 - heat/2;	//the heat output of the parent star
						heat *= 2;
						solarHeating = 1+heat - (position*2)/2;
						
						solarHeating *= 10;
						solarHeating += 10-randGen.nextInt(6);
						
						if(solarHeating > maxTemp)
							solarHeating = maxTemp;
						if(solarHeating < minTemp)
							solarHeating = minTemp;
						
						targetTemp = solarHeating;
						currentTemp = 50;
						
					}
					
					@Override
					public void applyIncremental() {	//Heats or cools the planet toward its target temperature and checks for evolution
						if(currentTemp != targetTemp)
						{
							tempROC = (targetTemp - currentTemp) / 10;
							
						}
						
						currentTemp += tempROC;
						
						temperature = planetTemp.values()[currentTemp/10];
						

						evolveLife();
										
						if( targetTemp > maxTemp)
								targetTemp = maxTemp;
					
					}
				}
		
		);
		
		localEffects.get(0).applyInitial();
		
		
		temperature = planetTemp.values()[currentTemp/10];
		
		mineralBase = planetMin.values()[randGen.nextInt(6)];
		size = planetSize.values()[randGen.nextInt(5)];
		
		//randomly assign water deposits
		int waterLevel = randGen.nextInt(21);
		if(waterLevel > 19)
			water = 5;
		else if(waterLevel > 17)
			water = 4;
		else if(waterLevel > 14)
			water = 3;
		else if(waterLevel > 10)
			water = 0;
		else if(waterLevel > 5)
			water = 2;
		else
			water = 1;
		
		gravity = planetGravity.values()[randGen.nextInt(3)];
		if(size.ordinal() < 3 && gravity.ordinal() > 0
				&& randGen.nextInt(3) == 1)
		{
			gravity = planetGravity.values()[gravity.ordinal() -1];
		}
		else if(size.ordinal() > 3 && gravity.ordinal() < 2
				&& randGen.nextInt(3) == 1)
		{
			gravity = planetGravity.values()[gravity.ordinal() +1];
		}
		else
		{
			int randWiggle = randGen.nextInt(2) -1;
			if(gravity.ordinal() + randWiggle >= 0 && gravity.ordinal() + randWiggle <= 2)
				gravity = planetGravity.values()[gravity.ordinal() + randWiggle];
		}
	
		//Maximum atmospheric pressure is limited by planet size and gravity
		int maxLocalPressure = Math.min(size.ordinal()+2, (gravity.ordinal()+1)*2);
		pressure = planetPressure.values()[Math.min(maxLocalPressure,randGen.nextInt(6))];
		
		if(pressure.ordinal() > 0)
		{	boolean hasGas = false;
			do{
				int randGas = randGen.nextInt(150);	//First item in the atmosphere array is the "base" or 70% of the atmosphere
				if(randGas < 100)
					atmosphere[0] = atmoGas.NITROGEN;
				else if(randGas < 110)
					atmosphere[0] = atmoGas.OXYGEN;
				else if(randGas < 120)
					atmosphere[0] = atmoGas.CARBONDIOXIDE;
				else if(randGas < 130)
					atmosphere[0] = atmoGas.WATERVAPOR;
				else if(randGas < 140)
					atmosphere[0] = atmoGas.METHANE;
				else
					atmosphere[0] = atmoGas.NONE;
				
				if( atmosphere[0] != atmoGas.NONE)
					hasGas = true;
				for(int i = 1; i < 3; i++)		//second and third gases are approx. 20% and 10%
				{								
					randGas = randGen.nextInt(150);
					if(randGas < 50)
						atmosphere[i] = atmoGas.NITROGEN;
					else if(randGas < 90)
						atmosphere[i] = atmoGas.OXYGEN;
					else if(randGas < 100)
						atmosphere[i] = atmoGas.CARBONDIOXIDE;
					else if(randGas < 110)
						atmosphere[i] = atmoGas.WATERVAPOR;
					else if(randGas < 120)
						atmosphere[i] = atmoGas.METHANE;
					else
						atmosphere[i] = atmoGas.NONE;
					
					if( atmosphere[i] != atmoGas.NONE)
						hasGas = true;
				}
												
					randGas = randGen.nextInt(150);		//fourth gas is 1 or less%
					if(randGas < 10)
						atmosphere[3] = atmoGas.NITROGEN;
					else if(randGas < 20)
						atmosphere[3] = atmoGas.OXYGEN;
					else if(randGas < 25)
						atmosphere[3] = atmoGas.CARBONDIOXIDE;
					else if(randGas < 35)
						atmosphere[3] = atmoGas.WATERVAPOR;
					else if(randGas < 40)
						atmosphere[3] = atmoGas.METHANE;
					else
						atmosphere[3] = atmoGas.NONE;
					
					if( atmosphere[3] != atmoGas.NONE)
						hasGas = true;
				
				
				randGas = randGen.nextInt(150);		//final gas is 1% or less
				if(randGas < 5)
					atmosphere[4] = atmoGas.NITROGEN;
				else if(randGas < 10)
					atmosphere[4] = atmoGas.OXYGEN;
				else if(randGas < 100)
					atmosphere[4] = atmoGas.CARBONDIOXIDE;
				else if(randGas < 130)
					atmosphere[4] = atmoGas.WATERVAPOR;
				else if(randGas < 140)
					atmosphere[4] = atmoGas.METHANE;
				else
					atmosphere[4] = atmoGas.NONE;
				
				if( atmosphere[4] != atmoGas.NONE)
					hasGas = true;
			}
			while(!hasGas);
		}
		
		
		//low surface pressure results in lower temperature if not in high sunlight
		if(pressure.ordinal() < 2 && solarHeating < 5 && temperature.ordinal() > minTemp)
			targetTemp -= 10;
		
		boolean greenhouse =false;		//Determine if the planet is under a greenhouse effect.
		{								//Greenhouse effect will take place if the atmosphere is over 20% water vapor
										//or contains significant CO2 or Methane.
			if(atmosphere[0] == atmoGas.CARBONDIOXIDE || 
					atmosphere[0] == atmoGas.METHANE ||
					atmosphere[0] == atmoGas.WATERVAPOR ||
					atmosphere[1] == atmoGas.CARBONDIOXIDE || 
					atmosphere[1] == atmoGas.METHANE ||
					atmosphere[1] == atmoGas.WATERVAPOR)
				greenhouse = true;
			int gh = 0;
			for(int i = 2; i < 5; i++)
				if(atmosphere[i] == atmoGas.CARBONDIOXIDE || atmosphere[i] == atmoGas.METHANE)
					gh++;
			if(gh > 1)
				greenhouse = true;
		}
		
		if(greenhouse && temperature.ordinal() < maxTemp)
			targetTemp += 10;
		
		if(pressure.ordinal() > 4 && temperature.ordinal() < maxTemp)		//High pressure atmospheres increase temperature
			targetTemp += 10;
		
		//boil off surface water
		if(temperature == planetTemp.HELLISH)
		{
			if(water > 0)
			{
				for(int a = 4; a >= 0; a--)
					if(atmosphere[a] == atmoGas.NONE)
						atmosphere[a] = atmoGas.WATERVAPOR;
				
				water--;
			}
		}
		
		
		ecology = planetEco.NONE;
				
				
		if( targetTemp > maxTemp)
			targetTemp = maxTemp;
	
	}
	
	String getBasicDesc()
	{
		String text = "";
		
		switch (water){
			case 0:
			{
				switch (temperature)
				{
				case COLD:
						text += "COLD, BARREN";
					break;
				case FRIGID:
						text += "FRIGID, BARREN";
					break;
				case HELLISH:
						text += "HELLISH, LAVA";
					break;
				case HOT:
						text += "HOT, DESERT";
					break;
				case MILD:
						text += "MILD, BARREN";
					break;
				case WARM:
						text += "WARM, DESERT";
					break;
				default:
					break;
				
				}
			}break;
			case 1:
			{
				switch(temperature)
				{
					case COLD:
						text += "COLD, BARREN";
					break;
					case FRIGID:
							text += "FRIGID, ROCKY";
						break;
					case HELLISH:
							text += "HELLISH, LAVA";
						break;
					case HOT:
							text += "HOT, DESERT";
						break;
					case MILD:
							text += "MILD, ROCKY";
						break;
					case WARM:
							text += "WARM, BARREN";
						break;
					default:
						break;
				}
			}break;
			case 2:
			{
				switch(temperature)
				{
					case COLD:
						text += "COLD, TUNDRA";
					break;
					case FRIGID:
							text += "FRIGID, ICE";
						break;
					case HELLISH:
							text += "HELLISH, GREENHOUSE";
						break;
					case HOT:
						{
							text += "HOT, ";
							if(ecology.ordinal() < planetEco.PLANT.ordinal())
								text += "CONTINENTAL";
							else
								text += "TERRAN";
						}
						break;
					case MILD:
					{
						text += "MILD, ";
						if(ecology.ordinal() < planetEco.PLANT.ordinal())
							text += "CONTINENTAL";
						else
							text += "TERRAN";
					}
						break;
					case WARM:
					{
						text += "WARM, ";
						if(ecology.ordinal() < planetEco.PLANT.ordinal())
							text += "CONTINENTAL";
						else
							text += "TERRAN";
					}
						break;
					default:
						break;
				}
			}break;
			case 3:
			{
				switch(temperature)
				{
					case COLD:
						text += "COLD, TUNDRA";
					break;
					case FRIGID:
							text += "FRIGID, ICE";
						break;
					case HELLISH:
							text += "HELLISH, GREENHOUSE";
						break;
					case HOT:
					{
						text += "HOT, ";
						if(ecology.ordinal() < planetEco.PLANT.ordinal())
							text += "ARCHIPALEGO";
						else
							text += "TERRAN ARCHIPALEGO";
					}
						break;
					case MILD:
					{
						text += "MILD, ";
						if(ecology.ordinal() < planetEco.PLANT.ordinal())
							text += "ARCHIPALEGO";
						else
							text += "TERRAN ARCHIPALEGO";
					}
						break;
					case WARM:
					{
						text += "WARM, ";
						if(ecology.ordinal() < planetEco.PLANT.ordinal())
							text += "ARCHIPALEGO";
						else
							text += "TERRAN ARCHIPALEGO";
					}
						break;
					default:
						break;
				}
			}break;
			case 4:
			{
				switch(temperature)
				{
					case COLD:
						text += "COLD, TUNDRA";
					break;
					case FRIGID:
							text += "FRIGID, ICE";
						break;
					case HELLISH:
							text += "HELLISH, GREENHOUSE";
						break;
					case HOT:
							text += "HOT, OCEAN";
						break;
					case MILD:
							text += "MILD, OCEAN";
						break;
					case WARM:
							text += "WARM, OCEAN";
						break;
					default:
						break;
				}
			}break;
			case 5:
			{
				switch(temperature)
				{
					case COLD:
						text += "COLD, ICE";
					break;
					case FRIGID:
							text += "FRIGID, ICE";
						break;
					case HELLISH:
							text += "HELLISH, GREENHOUSE";
						break;
					case HOT:
							text += "HOT, OCEAN";
						break;
					case MILD:
							text += "MILD, OCEAN";
						break;
					case WARM:
							text += "WARM, OCEAN";
						break;
					default:
						break;
				}
			}break;
			default:
				break;
		
		}
		
		return text;
		
	}
	
	public String toString()
	{
		String text = "";
		
		text += size.toString() + " " + getBasicDesc() + " PLANET\n";
		switch(gravity)
		{
		case HIGH:
			text += "HIGH-G\n";
			break;
		case LOW:
			text += "LOW-G\n";
			break;
		default:
			break;
		}
		
		text += "ATMOSPHERE: " + pressure.toString() + "\n";
		if(pressure != planetPressure.NONE)
		{
			text += "(";
			for(int a = 0; a < 5; a++)
			{
				switch(atmosphere[a])
				{
				case CARBONDIOXIDE:
						text += "CO2";
					break;
				case METHANE:
						text += "METH";
					break;
				case NITROGEN:
					text += "NITRO";
					break;
				case NONE:
					text += "---";
					break;
				case OXYGEN:
					text += "OXY";
					break;
				case WATERVAPOR:
					text += "H20";
					break;
				default:
					break;
				}
				if (a < 4)
					text += "/";
			}
		}
		
		text += "\n";
		switch(ecology)
		{
		case ANIMAL:
			text += "ANIMAL LIFE!";
			break;
		case LICHEN:
			text += "LICHEN";
			break;
		case LUSH:
			text += "LUSH ECOSYSTEM!";
			break;
		case MICROBE:
			text += "MICROBIAL LIFE";
			break;
		case NONE:
			break;
		case PLANT:
			text += "PLANT LIFE!";
			break;
		default:
			break;
		
		}
		text += "\n";
		return text;
	}
	
	void evolveLife()
	{
		//let's create life!
		
		//Each stage of ecological complexity has its own set of requirements that must be met, including temperature, 
		//the presence of water, and atmospheric pressure.
		//This simulation assumes advanced life can evolve in any of the available atmospheric compositions.

		
		Random randGen = new Random();
		planetEco nextEcoLevel = planetEco.NONE;
		if(temperature.ordinal() > minTemp && water > minWater && (randGen.nextInt(8) == 1 || ecology.ordinal() > planetEco.NONE.ordinal()))
		{
			//Microbial life requires at least some water and non-frigid temperatures.
			nextEcoLevel = planetEco.MICROBE;
		
			if(temperature.ordinal() < maxTemp && pressure.ordinal() > minPress && (randGen.nextInt(2) == 1 || ecology.ordinal() > planetEco.MICROBE.ordinal()))
			{
				//Lichen requires non-vacuum atmospheric pressure and less than hellish temperatures.
				nextEcoLevel = planetEco.LICHEN;
	
				if(water > 1 && temperature.ordinal() > 2 && pressure.ordinal() > 2 && (randGen.nextInt(3) == 1 ||  ecology.ordinal() > planetEco.LICHEN.ordinal()))
				{
					//Plant life requires additional water, heat, and atmospheric pressure
					nextEcoLevel = planetEco.PLANT;

					if(pressure.ordinal() < maxPress && (randGen.nextInt(4) != 1)  || ecology.ordinal() > planetEco.PLANT.ordinal())		
					{
						//Animal life requires non-crushing pressures
						nextEcoLevel = planetEco.ANIMAL;
						
						//And a "Lush" ecosystem can develop out of any Animal-level ecosystem 
						//(barring future implementation of sapient population development and pollution)
						if(randGen.nextInt(4) == 1  || ecology.ordinal() > planetEco.ANIMAL.ordinal())
							nextEcoLevel = planetEco.LUSH;
					}
				}
			}
			ecology = nextEcoLevel;
		}
	}
	

	void updateConditions()
	{
		localEffects.get(0).applyIncremental();
		
	}
		

	
	
}
