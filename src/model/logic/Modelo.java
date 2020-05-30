package model.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.teamdev.jxmaps.swing.MapView;
import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.Polygon;

import javax.swing.*;
import java.awt.*;

import model.data_structures.*;

/**
 * Definicion del modelo del mundo
 *
 */
public class Modelo {
	/**
	 * Atributos del modelo del mundo
	 */

	/**
	 * Cola de lista encadenada.
	 */

	private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

	private static final Double LONGMIN =-74.094723;
	private static final Double LONGMAX =-74.062707;

	private static final Double LATMIN =4.597714;
	private static final Double LATMAX = 4.621360;

	private Sectores cuadrantes;
	private Double latmenor=1000.0;
	private Double latmax=0.0;
	private Double longmenor=1000.0;
	private Double longmax=-1000.0;

	private Double latmenorver=1000.0;
	private Double latmaxver=0.0;
	private Double longmenorver=1000.0;
	private Double longmaxver=-1000.0;



	private static final int N=20;

	private Grafo grafo;

	private Grafo grafojson;

	private ArbolRojoNegroBTS datosArbol;

	private ListaDoblementeEncadenada<EstacionPolicia> estaciones;
	
	private HashSeparateChaining verticessinarcos ;
	
	private MaxHeapCP<Comparendo> heapgravedadcomparendos;

	/**
	 * Constructor del modelo del mundo con capacidad predefinida
	 */
	public Modelo()
	{
		grafo= new Grafo();
		grafojson= new Grafo();
		datosArbol= new ArbolRojoNegroBTS();
		estaciones= new ListaDoblementeEncadenada<EstacionPolicia>();
		verticessinarcos =new HashSeparateChaining();
		heapgravedadcomparendos= new MaxHeapCP<Comparendo>();
	}

	/**
	 * Carga el archivo .JSON en una lista enlazada.
	 * @throws FileNotFoundException. Si no encuentra el archivo.
	 */

	public void cargarGrafo() throws FileNotFoundException
	{
		//Definir mejor la entrada para el lector de json

		long inicio = System.currentTimeMillis();
		long inicio2 = System.nanoTime();
		String dirvertices= "./data/bogota_vertices.txt";
		String dirarcos= "./data/bogota_arcos.txt";
		String dir= "./data/estacionpolicia.geojson.json";


		File archivovertices= new File(dirvertices);
		FileReader f = new FileReader(archivovertices);
		BufferedReader b = new BufferedReader(f);

		File archivoarcos= new File(dirarcos);
		FileReader f2 = new FileReader(archivoarcos);
		BufferedReader b2 = new BufferedReader(f2);

		String cadena;

		String cadena1;
		try {

			while((cadena = b.readLine())!=null) {

				String[] propiedades= cadena.split(",");
				Integer id= Integer.parseInt(propiedades[0]);

				Double longitud= Double.parseDouble(propiedades[1]);
				Double latitud= Double.parseDouble(propiedades[2]);
				ListaDoblementeEncadenada coordenadas= new ListaDoblementeEncadenada();
				coordenadas.insertarFinal(longitud);
				coordenadas.insertarFinal(latitud);

				/**	if(latitud>latmaxver) 
				{
					latmaxver=latitud;
				}
				if(latitud <latmenorver) 
				{
					latmenorver=latitud;
				}
				if(longitud>longmaxver) 
				{
					longmaxver=longitud;
				}
				if(longitud<longmenorver) 
				{
					longmenorver=longitud;
				} **/

				if(latitud>latmax) 
				{
					latmax=latitud;
				}
				if(latitud <latmenor) 
				{
					latmenor=latitud;
				}
				if(longitud>longmax) 
				{
					longmax=longitud;
				}
				if(longitud<longmenor) 
				{
					longmenor=longitud;
				}

				grafo.addVertex (id, coordenadas,latitud,longitud);

			}
			b.close();

			verticessinarcos =grafo.getNodos();
			
			
			String dir1= "./data/Comparendos_DEI_2018_Bogot�_D.C_small_50000_sorted.geojson";

			File archivocomparendos= new File(dir1);
			JsonReader reader= new JsonReader( new InputStreamReader(new FileInputStream(archivocomparendos)));
			JsonObject gsonObj0= JsonParser.parseReader(reader).getAsJsonObject();


			JsonArray comparendos=gsonObj0.get("features").getAsJsonArray();
			int g=0;
			while(g<comparendos.size())
			{
				JsonElement obj= comparendos.get(g);
				JsonObject gsonObj= obj.getAsJsonObject();

				JsonObject gsonObjpropiedades=gsonObj.get("properties").getAsJsonObject();
				int objid= gsonObjpropiedades.get("OBJECTID").getAsInt();
				String fecha= gsonObjpropiedades.get("FECHA_HORA").getAsString();
				String mediodeteccion = gsonObjpropiedades.get("MEDIO_DETECCION").getAsString();
				String clasevehiculo=gsonObjpropiedades.get("CLASE_VEHICULO").getAsString();
				String tiposervi=gsonObjpropiedades.get("TIPO_SERVICIO").getAsString();
				String infraccion=gsonObjpropiedades.get("INFRACCION").getAsString();
				String desinfraccion=gsonObjpropiedades.get("DES_INFRACCION").getAsString();
				String localidad=gsonObjpropiedades.get("LOCALIDAD").getAsString();
				String municipio = "";

				JsonObject gsonObjgeometria=gsonObj.get("geometry").getAsJsonObject();

				JsonArray gsonArrcoordenadas= gsonObjgeometria.get("coordinates").getAsJsonArray();
				double longitud= gsonArrcoordenadas.get(0).getAsDouble();
				double latitud= gsonArrcoordenadas.get(1).getAsDouble();


				Comparendo agregar=new Comparendo(objid, fecha,mediodeteccion,clasevehiculo, tiposervi, infraccion, desinfraccion, localidad, municipio ,longitud,latitud);
				datosArbol.put(agregar.getLlave(), agregar);
				heapgravedadcomparendos.agregar(agregar);
				g++;
			} 

			crearCuadrantes();
			//anadirComparendosalgrafo20();


			while((cadena1=b2.readLine())!=null) 
			{
				String[] arcos=cadena1.split(" ");
				if(arcos.length>1) 
				{
					Integer idprincipal=Integer.parseInt(arcos[0]);

					Double latitudinicial=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idprincipal)).darCabeza2().darSiguiente().darE();
					Double longitudinicial=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idprincipal)).darCabeza();

					for(int i=1;i<arcos.length;i++) 
					{
						// calcular haversine con el vertice siguiente y crear arco
						Integer idactual=Integer.parseInt(arcos[i]);
						Double latitudfinal=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idactual)).darCabeza2().darSiguiente().darE();
						Double longitudfinal=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idactual)).darCabeza();

						double distanciaconverticeactual = distance(latitudinicial, longitudinicial,latitudfinal,longitudfinal);

						double numerodecompa= ((ListaDoblementeEncadenada)grafo.getInfoVertex(idactual)).darLongitud() + ((ListaDoblementeEncadenada)grafo.getInfoVertex(idprincipal)).darLongitud()- 4;


						grafo.addEdge2(idprincipal, idactual, distanciaconverticeactual, numerodecompa);
					}

				}

			}
			b2.close();


		} 
		catch (IOException e) {

			e.printStackTrace();
		}

		File archivo= new File(dir);


		JsonReader reader1= new JsonReader( new InputStreamReader(new FileInputStream(archivo)));
		JsonObject gsonObj02= JsonParser.parseReader(reader1).getAsJsonObject();

		//(OBJECTID, EPODESCRIP, EPODIR_SITIO,EPOLATITUD, EPOLONGITU, EPOSERVICIO, EPOHORARIO, EPOTELEFON, EPOIULOCAL) 

		JsonArray estaciones=gsonObj02.get("features").getAsJsonArray();
		int i=0;
		while(i<estaciones.size())
		{
			JsonElement obj= estaciones.get(i);
			JsonObject gsonObj= obj.getAsJsonObject();

			JsonObject gsonObjpropiedades=gsonObj.get("properties").getAsJsonObject();


			int objid= gsonObjpropiedades.get("OBJECTID").getAsInt();
			String correo=gsonObjpropiedades.get("EPOCELECTR").getAsString();
			String direccion=gsonObjpropiedades.get("EPODIR_SITIO").getAsString();
			String descripcion=gsonObjpropiedades.get("EPODESCRIP").getAsString();

			Double longitud= gsonObjpropiedades.get("EPOLONGITU").getAsDouble();
			Double latitud= gsonObjpropiedades.get("EPOLATITUD").getAsDouble();

			String servicio= gsonObjpropiedades.get("EPOSERVICIO").getAsString();
			String horario= gsonObjpropiedades.get("EPOHORARIO").getAsString();
			String telefono= gsonObjpropiedades.get("EPOTELEFON").getAsString();
			String local= gsonObjpropiedades.get("EPOIULOCAL").getAsString();

			EstacionPolicia agregar=new EstacionPolicia(objid,longitud,latitud ,direccion, descripcion, correo,servicio,horario,telefono,local);
			this.estaciones.insertarFinal(agregar);
			i++;
		}

		//anadirPoliciasalgrafo();



		long fin2 = System.nanoTime();
		long fin = System.currentTimeMillis();

		System.out.println((fin2-inicio2)/1.0e9 +" segundos, de la carga de datos normal.");

		System.out.println("El total de comparendos cargados fue de: " + datosArbol.size());
		System.out.println("El comparendo con mayor Object ID encontrado fue: "+ datosArbol.max().toString());

		System.out.println("Cantidad de estaciones de policia es de: "+this.estaciones.darLongitud());
		System.out.println("La estaci�n de polic�a con mayor Object ID encontrado fue: "+this.estaciones.darUltimo().toString());

		System.out.println("Total de vertices: "+grafo.V());
		System.out.println("El vertice con mayor Object ID encontrado fue de :");

		System.out.println("Total de arcos: "+grafo.E());
		System.out.println("El arco con mayor Object ID encontrado fue de :");

		//escribirJson();
		//leerJson();

		//pruebaMaps();


	}


	public void escribirJson() 
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "Feature Collection");

		JSONArray vertices = new JSONArray();

		NodoHash22<Integer,ListaDoblementeEncadenada<Vertice>>[] verticesnodos= grafo.getNodos().getNodosSet();
		int num=0;
		System.out.println("tamano total: "+grafo.getNodos().getTamTotal());
		System.out.println("tamano actual: "+grafo.getNodos().getTamActual());
		for(int i=0;i<verticesnodos.length;i++) 

		{
			if(verticesnodos[i]!=null) {
				num++;
				Vertice actual=verticesnodos[i].darv().darCabeza(); 

				JSONObject verticeactual = new JSONObject();

				Double latitud=(Double)((ListaDoblementeEncadenada)actual.getValue()).darCabeza2().darSiguiente().darE();
				Double longitud=(Double)((ListaDoblementeEncadenada)actual.getValue()).darCabeza();

				verticeactual.put("IDINICIAL", actual.getKey());
				verticeactual.put("LONGITUD", longitud);
				verticeactual.put("LATITUD", latitud);

				JSONArray arcos = new JSONArray();
				ListaDoblementeEncadenada<Arco> arcoslista=actual.darListaArcos();

				Node<Arco> a= arcoslista.darCabeza2();

				while(a!=null) {

					JSONObject arcoactual = new JSONObject();
					arcoactual.put("IDFINAL", a.darE().getvFinal().getKey());
					arcoactual.put("COSTO1", a.darE().getCosto());
					arcoactual.put("COSTO2", a.darE().getCosto2());
					arcos.add(arcoactual);
					a=a.darSiguiente();
				}

				verticeactual.put("ARCOS", arcos);

				vertices.add(verticeactual);
			}
		}

		obj.put("features", vertices);


		try {

			FileWriter filesalida = new FileWriter("./data/grafopersistido.json");
			filesalida.write(obj.toJSONString());
			filesalida.flush();
			filesalida.close();

		} catch (IOException e) {
			//manejar error
		}
		System.out.println(num);
	}


	public void leerJson() 
	{
		grafojson= new Grafo();
		File archivo= new File("./data/grafopersistido.json");
		JsonReader reader=null;
		try {
			reader = new JsonReader( new InputStreamReader(new FileInputStream(archivo)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonObject gsonObj0= JsonParser.parseReader(reader).getAsJsonObject();

		JsonArray vertices=gsonObj0.get("features").getAsJsonArray();
		int i=0;
		System.out.println(vertices.size());
		while(i<vertices.size())
		{
			JsonElement obj= vertices.get(i);
			JsonObject verticeactual= obj.getAsJsonObject();
			Double longitud=verticeactual.get("LONGITUD").getAsDouble();
			Double latitud=verticeactual.get("LATITUD").getAsDouble();
			Integer idvertice=verticeactual.get("IDINICIAL").getAsInt();

			JsonArray arcos=verticeactual.get("ARCOS").getAsJsonArray();
			ListaDoblementeEncadenada posicion= new ListaDoblementeEncadenada();
			posicion.insertarFinal(longitud);
			posicion.insertarFinal(latitud);
			grafojson.addVertex(idvertice, posicion,latitud,longitud);
			++i;
		}


		int j=0;
		while(j<vertices.size())
		{
			JsonElement obj= vertices.get(j);
			JsonObject verticeactual= obj.getAsJsonObject();

			JsonArray arcos=verticeactual.get("ARCOS").getAsJsonArray();
			Integer idvertice=verticeactual.get("IDINICIAL").getAsInt();
			for(int x=0;x<arcos.size();x++) 
			{
				JsonObject arcoactual=arcos.get(x).getAsJsonObject();

				Integer idfinal=arcoactual.get("IDFINAL").getAsInt();
				Double costoactual=arcoactual.get("COSTO1").getAsDouble();
				Double costoactual2=arcoactual.get("COSTO2").getAsDouble();
				Arco nuevoarco= new Arco(grafojson.getVertex(idvertice),grafojson.getVertex(idfinal),costoactual,costoactual2);
				grafojson.addEdge(idvertice, idfinal, costoactual,costoactual2);

			}
			++j;
		}

		System.out.println("Numero de vertices: "+grafojson.V());
		System.out.println("Numero de arcos: "+grafojson.E());


	}


	public void parteA1(Double longitud1, Double latitud1, Double longitud2, Double latitud2) 
	{
		Vertice ver1=null;
		Vertice ver2=null;

		for(int x=0;x<2;x++) 
		{

			Double lat1=0.0;
			Double lon1=0.0;

			if(x==0) 
			{
				lat1=latitud1;
				lon1=longitud1;
			}
			else 
			{
				lat1=latitud2;
				lon1=longitud2;
			}

			Vertice menor= null;
			Double menordistancia=10000.0;

			Integer idcuadrante=cuadrantes.elSectorenelqueesta(lat1, lon1);


			if(idcuadrante==-1) 
			{
				
				System.out.println("Las coordenadas ingresadas no estan dentro de lo delimitado de la ciudad");
				return;
			}


			Node<Vertice> actual22= cuadrantes.getSector(idcuadrante).getVertices().darCabeza2();

			while(actual22!=null) 
			{
				Double lat2= (Double) ((ListaDoblementeEncadenada) actual22.darE().getValue()).darCabeza2().darSiguiente().darE();
				Double long2=(Double) ((ListaDoblementeEncadenada) actual22.darE().getValue()).darCabeza();

				Double distancia=distance(lat1, lon1,lat2,long2);

				if(distancia<menordistancia) 
				{
					menordistancia=distancia;
					menor=actual22.darE();
				}

				actual22=actual22.darSiguiente();


			}


			if(menor!=null && x==0) {
				ver1=menor;

			}
			else if (menor!=null && x==1) 
			{
				ver2=menor;
			}
			else 
			{
				return;
			}


		}
		
		//System.out.println(ver1.toString() +"     "+ver2.toString());
		CC compnuevo= new CC(grafo);
	   // System.out.println(compnuevo.connected((Integer)ver1.getKey(), (Integer)ver2.getKey()));
	    
	    DijkstraUndirectedSP grafomascorto2= new DijkstraUndirectedSP(grafo,(Integer) ver1.getKey());
	    
		Integer numver= compnuevo.id((Integer)ver1.getKey());
		Integer numver2= compnuevo.id((Integer)ver2.getKey());
		int[] juas=compnuevo.getId();
		boolean encon= false;
		for(int i=0; i<juas.length && (!encon); i++) 
		{
			if(numver==juas[i]) 
			{
				//System.out.println(grafo.getVertex(i).toString());
				encon=true;
			}
			
		}
		
		encon= false;
		for(int i=0; i<juas.length && (!encon); i++) 
		{
			if(numver2==juas[i]) 
			{
				//System.out.println(grafo.getVertex(i).toString());
				encon=true;
			}
			
		}
		
	 
	 if(grafomascorto2.hasPathTo((Integer)ver2.getKey())) 
	  {
		
		
	 final String body1="<!DOCTYPE html>\n" + 
				"		<html>\n" + 
				"		  <head>\n" + 
				"		    <title>Simple Map</title>\n" + 
				"		    <meta name=\"viewport\" content=\"initial-scale=1.0\">\n" + 
				"		    <meta charset=\"utf-8\">\n" + 
				"		    <style>\n" + 
				"		     \n" + 
				"		      #map {\n" + 
				"		        height: 100%;\n" + 
				"		      }\n" + 
				"		      \n" + 
				"		      html, body {\n" + 
				"		        height: 100%;\n" + 
				"		        margin: 0;\n" + 
				"		        padding: 0;\n" + 
				"		      }\n" + 
				"		    </style>\n" + 
				"		  </head>\n" + 
				"		  <body>" +
				"<div id=\"map\"></div>\n" + 
				"    <script>";

		final String body2="</script>\n" + 
				"    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyBU3B0Mqn6Ez18yIhVuLYt397FGKCKOBPw&callback=initMap\"\n" + 
				"    async defer></script>\n" + 
				"  </body>\n" + 
				"</html>"
				;


		String[] coordenadas= {"4.609537", "-74.078715"};
		File file = new File("./data/punto1A.html");

		try {
			PrintWriter writer= new PrintWriter(file);

			writer.println(body1);
			writer.println("var loc={lat:"+coordenadas[0]+", lng:"+coordenadas[1]+"}; \n");
			writer.println("var map;");
			writer.println("var pos1 = { lat:"+ver1.getLatitud()+", lng:"+ver1.getLongitud()+"}; ");
			writer.println("var pos2 = { lat:"+ver2.getLatitud()+", lng:"+ver2.getLongitud()+"}; ");

			String vertices="var vertices= { \n";

			String lineas="var lineas = { \n";

			ListaDoblementeEncadenada<Arco> arcos = grafomascorto2.pathTo((Integer)ver2.getKey());

			Node<Arco> actual= arcos.darCabeza2();
			int k=0;

			Vertice vertice1coor=null;
			Vertice vertice2coor=null;

			Double lat1=0.0;
			Double lat2=0.0;
			Double long1=0.0;
			Double long2=0.0;
			
			Double num=0.0;
			long inicio2 = System.nanoTime();
			while(actual!=null) 
			{
					num+=(Double)actual.darE().getCosto();

				vertice1coor=actual.darE().getvInicio();
				vertice2coor=actual.darE().getvFinal();

				lat1=vertice1coor.getLatitud();
				lat2=vertice2coor.getLatitud();
				long1=vertice1coor.getLongitud();
				long2=vertice2coor.getLongitud();
				System.out.println(vertice1coor.toString());
				System.out.println(vertice2coor.toString());
					
					if(k!=0) {
						vertices+=",\n";
						lineas+=",\n";
					}
					vertices+= "vertia"+k+": {\r\n" + 
							"    center: {lat: "+lat1+", lng: "+long1+"}\r\n" + 

							"  },\n";

					vertices+= "vertib"+k+": {\r\n" + 
							"    center: {lat: "+lat2+", lng: "+long2+"}\r\n" + 

							"  }";



					lineas+="linea"+k+": {\r\n" + 
							"    lat1: "+lat1+",\r\n" + 
							"    lat2: "+lat2+",\r\n" + 
							"    long1: "+long1+",\r\n" + 
							"    long2: "+long2+"\r\n" + 
							"  }\n";


					k++;

				actual=actual.darSiguiente();
			}
			//System.out.println(k);
			long fin2 = System.nanoTime();
			System.out.println((fin2-inicio2)/1.0e9 +" segundos, de la carga de datos normal.");
			
			
			vertices+="};\n";
			lineas+="};\n";
			System.out.println("El costo total de este MST es: "+num); 
			
			writer.println(vertices);
			writer.println(lineas);

			writer.println("function initMap() {\n" + 
					"        map = new google.maps.Map(document.getElementById('map'), {\n" + 
					"          center: pos1,\n" + 
					"          zoom: 15\n" + 
					"        });\n");

			long inicio3 = System.nanoTime();	
			
			writer.println("for (var vertice in vertices) {\r\n" + 
					"    var vert = new google.maps.Circle({\r\n" + 
					"      strokeColor: '#000000',\r\n" + 
					"      strokeOpacity: 0.8,\r\n" + 
					"      strokeWeight: 2,\r\n" + 
					"      fillColor: '#000000',\r\n" + 
					"      fillOpacity: 0.35,\r\n" + 
					"      map: map,\r\n" + 
					"      center: vertices[vertice].center,\r\n" + 
					"      radius: 3\r\n" + 
					"    });\r\n" + 
					"  }\n");
			
			writer.println("var marker = new google.maps.Marker({\r\n" + 
					"    position: pos1 ,\r\n" + 
					"    map: map,\r\n" + 
					"    title: 'Vertice 1'\r\n" + 
					"  });");
			
			writer.println("var marker = new google.maps.Marker({\r\n" + 
					"    position: pos2 ,\r\n" + 
					"    map: map,\r\n" + 
					"    title: 'Vertice 2'\r\n" + 
					"  });");

			writer.println("for (var arc in lineas) {\r\n" + 
					"var li = [\r\n" + 
					"{lat: lineas[arc].lat1 , lng: lineas[arc].long1},\r\n" + 
					"{lat: lineas[arc].lat2 , lng: lineas[arc].long2}\r\n" + 
					"];\r\n" + 
					"var flightPath = new google.maps.Polyline({\r\n" + 
					"    path: li,\r\n" + 
					"    geodesic: true,\r\n" + 
					"    strokeColor: '#000000',\r\n" + 
					"    strokeOpacity: 1.0,\r\n" + 
					"    strokeWeight: 2\r\n" + 
					"  });  "
					+ "flightPath.setMap(map);"
					+ "}\r\n" + 
					"  }"
					);
		
			long fin3 = System.nanoTime();
			System.out.println((fin3-inicio3)/1.0e9 +" segundos, de la creaci�n del mapa.");
			writer.println(body2);
			writer.close();

			File f= new File("data/punto1A.html");

			try {
				java.awt.Desktop.getDesktop().browse(f.toURI());
			}
			catch (IOException e) {

				e.printStackTrace();
			}



		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
	  }
	 
	 else 
	 {
		 System.out.println("Los vertices no se encuentran conectados");
	 }

	}

	public void parteA2(Integer m) 
	{
		ListaDoblementeEncadenada<Integer> verticesss=new ListaDoblementeEncadenada<Integer>();
		
		for(int i=0;i<m;i++) 
		{
			Comparendo com=heapgravedadcomparendos.eliminarMayor();
			Double lat1=com.getLatitud();
			Double lon1=com.getLongitud();
			Vertice menor= null;
			Double menordistancia=10000.0;
			Integer idcuadrante=cuadrantes.elSectorenelqueesta(lat1, lon1);
			Node<Vertice> actual22= cuadrantes.getSector(idcuadrante).getVertices().darCabeza2();
			while(actual22!=null) 
			{
				Double lat2= (Double) ((ListaDoblementeEncadenada) actual22.darE().getValue()).darCabeza2().darSiguiente().darE();
				Double long2=(Double) ((ListaDoblementeEncadenada) actual22.darE().getValue()).darCabeza();

				Double distancia=distance(lat1, lon1,lat2,long2);

				if(distancia<menordistancia) 
				{
					menordistancia=distancia;
					menor=actual22.darE();
				}

				actual22=actual22.darSiguiente();
			}
			
			if(menor!=null)
			{
				verticesss.insertarFinal((Integer)menor.getKey());
			}
			
		}
		Grafo finalgrafo= new Grafo();
		Node<Integer> actualverti= verticesss.darCabeza2();
		while(actualverti!=null) 
		{
			Vertice v=grafo.getVertex(actualverti.darE());
			
			// FALTA MEJORAR ESTO 
			Node<Arco> n=v.darListaArcos().darCabeza2();
			while(n!=null) 
			{
				n.darE().setCosto((Double)n.darE().getCosto()*10000);
				finalgrafo.addEdge(n.darE());
				n=n.darSiguiente();
			}
			
			actualverti=actualverti.darSiguiente();
		}
		System.out.println(finalgrafo.V());
		System.out.println(finalgrafo.E());
		LazyPrimMST mstfinal= new LazyPrimMST(finalgrafo);
		
		
		final String body1="<!DOCTYPE html>\n" + 
				"		<html>\n" + 
				"		  <head>\n" + 
				"		    <title>Simple Map</title>\n" + 
				"		    <meta name=\"viewport\" content=\"initial-scale=1.0\">\n" + 
				"		    <meta charset=\"utf-8\">\n" + 
				"		    <style>\n" + 
				"		     \n" + 
				"		      #map {\n" + 
				"		        height: 100%;\n" + 
				"		      }\n" + 
				"		      \n" + 
				"		      html, body {\n" + 
				"		        height: 100%;\n" + 
				"		        margin: 0;\n" + 
				"		        padding: 0;\n" + 
				"		      }\n" + 
				"		    </style>\n" + 
				"		  </head>\n" + 
				"		  <body>" +
				"<div id=\"map\"></div>\n" + 
				"    <script>";

		final String body2="</script>\n" + 
				"    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyBU3B0Mqn6Ez18yIhVuLYt397FGKCKOBPw&callback=initMap\"\n" + 
				"    async defer></script>\n" + 
				"  </body>\n" + 
				"</html>"
				;


		String[] coordenadas= {"4.609537", "-74.078715"};
		File file = new File("./data/punto2A.html");

		try {
			PrintWriter writer= new PrintWriter(file);

			writer.println(body1);
			writer.println("var loc={lat:"+coordenadas[0]+", lng:"+coordenadas[1]+"}; \n");
			writer.println("var map;");
			String vertices="var vertices= { \n";

			String lineas="var lineas = { \n";

			ListaDoblementeEncadenada<Arco> arcos = mstfinal.edges2();
			System.out.println(arcos.darLongitud());
			Node<Arco> actual= arcos.darCabeza2();
			int k=0;

			Vertice vertice1coor=null;
			Vertice vertice2coor=null;

			Double lat1=0.0;
			Double lat2=0.0;
			Double long1=0.0;
			Double long2=0.0;
			
			Double num=0.0;
			long inicio2 = System.nanoTime();
			while(actual!=null) 
			{
					num+=(Double)actual.darE().getCosto();

				vertice1coor=actual.darE().getvInicio();
				vertice2coor=actual.darE().getvFinal();

				lat1=vertice1coor.getLatitud();
				lat2=vertice2coor.getLatitud();
				long1=vertice1coor.getLongitud();
				long2=vertice2coor.getLongitud();
				//System.out.println(vertice1coor.toString());
				//System.out.println(vertice2coor.toString());
					
					if(k!=0) {
						vertices+=",\n";
						lineas+=",\n";
					}
					vertices+= "vertia"+k+": {\r\n" + 
							"    center: {lat: "+lat1+", lng: "+long1+"}\r\n" + 

							"  },\n";

					vertices+= "vertib"+k+": {\r\n" + 
							"    center: {lat: "+lat2+", lng: "+long2+"}\r\n" + 

							"  }";



					lineas+="linea"+k+": {\r\n" + 
							"    lat1: "+lat1+",\r\n" + 
							"    lat2: "+lat2+",\r\n" + 
							"    long1: "+long1+",\r\n" + 
							"    long2: "+long2+"\r\n" + 
							"  }\n";


					k++;

				actual=actual.darSiguiente();
			}
			//System.out.println(k);
			long fin2 = System.nanoTime();
			System.out.println((fin2-inicio2)/1.0e9 +" segundos, de la carga de datos normal.");
			
			
			vertices+="};\n";
			lineas+="};\n";
			System.out.println("El costo total de este MST es: "+num); 
			
			writer.println(vertices);
			writer.println(lineas);

			writer.println("function initMap() {\n" + 
					"        map = new google.maps.Map(document.getElementById('map'), {\n" + 
					"          center: loc,\n" + 
					"          zoom: 15\n" + 
					"        });\n");

			long inicio3 = System.nanoTime();	
			
			writer.println("for (var vertice in vertices) {\r\n" + 
					"    var vert = new google.maps.Circle({\r\n" + 
					"      strokeColor: '#000000',\r\n" + 
					"      strokeOpacity: 0.8,\r\n" + 
					"      strokeWeight: 2,\r\n" + 
					"      fillColor: '#000000',\r\n" + 
					"      fillOpacity: 0.35,\r\n" + 
					"      map: map,\r\n" + 
					"      center: vertices[vertice].center,\r\n" + 
					"      radius: 3\r\n" + 
					"    });\r\n" + 
					"  }\n");
			
			writer.println("var marker = new google.maps.Marker({\r\n" + 
					"    position: pos1 ,\r\n" + 
					"    map: map,\r\n" + 
					"    title: 'Vertice 1'\r\n" + 
					"  });");
			
			writer.println("var marker = new google.maps.Marker({\r\n" + 
					"    position: pos2 ,\r\n" + 
					"    map: map,\r\n" + 
					"    title: 'Vertice 2'\r\n" + 
					"  });");

			writer.println("for (var arc in lineas) {\r\n" + 
					"var li = [\r\n" + 
					"{lat: lineas[arc].lat1 , lng: lineas[arc].long1},\r\n" + 
					"{lat: lineas[arc].lat2 , lng: lineas[arc].long2}\r\n" + 
					"];\r\n" + 
					"var flightPath = new google.maps.Polyline({\r\n" + 
					"    path: li,\r\n" + 
					"    geodesic: true,\r\n" + 
					"    strokeColor: '#000000',\r\n" + 
					"    strokeOpacity: 1.0,\r\n" + 
					"    strokeWeight: 2\r\n" + 
					"  });  "
					+ "flightPath.setMap(map);"
					+ "}\r\n" + 
					"  }"
					);
		
			long fin3 = System.nanoTime();
			System.out.println((fin3-inicio3)/1.0e9 +" segundos, de la creaci�n del mapa.");
			writer.println(body2);
			writer.close();

			File f= new File("data/punto2A.html");

			try {
				java.awt.Desktop.getDesktop().browse(f.toURI());
			}
			catch (IOException e) 
			{

				e.printStackTrace();
			}
		}
		
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	
		
	}
	
	public void dibujarTodin() 
	{
		JFrame frame= new JFrame("Grafito");

		class MapExample extends MapView {

			public MapExample() {

				// Setting of a ready handler to MapView object. onMapReady will be called when map initialization is done and
				// the map object is ready to use. Current implementation of onMapReady customizes the map object.
				setOnMapReadyHandler(new MapReadyHandler() {
					@Override
					public void onMapReady(MapStatus status) {
						if (status == MapStatus.MAP_STATUS_OK) {

							final Map map = getMap();

							MapOptions options = new MapOptions();

							MapTypeControlOptions controlOptions = new MapTypeControlOptions();

							options.setMapTypeControlOptions(controlOptions);

							map.setOptions(options);

							map.setCenter(new LatLng(4.609537, -74.078715));

							map.setZoom(15.0);

							ListaDoblementeEncadenada<Arco> arcos=grafojson.getList();


							Node<EstacionPolicia> actualpol= estaciones.darCabeza2();
							while(actualpol!=null) 
							{
								Double longitud=actualpol.darE().getLongitud();
								Double latitud=actualpol.darE().getLatitud();

								if(longitud<=LONGMAX && longitud>=LONGMIN && latitud<=LATMAX && latitud>=LATMIN) 
								{
									LatLng poli=new LatLng(latitud,longitud);

									Circle pol=new Circle(map);
									pol.setCenter(poli);
									pol.setRadius(20);
									CircleOptions co= new CircleOptions();
									co.setFillColor("#CB3234");
									pol.setOptions(co);
									pol.setVisible(true);

								}
								actualpol=actualpol.darSiguiente();
							}

							Node<Arco> actual= arcos.darCabeza2();
							while(actual!=null) 
							{
								ListaDoblementeEncadenada<Double> vertice1coor=(ListaDoblementeEncadenada<Double>)actual.darE().getvInicio().getValue();
								ListaDoblementeEncadenada<Double> vertice2coor=(ListaDoblementeEncadenada<Double>)actual.darE().getvFinal().getValue();

								if(vertice1coor.darCabeza()<=LONGMAX && vertice1coor.darCabeza()>=LONGMIN && vertice2coor.darCabeza()<=LONGMAX && vertice2coor.darCabeza()>=LONGMIN && (Double)vertice1coor.darCabeza2().darSiguiente().darE()<=LATMAX && (Double)vertice1coor.darCabeza2().darSiguiente().darE()>=LATMIN && (Double)vertice2coor.darCabeza2().darSiguiente().darE()<=LATMAX && (Double)vertice2coor.darCabeza2().darSiguiente().darE()>=LATMIN) 
								{
									LatLng ver1=new LatLng((double) vertice1coor.darCabeza2().darSiguiente().darE(),vertice1coor.darCabeza());
									LatLng ver2=new LatLng((double) vertice2coor.darCabeza2().darSiguiente().darE(),vertice2coor.darCabeza());

									Circle ver11=new Circle(map);
									ver11.setCenter(ver1);
									ver11.setRadius(1);
									CircleOptions co= new CircleOptions();
									co.setFillColor("#008F39");
									ver11.setOptions(co);
									ver11.setVisible(true);

									Circle ver22=new Circle(map);
									ver22.setCenter(ver2);
									ver22.setRadius(1);
									CircleOptions co1= new CircleOptions();
									co1.setFillColor("#008F39");
									ver22.setOptions(co1);
									ver22.setVisible(true);

									LatLng[] camino= new LatLng[2];
									camino[0]=ver1;
									camino[1]=ver2;
									Polygon linea= new Polygon(map);
									linea.setPath(camino);
									linea.setVisible(true);
								}

								actual=actual.darSiguiente();
							}	





						}
					}
				});

			}

		}

		MapExample map1 = new MapExample();
		frame.add( map1,BorderLayout.CENTER);
		frame.setSize(700, 500);
		frame.setVisible(true);

	}


	public void pruebaMaps() 
	{



		final String body1="<!DOCTYPE html>\n" + 
				"		<html>\n" + 
				"		  <head>\n" + 
				"		    <title>Simple Map</title>\n" + 
				"		    <meta name=\"viewport\" content=\"initial-scale=1.0\">\n" + 
				"		    <meta charset=\"utf-8\">\n" + 
				"		    <style>\n" + 
				"		     \n" + 
				"		      #map {\n" + 
				"		        height: 100%;\n" + 
				"		      }\n" + 
				"		      \n" + 
				"		      html, body {\n" + 
				"		        height: 100%;\n" + 
				"		        margin: 0;\n" + 
				"		        padding: 0;\n" + 
				"		      }\n" + 
				"		    </style>\n" + 
				"		  </head>\n" + 
				"		  <body>" +
				"<div id=\"map\"></div>\n" + 
				"    <script>";

		final String body2="</script>\n" + 
				"    <script src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyBU3B0Mqn6Ez18yIhVuLYt397FGKCKOBPw&callback=initMap\"\n" + 
				"    async defer></script>\n" + 
				"  </body>\n" + 
				"</html>"
				;


		String[] coordenadas= {"4.609537", "-74.078715"};
		File file = new File("./data/prueba.html");

		try {
			PrintWriter writer= new PrintWriter(file);

			writer.println(body1);
			writer.println("var loc={lat:"+coordenadas[0]+", lng:"+coordenadas[1]+"}; \n");
			writer.println("var map;");

			String vertices="var vertices= { \n";

			String lineas="var lineas = { \n";

			ListaDoblementeEncadenada<Arco> arcos=grafojson.getList();

			Node<Arco> actual= arcos.darCabeza2();
			int k=0;

			ListaDoblementeEncadenada<Double> vertice1coor=null;
			ListaDoblementeEncadenada<Double> vertice2coor=null;

			while(actual!=null) 
			{


				vertice1coor=(ListaDoblementeEncadenada<Double>)actual.darE().getvInicio().getValue();
				vertice2coor=(ListaDoblementeEncadenada<Double>)actual.darE().getvFinal().getValue();

				Double lat1=(Double)vertice1coor.darCabeza2().darSiguiente().darE();
				Double lat2=(Double)vertice2coor.darCabeza2().darSiguiente().darE();
				Double long1=vertice1coor.darCabeza();
				Double long2=vertice2coor.darCabeza();



				if(long1<=LONGMAX && long1>=LONGMIN && long2<=LONGMAX && long2>=LONGMIN && lat1<=LATMAX && lat1>=LATMIN && lat2<=LATMAX && lat2>=LATMIN) 
				{	

					if(k!=0) {
						vertices+=",\n";
						lineas+=",\n";
					}
					vertices+= "vertia"+k+": {\r\n" + 
							"    center: {lat: "+lat1+", lng: "+long1+"}\r\n" + 

							"  },\n";

					vertices+= "vertib"+k+": {\r\n" + 
							"    center: {lat: "+lat2+", lng: "+long2+"}\r\n" + 

							"  }";



					lineas+="linea"+k+": {\r\n" + 
							"    lat1: "+lat1+",\r\n" + 
							"    lat2: "+lat2+",\r\n" + 
							"    long1: "+long1+",\r\n" + 
							"    long2: "+long2+"\r\n" + 
							"  }\n";


					k++;

				}

				actual=actual.darSiguiente();
			}


			vertices+="};\n";
			lineas+="};\n";

			writer.println(vertices);
			writer.println(lineas);

			writer.println("function initMap() {\n" + 
					"        map = new google.maps.Map(document.getElementById('map'), {\n" + 
					"          center: loc,\n" + 
					"          zoom: 15\n" + 
					"        });\n");

			writer.println("for (var vertice in vertices) {\r\n" + 
					"    var vert = new google.maps.Circle({\r\n" + 
					"      strokeColor: '#FF0000',\r\n" + 
					"      strokeOpacity: 0.8,\r\n" + 
					"      strokeWeight: 2,\r\n" + 
					"      fillColor: '#FF0000',\r\n" + 
					"      fillOpacity: 0.35,\r\n" + 
					"      map: map,\r\n" + 
					"      center: vertices[vertice].center,\r\n" + 
					"      radius: 3\r\n" + 
					"    });\r\n" + 
					"  }\n");


			writer.println("for (var arc in lineas) {\r\n" + 
					"var li = [\r\n" + 
					"{lat: lineas[arc].lat1 , lng: lineas[arc].long1},\r\n" + 
					"{lat: lineas[arc].lat2 , lng: lineas[arc].long2}\r\n" + 
					"];\r\n" + 
					"var flightPath = new google.maps.Polyline({\r\n" + 
					"    path: li,\r\n" + 
					"    geodesic: true,\r\n" + 
					"    strokeColor: '#FF0000',\r\n" + 
					"    strokeOpacity: 1.0,\r\n" + 
					"    strokeWeight: 2\r\n" + 
					"  });  "
					+ "flightPath.setMap(map);"
					+ "}\r\n" + 
					"  }"
					);


			writer.println(body2);
			writer.close();

			File f= new File("data/prueba.html");

			try {
				java.awt.Desktop.getDesktop().browse(f.toURI());
			}
			catch (IOException e) {

				e.printStackTrace();
			}



		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}





	}


	public void anadirPoliciasalgrafo() 
	{
		HashSeparateChaining vertices= grafo.getNodos();

		Node<EstacionPolicia> actual= estaciones.darCabeza2();

		while(actual!=null) 
		{



			Double lat1=actual.darE().getLatitud();
			Double lon1=actual.darE().getLongitud();

			Vertice menor= null;
			Double menordistancia=10000.0;

			int j=0;
			while(j<vertices.getTamActual()) 
			{
				ListaDoblementeEncadenada lis= (ListaDoblementeEncadenada)grafo.getInfoVertex(j);
				Double lat2= (Double) lis.darCabeza2().darSiguiente().darE();
				Double long2=(Double) lis.darCabeza();
				Double distancia=distance(lat1, lon1,lat2,long2);

				if(distancia<menordistancia) 
				{
					menordistancia=distancia;
					menor=grafo.getVertex(j);
				}

				j++;
			}

			((ListaDoblementeEncadenada)menor.getValue()).insertarFinal(actual);
			actual = actual.darSiguiente();
		}



	}

	public void anadirComparendosalgrafo() 
	{
		HashSeparateChaining vertices= grafo.getNodos();

		Iterable<KeyComparendo> resultado= datosArbol.keys(datosArbol.min(), datosArbol.max());

		Iterator<KeyComparendo> iterator= resultado.iterator();
		int e=0;
		while(iterator.hasNext()&&e<2000) 
		{

			KeyComparendo llave= (KeyComparendo) iterator.next();
			Comparendo com=(Comparendo)datosArbol.get(llave);

			Double lat1=com.getLatitud();
			Double lon1=com.getLongitud();

			Vertice menor= null;
			Double menordistancia=10000.0;

			int j=0;
			while(j<vertices.getTamActual()) 
			{
				ListaDoblementeEncadenada lis= (ListaDoblementeEncadenada)grafo.getInfoVertex(j);
				Double lat2= (Double) lis.darCabeza2().darSiguiente().darE();
				Double long2=(Double) lis.darCabeza();
				Double distancia=distance(lat1, lon1,lat2,long2);

				if(distancia<menordistancia) 
				{
					menordistancia=distancia;
					menor=grafo.getVertex(j);
				}

				j++;
			}

			((ListaDoblementeEncadenada)menor.getValue()).insertarFinal(com);
			e++;
		}



	}

	public void anadirComparendosalgrafo20() 
	{
		HashSeparateChaining vertices= grafo.getNodos();

		Iterable<KeyComparendo> resultado= datosArbol.keys(datosArbol.min(), datosArbol.max());
		Iterator<KeyComparendo> iterator= resultado.iterator();

		int e=0;
		while(iterator.hasNext()) 
		{

			KeyComparendo llave= (KeyComparendo) iterator.next();
			Comparendo com=(Comparendo)datosArbol.get(llave);

			Double lat1=com.getLatitud();
			Double lon1=com.getLongitud();

			Vertice menor= null;
			Double menordistancia=10000.0;

			Integer idcuadrante=cuadrantes.elSectorenelqueesta(lat1, lon1);





			Node<Vertice> actual22= cuadrantes.getSector(idcuadrante).getVertices().darCabeza2();

			while(actual22!=null) 
			{
				Double lat2= (Double) ((ListaDoblementeEncadenada) actual22.darE().getValue()).darCabeza2().darSiguiente().darE();
				Double long2=(Double) ((ListaDoblementeEncadenada) actual22.darE().getValue()).darCabeza();

				Double distancia=distance(lat1, lon1,lat2,long2);

				if(distancia<menordistancia) 
				{
					menordistancia=distancia;
					menor=actual22.darE();
				}

				actual22=actual22.darSiguiente();
			}


			if(menor!=null) {
				((ListaDoblementeEncadenada)menor.getValue()).insertarFinal(com);
				grafo.setInfoVertex(menor.getKey(), menor.getValue());
				e++;
			}

		}
		System.out.println("numero de compa: "+e);
	}

	public void crearCuadrantes() {

		Double n=33.0;

		Double intervaloslat=(latmax-latmenor)/n;

		Double intervaloslong=(longmax-longmenor)/n;
		System.out.println("mayor"+longmax + "  menor:"+ longmenor);

		cuadrantes= new Sectores(latmax,latmenor,longmax,longmenor,intervaloslong,intervaloslat,n);

		//k=x , l=y
		int cont=0;
		for(int k=0; k<n;k++) 
		{
			Double lataminctual=latmenor+(k*intervaloslat);
			Double latamaxactual=latmenor+((k+1)*intervaloslat);

			for(int l=0; l<n;l++) 
			{
				Double longminactual=longmenor+(l*intervaloslong);
				Double longmaxactual=longmenor+((l+1)*intervaloslong);
				Sector nuevo=new Sector(latamaxactual,lataminctual,longmaxactual,longminactual);
				cuadrantes.agregarSector(cont, nuevo);
				cont++;
			}
		}


		HashSeparateChaining vertices= grafo.getNodos();
		int j=0;
		while(j<vertices.getTamActual()) 
		{

			Vertice actual= (Vertice)vertices.getSet(j).darCabeza();
			ListaDoblementeEncadenada lis= (ListaDoblementeEncadenada) actual.getValue();
			Integer idcuadrante=cuadrantes.elSectorenelqueesta((Double)lis.darCabeza2().darSiguiente().darE(), (Double)lis.darCabeza());

			if(idcuadrante!=-1) {
				cuadrantes.getSector(idcuadrante).agregarvertice(actual);
			}
			j++;
		} 
	}

	public int encontrarVertice (double lati, double longi) {

		Iterator it = grafo.getNodos().keysSet();
		double menorDistancia=-1;
		int idMasCercano=-1;
		while(it.hasNext()) {
			int actual=(int)it.next();
			Double struct=(Double)((ListaDoblementeEncadenada) grafo.getInfoVertex(actual)).darCabeza();

			Double struct2=(Double)((ListaDoblementeEncadenada) grafo.getInfoVertex(actual)).darUltimo();
			Double calculo=distance(lati,longi,struct2,struct);

			if(menorDistancia<=-1) {
				menorDistancia=calculo;
				idMasCercano=actual;
			}

			else if(calculo<=menorDistancia) {
				menorDistancia=calculo;
				idMasCercano=actual;
			}

		}
		return idMasCercano;

	}

	public static double distance(double startLat, double startLong,
			double endLat, double endLong) {

		double dLat  = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat   = Math.toRadians(endLat);

		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c; // <-- d
	}

	public static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

	private boolean less(Comparable v,Comparable w)
	{
		return v.compareTo(w) < 0;
	}
	
	public int componentesConectados() 
	{
		return grafo.cc();
	}

	private void exch(Comparable[] datos,int i, int j)
	{
		Comparable t=datos[i];
		datos[i]=datos[j];
		datos[j]=t;
	}








}

