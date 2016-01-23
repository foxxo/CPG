import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;


public class Map {

	int width, height;		//dimensions of the map
	int pSize;			//size of a plot in pixels
	Star myGrid[][];	//2-dimensional grid of plots (technically, Stars since we added the Galaxy Map)
	
	String statusReport;	//reports on the current step of world creation
	Random randGen;
	
	static Color darkGreen = new Color(0, 112, 0);
	static Color purple = new Color(112, 0, 112);
	static Color brown = new Color(139, 69, 19);
	static Color olive = new Color(107,142,35);
	static Color brightGreen = new Color(0,255,0);
	
	Map(int w, int h, int s)
	{
		statusReport = "Empty World";
		width = w; height = h;
		pSize = s;
		myGrid = new Star[width][height];
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				myGrid[x][y] = new Star(x,y,s);
		
		randGen = new Random();
	}
	
	
	/*addToList functions are helper functions to grow an ArrayList of Plots without adding duplicates.
	 * 
	 */
	
	void addToList(ArrayList<Plot> list, Plot p)	//add a single plot
	{
		if(!list.contains(p))
			list.add(p);
	}
	
	void addToList(ArrayList<Plot> list, ArrayList<Plot> add)	//add a series of plots
	{
		for (Plot p : add )
		{
			addToList(list, p);
		}		
	}
	
	
	
	/*Begin helper functions for World Generation 
	 * 
	 */
	
	//this smoothing function iterates over a specified set of Plots, possibly
	//swapping them from the "base" color to the "top" color
	//based on the number of "top" Plots touching it and a d100 roll.
	//Acts as a one-way "erosion"
	void erode(ArrayList<Plot> list, Color base, Color top, int contactThreshold, int randThreshold)
	{
		//contactThreshold represents the number of Top color plots touching each Base colored plot required
		//to trigger a possible color flip.
		//randThreshold is the percentage chance each targetted plot will flip.
		
		ArrayList<Plot> targets = new ArrayList<Plot>();
		for( Plot p : list)
		{
			if(p.shade == base && countNeighborsWithColor(p, top) >= contactThreshold)
				targets.add(p);	
		}

		for(Plot p : targets)
			if(randGen.nextInt(100) <= randThreshold)
				p.changeColor(top, 1);
	}
	
	//this smoothing function is back/fore-agnostic - it performs two-way
	//"erosion" to a single plot. (That is, it can swap from background to 
	//foreground or vice versa, based solely on the number of nearby 
	//Plots of that color.
	void smooth(Plot p, Color back, Color fore, int contactThreshold)
	{
		//while Erode only changes "base" plots into "top" plots, and randomly at that,
		//Smooth WILL flip any plot of either color that is touching contactThreshold
		//plots of the opposite color.
		
			int surroundingDifferences = 0;
			for(Plot n : getNeighbors(p))
			{
				if((n.shade == back && p.shade == fore) || (n.shade == fore && p.shade == back))
					surroundingDifferences++;
			}
			if(surroundingDifferences >= contactThreshold)
				if(p.shade == back)
					p.changeColor(fore, 1);
				else if (p.shade == fore)
					p.changeColor(back,  1);	
	}
	
	//Simple function to return all the Plots touching the parameter Plot
	ArrayList<Plot> getNeighbors(Plot p)
	{
		ArrayList<Plot> neighbors = new ArrayList<Plot>();
		
		if(p.xCoord > 0)
		{
			if(p.yCoord > 0)
				neighbors.add(myGrid[p.xCoord -1][p.yCoord -1]);
			if(p.yCoord < height-1)
				neighbors.add(myGrid[p.xCoord -1][p.yCoord +1]);
			neighbors.add(myGrid[p.xCoord -1][p.yCoord]);
		}
		if(p.xCoord < width-1)
		{
			if(p.yCoord > 0)
				neighbors.add(myGrid[p.xCoord +1][p.yCoord -1]);
			if(p.yCoord < height-1)
				neighbors.add(myGrid[p.xCoord +1][p.yCoord +1]);
			neighbors.add(myGrid[p.xCoord +1][p.yCoord]);
		}
		if(p.yCoord > 0)
			neighbors.add(myGrid[p.xCoord][p.yCoord -1]);
		if(p.yCoord < height-1)
			neighbors.add(myGrid[p.xCoord][p.yCoord +1]);
		
		
		return neighbors;
	}
	
	//Returns all plots in a circle around the center Plot
	ArrayList<Plot> getPlotsInRadius(Plot p, int radius)
	{
		ArrayList<Plot> circle = new ArrayList<Plot>();
		 
		circle = getPlotsInSquare(p, radius * 2);
		for(int i = circle.size(); i >= 0; i--)
		{
			if(i >= circle.size())
				continue;
			
			if(getDistance(p, circle.get(i)) > radius)
				circle.remove(i);
		}
		return circle;
		
	}
	
	//Simply returns the distance (rounded) between two plots
	int getDistance(Plot a, Plot b)
	{
		int d = 0;
		d = (int) Math.sqrt( Math.pow(a.xCoord - b.xCoord, 2) + Math.pow(a.yCoord - b.yCoord, 2) );
		
		return d;
	}
	
	//Returns a list of Plots in a square with sides length r around the center Plot p
	//note that a truly "centered" square will have odd sized sides
	//even-lengthed squares will extend further in the positive direction
	ArrayList<Plot> getPlotsInSquare(Plot p, int r)
	{
		ArrayList<Plot> box = new ArrayList<Plot>();
		
		for(int x = p.xCoord - r/2; x < p.xCoord + r/2+1; x++)
			for(int y = p.yCoord - r/2; y < p.yCoord + r/2+1; y++)
			{
				if(x < 0 || y < 0 || x >= width || y >= height)
					continue;
				box.add(myGrid[x][y]);
			}
		return box;
	}
	
	//returns a random Plot from the grid
	Plot getRandomPlot()
	{
		return(myGrid[randGen.nextInt(width)][randGen.nextInt(height)]);
	}
	
	//returns a random Plot from a given list
	Plot getRandomPlot(ArrayList<Plot> group)
	{
		return(group.get(randGen.nextInt(group.size())));
	}
	
	//returns a random plot within a rectangular selection of the grid
	Plot getRandomPlot(int startX, int endX, int startY, int endY)
	{
		int dX = endX - startX;
		int dY = endY - startY;
		
		return myGrid[startX + randGen.nextInt(dX)][startY + randGen.nextInt(dY)];
		
	}
	
	//Counts Plots of a given color in contact with the center Plot
	int countNeighborsWithColor(Plot p, Color checking)
	{
		ArrayList<Plot> n = getNeighbors(p);
		int count = 0;
		for(Plot x : n)
		{
			if(x.shade == checking)
				count++;
		}
		return count;
	}
	
		
	
	//Creates a simple green-and-blue map of continents on an ocean.
	void simpleContinentMap()
	{
		statusReport = "Loading Map for Processing";
		
		ArrayList<Plot> fullMap = new ArrayList<Plot>();	//a temporary holding list for the map
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{	
				fullMap.add(myGrid[x][y]);
			}
		
		statusReport = "Clearing Labels";
		for(Plot p : fullMap)
		{
			p.label = "";
		}
		
		statusReport = "Creating Sea";	//every plot begins as "ocean" (blue)
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				myGrid[x][y].changeColor(Color.BLUE, 0);
				
			}
	
		statusReport = "Planting Continents";
		//Begin creating continents by placing "seed" islands in each quadrant of the map.
		
		int reps = 2;	//number of seed islands in each quadrant
		Plot seed;		//center of each island
		ArrayList<Plot> continent;	//list for each island
		
		do{		//in this case the initial seed islands are radius 4-14
			seed = getRandomPlot(0, width / 2, 0, height / 2);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 4);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			
			seed = getRandomPlot(width / 2, width-1, 0, height /2);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 4);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			
			seed = getRandomPlot(0, width /2, height /2, height -1);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 4);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			
			seed = getRandomPlot(width / 2, width-1, height /2, height -1);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 4);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			reps--;
		}
		while(reps > 0);
		
	
		
		statusReport = "Extending Continents";
		//once the initial seeds are placed, we randomly select plots on coasts and grow 
		//the continent out from there. Again, we attempt to add a small semblance of balance by 
		//performing this process in each quadrant of the map.
		
		int numConts = 5;	//number of times in each quadrant to grow out the existing landmass
		do{
			//in this case the "growths" will be circles of radius 6-16.
			do{
				seed = getRandomPlot(0, width / 2, 0, height / 2);
			}while (seed.shade == Color.BLUE || countNeighborsWithColor(seed, Color.BLUE) < 1);
			
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 6);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			do{
				seed = getRandomPlot(width / 2, width-1, 0, height /2);
			}while (seed.shade == Color.BLUE || countNeighborsWithColor(seed, Color.BLUE) < 1);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 6);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			
			do{
				seed = getRandomPlot(0, width /2, height /2, height -1);
			}while (seed.shade == Color.BLUE || countNeighborsWithColor(seed, Color.BLUE) < 1);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 6);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			
			do{
				seed = getRandomPlot(width / 2, width-1, height /2, height -1);
			}while (seed.shade == Color.BLUE || countNeighborsWithColor(seed, Color.BLUE) < 1);
			continent = getPlotsInRadius(seed, randGen.nextInt(10) + 6);
			for(Plot c : continent)
				c.changeColor(Color.GREEN, 1);
			numConts--;
		}
		while(numConts > 0);
	
		statusReport = "Eroding";	//Once the continents' shapes are formed, we do a quick erosion.
		//This erosion converts some land to sea with a low contact threshold and a better than 50/50 chance.
		//We want to roughen up the round edges created by an all-circular continental growth pattern.
		erode(fullMap, Color.GREEN, Color.BLUE, 3, 60);
		
		statusReport = "Randomizing Land";	//To further roughen things up, we quickly spray a series of 
		//small square islands across the map. Any of these that are isolated will be quickly eroded away
		//in the next stage, but some will cause alterations in the outlines of the continents or even combine to form
		//islands large enough to survive erosion.
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				
				int c = randGen.nextInt(100);	//every plot has a 1% chance to spawn a mini-island.
				
				if(c == 1)
				{
					myGrid[x][y].changeColor(Color.GREEN, 1);
					ArrayList<Plot> block = getNeighbors(myGrid[x][y]);
					for (Plot p : block)
					{
						p.changeColor(Color.GREEN, 1);
					}
				}
				
			}
		
		statusReport = "Final Erosion)";	//Another two passes of land-to-sea erosion.
		//The first is low threshold, 50/50 chance, and the second has a slightly higher
		//threshold but a higher chance. This is to eliminate outliers.
		
		erode(fullMap, Color.GREEN, Color.BLUE, 3, 50);
		erode(fullMap, Color.GREEN, Color.BLUE, 4, 75);
	
		
		
		statusReport = "Smoothing";		//The final stage is two rounds of low threshold,
		//direction-agnostic smoothing to remove some of the "pixelated" effect of the 
		//random generation, hopefully giving us smooth, real-looking continent shapes.
		for(Plot p : fullMap)
			smooth( p, Color.GREEN, Color.BLUE, 3);
		
		for(Plot p : fullMap)
			smooth( p, Color.GREEN, Color.BLUE, 3);
	}
	
	//creates a simple map of interconnected rooms
	void simpleDungeonMap()
	{
		statusReport = "Loading Map for Processing";
		
		ArrayList<Plot> fullMap = new ArrayList<Plot>();	//temporary holding list for the map
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{	
				fullMap.add(myGrid[x][y]);
			}
		
		statusReport = "Clearing Labels";
		for(Plot p : fullMap)
		{
			p.label = "";
		}
		
		statusReport = "Laying Dirt";	//Begin delving the dungeon in a fully underground layer.
		for(Plot p: fullMap)
			p.changeColor(Color.BLACK, 0);
		

		
		statusReport = "Digging Rooms";		//We begin by excavating the rooms.
		
		ArrayList<Plot> roomStarts = new ArrayList<Plot>();		//We will want to remember the centers of each room for later.
		int numRooms = randGen.nextInt(6) + 6;					//Target number of rooms (currently 6-11)
		while(numRooms > 0)
		{
			//select a random plot at least 7 plots away from any of the map edges and grab a square around it of size 5-15.
			ArrayList<Plot> room = getPlotsInSquare(getRandomPlot(7, width-7, 7, height -7), randGen.nextInt(10) + 5);
			
			for(Plot p : room)		//dig the room itself
				p.changeColor(Color.DARK_GRAY, 1);
			for(Plot p : room)		//mark all "walls" as gray. (This implementation "grows" the walls inward.)
				if(countNeighborsWithColor(p, Color.BLACK) > 0)
					p.changeColor(Color.GRAY, 1);
			
			numRooms--;
			roomStarts.add(getRandomPlot(room));
		}
		
		statusReport = "Digging Tunnels";		//Once the rooms are placed, connect their centers with tunnels.
		for(Plot s : roomStarts)	//For each room's center...
		{
			int numTunnels = randGen.nextInt(1) + 1;	//...dig 1-2 tunnels to other rooms
			do{
				Plot start, end;
				
					start = s;								//The start of each tunnel is the current room's center.
					do
						end = getRandomPlot(roomStarts);	//The end is the center of a random second room.
					while(end == start);
					
					ArrayList<Plot> tunnel = new ArrayList<Plot>();
					tunnel.add(start);
					
					
					//the increment variables keep track of whether the endpoint is above or below and left or right of the start.
					int xIncrement =Integer.signum(end.xCoord - start.xCoord);
					int yIncrement = Integer.signum(end.yCoord - start.yCoord);
					int tunnelX = start.xCoord;
					int tunnelY = start.yCoord;
					
					while(tunnelX != end.xCoord)			//We simply cut horizontally first until we're lined up...
					{
						tunnel.add(myGrid[tunnelX][tunnelY]);
						tunnelX+= xIncrement;
					}
				
					while(tunnelY != end.yCoord)			//Then cut vertically.
					{
						tunnel.add(myGrid[tunnelX][tunnelY]);
						tunnelY+= yIncrement;
					}
					
					for(Plot p : tunnel)					//We cut the "walls" outward from the tunnel spaces unlike the rooms.
					{
						p.changeColor(Color.DARK_GRAY, 1);
						for(Plot n :getNeighbors(p))
							if(n.shade == Color.BLACK)
								n.changeColor(Color.GRAY, 0);	
					}
					numTunnels--;
				}
				while(numTunnels >= 0);
			}

	}
	
	//creates a map of stars with accompanying planets
	void simpleGalaxyMap()
	{
		statusReport = "Loading Map for Processing";
		
		ArrayList<Star> fullMap = new ArrayList<Star>();	//holding list for the map. In this case each "plot" is 
															//actually a Star object (even empty space.)
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{	
				fullMap.add(myGrid[x][y]);
			}
		
		statusReport = "Clearing Labels";
		for(Plot p : fullMap)
		{
			p.label = "";
		}
		
		statusReport = "Emptying Space";
		
		for(Star p: fullMap)
		{
			
			p.myType = null;
			p.bodies = new Orbital[p.maxBodies];
			p.changeColor(Color.BLACK,  0);
			
		}
		
		statusReport = "Placing Stars";
		
		ArrayList<Plot> verbotten = new ArrayList<Plot>();				//the verbotten list designates all space that cannot 
																		//accomodate a star. It is initially assigned the plots
																		//near to the edges of the map.
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				if(x < 5 || x > width - 6 || y < 5 || y > height - 6)
					verbotten.add(myGrid[x][y]);
		
		for(Star p: fullMap)
			if(randGen.nextInt(100) == 1 && !verbotten.contains(p))		//Each plot has a 1% chance to form a Star.
			{
				//(Roughly) actual class distributions (in %) A - 1, F - 3, G - 8, K - 12, M - 75	
				//Black Hole, Nebula, White Dwarf, Neutron Star, Blue Giant
				
				int starType = randGen.nextInt(104);					//Dice roll for the star's class.
				
				if(starType < 60)										//roughly 60% chance to form an M class red dwarf.
				{
					p.myType = Star.starType.REDDWARF;
					p.changeColor(Color.RED, 1);
					myGrid[p.xCoord+1][p.yCoord].changeColor(Color.RED, 0);
					myGrid[p.xCoord-1][p.yCoord].changeColor(Color.RED, 0);
					myGrid[p.xCoord][p.yCoord+1].changeColor(Color.RED, 0);
					myGrid[p.xCoord][p.yCoord-1].changeColor(Color.RED, 0);
					
					
				}
				else if(starType < 70)								//Roughly 10% chance to form a K class orange star.
				{
					p.myType = Star.starType.ORANGE;
					p.changeColor(Color.RED, 1);
					myGrid[p.xCoord+1][p.yCoord].changeColor(Color.ORANGE, 0);
					myGrid[p.xCoord-1][p.yCoord].changeColor(Color.ORANGE, 0);
					myGrid[p.xCoord][p.yCoord+1].changeColor(Color.ORANGE, 0);
					myGrid[p.xCoord][p.yCoord-1].changeColor(Color.ORANGE, 0);
				}
				else if(starType < 85)								//Roughly 15% chance to form a G class yellow star.
				{
					p.myType = Star.starType.YELLOW;
					p.changeColor(Color.ORANGE, 1);
					myGrid[p.xCoord+1][p.yCoord].changeColor(Color.YELLOW, 0);
					myGrid[p.xCoord-1][p.yCoord].changeColor(Color.YELLOW, 0);
					myGrid[p.xCoord][p.yCoord+1].changeColor(Color.YELLOW, 0);
					myGrid[p.xCoord][p.yCoord-1].changeColor(Color.YELLOW, 0);
				}
				else if(starType < 93)								//Roughly 8% chance to form an F class White star.
				{
					p.myType = Star.starType.WHITE;
					p.changeColor(Color.WHITE, 1);
					myGrid[p.xCoord+1][p.yCoord].changeColor(Color.WHITE, 0);
					myGrid[p.xCoord-1][p.yCoord].changeColor(Color.WHITE, 0);
					myGrid[p.xCoord][p.yCoord+1].changeColor(Color.WHITE, 0);
					myGrid[p.xCoord][p.yCoord-1].changeColor(Color.WHITE, 0);
				}
				else if(starType < 96)								//Roughly 3% chance to form an A class blue star.
				{
					p.myType = Star.starType.BLUE;
					p.changeColor(Color.CYAN, 1);
					myGrid[p.xCoord+1][p.yCoord].changeColor(Color.CYAN, 0);
					myGrid[p.xCoord-1][p.yCoord].changeColor(Color.CYAN, 0);
					myGrid[p.xCoord][p.yCoord+1].changeColor(Color.CYAN, 0);
					myGrid[p.xCoord][p.yCoord-1].changeColor(Color.CYAN, 0);
				}
				else if(starType <98)								//Roughly 2% chance to form a Red Giant.
				{
					p.myType = Star.starType.REDGIANT;
					for(Plot n : getPlotsInRadius(p, 2))
						n.changeColor(Color.PINK, 0);
					for(Plot n : getPlotsInRadius(p, 1))
						n.changeColor(purple, 0);
					p.changeColor(Color.RED, 0);
				}
				else												//Roughly 5% chance to be a special star type
				{
					int specialType = randGen.nextInt(5);
					switch(specialType)
					{
						case 0:	//black hole
						{
							p.myType = Star.starType.BLACKHOLE;
							for(Plot n : getPlotsInRadius(p, 6))
								n.changeColor(Color.MAGENTA, 0);
							for(Plot n : getPlotsInRadius(p, 5))
								n.changeColor(Color.DARK_GRAY, 0);
							p.changeColor(Color.BLACK, 0);
						}
						break;
						case 1:	//Blue Giant
						{
							p.myType = Star.starType.BLUEGIANT;
							for(Plot n : getPlotsInRadius(p, 2))
								n.changeColor(Color.BLUE, 0);
							for(Plot n : getPlotsInRadius(p, 1))
								n.changeColor(Color.CYAN, 0);
							p.changeColor(Color.WHITE, 0);
						}
						break;
						case 2: //White Dwarf
						{
							p.myType = Star.starType.WHITEDWARF;
							p.changeColor(Color.WHITE, 0);
						}
						break;
						case 3: //Neutron Star
						{
							p.myType = Star.starType.NEUTRON;
							p.changeColor(Color.LIGHT_GRAY, 0);
						}
						break;
						case 4: //Nebula
						{
							p.myType = Star.starType.NEBULA;
							ArrayList<Plot> cloud = new ArrayList<Plot>();
							//A nebula expands out 8 plots from its center
							addToList(cloud, getPlotsInRadius(p, 8));
							int wisps = randGen.nextInt(4) + 3;
							//"wisps" of nebula gas extend out from the edges of the cloud
							while(wisps >= 0)
							{
								Plot n;
								do
									n = getRandomPlot(cloud);
								while(cloud.containsAll(getNeighbors(n)));
								addToList(cloud, getPlotsInRadius (n, 4));
								wisps--;
							}
							
							for(Plot c : cloud)
								c.changeColor(purple, 0);
							
							//Do a quick erosion pass to roughen up the edge of the cloud.
							erode(cloud, purple, Color.BLACK, 3, 50);
							
						}
						break;
					}
				}
				
				for(Plot n : getPlotsInRadius(p, 12))	//Do not allow any stars within 12 plots of each other.
				{
					addToList(verbotten, n);
				}
				
				p.generatePlanets();	//Once the star is generated, create its orbiting planets.
				
				//Then make them actually appear on the map. (Since planets are not Plots, the plots that display them on the map
				//must be color-changed to show them.)
				for(int i = 0; i < p.maxBodies; i++)
				{
					if(p.bodies[i] != null)
						displayPlanet((Planet)p.bodies[i]);
				}	
				
				
				
			}
		
		for(int i = 0; i < 25; i++)		//Advance the galaxy 25 times to allow the planets to cool and life to evolve.
		{
			nextTurn();
			
		}
		
		statusReport = basicGalaxyStats();
	}
	
	
	void displayPlanet(Planet p)		//Function to set the Plot that represents a given Planet to the appropriate color.
	{
		int offsetX, offsetY;		//Number of plots offset from the position of the planet's star depends on the planet's
									//orbital position.
		switch(p.position)
		{
		case 0:
			offsetX = -1; offsetY = -3;
		break;
		case 1:
			offsetX = 1; offsetY = -3;
		break;
		case 2:
			offsetX = 3; offsetY = 0;
		break;
		case 3:
			offsetX = 1; offsetY = 3;
		break;
		case 4:
			offsetX = -1; offsetY = 3;
		break;
		case 5:
			offsetX = -3; offsetY = 0;
		break;
		default:
			offsetX = 0; offsetY = 0;
		}
		
		Color planetColor = null;
		
		switch(p.temperature)	//Initial planet color is based solely on temperature.
		{
		case COLD:
			planetColor = Color.CYAN;
			break;
		case FRIGID:
			planetColor = Color.WHITE;
			break;
		case HELLISH:
			planetColor = Color.RED;
			break;
		case HOT:
			planetColor = Color.ORANGE;
			break;
		case MILD:
			planetColor = Map.brown;
			break;
		case WARM:
			planetColor = Color.YELLOW;
			break;
		default:
			break;
		}
		
		//...However, if a planet has life, that trumps temperature.
		if(p.ecology == Planet.planetEco.LUSH)
			planetColor = Map.brightGreen;
		else if(p.ecology == Planet.planetEco.ANIMAL)
			planetColor = Map.darkGreen;
		else if(p.ecology == Planet.planetEco.PLANT)
			planetColor = Map.olive;
		else if(p.ecology == Planet.planetEco.LICHEN)
			planetColor = Color.MAGENTA;
		else if(p.ecology == Planet.planetEco.MICROBE)
			planetColor = Map.purple;
		
		myGrid[p.myParent.xCoord + offsetX][p.myParent.yCoord + offsetY].changeColor(planetColor, 3);	//Set the appropriate plot's color

	}

	
	
	//creates a basic landscape with mountains, forests, and water features
	void simpleLandscapeMap()
	{
		statusReport = "Loading Map for Processing";
		
		ArrayList<Plot> fullMap = new ArrayList<Plot>();		//temporary list to hold the map
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{	
				fullMap.add(myGrid[x][y]);
			}
		
		statusReport = "Clearing Labels";
		for(Plot p : fullMap)
		{
			p.label = "";
		}
		
		statusReport = "Placing Land";		//Begin with an all-green map.
		
		for(Plot p : fullMap)
			p.changeColor(Color.GREEN, 0);
		
		statusReport = "Scattering Mountains";			//Randomly grow a few mountains across the landscape. 
		ArrayList<Plot> peaks = new ArrayList<Plot>();	//use the Peaks list to ensure mountains don't overgrow each other.
		for(Plot p: fullMap)
			if(randGen.nextInt(2000) == 1 && !peaks.contains(p))
			{
				
				for(Plot n : getPlotsInRadius(p, randGen.nextInt(6) + 4))		//the base of the mountain is radius 4-10
				{
					n.changeColor(Color.GRAY, 1);
					peaks.add(n);
					
				}
				for(Plot peak: getPlotsInRadius(p, randGen.nextInt(2)+ 1))		//The actual "peak" is radius 1-3.
				{	
					peak.changeColor(Color.WHITE, 1);
					peaks.add(peak);
				}
					
			}
		statusReport = "Eroding Mountains";		//Once the mountains are laid, erode them slightly so they're not so round.
			
		erode(peaks, Color.GRAY, Color.WHITE, 3, 35);		//Initial erosion grows the peak downward at a low %.
		erode(peaks, Color.WHITE, Color.GRAY, 3, 25);		//Next erosion does the opposite.
		erode(fullMap, Color.GREEN, Color.GRAY, 1, 75);		//Final erosion aggeressively grows the base of the mountain outward.

		
		statusReport = "Flowing Rivers";		//Create rivers in blue.
		
		int targetRivers = 2 + randGen.nextInt(3);		//There should be 2-5 rivers on the map.
		ArrayList<Plot> validFountains = new ArrayList<Plot>();		//All rivers should start from a valid "fountain"/"spring"
		Plot nextFork = null;							//Rivers may fork; 
		
		for(Plot p : fullMap)
			if(p.shade == Color.GREEN && countNeighborsWithColor(p, Color.GRAY) > 0)
				validFountains.add(p);						//Valid springs will be on the edge of mountains.
		
		do{
			ArrayList<Plot> river = new ArrayList<Plot>();
			int desiredFlow = randGen.nextInt(40) + 40;		//Rivers should be 40-80 tiles in length.
			int backflow = -2;	//"backflow" is used to prevent rivers from flowing backwards into themselves. It will increment
			//with every pass through the flowing loop and all tiles in its radius from the river's origin will be forbidden.
			//We initialize it as -2 so that the initial river can get out away from the exclusion radius before we start locking down plots.
				
			Plot fount;		//The start point for this river. Will be the designated forking point, or a randomly selected spring plot.
			
			if(nextFork != null)	
			{
				fount = nextFork;
				nextFork = null;
			}
			else
				fount = getRandomPlot(validFountains);
			
			river.add(fount);
			Plot lastFlow = fount;		//lastFlow tracks the last place the river flowed to, so we can know which direction it is flowing.
			Plot fork = fount;			//fork tracks the current river's origin (whether actually a fork, or a spring)
			//This is used to prevent backflow.
			
			boolean deadEnd = false;	//we will detect whether the river has come up against a mountain or map edge.
			ArrayList<Plot> forbidden = new ArrayList<Plot>();	//To keep the river flowing, we will outlaw plots 
			
			while(desiredFlow > 0 && !deadEnd)
			{

				
				Plot nextFlow = lastFlow;	
				
				
				deadEnd = true;
				for(Plot n : shuffle(getNeighbors(lastFlow)))	//Check that there are valid destinations to continue flowing into
				{
					if(n.shade == Color.GREEN && !forbidden.contains(n))
					{
						deadEnd = false;
						nextFlow = n;
						break;
					}
				}
				
				
				if(deadEnd)		//If the river has flowed into a dead-end, stop trying to flow further.
					break;
				
				nextFlow.changeColor(Color.BLUE, 5);	//Flow into the current target plot
				for(Plot f : getNeighbors(lastFlow))	//Then forbid all of the tiles adjacent to the river's previous position.
				{	
					addToList(forbidden, f);
					if(f.shade == Color.GREEN)
					{
						
						if(getNeighbors(nextFlow).contains(f))	//"flow" into all tiles adjacent to both current and previous positions.
							f.changeColor(Color.BLUE, 5);
					}
				}
				
				addToList(forbidden, nextFlow);					//forbid current position
				addToList(forbidden, getPlotsInRadius(fork, backflow));	//forbid plots in radius from the river's source
				backflow++;										//increment the backflow prevention radius
				
				if(randGen.nextInt(60) == 1)					//tiny chance that the next river will fork off from this one
				{
					nextFork = fork;
				}
				
				
				if(river.size() > 1)			//forbid the source's neighbors and their neighbors once the river has
					for(Plot l : getNeighbors(fount))
					{
						for(Plot n : getNeighbors(l))
						{
							addToList(forbidden, n);
						}
					
					}
				
				fount = lastFlow;		//fount becomes our previous location
				lastFlow = nextFlow;	//and our previous location becomes our current location
				
				river.add(nextFlow);
				desiredFlow--;
			}
			if(desiredFlow > 0 )		//If we've terminated in a dead-end before using up our "water pressure,"
			{							//create a circular lake on the flat land where we ended.
				ArrayList<Plot> lake = getPlotsInRadius(lastFlow, desiredFlow/8);
				for(Plot p : lake)
					if(p.shade == Color.GREEN)
						p.changeColor(Color.BLUE, 1);
				
				erode(lake, Color.BLUE, Color.GREEN, 3, 35);	//Erode the edges of the lake.
			}
			
			targetRivers--;
			validFountains.removeAll(river);	//Don't let any new rivers come out of springs that touch this one.
			
	
		}
		while(targetRivers >= 0);
		
	
		statusReport = "Planting Trees";	//Randomly scatter trees across the landscape
		
		//First, create a small number of large circular forests
		for(int f = 0; f < 3 + randGen.nextInt(3); f++)
		{
			Plot t = getRandomPlot();
			for(Plot p : getPlotsInRadius(t, 10 + randGen.nextInt(6)))
				if(p.shade == Color.GREEN)
					p.changeColor(darkGreen, 1);
		}
		
		//Second, do a random spray of trees in single tiles.
		for(Plot p : fullMap)
			if(p.shade == Color.GREEN && randGen.nextInt(50) == 1)
				p.changeColor(darkGreen, 1);
		
		statusReport = "Growing Forests";				//Grow forests outward with "erosion"
		erode(fullMap, Color.GREEN, darkGreen, 1, 60);	//Begin very low-threshold and high chance, increasing threshold and reducing odds
		erode(fullMap, Color.GREEN, darkGreen, 2, 35);
		erode(fullMap, darkGreen, Color.GREEN, 3, 10);
		
		statusReport = "Aging Forests";		//Once the forests are overgrown, do several smoothing passes.
		for(int i = 0; i < 4; i++)
			for(Plot p: fullMap)
				smooth(p, Color.GREEN, darkGreen, 5);
	}
	
	ArrayList<Plot> shuffle(ArrayList<Plot> list)		//Randomize a list of Plots
	{
		Collections.shuffle(list);
		return(list);
	}
	
	String basicGalaxyStats()	//Return a string that breaks down the number of stars and planets in the galaxy and what kind of life it supports.
	{
		String text = "";
		
		int numStars =0; int numPlanets =0; int life = 0; int animals=0; int plants=0; 
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				if(myGrid[x][y] != null && myGrid[x][y] instanceof Star)
				{
					if(myGrid[x][y].myType != null && myGrid[x][y].myType.ordinal() > 3)
						numStars++;
					for(int o = 0; o < myGrid[x][y].maxBodies; o++)
					{
						if(myGrid[x][y].bodies[o] != null)
						{
							numPlanets++;
							if( ((Planet)(myGrid[x][y].bodies[o])).ecology.ordinal() > 0 )
								life++;
							if( ((Planet)(myGrid[x][y].bodies[o])).ecology.ordinal() > 2 )
								plants++;
							if( ((Planet)(myGrid[x][y].bodies[o])).ecology.ordinal() > 3 )
								animals++;
						}
					}
				}
			}
		
		text += "Number of Stars : " + numStars + "\n";
		text += "Number of Planets : " + numPlanets + "\n";
		text += life + " Planets with life\n";
		text += "( " + plants + " with plant life, " + animals + " with animal life)";
		
		return text;
	}
	
	void nextTurn()	//Advance a galaxy map through time, applying all planets' Incremental Effects.
	{				//(Effectively, just updates temperatures and gives life a chance to evolve.)
		statusReport = "Aging Galaxy...";
		if(myGrid != null)
		{
			for(int x = 0; x < width; x++)
				for(int y = 0; y < height; y++)
				{
					if(myGrid[x][y] != null && myGrid[x][y] instanceof Star)
					{
						if(myGrid[x][y].myType != null && myGrid[x][y].myType.ordinal() > 3)
							
						for(int o = 0; o < myGrid[x][y].maxBodies; o++)
						{
							if(myGrid[x][y].bodies[o] != null)
							{
								
								((Planet)(myGrid[x][y].bodies[o])).updateConditions();
								displayPlanet( ((Planet)(myGrid[x][y].bodies[o])) );
							}
						}
					}
				}
		}
		statusReport = basicGalaxyStats();
	}
	
	
	interface planetaryEffect	//Interface for creating new effects on planets
	{
		boolean checkExpiry();	//expiration condition
		void applyInitial();	//any immediate change
		void applyIncremental();//incremental (per turn) change
		
	}

}
