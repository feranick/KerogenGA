package GeneticAlgorithm;

import general.Spectrum;

import org.apache.commons.math3.linear.RealVector;

public class Evalu {
	public RealVector parameters;
	public RealVector residual;
	public double rms;
	public Spectrum finalspectrum;
	
	public Evalu(RealVector arg0, RealVector res, double rootms, Spectrum finalsp)
	{
		parameters = arg0;
		residual = res;
		rms = rootms;
		finalspectrum = finalsp;
	}
}
