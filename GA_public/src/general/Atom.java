package general;
import java.util.ArrayList;


public class Atom {
	public String species = ""; //should always be the same as name unless specified for a special reason (see "name"). 
	public double x = 0.0;
	public double y = 0.0;
	public double z = 0.0;
	public double c = 0.0; //charge
	int index = 0; 
	int spcy = 0; //1 for carbon, 2 for hydrogen, 3 for O, 4 for N, 5 for S, start from 6 it is periodic table
	public boolean collide = false;
	public double ankorOrient = 0.0;
	public String name = "";  //should be the same as species unless specified for a special reason ("H1, H2, etc."). 
	public boolean movable = true;
	public ArrayList<Atom> neighbourlist = new ArrayList<Atom>();
	
	public Atom (String name, double l1, double l2, double l3)
	{
		species = name;
		this.name = species;
		convertSpeciesToSpcy();
		x = l1;
		y = l2;
		z = l3;
	}
	
	public Atom(double a, double b, double h, double chr, int i, int sp)
	{
		x = a;
		y = b;
		z = h;
		c = chr;
		index = i;
		spcy = sp;
	}
	
	public Atom (String name, double l1, double l2, double l3, int i)
	{
		species = name;
		this.name = species;
		convertSpeciesToSpcy();
		x = l1;
		y = l2;
		z = l3;
		index = i;
	}
	
	public Atom(double a, double b, double h, int i, String sp)
	{
		x = a;
		y = b;
		z = h;
		c = 0.0;
		index = i;
		species = sp;
		convertSpeciesToSpcy();
	}
	
	public Atom(double a, double b, double h, int i, int sp)
	{
		x = a;
		y = b;
		z = h;
		c = 0.0;
		index = i;
		spcy = sp;
	}
	
	public Atom(double a, double b, double h, int i, String sp, String name)
	{
		x = a;
		y = b;
		z = h;
		c = 0.0;
		index = i;
        species = sp;
		this.name = name;
		convertSpeciesToSpcy();
	}
	
	private void convertSpeciesToSpcy()
	{
		if(species.equals("C"))
			spcy = 1;
		else if(species.equals("H"))
			spcy = 2;
		else if(species.equals("O"))
			spcy = 3;
		else if(species.equals("N"))
			spcy = 4;
		else if(species.equals("S"))
			spcy = 5;
		else if(species.equals("He"))
			spcy = 6;
		else if(species.equals("Li"))
			spcy = 7;
		else if(species.equals("Be"))
			spcy = 8;
		else if(species.equals("B"))
			spcy = 9;
		else if(species.equals("F"))
			spcy = 10;
		else if(species.equals("Ne"))
			spcy = 11;
		else if(species.equals("Na"))
			spcy = 12;
		else if(species.equals("Mg"))
			spcy = 13;
		else if(species.equals("Al"))
			spcy = 14;
		else if(species.equals("Si"))
			spcy = 15;
		else if(species.equals("P"))
			spcy = 16;
		else if(species.equals("Cl"))
			spcy = 17;
		else if(species.equals("Ar"))
			spcy = 18;
		else if(species.equals("K"))
			spcy = 19;
		else if(species.equals("Ca"))
			spcy = 20;
		else if(species.equals("Sc"))
			spcy = 21;
		else if(species.equals("Ti"))
			spcy = 22;
		else if(species.equals("V"))
			spcy = 23;
		else if(species.equals("Cr"))
			spcy = 24;
		else if(species.equals("Mn"))
			spcy = 25;
		else if(species.equals("Fe"))
			spcy = 26;
		else if(species.equals("Co"))
			spcy = 27;
		else if(species.equals("Ni"))
			spcy = 28;
		else if(species.equals("Cu"))
			spcy = 29;
		else if(species.equals("Zn"))
			spcy = 30;
		else if(species.equals("Ga"))
			spcy = 31;
		else if(species.equals("Ge"))
			spcy = 32;
		else if(species.equals("As"))
			spcy = 33;
		else if(species.equals("Se"))
			spcy = 34;
		else if(species.equals("Br"))
			spcy = 35;
		else if(species.equals("Kr"))
			spcy = 36;
		else spcy = 0;
	}
	
	public String toString()
	{	
		return (" "+index+" "+spcy+" "+c+" "+x +" "+y+" "+z);
	}
	
	public boolean equals(Atom another)
	{
		if(Math.abs(another.x - x) < 0.01 && Math.abs(another.y -y)<0.01 && Math.abs(another.z-z)<0.01 && another.spcy == spcy) return true;
		else return false;
	}
	
	public boolean nameequals(Atom another)
	{
		if(!name.equals("") && name.equals(another.name)) return true;
		else return false;
	}
	
	public String toXYZ()
	{
		if(spcy == 1)
			return ("C "+x +" "+y+" "+z);
		else if(spcy == 2)
			return ("H "+x +" "+y+" "+z);
		else if(spcy == 3)
			return ("O "+x +" "+y+" "+z);
		else if(spcy == 4)
			return ("N "+x +" "+y+" "+z);
		else if(spcy == 5)
			return ("S "+x +" "+y+" "+z);
		else if(spcy == 6)
			return ("He "+x +" "+y+" "+z);
		else if(spcy == 7)
			return ("Li "+x +" "+y+" "+z);
		else if(spcy == 8)
			return ("Be "+x +" "+y+" "+z);
		else if(spcy == 9)
			return ("B "+x +" "+y+" "+z);
		else if(spcy == 10)
			return ("F "+x +" "+y+" "+z);
		else if(spcy == 11)
			return ("Ne "+x +" "+y+" "+z);
		else if(spcy == 12)
			return ("Na "+x +" "+y+" "+z);
		else if(spcy == 13)
			return ("Mg "+x +" "+y+" "+z);
		else if(spcy == 14)
			return ("Al "+x +" "+y+" "+z);
		else if(spcy == 15)
			return ("Si "+x +" "+y+" "+z);
		else if(spcy == 16)
			return ("P "+x +" "+y+" "+z);
		else if(spcy == 17)
			return ("Cl "+x +" "+y+" "+z);
		else if(spcy == 18)
			return ("Ar "+x +" "+y+" "+z);
		else if(spcy == 19)
			return ("K "+x +" "+y+" "+z);
		else if(spcy == 20)
			return ("Ca "+x +" "+y+" "+z);
		else if(spcy == 21)
			return ("Sr "+x +" "+y+" "+z);
		else if(spcy == 22)
			return ("Ti "+x +" "+y+" "+z);
		else if(spcy == 23)
			return ("V "+x +" "+y+" "+z);
		else if(spcy == 24)
			return ("Cr "+x +" "+y+" "+z);
		else if(spcy == 25)
			return ("Mn "+x +" "+y+" "+z);
		else if(spcy == 26)
			return ("Fe "+x +" "+y+" "+z);
		else if(spcy == 27)
			return ("Co "+x +" "+y+" "+z);
		else if(spcy == 28)
			return ("Ni "+x +" "+y+" "+z);
		else if(spcy == 29)
			return ("Cu "+x +" "+y+" "+z);
		else if(spcy == 30)
			return ("Zn "+x +" "+y+" "+z);
		else if(spcy == 31)
			return ("Ga "+x +" "+y+" "+z);
		else if(spcy == 32)
			return ("Ge "+x +" "+y+" "+z);
		else if(spcy == 33)
			return ("As "+x +" "+y+" "+z);
		else if(spcy == 34)
			return ("Se "+x +" "+y+" "+z);
		else if(spcy == 35)
			return ("Br "+x +" "+y+" "+z);
		else if(spcy == 36)
			return ("Kr "+x +" "+y+" "+z);
		else
			return("X "+x +" "+y+" "+z);
	}
	
	public String todumpline()
	{
		return (index+" "+spcy+" "+((x+50.0)/100.0) +" "+((y+50.0)/100.0)+" "+((z+50.0)/100.0));
	}
	
	public double getDistance(Atom another)
	{
		double dx = another.x-x;
		double dy = another.y-y;
		double dz = another.z-z;
		
		return (Math.sqrt(dx*dx+dy*dy+dz*dz));
	}
	
	public Atom copy()
	{
		return(new Atom(this.x,this.y,this.z,this.c,this.index,this.spcy));
	}
	
	public boolean addNeighbour(Atom another)
	{
		if(!neighbourlist.contains(another))
		{
			neighbourlist.add(another);
			return true;
		}
		else
		{
			return false;
		}
	}

	public void setX(double d)
	{
		x = d;
	}
	public void setY(double d)
	{
		y = d;
	}
	public void setZ(double d)
	{
		z = d;
	}
	
	public double getWeight()   //only C H N P S for now
	{
		if(spcy == 1)
			return 12.0;
		else if(spcy == 2)
			return 1.0;
		else if(spcy == 3)
			return 16.0;
		else if(spcy == 4)
			return 14.0;
		else if(spcy == 5)
			return 32.0;
		else
			return 0.0;
	}
}