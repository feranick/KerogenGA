package GeneticAlgorithm;

import general.Spectrum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

public class ElitePopulation {

	public static int ELITISM_K = 30;
    static int POP_SIZE = 200 + ELITISM_K;  // population size
    static int MAX_ITER = 50000;             // max number of iterations
    static double MUTATION_RATE = 0.4;     // probability of mutation

    private static Random m_rand = new Random(System.nanoTime());  // random-number generator
    private Individual[] m_population;
    private Spectrum target = null;
    private ArrayList<Spectrum> geneLibrary = new ArrayList<Spectrum>();
    private String checkpointfilehead = "GAcheckpoint";
    private ArrayList<Individual> theElites ;

    public ElitePopulation(Spectrum target, ArrayList<Spectrum> input, int genesize) {
    	this.target = target;
    	geneLibrary = input;
    	m_population =new Individual[POP_SIZE];
    	
    	for (int i = 0; i < POP_SIZE; i++) {
            m_population[i] = new Individual(geneLibrary.size(), genesize);
        }
    }

    public void setPopulation(Individual[] newPop) {
        System.arraycopy(newPop, 0, this.m_population, 0, POP_SIZE);
    }

    public Individual[] getPopulation() {
        return this.m_population;
    }
    
    public void evolve()
    {
		double avefitness = evaluate();
    	for(int i=0; i<MAX_ITER; i++)
    	{
            theElites = findBestIndividuals();
            evaluateElites();
            //Record the results
    		System.out.print("Step "+i+" ; "+"Average Fitness = " + avefitness );
    		System.out.println(" ; Best Fitness = " + 
    				theElites.get(0).getFitnessValue());
    		if(i<200 && i%10 ==0)
    			checkpoint(checkpointfilehead+"."+i+".txt");
    		else if(i<2000 && i%100 == 0)
            	checkpoint(checkpointfilehead+"."+i+".txt");
    		else if(i%1000 == 0)
    			checkpoint(checkpointfilehead+"."+i+".txt");
    		
    		//Start to build new population
    		Individual[] newPop = new Individual[POP_SIZE];
            Individual[] indiv = new Individual[2];
            int count = 0;
            newPop[0] = theElites.get(0);
            count++;
            while (count < POP_SIZE) {
                // Selection
                indiv[0] = randomSelection();
                indiv[1] = randomSelection();

                // Complete Crossover
                indiv = crossover(indiv[0], indiv[1]);
            	if ( m_rand.nextDouble() < MUTATION_RATE ) {
            		indiv[0].mutate();
            	}
            	if ( m_rand.nextDouble() < MUTATION_RATE ) {
            		indiv[1].mutate();
            	}

                // add to new population
                newPop[count] = indiv[0];
                if(count<POP_SIZE-1)
                	newPop[count+1] = indiv[1];
                count += 2;
            }
            setPopulation(newPop);

            if(i%1000 ==0)
            	catastrophegenesis();

    		avefitness =evaluate();
    		
    	}	
    }
    
    public void checkpoint(String filename)
    {
    	PrintWriter ckoutput = null;
    	try {
			ckoutput = new PrintWriter(new FileWriter(filename));
			for(int i=0; i< POP_SIZE; i++)
			{
				ckoutput.println(m_population[i].toString()+" : "+m_population[i].getFitnessValue());
			}
			
		}catch (IOException e) {
			e.printStackTrace();
		}finally
		{
			if(ckoutput!= null)ckoutput.close();
		}
    }

    public double evaluate() {
    	
        double totalFitness = 0.0;
        int count = 0;
        for (int i = 0; i < POP_SIZE; i++) {
        	if(m_population[i].getFitnessValue()<0)
        	{
        		evaluate(m_population[i]);
        	}
        	if(m_population[i].getFitnessValue()<1000000.0)
        	{
        		totalFitness += m_population[i].getFitnessValue();
        		count++;
        	}
        }
        return totalFitness/count;
    }
    
    public double evaluateElites(){
    	double totalFitness = 0.0;
        int size = theElites.size();
        for (int i = 0; i < size; i++) {
        	
        	totalFitness += theElites.get(i).getFitnessValue();
        }
        if(size>0)return totalFitness/size;
        else return -1;
    }

    
    //Using random selection method to get parents from the elites
    public Individual randomSelection() {
        int size = theElites.size();
        int randNum = m_rand.nextInt(size);
        return theElites.get(randNum);
    }
    
    public ArrayList<Individual> findBestIndividuals() {
        ArrayList<Individual> resultset = new ArrayList<Individual>(ELITISM_K);
        double currentVal;

        if(POP_SIZE>0)
        	resultset.add(m_population[0]);
        for (int idx=1; idx<POP_SIZE; idx++) {
            currentVal = m_population[idx].getFitnessValue();
            
    	if(currentVal<resultset.get(0).getFitnessValue()) //strictly less
    	{
    		resultset.add(0, m_population[idx]);
    		if(resultset.size()>ELITISM_K)
    			resultset.remove(resultset.size()-1);
    	}
    	else
    	{
    		if(resultset.size()<ELITISM_K)
        	{
    			int i=1;
            	while(i<resultset.size())
                {
                	if(currentVal<resultset.get(i).getFitnessValue() && currentVal>resultset.get(i-1).getFitnessValue()) //prevent same value elites
                	{
                		resultset.add(i, m_population[idx]);
                		i=resultset.size()+ELITISM_K+1; 
                	}
                	i++;
                }
            	if(i==resultset.size())resultset.add(m_population[idx]);
        	}
    		else
    		{
    			int i=1;
            	while(i<resultset.size())
                {
            		if(currentVal<resultset.get(i).getFitnessValue() && currentVal>resultset.get(i-1).getFitnessValue())//prevent same value elites
                	{
                		resultset.add(i, m_population[idx]);
                		i=resultset.size()+1;
                		resultset.remove(resultset.size()-1);
                		i=resultset.size()+ELITISM_K+1;
                	}
                	i++;
                }
    		}
    	}
        }
        return resultset;      
    }
    
    public Individual findBestIndividual() {
        ArrayList<Individual> resultset = findBestIndividuals();

        return resultset.get(0);      
    }
    
    public ArrayList<Spectrum> getSpectraRep(Individual ind)
    {
    	ArrayList<Spectrum> spectra = new ArrayList<Spectrum>();
    	for(int i=0; i< ind.SIZE; i++)
    	{
    		spectra.add(geneLibrary.get(ind.getGene(i)));
    	}
    	return spectra;
    }

    //random crossover
    public Individual[] crossover(Individual indiv1,Individual indiv2) {
        Individual[] newIndiv = new Individual[2];
        newIndiv[0] = new Individual(indiv1);
        newIndiv[1] = new Individual(indiv2);

        int randPoint = m_rand.nextInt(indiv1.SIZE);
        int i;
        for (i=0; i<randPoint; ++i) {
            newIndiv[0].setGene(i, indiv1.getGene(i));
            newIndiv[1].setGene(i, indiv2.getGene(i));
        }
        for (; i<indiv1.SIZE; ++i) {
            newIndiv[0].setGene(i, indiv2.getGene(i));
            newIndiv[1].setGene(i, indiv1.getGene(i));
        }

        return newIndiv;
    }


    private double evaluate(Individual a)
    {
    	double rms = 0.0;
    	ArrayList<Spectrum> collectionSpectrum = new ArrayList<Spectrum>();
    	for(int i=0; i<a.SIZE; i++)
    	{
    		collectionSpectrum.add(geneLibrary.get(a.getGene(i)));
    	}
    	
    	try {
			Evalu calculation = leastSquareFit(target, collectionSpectrum);
			rms = calculation.rms;
			a.setFitnessValue(rms);
			a.setEvalu(calculation);
			return rms;
		} catch (SingularMatrixException e) {
			/*use something really really big.*/
			a.setFitnessValue(2000000.0);
			return 2000000.0;
		}
    }
    
   
    private static Evalu leastSquareFit(Spectrum target, ArrayList<Spectrum> input)
	{
		double rms = 0.0;
		int datasize = target.size();
		int inputsize = input.size();
		double[] Y = new double[datasize];
		for(int i=0; i<datasize; i++)
		{
			Y[i] = target.peaks.get(i)[1];
		}
		RealVector Yvector = new ArrayRealVector(Y);
		
		double[][]inputX = new double[input.size()][datasize];
		for(int i=0; i<inputsize; i++)
		{
			ArrayList<double[]> thissp = input.get(i).peaks;
			for(int j=0; j<datasize; j++)
			{
				inputX[i][j] = thissp.get(j)[1];
			}
		}
		RealMatrix Xmatrix = new Array2DRowRealMatrix(inputX);
		RealMatrix Xtrans = Xmatrix.transpose();
		RealMatrix product = Xmatrix.multiply(Xtrans);
		RealVector OpYvector = Xmatrix.operate(Yvector);
		DecompositionSolver solver = new LUDecomposition(product).getSolver();
		RealVector finalparameter = solver.solve(OpYvector);
		for(int i=0; i<finalparameter.getDimension();i++)
		{
			if(finalparameter.getEntry(i)<0.0)
				finalparameter.setEntry(i, 0.0);
		}
		RealVector finalspectrum = Xtrans.operate(finalparameter);
		RealVector residual = finalspectrum.subtract(Yvector);
		rms = residual.getNorm();
		double ytss = Yvector.getNorm();
		rms = rms/ytss;
		
		Spectrum spc = new Spectrum(); 
		for(int i=0; i<datasize; i++)
		{
			spc.addPeak(target.peaks.get(i)[0], finalspectrum.getEntry(i));
		}
		return new Evalu(finalparameter, residual, rms, spc);
	}
    
    private void catastrophegenesis()
    {
    	Individual noah = theElites.get(0);
    	m_population =new Individual[POP_SIZE];
    	m_population[0] = noah;
    	for (int i = 1; i < POP_SIZE; i++) {
            m_population[i] = new Individual(geneLibrary.size(), noah.SIZE);
        }
    }
}
