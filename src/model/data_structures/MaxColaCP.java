package model.data_structures;

public class MaxColaCP <T extends Comparable<T>>{

	/**
	 * Cantidad de elementos de la lista
	 */
	private int longitud;
	
	/**
	 * Referencia del primer elemento de la lista
	 */
	private Node<T> cabeza = null;
	
	
	/**
	 * Referencia del ultimo elemento de la lista
	 */
	private Node<T> ultimo=null;
	
	
	/**
	 * Metodo Constructro Lista Encadenada
	 */
	public MaxColaCP()
	{
		longitud=0;
		cabeza=null;
		ultimo=null;
	}
	
	/**
	 * Da el elemento de la clase generica almacenada en la lista.
	 * @return Elemento que esta primero en la Lista
	 */
	
	public T darMax()
	{
		return cabeza.darE();
	}
	
	public Node darMax2()
	{
		return cabeza;
	}
	
	/**
	 * Retorna el tama�o de la longitud.
	 * @return longitud lista
	 */
	
	public int darNumElementos()
	{
		return longitud;
	}
	
	/**
	 * Dice si la lista esta vacia o no.
	 * @return true si esta vacia, false de lo contrario
	 */
	
	public boolean esVacia()
	{
		return cabeza==null;
	}
	
	/**
	 * Inserta un nuevo elemento gen�rico al final de la lista.
	 * @param t2. Elemento nuevo a agregar.
	 */
	
	public void agregar(T t2)
	{
		Node<T> node= new Node<T>(t2);
		if(esVacia())
		{
			cabeza = node;
			ultimo=cabeza;
		}
		
		else if(t2.compareTo(cabeza.darE())>0)
		{
			node.cambiarSiguiente(cabeza);
			cabeza.cambiarAnterior(node);
			cabeza=node;
		}
		
		else
		{	
			Node<T> puntero= cabeza;
			boolean agregado=false;
			while(puntero!=null && !agregado)
			{
				Node<T> siguiente= puntero.darSiguiente();
				if(siguiente!=null)
				{
					if(puntero.darE().compareTo(t2) >= 0 && siguiente.darE().compareTo(t2) <= 0)
					{
						puntero.cambiarSiguiente(node);
						node.cambiarAnterior(puntero);
						node.cambiarSiguiente(siguiente);
						siguiente.cambiarAnterior(node);
						agregado=true;
					}
					
				}
				else if(siguiente==null)
				{
					ultimo.cambiarSiguiente(node);
					node.cambiarAnterior(ultimo);
					ultimo=node;
					agregado=true;
				}
				
				puntero=puntero.darSiguiente();
			}
			
		}
		longitud++;
		
	}
	
	/**
	 * Elimina el primer elemento de la lista.
	 */
	
	public T sacarMax()
	{
		if(cabeza!=null)
		{
			Node<T> primer= cabeza;
			T elem= cabeza.darE();
			cabeza= cabeza.darSiguiente();
			primer.cambiarSiguiente(null);
			longitud--;
			return elem;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Retorna un objeto de la lista , dado su posici�n.
	 * @param n. Posici�n en la lista.
	 * @return Elemento de clase g�nerica.
	 */
	
	public T darObjeto(int pos)
	{
		if(cabeza==null)
		{
			return null;
		}
		else
		{
			Node<T> puntero= cabeza;
			int cont=0;
			while(cont < pos && puntero.darSiguiente()!=null)
			{
				puntero = puntero.darSiguiente();
				cont++;
			}
			if(cont != pos)
			{
				return null;
			}
			else
			{
				return (T) puntero.darE();
				
			}
		
		}
		
	}
	
	
	public T sacarUltimo() 
	{
		if(!esVacia()) 
		{
			
				T ult=null;
				if(cabeza==ultimo)
				{
					ult=cabeza.darE();
					cabeza = null;
				}
				else
				{
				ult= ultimo.darE();
				Node<T> nuevoultimo=ultimo.darAnterior();
				nuevoultimo.cambiarSiguiente(null);
				ultimo=nuevoultimo;
				
				}
				longitud--;
				
				return ult;
		
		}
		else 
		{
			return null;
		}
	}
	
	/**
	 * Retorna el ultimo elemento g�nerico de la lista
	 * @return Elemento de clase g�nerica.
	 */
	
	public T darUltimo()
	{
		return ultimo.darE();
	}
	
	
}
