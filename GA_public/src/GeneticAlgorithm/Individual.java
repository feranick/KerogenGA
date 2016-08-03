package GeneticAlgorithm;

import java.text.DecimalFormat;
import java.util.Random;

public class Individual {

	public int SIZE = 6;
	private int[] genes ;
    private double fitnessValue = -1.0;
    private int genepool = 900;
    private Random rand = new Random(System.nanoTime());
    private Evalu evaluation = null;

    public Individual(int[] initialization, int poolsize) {
    	SIZE = initialization.length;
    	genes = initialization;
    	genepool = poolsize;
    }
    
    public Individual(int poolsize) {
    	genepool = poolsize;
    	genes = new int[SIZE];
    	randGenes();
    }
    
    
    public Individual(int poolsize, int genesize) {
    	SIZE = genesize;
    	genes = new int[SIZE];
    	genepool = poolsize;
    	randGenes();
    }
    
    public Individual(Individual model){
    	SIZE = model.SIZE;
    	genes = new int[SIZE];
    	genepool = model.genepool;
    	randGenes();
    }
    
    public double getFitnessValue() {
        return fitnessValue;
    }

    public void setFitnessValue(double fitnessValue) {
        this.fitnessValue = fitnessValue;
    }
    
    public Evalu getEvalu() {
        return evaluation;
    }
    
    public void setEvalu(Evalu value) {
        evaluation = value;
    }

    public int getGene(int index) {
        return genes[index];
    }

    public void setGene(int index, int gene) {
    	//avoid false setting
        if (gene<=genepool)
        {
        	if(geneCollide(gene))
        		this.setGene(index, rand.nextInt(genepool));
        	else
        		this.genes[index] = gene;
        }
    }
    
    public boolean geneCollide(int gene)
    {
    	for(int i=0; i<SIZE; i++)
    		if(genes[i] == gene) return true;
    	return false;
    }

    public void randGenes() {
        for(int i=0; i<SIZE; ++i) {
            this.setGene(i, rand.nextInt(genepool));
        }
    }

    public void mutate() {
        int index = rand.nextInt(SIZE);
        this.setGene(index, rand.nextInt(genepool));   //random mutate
    }
    
    public String toString(){
    	String rt = "";
    	DecimalFormat plain = new DecimalFormat("0.0000");
    	try {
			for(int i=0; i< SIZE; i++)
			{
				rt = rt+" "+genes[i]+"/"+plain.format(evaluation.parameters.getEntry(i));
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
    	return rt;
    }
}
