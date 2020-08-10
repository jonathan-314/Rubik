package Rubik;

/**
 * Square of the cube
 * 
 * @author jonguo6
 */
class Square implements Comparable<Square> {

	/**
	 * corners of the square represented by a 2d array
	 */
	double[][] xyz = new double[4][3];

	/**
	 * corners of the square represented by an array of {@link Rubik.Point}
	 */
	Point[] points = new Point[4];

	/**
	 * Color of the square, as indicated by {@link Rubik#colors}
	 */
	int color;

	/**
	 * coordinates of the center of the square
	 */
	double[] center = new double[3];

	/**
	 * distance from the center of the camera {@link Rubik#cameraCenter}
	 */
	double dist = 0;

	/**
	 * is this square rotating
	 */
	boolean rotate = false;

	/**
	 * Square constructor
	 * 
	 * @param coordinates coordinates of center
	 * @param dimension   which direction the normal vector is: x,y,z
	 * @param color       color of the cube, see {@link #color}
	 */
	public Square(double[] coordinates, int dimension, int color) {
		for (int i = 0; i < 3; i++)
			center[i] = coordinates[i];
		int face1 = 0;
		int face2 = 1;
		if (dimension <= 1)
			face2 = 2;
		if (dimension == 0)
			face1 = 1;
		// dimension, face1, face2 = {0, 1, 2}
		double sideLength = 1;
		this.color = color;
		if (this.color == 6) { // internal black squares are slightly larger, behind
			sideLength *= 100;
			sideLength /= (2 * Rubik.dx[0]);
		}
		for (int i = 0; i < 4; i++) {
			xyz[i][dimension] = center[dimension] / Math.sqrt(sideLength);
			xyz[i][face1] = center[face1] + sideLength * Rubik.dx[i];
			xyz[i][face2] = center[face2] + sideLength * Rubik.dy[i];
			points[i] = new Point(xyz[i]);
		}
	}

	/**
	 * updates the distance to the center as well as the coordinates
	 */
	public void update() {
		for (int i = 0; i < 4; i++) {
			points[i].x = xyz[i][0];
			points[i].y = xyz[i][1];
			points[i].z = xyz[i][2];
		}
		dist = Rubik.distance(center, new double[] { Rubik.game.cameraCenter.x, Rubik.game.cameraCenter.y, Rubik.game.cameraCenter.z });
	}

	@Override
	public int compareTo(Square o) {
		return Double.compare(this.dist, o.dist);
	}
}