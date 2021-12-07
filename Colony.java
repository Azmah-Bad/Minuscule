//import javax.swing.*;
import java.awt.*;
//import java.awt.image.*;
import java.util.*;

// La classe Colony comprend une liste de ants et une liste de pheromones.

public class Colony
{
	// timeout en nombre de cycles pour le benchmark
	// si le stock de nourriture de la fourmillière ne varie pas pendant BENCH_TIMEOUT cycles 
	// et que toute la nourriture n'a pas été ramassée, on considère le bench terminé 
	private final static int BENCH_TIMEOUT = 10000;
	
	private int benchCycle; // numéro du cycle courant
	private int benchLastVariationCycle; // numéro du cycle de la dernière variation
	private int benchPreviousCycleFoodStock; // stock de nourriture du cycle précédent
	
	protected Position pos;
    private ArrayList<Worker> ants;
    private int foodStock;
    private Terrain terrain;
	private Color color;

	private Color myColor;

	private void initColony(Color c, int _nb, Terrain t)
    {	
		
		terrain = t;
		color = c;
		ants = new ArrayList<Worker>();
		
		myColor = new Color(118,133,255);

		for(int i=0;i<_nb;i++) 
		{
			ants.add(new Worker(this,terrain));
		}
    }
		
	public Colony(Position p,Color c, int _nb, Terrain t){
		initColony(c, _nb, t);
		setPosition(p);
	}
	
	public Colony(Color c, int _nb, Terrain t){
		initColony(c, _nb, t);
	}
	
    
		

	// bouge() est la fonction appelee par le Terrain pour faire evoluer les differents elements.

    public int bouge()
    {
    	if(pos==null){
    		System.out.println("ERREUR : POsition Base inconnue");
    	}
    	
    	
		int i;
		// Parcours des ants
		
		for(i = 0; i < ants.size(); i++)
		{
			ants.get(i).bouge();
		}
		
		// -----------
		// benchmark
		
		if(foodStock>=terrain.getFoodQuantity())
		{
			System.out.println("toute la nourriture a été ramassée. nb cycles:"+benchCycle);
			return 1; // toutes les sources de nourriture ont été ramassées
		}
		
		
		if(foodStock!=benchPreviousCycleFoodStock){
			// on a une variation du stock de nourriture
			benchPreviousCycleFoodStock = foodStock;
			benchLastVariationCycle = benchCycle;
		}
		else if( (benchCycle-benchLastVariationCycle)>=BENCH_TIMEOUT ){
			// le timeout est atteint
			System.out.println("Bench Timeout atteint : toutes les sources n'ont pu être ramassées. nb cycles:"+benchCycle);
			return 1;
		}
		
		
		benchCycle++;
		return 0;
    }
	
	// Fonction se chargeant du dessin de la fourmiliere et passant le message aux differents elements.

    public void draw (Graphics g)
    {
		int i;
	
		g.setColor(myColor);
	
		int fact = terrain.getZoom();
		g.fillOval(fact*pos.x, fact*pos.y, fact, fact);
	
//		for(i=0;i<pheromones.size();i++)
//		{
//			p = (Pheromone)pheromones.get(i);
//			p.draw(g);
//		}
	
		g.setColor(color);
	
		for(i = 0; i < ants.size(); i++)
		{
			ants.get(i).draw(g);
		}
    }

    public void removeAnt(int i)
	{
		ants.remove(i);
    }

    public Worker getAnt(int _x, int _y)
	{
		Worker f;
		int i;

		for(i = 0; i < ants.size(); i++)
		{
			f = (Worker) ants.get(i);
			if((f.getX() == _x) && (f.getY() == _y))
			{
				return f;
			}
		}
		
		return null;
    }

	// Fonction servant a ajouter de la pheromone a un point precis.
	// Si la pheromone existe deja, on en rajoute sans en recreer.

//	public void addPheromone(int _x, int _y)
//	{
//		Pheromone p;
//		boolean alreadyAdded = false;
//		for(int i=0;i<pheromones.size();i++)
//		{
//			p = (Pheromone)pheromones.get(i);
//			if((p.getX()==_x) && (p.getY()==_y))
//			{
//				p.addQuantity(150);
//				alreadyAdded = true;
//			}
//		}
//	
//		if(!alreadyAdded)
//			pheromones.add(new Pheromone(_x,_y,this,terrain,pColor));
//	}
	
//	public boolean isPheromoneAtPoint(int _x, int _y)
//	{
//		Pheromone p;
//	
//		for(int i=0;i<pheromones.size();i++)
//		{
//			p = (Pheromone)pheromones.get(i);
//			if((p.getX()==_x) && (p.getY()==_y))
//				return true;
//		}
//		
//		return false;
//	}
	
	
    public void addFood (int newFood)
	{foodStock += newFood;}

    public Terrain getTerrain ()
	{return terrain;}
	
	public Color getColor(){return color;}
	
	public Position getPosition(){return pos;}
	
	// modifie la position de la base et des fourmis
	public void setPosition(Position p){
		pos = new Position(p);
		Iterator<Worker> it = ants.iterator();
		while(it.hasNext()){
			Worker w = it.next();
			w.initPosition();
		}
	}
	
	public int getX(){return pos.x;}
	public int getY(){return pos.y;}
	public int getFoodStock(){return foodStock;}
}
