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
	public String databasename = "hydrocarbon";
	
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
	
}
