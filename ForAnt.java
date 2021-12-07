//import javax.swing.*;
//import java.awt.*;
//import java.awt.image.*;
//import java.util.*;

// La classe ForAnt ne fait que creer un terrain et lancer le programme. Elle n'intervient plus ensuite.

public class ForAnt
{
	private MainFrame frame;
	
	ForAnt()
	{
		frame = new MainFrame();
	}
	
	public void begin()
	{
		boolean testLoop=true;
		
		while(testLoop)
		{
			try {Thread.sleep(1);}
			catch(InterruptedException e) {System.out.println("Sleep interrupted:"+e);}
			if(frame.action()!=0)
				testLoop = false;
		}
	}
	
	public static void main(String[]args)
	{
		ForAnt s = new ForAnt();
		s.begin();
		System.out.println("Fin de la simulation");
	}
}