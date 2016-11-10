package general;
import java.util.ArrayList;



public class Spectrum {

	public ArrayList<double[]>  peaks; //double vector of {frequency, activity}
	
	public Spectrum()
	{
		peaks = new ArrayList<double[]>();
	}
	
	public void addPeak(double freq, double act)
	{
		//sort the peaks when adding a new peak. low freq comes first.
		if(peaks.size()>0)
		{
			boolean added = false;
			for(int i=0;i<peaks.size();i++)
			{
				if(peaks.get(i)[0]>freq)
				{
					peaks.add(i, new double[]{freq,act});
					added = true;
				}
			}
			if(!added)peaks.add(new double[]{freq,act});
		}
		else
			peaks.add(new double[]{freq, act});
	}
	
	public void addPeak(double[] peak)
	{
		if(peak.length == 2)
		{
			if(peaks.size()>0)
			{
				boolean added = false;
				for(int i=0;i<peaks.size();i++)
				{
					if(peaks.get(i)[0]>peak[0])
					{
						peaks.add(i, peak);
						added = true;
					}
				}
				if(!added)peaks.add(peak);
			}
			else
				peaks.add(peak);
		}
	}
	
	public int size()
	{
		return peaks.size();
	}
	
	public String print(int i)
	{
		return peaks.get(i)[0] + "\t"+peaks.get(i)[1];
	}

	
}
