import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.*;
import java.io.*;

public class Terrain extends JPanel
{
    private final int panelWidth = 50, panelHeight = 50, zoom = 8;
    
    private boolean isRandomlyGenerated;

    // nombre de fourmis dans la fourmilière
    private final static int NUMBER_OF_ANTS = 25;
    
    // nombre de cases nourriture initialement dans le cas ou génération aléatoire de terrain
    public final int RAND_NOURRITURE = 20;
    
    //
    // /!\ une densité trop élevée bloque l'application à la génération du terrain /!\
    //
    private final double DENSITY_OBSTACLE = 0.15; // densité d'obstacle, comprise entre 0 et 1. <0.5 recommandé
    
    
    // quantité initiale de nourriture placée dans une case nourriture 
    public final int INIT_FOOD_QUANTITY_PER_CELL = 1000;
    
    private int fileFoodQuantity; // quantité de cases "nourriture" dans le fichier 
    
    private Gradient foodGradient;

	private Color color;

	public double foodPheromones[][];
	double fpBuffer1[][];
	double fpBuffer2[][];
	
//	public double homePheromones[][];
//	double hpBuffer1[][];
//	double hpBuffer2[][];
	
	int food[][];
	boolean obstacles[][];

    BufferedImage bi;
    Colony base;
    
	Random r;
	
    Terrain(JFrame newParent)
    {	
		color = Color.black;
		r = new Random();
		
		foodGradient = new Gradient();
		
		foodGradient.addPoint(Color.black);
		foodGradient.addPoint(new Color(255,0,255)); //violet
//		foodGradient.addPoint(Color.blue);
//		foodGradient.addPoint(Color.cyan);
//		foodGradient.addPoint(Color.green);
//		foodGradient.addPoint(Color.yellow);
//		foodGradient.addPoint(Color.orange);
		foodGradient.addPoint(Color.red);

//		foodGradient.addPoint(Color.white);
//		foodGradient.addPoint(Color.green.brighter());
//		foodGradient.addPoint(Color.orange);
//		foodGradient.addPoint(Color.red);
		
		foodGradient.createGradient(251);
		
		base = new Colony(Color.white, NUMBER_OF_ANTS,this);
	
		//System.out.println("Base is at point "+base.getX()+" "+base.getY());
	
		food= new int[panelWidth][panelHeight];
		
		obstacles = new boolean[panelWidth][panelHeight];
		
		fpBuffer1 = new double[panelWidth][panelHeight];
		fpBuffer2 = new double[panelWidth][panelHeight];
		
		


		// initialisation à partir du fichier "ant.map" ou aléatoire
		// cette initialisation fixe la position de la base
		
		if(!initFromFile("")){ //map_maze2.map")){
			// erreur de chargement de fichier
			initRand(); // on utilise une génération aléatoire
		}

    }

    public int action()
    {
		//System.out.println("action " + (new Date()).getTime());
	
		spreadFoodPheromone();
		//spreadHomePheromone();

//		int i,j;
		
//		for(j=0;j<panelHeight;j++){
//		
//			for(i=0;i<panelWidth;i++) {
//			
//					System.out.print("["+ foodPheromones[i][j] +"]");
//			}
//			
//			System.out.println("");
//		}

		
		repaint();

		return base.bouge();

    }

    public void update(Graphics g) {
	
		paint(g);
    }

    public void paint(Graphics g) {
		//System.out.println("drawing");
	
		int i, j;
		Colony colony;
		
		g.setColor (color);
		g.fillRect (0, 0, panelWidth*zoom +1, panelHeight*zoom +1);
		
		double quantity;
		
		for(i = 0; i < panelWidth; i++)
		{
			for(j = 0; j < panelHeight; j++) {
			
				quantity = foodPheromones[i][j];
				
				if(quantity > 0) {
				
					//System.out.println("There is "+quantity+" pheromones");
					//System.out.println("There is a pheromone to draw at "+i+","+j);
				
					if(quantity > 250)
						g.setColor(Color.red);
					else
						g.setColor(foodGradient.getColour((int)quantity));

					g.fillRect(zoom*i,zoom*j,zoom,zoom);
				}
				
				if(food[i][j] > 0) {
				
					//g.setColor(Color.green.darker());
					g.setColor(Color.yellow);
					g.fillRect(zoom*i,zoom*j,zoom,zoom);
				}
				
				if(obstacles[i][j]) {
				
					g.setColor(Color.gray);
					g.fillRect(zoom*i,zoom*j,zoom,zoom);
				}
			}
		}
		
		base.draw(g);
    }

    public int getRandom(int i)
	{
		return r.nextInt(i);
    }



	public void addFoodPheromone(Position pos)
	{
		foodPheromones[pos.x][pos.y] +=10;
	}

	public int getPanelWidth() {
		return panelWidth;
    }

    public int getPanelHeight() {
    	return panelHeight;
    }

    public int getZoom() {
    	return zoom;
    }
	
	private void spreadFoodPheromone() {
	
		double calcBuffer[][];
		double diffusion = 0.05;
		double evaporation = 0.005;
		
		double newQuantity;
		double threshold = 1; // 0 à tester
		int i,j;
		
		if(foodPheromones == null) {
		
			//System.out.println("foodPheromones was null");
			foodPheromones = fpBuffer1;
		}
		
		// Si le buffer courant est le no. 1, les calculs doivent se faire dans le buffer 2
		if(foodPheromones == fpBuffer1) {
		
			//System.out.println("foodPheromones was fpBuffer1");
			calcBuffer = fpBuffer2;
		}
		// Sinon, les calculs se font dans le buffer 1
		else {
		
			//System.out.println("foodPheromones was fpBuffer2");
			calcBuffer = fpBuffer1;
		}
		
		//Calcul des nouvelles valeurs dans calcBuffer
		
		for(i=1;i<(panelWidth-1);i++) {
		
			for(j=1;j<(panelHeight-1);j++) {
		
				//System.out.println("i="+i+",j="+j);
		
				newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i+1][j] + foodPheromones[i-1][j] + foodPheromones[i][j+1] + foodPheromones[i][j-1]);
			
				if(newQuantity > threshold && !obstacles[i][j])
					calcBuffer[i][j] = newQuantity;
				else
					calcBuffer[i][j] = 0;
			}
		}
		
		//Calcul des nouvelles valeurs pour les 4 coins
		//Haut gauche
		i=j=0;
		
		newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i+1][j] + foodPheromones[i][j+1]);
			
		if(newQuantity > threshold && !obstacles[i][j])
			calcBuffer[i][j] = newQuantity;
		else
			calcBuffer[i][j] = 0;
		
		//Bas gauche
		i=0;j=panelHeight-1;
		
		newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i+1][j] + foodPheromones[i][j-1]);
			
		if(newQuantity > threshold && !obstacles[i][j])
			calcBuffer[i][j] = newQuantity;
		else
			calcBuffer[i][j] = 0;
		
		//Haut droit
		i=panelWidth-1;j=0;
		
		newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i][j+1] + foodPheromones[i-1][j]);
			
		if(newQuantity > threshold && !obstacles[i][j])
			calcBuffer[i][j] = newQuantity;
		else
			calcBuffer[i][j] = 0;
		
		//Bas droit
		i=panelWidth-1;j=panelHeight-1;
		
		newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i][j-1] + foodPheromones[i-1][j]);
			
		if(newQuantity > threshold && !obstacles[i][j])
			calcBuffer[i][j] = newQuantity;
		else
			calcBuffer[i][j] = 0;
			
		//Calcul des lignes de bord
		// Bord gauche
		i=0;
		for(j=1;j<panelHeight-1;j++) {
		
			newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i][j-1] + foodPheromones[i+1][j] + foodPheromones[i][j+1]);
			
			if(newQuantity > threshold && !obstacles[i][j])
				calcBuffer[i][j] = newQuantity;
			else
				calcBuffer[i][j] = 0;
		}
		
		// Bord haut
		j=0;
		for(i=1;i<panelWidth-1;i++) {
		
			newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i-1][j] + foodPheromones[i][j+1] + foodPheromones[i+1][j]);
			
			if(newQuantity > threshold && !obstacles[i][j])
				calcBuffer[i][j] = newQuantity;
			else
				calcBuffer[i][j] = 0;
		}
		
		// Bord droit
		i=panelWidth-1;
		for(j=1;j<panelHeight-1;j++) {
		
			newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i][j-1] + foodPheromones[i-1][j] + foodPheromones[i][j+1]);
			
			if(newQuantity > threshold && !obstacles[i][j])
				calcBuffer[i][j] = newQuantity;
			else
				calcBuffer[i][j] = 0;
		}
		
		// Bord bas
		j=panelHeight-1;
		for(i=1;i<panelWidth-1;i++) {
		
			newQuantity = (1-evaporation)*foodPheromones[i][j] 
								- diffusion*foodPheromones[i][j] 
								+ diffusion*0.25*(foodPheromones[i-1][j] + foodPheromones[i][j-1] + foodPheromones[i+1][j]);
			
			if(newQuantity > threshold && !obstacles[i][j])
				calcBuffer[i][j] = newQuantity;
			else
				calcBuffer[i][j] = 0;
		}
		
		// Fin du calcul
		
		foodPheromones = calcBuffer;
	
//		if(foodPheromones == fpBuffer1)
//			System.out.println("fooPheromones is now fpBuffer1");
//		if(foodPheromones == fpBuffer2)
//			System.out.println("fooPheromones is now fpBuffer2");
//		if(foodPheromones == null)
//			System.out.println("foodPheromones is now null");
	}
	
//	private void spreadHomePheromone() {
//	
//		double calcBuffer[][];
//		double diffusion = 0.05;
//		double evaporation = 0.01;
//		
//		double newQuantity;
//		
//		int i,j;
//		
//		if(homePheromones == null) {
//		
//			//System.out.println("homePheromones was null");
//			homePheromones = hpBuffer1;
//		}
//		
//		// Si le buffer courant est le no. 1, les calculs doivent se faire dans le buffer 2
//		if(homePheromones == hpBuffer1) {
//		
//			//System.out.println("homePheromones was hpBuffer1");
//			calcBuffer = hpBuffer2;
//		}
//		// Sinon, les calculs se font dans le buffer 1
//		else {
//		
//			//System.out.println("homePheromones was hpBuffer2");
//			calcBuffer = hpBuffer1;
//		}
//		
//		//Calcul des nouvelles valeurs dans calcBuffer
//		
//		for(i=1;i<(panelWidth-1);i++) {
//		
//			for(j=1;j<(panelHeight-1);j++) {
//		
//				//System.out.println("i="+i+",j="+j);
//		
//				newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i+1][j] + homePheromones[i-1][j] + homePheromones[i][j+1] + homePheromones[i][j-1]);
//			
//				if(newQuantity > 1)
//					calcBuffer[i][j] = newQuantity;
//				else
//					calcBuffer[i][j] = 0;
//			}
//		}
//		
//		//Calcul des nouvelles valeurs pour les 4 coins
//		//Haut gauche
//		i=j=0;
//		
//		newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i+1][j] + homePheromones[i][j+1]);
//			
//		if(newQuantity > 1)
//			calcBuffer[i][j] = newQuantity;
//		else
//			calcBuffer[i][j] = 0;
//		
//		//Bas gauche
//		i=0;j=panelHeight-1;
//		
//		newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i+1][j] + homePheromones[i][j-1]);
//			
//		if(newQuantity > 1)
//			calcBuffer[i][j] = newQuantity;
//		else
//			calcBuffer[i][j] = 0;
//		
//		//Haut droit
//		i=panelWidth-1;j=0;
//		
//		newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i][j+1] + homePheromones[i-1][j]);
//			
//		if(newQuantity > 1)
//			calcBuffer[i][j] = newQuantity;
//		else
//			calcBuffer[i][j] = 0;
//		
//		//Bas droit
//		i=panelWidth-1;j=panelHeight-1;
//		
//		newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i][j-1] + homePheromones[i-1][j]);
//			
//		if(newQuantity > 1)
//			calcBuffer[i][j] = newQuantity;
//		else
//			calcBuffer[i][j] = 0;
//			
//		//Calcul des lignes de bord
//		// Bord gauche
//		i=0;
//		for(j=1;j<panelHeight-1;j++) {
//		
//			newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i][j-1] + homePheromones[i+1][j] + homePheromones[i][j+1]);
//			
//			if(newQuantity > 1)
//				calcBuffer[i][j] = newQuantity;
//			else
//				calcBuffer[i][j] = 0;
//		}
//		
//		// Bord haut
//		j=0;
//		for(i=1;i<panelWidth-1;i++) {
//		
//			newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i-1][j] + homePheromones[i][j+1] + homePheromones[i+1][j]);
//			
//			if(newQuantity > 1)
//				calcBuffer[i][j] = newQuantity;
//			else
//				calcBuffer[i][j] = 0;
//		}
//		
//		// Bord droit
//		i=panelWidth-1;
//		for(j=1;j<panelHeight-1;j++) {
//		
//			newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i][j-1] + homePheromones[i-1][j] + homePheromones[i][j+1]);
//			
//			if(newQuantity > 1)
//				calcBuffer[i][j] = newQuantity;
//			else
//				calcBuffer[i][j] = 0;
//		}
//		
//		// Bord bas
//		j=panelHeight-1;
//		for(i=1;i<panelWidth-1;i++) {
//		
//			newQuantity = (1-evaporation)*homePheromones[i][j] 
//								- diffusion*homePheromones[i][j] 
//								+ diffusion*0.25*(homePheromones[i-1][j] + homePheromones[i][j-1] + homePheromones[i+1][j]);
//			
//			if(newQuantity > 1)
//				calcBuffer[i][j] = newQuantity;
//			else
//				calcBuffer[i][j] = 0;
//		}
//		
//		// Fin du calcul
//		
//		homePheromones = calcBuffer;
//	
////		if(homePheromones == hpBuffer1)
////			System.out.println("fooPheromones is now hpBuffer1");
////		if(homePheromones == hpBuffer2)
////			System.out.println("fooPheromones is now hpBuffer2");
////		if(homePheromones == null)
////			System.out.println("homePheromones is now null");
//	}
	
	private int nbOfObstacles(Position p){
		// on suppose p comme étant sur le terrain

		int count=0;

		for(Worker.Direction d:Worker.Direction.values()){
			Position tmpPos = p.getPositionFromDirection(d);
			if(isObstacleAtPoint(tmpPos))
				count++;
		}
		return count;
	}
	
	private void initRand()
	{
		
		isRandomlyGenerated = true;
		
		int i;
		int xRand, yRand;
		
		// on place la base
		
		// position aléatoire
		// base.setPosition(new Position(getRandom(panelWidth),getRandom(panelHeight)));

		// base au centre
		base.setPosition(new Position(panelWidth/2,panelHeight/2));
		
		
		// obstacles
		int obstaclesCount = (int)(DENSITY_OBSTACLE*panelWidth*panelHeight);
		for(i = 0; i < obstaclesCount; i++)
		{
			boolean isSet = false;
			while(!isSet){
				xRand = getRandom(panelWidth);
				yRand = getRandom(panelHeight);
				Position randPos = new Position(xRand,yRand);
				if( !base.getPosition().equals(randPos) && nbOfObstacles(base.getPosition())<3  ){
					obstacles[xRand][yRand] = true;
					isSet = true;
					}
			}
		}
		
		
		// nourriture, placée en dehors des obstacles et de la base
		for(i = 0; i < RAND_NOURRITURE; i++)
		{
			boolean isSet = false;
			while(!isSet){
			xRand = getRandom(panelWidth);
			yRand = getRandom(panelHeight);
			Position randPos = new Position(xRand,yRand);
			if(!isObstacleAtPoint(randPos) && !base.getPosition().equals(randPos) && nbOfObstacles(randPos)<3 ){
				food[randPos.x][randPos.y] = INIT_FOOD_QUANTITY_PER_CELL;
				isSet = true;
				}
			}
		}
		
	}
	
	/** @deprecated inutile car ne fait que mettre à zéro les matrices de calcul, qui le sont déja par défaut*/
	private void initPheromones() {
	
		int i,j;
		
		for(i=0;i<panelWidth;i++)
			for(j=0;j<panelHeight;j++) {
			
				fpBuffer1[i][j] = 0;
				fpBuffer2[i][j] = 0;
//				hpBuffer1[i][j] = 0;
//				hpBuffer2[i][j] = 0;
			}
	}
	
	

	
	private boolean initFromFile(String fileName) {

		
		BufferedReader in=null;
	
		try{
			in = new BufferedReader(new FileReader(fileName));
		}catch(FileNotFoundException e){
//			System.out.println("File not Found:\""+fileName+"\"");
			return false;
			}

		
		
		int j=0;
		String line=null;
		do{
			
			try{
				line = in.readLine();
			}catch(IOException e){
//				System.out.println("Line Read Error");
				return false;
				}	
			
			for(int i=0;line!=null && i<line.length() && i<panelWidth;i++)
			{
				int car = line.charAt(i);
				switch(car)
				{
					case ' ':
					case '_': obstacles[i][j] = false; break; //VIDE 
					case 'X': obstacles[i][j] = true; break; //OBSTACLE
					case 'F': food[i][j] = INIT_FOOD_QUANTITY_PER_CELL; fileFoodQuantity++; break; // FOOD
					case 'B': base.setPosition(new Position(i,j));

				}
				
			}
			j++;
			
		}while(line!=null);
		
		return true;
	
	}

//	----------------------------------------------
//	méthodes pour utiliser la "Structure" Position
//	----------------------------------------------
	
	
	/* vérifie qu'une position est bien sur le terrain 
	 * @return true ou false si la position est dans ou en dehors du terrain */
	private boolean isNotOut(Position pos){	
		if((pos.x<0) || (pos.y<0) || (pos.x >= panelWidth) || (pos.y >= panelHeight))
			return false;
		return true;
	}
	

	
//	
//	nb: j'ai rajouté les appels à la fonction de vérification car Worker.getRelativePosition ne fait pas de vérification
// 	à terme quand on fusionnera les deux méthodes il n'y aura plus de double vérification
	
	public boolean isObstacleAtPoint(Position pos)
	{
		if(isNotOut(pos))
			return (obstacles[pos.x][pos.y]);
		else return true;
	}
	
	public boolean isFoodAtPoint(Position pos)
	{
		if(isNotOut(pos))
			return (food[pos.x][pos.y] > 0);
		else return false;
	}
	
	public boolean isColonyAtPoint(Position pos)
	{
		if(isNotOut(pos))
			return ((pos.x == base.getX()) && (pos.y == base.getY())) ;
		else return false;
	}
	
	public boolean isFoodPheromoneAtPoint(Position pos)
	{
		if(isNotOut(pos))
			return (foodPheromones[pos.x][pos.y] > 0);
		else return false;
	}
	
	public double quantityOfFoodPheromoneAtPoint(Position pos)
	{
		if(isNotOut(pos))
			return foodPheromones[pos.x][pos.y];
		else return 0;
	}
	
//	public boolean isHomePheromoneAtPoint(Position pos)
//	{
//		if(isNotOut(pos))
//			return (homePheromones[pos.x][pos.y] > 0);
//		else return false;
//	}
//	
//	public double quantityOfHomePheromoneAtPoint(Position pos)
//	{
//		if(isNotOut(pos))
//			return homePheromones[pos.x][pos.y];
//		else return 0;
//	}
	
	public void updateFood (Position pos) {
		
		food[pos.x][pos.y] -= 10;
		
		if(food[pos.x][pos.y] < 0)
			food[pos.x][pos.y] = 0;
	}
	
	public int getFoodQuantity(){
		if(isRandomlyGenerated){
			return RAND_NOURRITURE*INIT_FOOD_QUANTITY_PER_CELL;
		}
		else return fileFoodQuantity*INIT_FOOD_QUANTITY_PER_CELL;
	}
}
