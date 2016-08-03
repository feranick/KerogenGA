package general;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import org.bson.BasicBSONObject;
import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


public class DataManager {

	private String serveraddress = "localhost";
	private int serverport = 27017;
	public MongoClient mongoClient ;
	public String databasename = "shale";
	
	public DataManager()
	{
		try {
			mongoClient = new MongoClient( serveraddress , serverport );
		} catch (UnknownHostException e) {
			System.out.println("Database link error.");
		}
	}
	
	public DataManager(String address, int port)
	{
		serveraddress = address;
		serverport = port;
		try {
			mongoClient = new MongoClient( serveraddress , serverport );
		} catch (UnknownHostException e) {
			System.out.println("Database link error.");
		}
	}
	
	public DataManager(String address, String dbname, int port)
	{
		serveraddress = address;
		serverport = port;
		databasename = dbname;
		try {
			mongoClient = new MongoClient( serveraddress , serverport );
		} catch (UnknownHostException e) {
			System.out.println("Database link error.");
		}
	}
		
	public Spectrum readRamanfromdatabase(String moleculename)
	{
		Spectrum readspec = new Spectrum();
		DBObject results = null;
		
		DB db = mongoClient.getDB( databasename );
		Set<String> colls = db.getCollectionNames();
		DBCollection coll = null;
		for (String s : colls) 
		{
		    if(s.contains("ramanspectra"))
		    	coll = db.getCollection(s);
		}
		BasicDBObject doc = new BasicDBObject("moleculename", moleculename);
		if(coll!=null)
		{
			DBCursor cursor = coll.find(doc, new BasicDBObject("_id", 0).append("content", 1));
			try 
			{
				   while(cursor.hasNext()) 
				   {
				       results = cursor.next();
				   }
				} 
			finally 
			{
				cursor.close();
			}
		}
		if(results != null)
		{
			BasicDBList peaks = (BasicDBList) results.get("content");
			if(peaks!=null)
			{
				int nofpeaks = peaks.size();
				double freq = 0.0;
				double activ = 0.0;
				for(int i=0; i<nofpeaks; i++)
				{
					freq=((BasicBSONObject)peaks.get(i)).getDouble("frequency");
					activ=((BasicBSONObject)peaks.get(i)).getDouble("activity");
					readspec.addPeak(freq, activ);
				}
			}
		}
		return readspec;
	}
	
	public Molecule readStructurefromdatabase(String moleculename)
	{
		Molecule readmole = new Molecule();
		DBObject results = null;
		
		String id = getdatabaseid(moleculename);
		if(!id.equalsIgnoreCase(""))
		{
			ObjectId thisid = new ObjectId(getdatabaseid(moleculename));
			DB db = mongoClient.getDB( databasename );
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = null;
			for (String s : colls) 
			{
			    if(s.contains("structure"))
			    	coll = db.getCollection(s);
			}
			BasicDBObject doc = new BasicDBObject("molecule_id", thisid);
			if(coll!=null)
			{
				DBCursor cursor = coll.find(doc, new BasicDBObject("_id", 0).append("atoms", 1));
				try 
				{
					   while(cursor.hasNext()) 
					   {
					       results = cursor.next();
					   }
					} 
				finally 
				{
					cursor.close();
				}
			}
			if(results != null)
			{
				BasicDBList peaks = (BasicDBList) results.get("atoms");
				int atomcount = peaks.size();
				
				for(int i=0; i<atomcount; i++)
				{
					BasicDBObject currentatom = (BasicDBObject)peaks.get(i);
					readmole.addAtom( (new Atom(currentatom.getDouble("x"),currentatom.getDouble("y"),
							currentatom.getDouble("z"),currentatom.getInt("index"),currentatom.getInt("element"))));
				}
			}
		}
		
		return readmole;
	}
	
	public ArrayList<DBObject> readcollectionlist(String collectionName)
	{
		ArrayList<DBObject> results = new ArrayList<DBObject>();
		DB db = mongoClient.getDB( databasename );
		Set<String> colls = db.getCollectionNames();
		DBCollection coll = null;
		for (String s : colls) 
		{
		    if(s.contains(collectionName))
		    	coll = db.getCollection(s);
		}
		BasicDBObject doc = new BasicDBObject();
		if(coll!=null)
		{
			DBCursor cursor = coll.find(doc, new BasicDBObject("_id", 0));
			try 
			{
				   while(cursor.hasNext()) 
				   {
				       results.add(cursor.next());
				   }
				} 
			finally 
			{
				cursor.close();
			}
		}
		return results;
	}
	
	public String getdatabaseid(String moleculename)
	{
		String result ="";
		DB db = mongoClient.getDB( databasename );
		Set<String> colls = db.getCollectionNames();
		DBCollection coll = null;
		for (String s : colls) 
		{
		    if(s.contains("molecule"))
		    	coll = db.getCollection(s);
		}
		BasicDBObject doc = new BasicDBObject("name", moleculename);
		if(coll!=null)
		{
			DBCursor cursor = coll.find(doc);
			try 
			{
				   while(cursor.hasNext()) 
				   {
				       result = cursor.next().get("_id").toString();
				   }
				} 
			finally 
			{
				cursor.close();
			}
		}
		return result;
	}
	
	public boolean putindatabase(String moleculename, String property, int value, boolean overwrite)
	{
			DB db = mongoClient.getDB( databasename );
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = null;
			for (String s : colls) 
			{
			    if(s.contains("molecule"))
			    	coll = db.getCollection(s);
			}
			BasicDBObject doc = new BasicDBObject("name", moleculename);
			if(coll!=null)
			{
				DBCursor cursor = coll.find(doc);
				try 
				{
					   if(cursor.hasNext()) 
					   {
						   if(overwrite)
				    	   {
						       while(cursor.hasNext()) 
						       {
						    	   DBObject result = cursor.next();
						    	   coll.update(result, new BasicDBObject("$set",new BasicDBObject(property,value)));
						       }
				    	   }
					   } 
					   else
					   {
						   coll.insert(doc.append(property, value));
					   }
					} 
				finally 
				{
					cursor.close();
				}
				
			}
			
			return true;
	}
	
	public boolean putindatabase(String moleculename, String property, String textvalue, boolean overwrite)
	{
			DB db = mongoClient.getDB( databasename );
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = null;
			for (String s : colls) 
			{
			    if(s.contains("molecule"))
			    	coll = db.getCollection(s);
			}
			BasicDBObject doc = new BasicDBObject("name", moleculename);
			if(coll!=null)
			{
				DBCursor cursor = coll.find(doc);
				try 
				{
					   if(cursor.hasNext()) 
					   {
						   if(overwrite)
				    	   {
						       while(cursor.hasNext()) 
						       {
						    	   DBObject result = cursor.next();
						    	   coll.update(result, new BasicDBObject("$set",new BasicDBObject(property,textvalue)));
						       }
				    	   }
					   } 
					   else
					   {
						   coll.insert(doc.append(property, textvalue));
					   }
					} 
				finally 
				{
					cursor.close();
				}
				
			}
			
			return true;
	}
	
	public boolean putRamanindatabase(String moleculename, Spectrum spctr, boolean overwrite)
	{
		String id = getdatabaseid(moleculename);
		if(!id.equalsIgnoreCase(""))
		{
			
			DB db = mongoClient.getDB( databasename );
			Set<String> colls = db.getCollectionNames();
			DBCollection coll = null;
			for (String s : colls) 
			{
			    if(s.contains("ramanspectra"))
			    	coll = db.getCollection(s);
			}
			BasicDBObject doc = new BasicDBObject("moleculename", moleculename);
			
			if(coll!=null)
			{
				DBCursor cursor = coll.find(doc);
				try 
				{
					   if(cursor.hasNext()) 
					   {
					       while(cursor.hasNext()) 
					       {
					    	   DBObject result = cursor.next();
					    	   if(overwrite)
					    	   {
					    		   coll.remove(result);
					    		   
					    		   DBObject strobj = doc;
								   coll.insert(strobj);
								   if(spctr != null)
								   {
									   int spectrumsize = spctr.peaks.size();
									   for(int i=0; i< spectrumsize; i++)
									   {
										   double[] pk= spctr.peaks.get(i);
										   coll.update(strobj, new BasicDBObject("$push", new BasicDBObject("content",
												   new BasicDBObject("frequency",pk[0]).append("activity", pk[1])))); 
									   }
								   }
								   Object newid = coll.find(strobj).next().get("_id");
								   DBCollection molecules = db.getCollection("molecule");
					    		   DBObject thismolecule = null;
					    		   BasicDBObject search = new BasicDBObject("name", moleculename);
					    		   if(molecules.find(search).hasNext())
					    		   {
					    			   thismolecule = molecules.find(search).next();
					    			   molecules.update(thismolecule, new BasicDBObject("$set",new BasicDBObject("ramanspectrum_id",newid)));
					    		   }
					    	   }
					    	   else return false;
					       }
					   } 
					   else
					   {
						   DBObject strobj = doc;
						   coll.insert(strobj);
						   if(spctr != null)
						   {
							   int spectrumsize = spctr.peaks.size();
							   for(int i=0; i< spectrumsize; i++)
							   {
								   double[] pk= spctr.peaks.get(i);
								   coll.update(strobj, new BasicDBObject("$push", new BasicDBObject("content",
										   new BasicDBObject("frequency",pk[0]).append("activity", pk[1])))); 
							   }
						   }
						   DBCollection molecules = db.getCollection("molecule");
						   Object newid = coll.find(strobj).next().get("_id");
						   DBObject thismolecule = null;
						   BasicDBObject search = new BasicDBObject("name", moleculename);
			    		   if(molecules.find(search).hasNext())
			    		   {
			    			   thismolecule = molecules.find(search).next();
			    			   molecules.update(thismolecule, new BasicDBObject("$set",new BasicDBObject("ramanspectrum_id",newid)));
			    		   }
					   }
					} 
				finally 
				{
					cursor.close();
				}
				
			}
			
			return true;
		}
		else
			return false;
	}
	
	public boolean putStructureindatabase(String structurename, String moleculename, Molecule structure, boolean overwrite)
	{
		
			String id = getdatabaseid(moleculename);
			if(!id.equalsIgnoreCase(""))
			{
				ObjectId thisid = new ObjectId(getdatabaseid(moleculename));
				structure.moleculename = moleculename;
				putStructureindatabase(structurename, thisid, structure, overwrite);
				return true;
			}
			else
			{
				return false;
			}
	}
	
	public boolean putStructureindatabase(String structurename, ObjectId molecule_id, Molecule structure, boolean overwrite)
	{
		DB db = mongoClient.getDB( databasename );
		Set<String> colls = db.getCollectionNames();
		DBCollection coll = null;
		for (String s : colls) 
		{
		    if(s.contains("structure"))
		    	coll = db.getCollection(s);
		}
		BasicDBObject doc = new BasicDBObject("structurename", structurename);
		if(coll!=null)
		{
			DBCursor cursor = coll.find(doc);
			try 
			{
				   if(cursor.hasNext()) 
				   {
				       while(cursor.hasNext()) 
				       {
				    	   DBObject result = cursor.next();
				    	   if(overwrite)
				    	   {
				    		   coll.remove(result);
				    		   BasicDBObject qu = new BasicDBObject("_id",molecule_id);
				    		   DBCollection molecules = db.getCollection("molecule");
				    		   DBObject thismolecule = null;
				    		   if(molecules.find(qu).hasNext())
				    		   {
				    			   thismolecule = molecules.find(qu).next();
				    			   molecules.update(thismolecule,new BasicDBObject("$pull", new BasicDBObject("structure",
										   new BasicDBObject("structurename", structurename))));
				    		   }
				    		   
				    		   DBObject strobj = doc.append("molecule_id", molecule_id);
							   coll.insert(strobj);
							   if(structure != null)
							   {
								   for(int i=0; i< structure.atoms.size(); i++)
								   {
									   Atom currentatom = structure.atoms.get(i);
									   coll.update(strobj, new BasicDBObject("$push", new BasicDBObject("atoms",
											   new BasicDBObject("index",i+1).append("element", currentatom.spcy)
									   			.append("x", currentatom.x).append("y", currentatom.y).append("z", currentatom.z)))); //atom index start from 1
								   }
								   for(int i=0; i< structure.bonds.size(); i++)
								   {
									   Bond currentbond = structure.bonds.get(i);
									   coll.update(strobj, new BasicDBObject("$push", new BasicDBObject("bonds",
											   new BasicDBObject("index",i).append("atom1", currentbond.atom1.index)
									   			.append("atom2", currentbond.atom2.index).append("bondlength", currentbond.length).append("canRotate", currentbond.movable))));
								   }
								   if(!structure.moleculename.equals(""));
								           coll.update(strobj, new BasicDBObject("$set",new BasicDBObject("moleculeName",structure.moleculename)));
							   }
							   Object newid = coll.find(strobj).next().get("_id");
				    		   thismolecule = null;
				    		   if(molecules.find(qu).hasNext())
				    		   {
				    			   thismolecule = molecules.find(qu).next();
				    			   molecules.update(thismolecule,new BasicDBObject("$push", new BasicDBObject("structure",
										   new BasicDBObject("structure_id",newid).append("structurename", structurename))));
				    		   }
				    	   }
				       }
				   } 
				   else
				   {
					   DBObject strobj = doc.append("molecule_id", molecule_id);
					   coll.insert(strobj);
					   if(structure != null)
					   {
						   for(int i=0; i< structure.atoms.size(); i++)
						   {
							   Atom currentatom = structure.atoms.get(i);
							   coll.update(strobj, new BasicDBObject("$push", new BasicDBObject("atoms",
									   new BasicDBObject("index",i+1).append("element", currentatom.spcy)
							   			.append("x", currentatom.x).append("y", currentatom.y).append("z", currentatom.z)))); //atom index start from 1
						   }
						   for(int i=0; i< structure.bonds.size(); i++)
						   {
							   Bond currentbond = structure.bonds.get(i);
							   coll.update(strobj, new BasicDBObject("$push", new BasicDBObject("bonds",
									   new BasicDBObject("index",i).append("atom1", currentbond.atom1.index)
							   			.append("atom2", currentbond.atom2.index).append("bondlength", currentbond.length).append("canRotate", currentbond.movable))));
						   }
						   if(!structure.moleculename.equals(""));
				           		coll.update(strobj, new BasicDBObject("$set",new BasicDBObject("moleculeName",structure.moleculename)));
					   }
					   Object newid = coll.find(doc).next().get("_id");
					   BasicDBObject qu = new BasicDBObject("_id",molecule_id);
					   DBCollection molecules = db.getCollection("molecule");
					   if(molecules.find(qu).hasNext()) 
					   {
						   DBObject thismolecule = molecules.find(qu).next();
						   molecules.update(thismolecule,new BasicDBObject("$push", new BasicDBObject("structure",
								   new BasicDBObject("structure_id",newid).append("structurename", structurename))));
					   }
				   }
				} 
			finally 
			{
				cursor.close();
			}
			
		}
		
		return true;
	}
}
