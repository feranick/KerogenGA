package general;

public class Bond {
	public final Atom atom1;
	public final Atom atom2;
	public double length; 
	public final int type; //1 single bond, 2 double bond, 3 triple bond
	public boolean movable = true;
	
	public Bond(Atom a1, Atom a2, int t)
	{
		atom1 = a1;
		atom2 = a2;
		length = a1.getDistance(a2);
		type = t;
	}
	
	public Bond(Atom a1, Atom a2, double distance)
	{
		atom1 = a1;
		atom2 = a2;
		length = distance;
		type = 1; //unrecognizable bond type
	}
	
	public void recalcLength()
	{
		length = atom1.getDistance(atom2);
	}
	
	public boolean equals(Bond another)
	{
		return((atom1.equals(another.atom1) && atom2.equals(another.atom2))||(atom2.equals(another.atom1)&&atom1.equals(another.atom2)));
	}
	
	public String toMOL()
	{
		return atom1.index+"  "+atom2.index+ "  "+type+"  0  0  0  0";
	}

}
