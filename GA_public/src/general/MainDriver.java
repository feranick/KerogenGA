package general;

import java.io.BufferedReader;
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
		
		for(int i=0; i<args.length; i++)
		{
			if(args[i].startsWith("--elitefit"))
			{
				if(args.length>i+1)
				{
					GAgenesize = Integer.parseInt(args[i+1]);
					inputSpectrum = args[i+2];
				}
				else
				{
					System.out.println("Missing input file name.");
				}
			}
		}
		
		Elitismfit(inputSpectrum, GAgenesize);
	}
	
	private static void Elitismfit(String inputSpectrum, int genesize) 
	{
		BufferedReader input = null;
		PrintWriter spectraoutput = null;
		double generalBrd = 10.0;
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
				ArrayList<Spectrum> sharpspectrumcollection = new ArrayList<Spectrum>();
				ArrayList<String> namecollection = new ArrayList<String>();
				
				for (int i = 0; i < sizeofdb; i++) 
				{
					String currentSpName = (String) dbcollection.get(i)
							.get("name");
					namecollection.add(currentSpName);
					originalspectrumcollection.add(database.readRamanfromdatabase(currentSpName));
					
					//normalize to same frequency range, add smearing to the spectra. 
					spectrumcollection.add(normalizeSpec(readinspectrum, originalspectrumcollection.get(i), generalBrd));
					sharpspectrumcollection.add(normalizeSpec(readinspectrum, originalspectrumcollection.get(i), 5.0));
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
						fittedspectra.add(sharpspectrumcollection.get(bestchromsome.getGene(i)));
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
				} catch (SingularMatrixException e) {
					System.out.println("Spectrum too close to resolve.");
				} catch (NullPointerException e) {
					e.printStackTrace();
					System.out.println("Spectrum error.");
				}
						
				
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Can't find the experimental Raman spectrum file!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(input!= null)
			{
				try {
					input.close();
				} catch (IOException e) {
					// 
					e.printStackTrace();
				}
			}
			if(spectraoutput!= null)
			{
				spectraoutput.close();
			}
		}
		
	}

	private static Spectrum normalizeSpec(Spectrum target, Spectrum input, double smearing) {
		double gamma = smearing;
		double pre = 1.0/gamma * Math.sqrt(2.0*Math.PI);
		double gamsquare = gamma*gamma;
		double[][] states = new double[input.size()][2];
		Spectrum normalized = new Spectrum();
		for(int i=0; i<input.size(); i++)
		{
			double waveincm = input.peaks.get(i)[0];
			states[i][0] = waveincm;
			
			double frequencydependency = Math.pow((15798.0-waveincm), 4.0)/(1.0e13*waveincm*(1-Math.exp(-0.0048*waveincm)));//633nm
			
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

}
