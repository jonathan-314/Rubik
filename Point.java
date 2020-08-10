package Rubik;

/**
 * represents a point/vector in 3d space
 * 
 * @author jonguo6
 */
class Point {
	double x, y, z;
	// used for coordinates after linear transformation
	double ax, ay, az;

	/**
	 * Point constructor with 3 doubles
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 */
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Point constructor with an array
	 * 
	 * @param coordinates array of coordinates
	 */
	public Point(double[] coordinates) {
		this(coordinates[0], coordinates[1], coordinates[2]);
	}
}