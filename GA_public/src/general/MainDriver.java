package general;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import GeneticAlgorithm.ElitePopulation;
import GeneticAlgorithm.Individual;

import com.mongodb.DBObject;


public class MainDriver {

//	public static String fontfile = "/Library/Fonts/Microsoft/Arial.ttf";
	public static String fontfile = "~/Arial.ttf";
	private static DataManager database = new DataManager();
/*
 * When running, give it parameters like:
 * driver.jar --elitefit 10 /your/path/here/expspectrum.txt
 * where 10 is the number of fingerprint spectra
 * expspectrum.txt is a two column text file of the experimental Raman spectrum, 
 * with the first column being frequency and second column being Raman response.  
 */
	public static void main(String[] args) {

		String inputSpectrum = "";
		int GAgenesize = 5;
		
		String gaussianoutputfile = "";
		String infofile = "";
		String moleculename = "";
		boolean storetodatabase = false;
		boolean elitefit = false;
		boolean NIST=false;
		boolean diamondoid = false;
		double laserwavelength = 633.0;
        double generalBrd = 5.0;
		
		for(int i=0; i<args.length; i++)
		{
			if(args[i].startsWith("--version"))
			{
				System.out.println("GA tool version 1.0");
			}
			if(args[i].startsWith("--elitefit"))
			{
				if(args.length>i+2)
				{
					GAgenesize = Integer.parseInt(args[i+1]);
					inputSpectrum = args[i+2];
					i=i+2;
					elitefit=true;
				}
				else
				{
					System.out.println("Missing input file name.");
				}
			}
			else if(args[i].startsWith("--laser"))
			{
				if(args.length>i)
				{
					laserwavelength = Double.parseDouble(args[i+1]);
				}
				else
				{
					System.out.printf("Excitation wavelength set to %s nm\n", laserwavelength);
				}
			}
            else if(args[i].startsWith("--width"))
            {
                if(args.length>i)
                {
                    generalBrd = Double.parseDouble(args[i+1]);
                }
                else
                {
                    System.out.printf("Peak width set to default: %s 1/cm\n", generalBrd);
                }
            }
			else if(args[i].startsWith("--combineraman"))
			{
				combineraman("./", laserwavelength);
			}
			else if(args[i].startsWith("--database"))
			{
				if(args.length>i)
				{
					String[] addr=args[i+1].split("[:]");
					if(addr.length==3)
					{
						System.out.println("Database address: "+addr[0]);
						System.out.println("Port number: "+Integer.parseInt(addr[1]));
						System.out.println("DB name: "+addr[2]);
						database = new DataManager(addr[0], addr[2], Integer.parseInt(addr[1]));
					}
				}
				else
				{
					System.out.println("Missing database address.");
				}
			}
			else if(args[i].startsWith("--storeraman"))
			{
				if(args.length>i)
				{
					//gaussiantoxyz(args[i+1],args[i+1]+".xyz");
					gaussianoutputfile = args[i+1];
					infofile = args[i+2];
					moleculename = args[i+3];
					storetodatabase = true;
				}
				else
				{
					System.out.println("Missing input files.");
				}
			}
			else if(args[i].startsWith("--diamondoid"))
			{
					diamondoid = true;
			}
			else if(args[i].startsWith("--NIST"))
			{
					NIST = true;
			}
			else if(args[i].startsWith("--help"))
			{
				System.out.println("\nGT version 1.1-20161109a\n");
				System.out.println("Usage:");
				System.out.println("--storeraman: store calculated raman activity into the database.");
				System.out.println("    java -jar gt.jar --storeraman gaussianoutput infofile moleculename (with info file)");
				System.out.println("    java -jar gt.jar --storeraman gaussianoutput null moleculename (without info file)\n");
				System.out.println("--elitefit N: fit an experimental raman spectrum with N spectra using genetic algorithm.");
				System.out.println("    java -jar gt.jar --elitefit 10 ramanfilename\n");
				System.out.println("--diamondoid: include diamondoids in fitting.");
				System.out.println("    java -jar gt.jar --diamondoid --elitefit 10 ramanfilename\n");
				System.out.println("--NIST: include NIST library in fitting.");
				System.out.println("    java -jar gt.jar --NIST --elitefit 10 ramanfilename\n");
				System.out.printf("--laser wavelength: Change excitation wavelength in nm (default: %s).\n", laserwavelength);
				System.out.println("    java -jar gt.jar --laser 488.0 --NIST --elitefit 10 ramanfilename\n");
                System.out.printf("--width peakWidth: Change peak width in 1/cm (default: %s).\n", generalBrd);
                System.out.println("    java -jar gt.jar --width 10.0 --NIST --elitefit 10 ramanfilename\n");
			}
		}
		
		if(storetodatabase)
		{
			addramantodatabase(gaussianoutputfile, infofile, moleculename);
		}
		if(elitefit)
		{
			System.out.println("Link Start.");
            System.out.printf(" Experimental Raman spectra: %s\n", inputSpectrum);
            System.out.printf(" Excitation wavelength: %s\n", laserwavelength);
            System.out.printf(" Peak width: %s\n", generalBrd);
            System.out.printf(" Number of molecule per fit: %s\n", GAgenesize);
            System.out.printf(" Using libraries:");
            if(diamondoid == true)
            {
                System.out.printf(" diamondoids");
            }
            if(NIST==true)
            {
                System.out.printf(" NIST");
            }
            System.out.println("\n");
			Elitismfit(inputSpectrum, diamondoid, GAgenesize, NIST, laserwavelength, generalBrd);
		}
	}
	
	private static void addramantodatabase(String gaussianoutputfilename, String initialconfigfilename, String moleculename)
	{
		BufferedReader input = null;
		BufferedReader configureinput = null;
		try {
			String line;
			if(!initialconfigfilename.equalsIgnoreCase("null"))
			{
				int ringnumber = 0;
				configureinput = new BufferedReader(new FileReader(initialconfigfilename));
				line = configureinput.readLine();
				while(line!=null )
				{
					if(line.contains("ring index:"))
					{
						ringnumber ++;
						String indexes = line.substring(line.indexOf(":")+1);
						database.putindatabase(moleculename, ringnumber+"th ring", indexes, true);
					}
					line=configureinput.readLine();
				}
				database.putindatabase(moleculename, "numberofrings", ringnumber, true);
			} else {
				database.putindatabase(moleculename, "numberofrings", 0, true);
			}
			
			input = new BufferedReader(new FileReader(gaussianoutputfilename));
			
			Spectrum ramanspec = new Spectrum();
			Molecule thismolecule = new Molecule();
			line = input.readLine();
			while(line!=null )
			{
				if(line.contains("orientation:"))
				{
					thismolecule = new Molecule(); //empty the molecule every time there is a read-in of coordinates.
					line = input.readLine(); // ---------------------------------------------------------------------
					line = input.readLine(); // Center     Atomic      Atomic             Coordinates (Angstroms)
					line = input.readLine(); // Number     Number       Type             X           Y           Z
					line = input.readLine(); // ---------------------------------------------------------------------
					line = input.readLine(); // atom 1
					while(!line.startsWith(" -----------"))
					{
						String[] tokens = line.split("[ ]+");
						int atomtype = 0;
						switch(Integer.parseInt(tokens[2]))
						{
						case 6:
							atomtype =1;
							break;
						case 1:
							atomtype =2;
							break;
						case 8:
							atomtype =3;
							break;
						case 7:
							atomtype =4;
							break;
						case 15:
							atomtype =5;
							break;
						default:
							atomtype =6;
							System.out.println(tokens[2]);
						}
						Atom currentatom = new Atom(Double.parseDouble(tokens[4]), Double.parseDouble(tokens[5]), 
								Double.parseDouble(tokens[6]),0.0, Integer.parseInt(tokens[1]),atomtype);
						thismolecule.addAtom(currentatom);
						line = input.readLine();
					}
				}
				if(line.startsWith(" Frequencies "))
				{
					
					String[] tokens = line.split("[ ]+");
					if(tokens.length == 6){
						double freq1 = Double.parseDouble(tokens[3]);
						double freq2 = Double.parseDouble(tokens[4]);
						double freq3 = Double.parseDouble(tokens[5]);
						line = input.readLine();  // Red. masses -- 
						line = input.readLine();  // Frc consts  --    
						line = input.readLine();  // IR Inten    --   
						line = input.readLine();  // Raman Activ --   
						tokens = line.split("[ ]+");
						double ramanact1 = Double.parseDouble(tokens[4]);
						double ramanact2 = Double.parseDouble(tokens[5]);
						double ramanact3 = Double.parseDouble(tokens[6]);
						ramanspec.addPeak(freq1, ramanact1);
						ramanspec.addPeak(freq2, ramanact2);
						ramanspec.addPeak(freq3, ramanact3);
						line = input.readLine();  // Depolar (P) --  
						line = input.readLine();  // Depolar (U) --   
						line = input.readLine();  //  Atom  AN      X      Y      Z    ...
						for(int i=0; i<thismolecule.atoms.size(); i++)
						{
							line = input.readLine();
						}
					}
					else if(tokens.length == 5)
					{
						double freq1 = Double.parseDouble(tokens[3]);
						double freq2 = Double.parseDouble(tokens[4]);
						line = input.readLine();  // Red. masses -- 
						line = input.readLine();  // Frc consts  --    
						line = input.readLine();  // IR Inten    --   
						line = input.readLine();  // Raman Activ --  
						tokens = line.split("[ ]+");
						double ramanact1 = Double.parseDouble(tokens[4]);
						double ramanact2 = Double.parseDouble(tokens[5]);
						ramanspec.addPeak(freq1, ramanact1);
						ramanspec.addPeak(freq2, ramanact2);
						line = input.readLine();  // Depolar (P) --  
						line = input.readLine();  // Depolar (U) --   
						line = input.readLine();  //  Atom  AN      X      Y      Z    ...
						for(int i=0; i<thismolecule.atoms.size(); i++)
						{
							line = input.readLine();
						}
					}
					else if(tokens.length == 4){
						double freq1 = Double.parseDouble(tokens[3]);
						line = input.readLine();  // Red. masses -- 
						line = input.readLine();  // Frc consts  --    
						line = input.readLine();  // IR Inten    --   
						line = input.readLine();  // Raman Activ --   
						tokens = line.split("[ ]+");
						double ramanact1 = Double.parseDouble(tokens[4]);
						ramanspec.addPeak(freq1, ramanact1);
						line = input.readLine();  // Depolar (P) --  
						line = input.readLine();  // Depolar (U) --   
						line = input.readLine();  //  Atom  AN      X      Y      Z    ...
						for(int i=0; i<thismolecule.atoms.size(); i++)
						{
							line = input.readLine();
						}
					}
				}
				line = input.readLine();
			}
			if(database.putStructureindatabase(moleculename+"_ga", moleculename, thismolecule, true))
				System.out.println("sturcutre update success!");
			
			if(database.putRamanindatabase(moleculename, ramanspec, true))
				System.out.println("raman update success!");
			
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(input!= null)
			{
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(configureinput!= null)
			{
				try {
					configureinput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void Elitismfit(String inputSpectrum, boolean isdiamondoid, int genesize, boolean withNIST, double wavelength, double generalBrd)
	{
		BufferedReader input = null;
		PrintWriter spectraoutput = null;
        //double generalBrd = 10.0;
		try 
		{
			input = new BufferedReader(new FileReader(inputSpectrum));
			
			Spectrum readinspectrum = new Spectrum();
			String line = input.readLine();
			while(line!=null )
			{
				String[] tokens = line.split("[ \t]+");
				if(tokens.length == 2)
				{
					double freq = Double.parseDouble(tokens[0]);
					double acti = Double.parseDouble(tokens[1]);
					readinspectrum.addPeak(freq, acti);
				}
				line=input.readLine();
			}
			
			if(readinspectrum.size() >0)
			{
				ArrayList<DBObject> dbcollection = database.readcollectionlist("molecule");
				int sizeofdb = dbcollection.size();
				ArrayList<Spectrum> spectrumcollection = new ArrayList<Spectrum>();
				ArrayList<Spectrum> originalspectrumcollection = new ArrayList<Spectrum>();
				ArrayList<String> namecollection = new ArrayList<String>();
				if (!withNIST && !isdiamondoid) 
				{
					for (int i = 0; i < sizeofdb; i++) 
					{
						
						if (dbcollection.get(i).get("numberofrings") != null && (Integer) dbcollection.get(i).get("numberofrings") > 0) 
						{
							String currentSpName = (String) dbcollection.get(i)
									.get("name");
							namecollection.add(currentSpName);
							originalspectrumcollection.add(database
									.readRamanfromdatabase(currentSpName));
							spectrumcollection.add(normalizeSpec(
									readinspectrum,
									originalspectrumcollection.get(originalspectrumcollection.size()-1),generalBrd, wavelength));
						}
					}
				}
				else if(!isdiamondoid)
				{
					for (int i = 0; i < sizeofdb; i++) 
					{
						
						if (dbcollection.get(i).get("numberofrings") != null && (Integer) dbcollection.get(i).get("numberofrings") > 0) 
						{
							String currentSpName = (String) dbcollection.get(i)
									.get("name");
							namecollection.add(currentSpName);
							originalspectrumcollection.add(database
									.readRamanfromdatabase(currentSpName));
							spectrumcollection.add(normalizeSpec(
									readinspectrum,
									originalspectrumcollection.get(originalspectrumcollection.size()-1),generalBrd, wavelength));
						}
						else
						{
							String currentSpName = (String) dbcollection.get(i)
									.get("name");
							if(currentSpName!=null && currentSpName.contains("NIST"))
							{
								namecollection.add(currentSpName);
								originalspectrumcollection.add(database
										.readRamanfromdatabase(currentSpName));
								spectrumcollection.add(normalizeSpec(
										readinspectrum,
										originalspectrumcollection.get(originalspectrumcollection.size()-1),generalBrd, wavelength));
							}
						}
					}
				}
				else {
					for (int i = 0; i < sizeofdb; i++) {
						String currentSpName = (String) dbcollection.get(i)
								.get("name");
						namecollection.add(currentSpName);
						originalspectrumcollection.add(database
								.readRamanfromdatabase(currentSpName));
						spectrumcollection.add(normalizeSpec(readinspectrum,
								originalspectrumcollection.get(i), generalBrd, wavelength));
					}
				}
				sizeofdb = originalspectrumcollection.size();
				Spectrum bestfit = null;
				GeneticAlgorithm.Evalu bestevaluation = null;
				
				try {
					try {
						spectraoutput = new PrintWriter(new FileWriter("checkpoint.conversion.txt"));
						for(int l=0; l<namecollection.size(); l++)
						{
							spectraoutput.println(l+" : "+namecollection.get(l));
						}
					}catch (IOException e) {
						e.printStackTrace();
					}finally
					{
						if(spectraoutput!= null)spectraoutput.close();
					}
					ElitePopulation sandbox = new ElitePopulation(readinspectrum,spectrumcollection,genesize);
					sandbox.evolve();
					Individual bestchromsome = sandbox.findBestIndividual();
					ArrayList<Spectrum> fittedspectra = new ArrayList<Spectrum>();
					for(int i=0; i<bestchromsome.SIZE; i++)
					{
						fittedspectra.add(spectrumcollection.get(bestchromsome.getGene(i)));
					}
					bestevaluation=bestchromsome.getEvalu();
					if(bestevaluation == null)System.out.println("empty evaluation");
					
					bestfit = new Spectrum();
					double[][] inputX = new double[fittedspectra.size()][readinspectrum.size()];
					int datasize = readinspectrum.size();
					for(int i=0; i<inputX.length; i++)
					{
						ArrayList<double[]> thissp = fittedspectra.get(i).peaks;
						for(int j=0; j<datasize; j++)
						{
							inputX[i][j] = thissp.get(j)[1];
						}
					}
					RealMatrix Xmatrix = new Array2DRowRealMatrix(inputX);
					RealMatrix Xtrans = Xmatrix.transpose();
					RealVector finalspectrum = Xtrans.operate(bestevaluation.parameters);
					for(int i=0; i<datasize; i++)
					{
						bestfit.addPeak(readinspectrum.peaks.get(i)[0], finalspectrum.getEntry(i));
					}
					
					double carboncount = 0;
					double hcount = 0;
					double ratiofactor = 0.0;
					for(int l=0; l<bestchromsome.SIZE; l++)
					{
						try {
							Molecule molecule1st = database.readStructurefromdatabase(namecollection.get(bestchromsome.getGene(l)));
							ratiofactor = bestevaluation.parameters.getEntry(l);
							for(int m=0; m<molecule1st.atoms.size(); m++)
							{
								if(molecule1st.atoms.get(m).spcy ==1)
									carboncount += ratiofactor;
								else if(molecule1st.atoms.get(m).spcy ==2)
									hcount +=ratiofactor;
							}
						} catch (NullPointerException e) {
							System.out.println("Missing atomic structure for "+namecollection.get(l));
						}
					}
					System.out.println("Predicted fit H/C ratio: "+(hcount/carboncount));
					
					try {
						spectraoutput = new PrintWriter(new FileWriter("bestfit.dat"));
						for(int l=0; l<bestfit.size(); l++)
						{
							spectraoutput.println(bestfit.print(l));
						}
					}catch (IOException e) {
						e.printStackTrace();
					}finally
					{
						if(spectraoutput!= null)spectraoutput.close();
					}
					try {
						spectraoutput = new PrintWriter(new FileWriter("bestIndividual.txt"));
						for(int l=0; l<bestchromsome.SIZE; l++)
						{
							spectraoutput.println(namecollection.get(bestchromsome.getGene(l))
									+" : "+bestchromsome.getEvalu().parameters.getEntry(l) );
						}
					}catch (IOException e) {
						e.printStackTrace();
					}finally
					{
						if(spectraoutput!= null)spectraoutput.close();
					}

					for (int l=0; l<bestchromsome.SIZE; l++) {
						try {
							spectraoutput = new PrintWriter(new FileWriter(
									namecollection.get(bestchromsome.getGene(l))+".dat"));
							for (int m = 0; m < fittedspectra.get(l).size(); m++) {
								spectraoutput.println(fittedspectra.get(l).print(m));
							}
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							if (spectraoutput != null)
								spectraoutput.close();
						}
					}
				} catch (SingularMatrixException e) {
					System.out.println("Spectrum too close to resolve.");
				} catch (NullPointerException e) {
					e.printStackTrace();
					System.out.println("Spectrum error.");
				}
						
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(input!= null)
			{
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(spectraoutput!= null)
			{
				spectraoutput.close();
			}
		}
		
	}
	
	private static void combineraman(String directoryname, double wavelength)
	{
		File workdir = new File(directoryname);
		if(workdir.exists() && workdir.isDirectory())
		{
			File[] listOfFiles = workdir.listFiles();
			if(listOfFiles != null)
			{
				if(listOfFiles.length>0)
				{
					ArrayList<Double> freqs = new ArrayList<Double>();
					ArrayList<Double> actis = new ArrayList<Double>();
					for(int i=0; i<listOfFiles.length;i++)
					{
						if(listOfFiles[i].getName().endsWith(".txt"))
						try 
						{
							BufferedReader input = new BufferedReader(new FileReader(listOfFiles[i].getAbsolutePath()));
							String line = input.readLine(); //first line is headline, throw away.
							line = input.readLine();
							while(line != null)
							{
								String[] tokens = line.split("[ ]+");
								double freq = Double.parseDouble(tokens[0]);
								double ramanact = Double.parseDouble(tokens[1]);
								freqs.add(new Double(freq));
								actis.add(new Double(ramanact));
								line = input.readLine();
							}
							input.close();
						} catch(FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					gaussianbroadening(freqs, actis, 10.0, wavelength);
				}
			}
		}
	}

	private static Spectrum normalizeSpec(Spectrum target, Spectrum input, double smearing, double wavelength) {
		double gamma = smearing;
		double pre = 1.0/gamma * Math.sqrt(2.0*Math.PI);
		double gamsquare = gamma*gamma;
		double[][] states = new double[input.size()][2];
		Spectrum normalized = new Spectrum();
		double factor = 10000000/wavelength; //633nm -> factor=15798.0
		for(int i=0; i<input.size(); i++)
		{
			double waveincm = input.peaks.get(i)[0];
			states[i][0] = waveincm;
			double frequencydependency = Math.pow((factor-waveincm), 4.0)/(1.0e13*waveincm*(1-Math.exp(-0.0048*waveincm)));
			
			states[i][1] = frequencydependency*input.peaks.get(i)[1];
		}

		int numberofdots = target.size();
		
		for(int i=0; i<numberofdots; i++)
		{
			double freq = target.peaks.get(i)[0];
			double spectra = 0.0;
			for(int j=0; j<states.length; j++)
			{
				spectra+= states[j][1] * pre * Math.exp(
						(freq-states[j][0])*(freq-states[j][0]) / (-2.0*gamsquare));
			}
			normalized.addPeak(freq, spectra);
		}
		return normalized;
	}
	
	//broadening for Raman, also have Raman activity to Intensity conversion here.
	private static void gaussianbroadening(ArrayList<Double> freqs, ArrayList<Double> actis, double gamma, double wavelength) 
	{
		double pre = 1.0/gamma * Math.sqrt(2.0*Math.PI);
		double gamsquare = gamma*gamma;
		double[][] states = new double[freqs.size()][2];
		double factor = 10000000/wavelength;
		for(int i=0; i<freqs.size(); i++)
		{
			double waveincm = freqs.get(i).doubleValue();
			states[i][0] = waveincm;
			double frequencydependency = Math.pow((factor-waveincm), 4.0)/(1.0e13*waveincm*(1-Math.exp(-0.0048*waveincm)));
			states[i][1] = frequencydependency*actis.get(i).doubleValue();
		}
		double dE = 0.2; //0.2 cm-1 interval
		double Emin = 400.0; //400 cm-1 lower limit
		double Emax = 2000.0; //2000 cm-1 upper limit
		int numberofdots = (int)((Emax-Emin)/dE);
		double[][] spectra = new double[numberofdots][2];
		for(int i=0; i<spectra.length; i++)
		{
			spectra[i][0] = Emin + i * dE;
			spectra[i][1] = 0.0;
			for(int j=0; j<states.length; j++)
			{
				spectra[i][1] += states[j][1] * pre * Math.exp(
						(spectra[i][0]-states[j][0])*(spectra[i][0]-states[j][0]) / (-2.0*gamsquare));
			}
		}
		
		PrintWriter output = null;
		try {
			output = new PrintWriter(new FileWriter("gnuplot.dat"));
			for(int i=0; i<spectra.length; i++)
			{
				output.println(spectra[i][0] +"  "+spectra[i][1]);
			}
			output.close();
			output = new PrintWriter(new FileWriter("gnuplot.in"));
			output.println("reset");
			output.println("set term png font \""+fontfile+"\"");
			output.println("set term png size 1024, 768");
			output.println("set style line 5 lt rgb \"blue\" lw 2 pt 0");
			output.println("set output \"ramanspectra.png\"");
			output.println("set timestamp \"generated on %Y-%m-%d\"");
			output.println("graphtitle = \"Raman Spectra\"");
			output.println("plot \'gnuplot.dat\' title graphtitle with linespoints ls 5");
			output.close();
			Runtime rt=Runtime.getRuntime();
			String[] commands = new String[3];
			commands[0] = "/bin/sh";
			commands[1] = "-c";
			commands[2] = "gnuplot gnuplot.in";
			rt.exec(commands);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(output!= null)
			{
				output.close();
			}
		}
	}

}
