package general;
import java.util.ArrayList;

public class Molecule {

	public ArrayList<Atom> atoms;
	public ArrayList<Bond> bonds;
	public boolean issimplemolecule;
	public boolean[] containsimpleType = new boolean[6];
	public double cella = 0.0;
	public double cellb = 0.0;
	public double cellc = 0.0;
	public double cellalpha = 90.0;
	public double cellbeta = 90.0;
	public double cellgamma = 90.0;
	public String moleculename = "";
	
	public Molecule()
	{
		atoms = new ArrayList<Atom>();
		bonds = new ArrayList<Bond>();
		issimplemolecule = true;
	}
	
	public Molecule(int natom, int nbond)
	{
		atoms = new ArrayList<Atom>(natom);
		bonds = new ArrayList<Bond>(nbond);
		issimplemolecule = true;
		for(int i=0; i<6; i++)
		{
			containsimpleType[i] = false;
		}
	}
	
	public Molecule(double l1, double l2, double l3, double a1, double a2, double a3)
	{
		cella = l1;
		cellb = l2;
		cellc = l3;
		cellalpha = a1;
		cellbeta = a2;
		cellgamma = a3;
	}
	
	public Molecule(double l1, double l2, double l3, double a1, double a2, double a3, ArrayList<Atom> alist){
		cella = l1;
		cellb = l2;
		cellc = l3;
		cellalpha = a1;
		cellbeta = a2;
		cellgamma = a3;
		atoms = alist;
	}
	
	public void addAtom(Atom a)
	{
		if(atoms.size()>0)
		{
			boolean exist = false;
			/*for(int i=0; i<atoms.size(); i++)
			{
				if(atoms.get(i).equals(a))
					exist = true;
			}*/
			if(!exist)
			{
				atoms.add(a);
			}
		}
		else
		{
			atoms.add(a);
		}
		int sp = a.spcy;
		if(sp == 0)
			issimplemolecule = false;
		else if(sp ==1)
			containsimpleType[0] = true;
		else if(sp ==2)
			containsimpleType[1] = true;
		else if(sp ==3)
			containsimpleType[2] = true;
		else if(sp ==4)
			containsimpleType[3] = true;
		else if(sp ==5)
			containsimpleType[4] = true;
//		else if(sp ==6)
//			containsimpleType[5] = true;
	}
	
	public boolean removeAtom(Atom a)
	{
		return atoms.remove(a);
	}
	
	public void addBond(Bond a)
	{
		if(a.atom1.spcy ==2 || a.atom2.spcy ==2)
			a.movable = false;  //anything bond with H
		//if(a.atom1.spcy ==1 && a.atom2.spcy ==1)
		//	a.movable = true;  //C3C C=C and C-C
		bonds.add(a);
		a.atom1.addNeighbour(a.atom2);
		a.atom2.addNeighbour(a.atom1);
	}
}
