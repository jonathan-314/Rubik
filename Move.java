package Rubik;

/**
 * represents a rotation of the cube
 * 
 * @author jonguo6
 */
class Move {
	int face, layer, direction;

	/**
	 * Move constructor
	 * 
	 * @param face      which face to rotate
	 * @param layer     which layer to rotate
	 * @param direction which direction to rotate
	 */
	public Move(int face, int layer, int direction) {
		this.face = face;
		this.layer = layer;
		this.direction = direction;
	}
}