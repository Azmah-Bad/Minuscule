import java.awt.*;
import java.util.*;

import static java.util.Collection.*;
import static java.util.Collections.frequency;

public class Worker
{
	
	public int THRESHOLD=  12;
	//private int x,y; // position
	private Position currentPos;
	
	int num; 
	
	private enum Etat{RECHERCHE,RETOUR,MORTE, DEADLOCK};
	public enum Direction{NORD,EST,SUD,OUEST};
	
    private  Colony colony;
	
    private Terrain terrain;
	
	/* etat courant */
	protected Etat etat;
	/* direction courante */
	protected Direction direction;
	protected ArrayList<Position> positionHistory;
	protected int DeadlockStepCounter = 0;



	public Worker(Colony col, Terrain ter){
		etat = Etat.RECHERCHE;
		colony = col;
		terrain = ter;
		positionHistory = new ArrayList<Position>();
	}
	
	
	
    public int bouge() // SEULE FONCTION A COMPLETER !
	{
		
		Direction newDirection=null;
		Direction foodDirection;
		Direction pheroDirection;

		if (etat == Etat.RECHERCHE) {
			if (terrain.isFoodAtPoint(currentPos)){ // found food
				terrain.updateFood(currentPos);
				etat = Etat.RETOUR;
			} else { // searching for food

				newDirection = searchForFood();
				if (newDirection == null){
					newDirection = searchForFoodPheromone(null);


					if (newDirection == null){ // no pheromone nearby
						newDirection = randomDirection();
					}

					searchForFoodPheromone(null);
				}
			}
		}

		if (etat == Etat.RETOUR) {
			if (terrain.isColonyAtPoint(currentPos)){
				dropFood();
				etat = Etat.RECHERCHE;
			} else {
				dropFoodPheromone();

				newDirection = direction(colony.getPosition());
				registerPosition(currentPos);

				if (isAntDeadlocked()){
					etat = Etat.DEADLOCK;
				}
			}
		}

		if (etat == Etat.DEADLOCK) {
			System.out.println("Ant is deadlocked... trying to unlock");
			newDirection = randomDirection(); // lol
			DeadlockStepCounter = DeadlockStepCounter + 1;
			if (DeadlockStepCounter > 50){
				System.out.println("Ant is unlocked!");
				DeadlockStepCounter = 0;
				etat = Etat.RETOUR;
			}
		}



//		if(etat==Etat.RETOUR)
//		{
//
//		}
//
//		if(etat==Etat.RECHERCHE)
//		{
//
//		}
		newDirection = decideNewDirectionForDirection(newDirection); // gère évitement d'obstacles
			
		move(newDirection); // r�alise le d�placement
				
		direction = newDirection; // maj direction
		
		return 0;
	}

	private void registerPosition(Position position){
		Position mPosition = new Position(position);
		positionHistory.add(mPosition);
		if (positionHistory.size() >= THRESHOLD) {
			positionHistory.remove(0); // remove last direction
		}
	}

	private void unlock(){
		System.out.println("--------------------");
		System.out.println("unlocking ant...");
		for (int i = 0; i < 50; i++) {
			move(randomDirection());
		}
		System.out.println("ant unlocked!");
	}

	private Boolean isAntDeadlocked() {
		Map<String, Integer> freq = new HashMap<String, Integer>();
		for (Position p : positionHistory) {
			if (freq.containsKey(p.toString())) {
				System.out.println("Ant is deadlocked!");
				return true;
			} else {
				freq.put(p.toString(), 1);
			}
		}
		return false;
	}


	/* Recherche de nourriture dans le voisinage, 
	 * @return direction vers la nourriture dans le voisinage, null s'il n'y en a pas */
    private Direction searchForFood ()
	{
    	for(Direction d:Direction.values()){
    		if(terrain.isFoodAtPoint(getRelativePosition(d)))
    			return d;
    	}
    	return null;
	}


    /*
     * A partir de la position courante, donne les coordonnées de la cellule indiquée par dir  
     * @return position de la cellule voisine indiquée par dir, qui peut éventuellement �tre en dehors du terrain*/
    private Position getRelativePosition(Direction dir){
    	Position relativePos = new Position(currentPos);
    	
    	switch(dir)
    	{
	    	case NORD: relativePos.y--;break;
	    	case EST: relativePos.x++;break;
	    	case SUD: relativePos.y++;break;
	    	case OUEST: relativePos.x--;break;
    	}
		return relativePos; 
    }

    /*
     * recherche la direction contenant le plus de phéromone, null s'il n'y en a pas
     * @param exept direction qui sera systématiquement ignorée dans cette recherche
     */
	private Direction searchForFoodPheromone (Direction except)
	{


		Position pos = colony.getPosition();

		int dx = pos.x - currentPos.x;
		int dy = pos.y - currentPos.y;
		
		//Direction possibleDirection1, possibleDirection2;
		
		ArrayList<Direction> possibleDirections = new ArrayList<Direction>();
		
		if(dx==0 && dy==0) {
		
			possibleDirections.add(Direction.OUEST);
			possibleDirections.add(Direction.EST);
			possibleDirections.add(Direction.SUD);
			possibleDirections.add(Direction.NORD);
		}
		
		if(dx==0) {
		
			if(dy<0) {
			
				// La fourmi est au sud direct de la colonie
				possibleDirections.add(Direction.OUEST);
				possibleDirections.add(Direction.EST);
				possibleDirections.add(Direction.SUD);
			}
			if(dy>0) {
			
				//La fourmi est au nord direct de la colonie, 
				possibleDirections.add(Direction.OUEST);
				possibleDirections.add(Direction.NORD);
				possibleDirections.add(Direction.EST);
			}
		}
		
		if(dy==0) {
		
			if(dx<0) {
			
				// La fourmi est a l'ouest direct de la colonie
				possibleDirections.add(Direction.OUEST);
				possibleDirections.add(Direction.NORD);
				possibleDirections.add(Direction.SUD);
			}
			if(dx>0) {
			
				// la fourmi est à l'est direct de la colonie
				possibleDirections.add(Direction.EST);
				possibleDirections.add(Direction.NORD);
				possibleDirections.add(Direction.SUD);
			}
		}
		
		if(dx<0) {
		
			if(dy<0) {
			
				//la fourmi est au sud est
				possibleDirections.add(Direction.EST);
				possibleDirections.add(Direction.SUD);
			}
			if(dy>0) {
			
				//la fourmi est au nord est
				possibleDirections.add(Direction.NORD);
				possibleDirections.add(Direction.EST);
			}
		}
		
		if(dx>0) {
		
			if(dy<0) {
				
				// la fourmi est au sud ouest
				possibleDirections.add(Direction.OUEST);
				possibleDirections.add(Direction.SUD);
			}
			if(dy>0) {
			
				// la fourmi est au nord ouest
				possibleDirections.add(Direction.NORD);
				possibleDirections.add(Direction.OUEST);
			}
		}
		
		// L'algo interessant commence ici
		
		Iterator<Direction> it = possibleDirections.iterator();
		double maxPheromone = 0;
		double tempPheromone;
		Direction chosenDirection = null;
		//boolean found = false;
		
		while(it.hasNext()){
		
			Direction d = it.next();
			
			if((tempPheromone = terrain.quantityOfFoodPheromoneAtPoint(getRelativePosition(d))) > maxPheromone )
    		{
    			maxPheromone = tempPheromone;
    			chosenDirection = d;
    		}
		}
		
		return chosenDirection;
	}

   

	/* La fourmi pose la nouriturre a la colonie */
    private void dropFood()
	{
		colony.addFood(10);
    }
	
    
	private void dropFoodPheromone() {
		
		terrain.addFoodPheromone(currentPos);
	}

	/* Fonction de dessin. */
    public void draw (Graphics g)
    {
		int fact = terrain.getZoom();
		g.fillOval(fact*currentPos.x,fact*currentPos.y,fact,fact);
    }
	
    /* teste si la direction choisie n'a pas d'obstacle, et la change le cas échéant pour la première qui n'en a pas */
	protected Direction decideNewDirectionForDirection(Direction proposedDirection)
	{

	
		if(proposedDirection==null)
			proposedDirection = Direction.EST;
		
		Direction finalDir = Direction.NORD;
		
		Position pos = colony.getPosition();
		
		int dx = pos.x - currentPos.x;
		int dy = pos.y - currentPos.y;
		
		if(!terrain.isObstacleAtPoint(getRelativePosition(proposedDirection))) {
		
			// Si il n'y a pas d'obstacle, on cherche meme pas a comprendre
			return proposedDirection;
		}
		else {
		
			// par contre, s'il y en a un, on va l'eviter en cherchant a revenir vers la fourmiliere
			if(proposedDirection == Direction.NORD || proposedDirection == Direction.SUD) {
			
				// on va virer est/ouest
				if(dx>=0)
					finalDir = Direction.EST;
				else
					finalDir = Direction.OUEST;
			}
			if(proposedDirection == Direction.EST || proposedDirection == Direction.OUEST) {
			
				// on va virer sud/nord
				if(dy>=0)
					finalDir = Direction.SUD;
				else
					finalDir = Direction.NORD;
			}
			
			// si il y a toujours un probleme, on renvoie une direction aleatoire tant qu'on ne trouve pas de direction echappatoire
			if(terrain.isObstacleAtPoint(getRelativePosition(finalDir))) {
			
				boolean targetFound = false;
				
				while(targetFound == false) {
				
					finalDir = randomDirection();
					if(!terrain.isObstacleAtPoint(getRelativePosition(finalDir)))
						targetFound = true;
				}
			}
			
			return finalDir;
		}
	}
	
	
	// Fonction retournant une direction aleatoire biaisée

	protected Direction randomDirection ()
	{
		int firstRand = colony.getTerrain().getRandom(Direction.values().length); // random de 0 à 3 en 4-connexivité
		Direction newDir = null;
		
		if(direction == null)
		{
			// direction indéterminée, on en prend une totalement aléatoire
			
			newDir = Direction.values()[firstRand];
		}
		else
		{
			// on suit avec la probabilité de 80% la direction précédente
			
			int secondRand = colony.getTerrain().getRandom(100);
			
			if(secondRand > 90) // 80)
			{
				// random de 1 à 3 en 4-connexivité
				int thirdRand = colony.getTerrain().getRandom(Direction.values().length-1) +1 ;
				
				int intNewDir = ( direction.ordinal()+ thirdRand ) % Direction.values().length ;
				
				newDir = Direction.values()[intNewDir];
				
			}
			else
			{   
				newDir = direction;
			}
		}
	
		return newDir;
    }
	
		
	/* renvoie la direction a prendre en fonction d'un point que la fourmi cherche a atteindre, null si ce point est atteint */
	private Direction direction(Position pos)
	{
		
		int dx = pos.x - currentPos.x;
		int dy = pos.y - currentPos.y;

			int sumDep = Math.abs(dx) + Math.abs(dy);
			
			if(sumDep == 0) return null;
			
			if(sumDep == 1) {
			
				if(dx == 1 || dx == -1) {
					if(dx>0) {return Direction.EST;}
					else {return Direction.OUEST;}
				}
				if(dy == 1 || dy == -1) {
					if(dy>0) {return Direction.SUD;}
					else {return Direction.NORD;}
				}
			}
			
			int randValue = terrain.getRandom(sumDep);
			
			if(randValue<=Math.abs(dx)){
				// mouvement en dx
				if(dx>0) return Direction.EST;
				else return Direction.OUEST;
			}
			else {
				// mouvement en dy
				if(dy>0) return Direction.SUD;
				else return Direction.NORD;
			}
	}
	
	
	
	
	/* En fonction d'une direction choisie, effetue le deplacement.
	 * la direction dans laquelle on effectue est censée être valide : pas d'obstacle ni bord de terrain */
	private void move(Direction direction)
	{
		switch(direction)
    	{
	    	case NORD: currentPos.y--;break;
	    	case EST: currentPos.x++;break;
	    	case SUD: currentPos.y++;break;
	    	case OUEST: currentPos.x--;break;
    	}
	}
	
	// ----------------
	// -- Accesseurs --
	
	public void initPosition(){
		currentPos = new Position(colony.getPosition());
	}
	
	public int getDirection()
	{
		return direction.ordinal();
	}
	
	public int getX()
	{
		return currentPos.x;
	}
	
	public int getY()
	{
		return currentPos.y;
	}
	
	public void die()
	{
		etat=Etat.MORTE;
	}
}
