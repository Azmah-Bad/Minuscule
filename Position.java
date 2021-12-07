

public class Position {

	public int x,y;
	
	public Position(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Position(Position pos){
		this.x = pos.x;
		this.y = pos.y;
	}
	
	public boolean equals(Position p){
		return (p.x==x && p.y==y);
	}
	
	// renvoie une position relative à une direction
	public Position getPositionFromDirection(Worker.Direction dir){
    	Position relativePos = new Position(this);
    	
    	switch(dir)
    	{
	    	case NORD: relativePos.y--;break;
	    	case EST: relativePos.x++;break;
	    	case SUD: relativePos.y++;break;
	    	case OUEST: relativePos.x--;break;
    	}
		return relativePos; 
    }
	
	public String toString(){
		return "("+x+","+y+")";
	}
}
