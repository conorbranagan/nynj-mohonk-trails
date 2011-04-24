package edu.newpaltz.nynjmohonk;

/**
 * Minimal polygon class used soley for the algorithm that tells if a point is within
 * the current polygon instance
 * Credit to http://www.anddev.org/using_javaawtpolygon_in_android-t6521.html
 *
 */
public class Polygon {
	private int[] polyY, polyX;
	private int polySides;
	
	/**
	 * Constructs a new Polygon object
	 * @param px X coordinates of the polygon
	 * @param py Y coordinates of the polygon
	 * @param ps The count of polygon sides
	 */
	public Polygon(int[] px, int[] py, int ps) {
		polyX = px;
		polyY = py;
		polySides = ps;
	}
	
	/**
	 * Checks if the polygon contains a point
	 * @param x The x value of the point
	 * @param y The y value of the point
	 * @return True if the point is within the polygon and false otherwise
	 */
	public boolean contains(int x, int y) {
		boolean oddTransitions = false;
		for(int i = 0, j = polySides - 1; i < polySides; i++) {
			if( ( polyY[ i ] < y && polyY[ j ] >= y ) || ( polyY[ j ] < y && polyY[ i ] >= y ) ) {
				if( polyX[ i ] + ( y - polyY[ i ] ) / ( polyY[ j ] - polyY[ i ] ) * ( polyX[ j ] - polyX[ i ] ) < x ) {
					oddTransitions = !oddTransitions;
				}
			}
		}
		return oddTransitions;
	}
}
