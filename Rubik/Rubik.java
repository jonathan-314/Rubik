package Rubik;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Rubik extends JPanel implements KeyListener, MouseListener {

	/**
	 * the rubik cube
	 */
	static Rubik game;

	/**
	 * number of layers (i.e. 3 means 3x3x3 cube)
	 */
	int n = 7;

	/**
	 * the cube itself
	 */
	Square[] cube;

	/**
	 * rotation constant, {@link Math#PI} / 720
	 */
	final double rotationConstant = Math.PI / 720;

	/**
	 * magnification of screen
	 */
	final double magnification = 4000;

	/**
	 * height of the top bar
	 */
	final int topHeight = 44;

	/**
	 * width of screen
	 */
	int screenWidth = getToolkit().getScreenSize().width;

	/**
	 * height of screen
	 */
	int screenHeight = getToolkit().getScreenSize().height;

	/**
	 * center of screen (screenWidth/2)
	 */
	int centerx = screenWidth / 2;

	/**
	 * center of screen (screenHeight/2)
	 */
	int centery = screenHeight / 2;

	/**
	 * Colors of the cube
	 */
	final Color[] colors = { Color.WHITE, Color.MAGENTA, Color.RED, Color.YELLOW, Color.CYAN, Color.GREEN,
			Color.BLACK };

	/**
	 * Linear transformation matrix
	 */
	double[][] matrix = new double[3][3];

	/**
	 * Point describing the center of the camera
	 */
	public Point cameraCenter = new Point(0, 0, -1000);

	static double[] dx = { 1, 1, -1, -1 };
	static double[] dy = { 1, -1, -1, 1 };

	// keyboard rotation
	boolean left = false;
	boolean right = false;
	boolean up = false;
	boolean down = false;

	// cube microrotation
	/**
	 * How many frames left of rotation, -1 if not rotating
	 */
	int rotateTime = -1;

	/**
	 * Which face of the cube is rotating (x,y,z)
	 */
	int rotateFace = -1;

	/**
	 * Which layer of the cube is rotating
	 */
	int rotateLayer = -1;

	/**
	 * Which direction the layer is rotating
	 */
	int rotateDirection = -1;

	/**
	 * minimum coordinate of the cube
	 */
	final double start = -100 * (n - 1) / 2;

	/**
	 * thickness of each layer
	 */
	double value = 100 * n / 2;

	// user control variables
	/**
	 * Is the cube being randomly shuffled
	 */
	boolean shuffle = false;

	/**
	 * Is the cube being solved
	 */
	boolean solve = false;

	/**
	 * Did the user pause the cube
	 */
	boolean pause = false;

	/**
	 * List of moves
	 */
	ArrayList<Move> moves = new ArrayList<Move>();

	/**
	 * 'null' square
	 */
	Square none = new Square(new double[] { 0, 0, 0 }, 1, 2);

	/**
	 * the square the cursor is hovering over
	 */
	Square selecting = none;

	/**
	 * the square the user has clicked on
	 */
	Square selected = none;

	/**
	 * Center of the square the user has clicked on, {@link #selected}
	 */
	double[] center = { 0, 0, 0 };

	/**
	 * JFrame for graphics
	 */
	JFrame jf = new JFrame("rubik");

	/**
	 * Robot to override mouse movement
	 */
	Robot robot;

	/**
	 * rubik constructor, constructs the cube
	 */
	public Rubik() {
		cube = new Square[n * n * 12];
		for (int i = 0; i < 4; i++) {
			dx[i] *= 47.5;
			dy[i] *= 47.5;
		}
		int index = 0;
		for (int i = 0; i < 3; i++) {
			int face1 = 0;
			int face2 = 1;
			if (i <= 1) {
				face2 = 2;
			}
			if (i == 0) {
				face1 = 1;
			}
			// i, face1, face2 = {0, 1, 2}
			for (int j = -1; j <= 1; j += 2) {
				int color = i * 2 + (j + 1) / 2;
				for (int k = 0; k < n; k++) {
					for (int l = 0; l < n; l++) {
						double[] center = new double[3];
						center[i] = j * value;
						center[face1] = start + k * 100;
						center[face2] = start + l * 100;
						cube[index] = new Square(center, i, color);
						index++;
						cube[index] = new Square(center, i, 6);
						index++;
					}
				}
			}
		}

		// sets matrix to the identity matrix
		for (int i = 0; i < 3; i++) {
			Arrays.fill(matrix[i], 0);
			matrix[i][i] = 1;
		}

		// move the mouse to the center
		try {
			robot = new Robot();
			robot.mouseMove(centerx, centery + topHeight);
		} catch (Exception e) {
		}

		addKeyListener(this);
		addMouseListener(this);
		setFocusable(true);
	}

	/**
	 * initializes the graphics and calls {@link #move()} each frame
	 * 
	 * @throws InterruptedException if while loop is terminated
	 */
	public void init() throws InterruptedException {
		jf.add(game);
		jf.setVisible(true);
		jf.setSize(screenWidth, screenHeight);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Thread.sleep(1000);

		Random random = new Random();
		while (true) {
			move();
			Thread.sleep(40);
			if (rotateTime < 0) {
				if (shuffle) {
					int face = random.nextInt(3);
					int layer = random.nextInt(n);
					int direction = random.nextInt(2) * 2 - 1;
					if (moves.size() > 0) {
						while (face == rotateFace && 5 * direction == -1 * rotateDirection && layer == rotateLayer) {
							// don't want to undo last move!
							face = random.nextInt(3);
							layer = random.nextInt(n);
							direction = random.nextInt(2) * 2 - 1;
						}
					}
					rotateTime = 8;
					rotateFace = face;
					rotateLayer = layer;
					rotateDirection = 5 * direction;
					moves.add(new Move(face, layer, -10 * direction));
				} else if (solve) {
					Move current = moves.remove(moves.size() - 1);
					rotateTime = 4;
					rotateFace = current.face;
					rotateLayer = current.layer;
					rotateDirection = current.direction;
					if (moves.size() == 0) {
						solve = false;
					}
				}
			}
		}
	}

	/**
	 * Each frame, rotates the camera and the cube
	 */
	public void move() {

		this.requestFocusInWindow(); // probably useless now but idk

		// keyboard rotation
		if (up) {
			macrorotate(1, 1.5d * n);
		}
		if (down) {
			macrorotate(1, -1.5d * n);
		}
		if (left) {
			macrorotate(0, 1.5d * n);
		}
		if (right) {
			macrorotate(0, -1.5d * n);
		}

		// mouse rotation
		int mousex = MouseInfo.getPointerInfo().getLocation().x;
		int mousey = MouseInfo.getPointerInfo().getLocation().y - topHeight;
		macrorotate(1, centerx - mousex);
		macrorotate(0, centery - mousey);
		cameraCenter.x = matrix[2][0] * 900 * n;
		cameraCenter.y = matrix[2][1] * 900 * n;
		cameraCenter.z = matrix[2][2] * 900 * n;

		// move the mouse back to the center
		try {
			robot.mouseMove(centerx, centery + topHeight);
		} catch (Exception e) {
		}

		if (!pause) {
			if (rotateTime >= 0) {
				rotateTime--;
				microrotate(rotateFace, rotateLayer, rotateDirection);
			}
			if (rotateTime == 0) {
				rotateTime = -1;
			}
		}
		jf.repaint();
	}

	public void paint(Graphics g) {
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, screenWidth, screenHeight); // background
		for (int i = 0; i < n * n * 12; i++) {
			cube[i].update();
		}

		g.translate(centerx, centery);

		Arrays.sort(cube);
		int[] cursorx = new int[4];
		int[] cursory = new int[4];
		double minimumDistance = Double.MAX_VALUE;
		for (int i = n * n * 12 - 1; i >= 0; i--) {
			Square d = cube[i];
			int[] drawx = new int[4];
			int[] drawy = new int[4];
			for (int j = 0; j < 4; j++) {
				Point p = d.points[j];

				// matrix multiplication
				p.ax = matrix[0][0] * (p.x - cameraCenter.x) + matrix[0][1] * (p.y - cameraCenter.y)
						+ matrix[0][2] * (p.z - cameraCenter.z);
				p.ay = matrix[1][0] * (p.x - cameraCenter.x) + matrix[1][1] * (p.y - cameraCenter.y)
						+ matrix[1][2] * (p.z - cameraCenter.z);
				p.az = matrix[2][0] * (p.x - cameraCenter.x) + matrix[2][1] * (p.y - cameraCenter.y)
						+ matrix[2][2] * (p.z - cameraCenter.z);
				drawx[j] = (int) (p.ax / p.az * magnification);
				drawy[j] = (int) (p.ay / p.az * magnification);
			}
			g.setColor(colors[d.color]);
			if (d.equals(selected)) {
				g.setColor(Color.GRAY);
			}
			g.fillPolygon(drawx, drawy, 4);
			if (d.color == 6) {
				continue;
			}

			// determining if the cursor hovers on this square
			int[] crossProducts = new int[4];
			for (int j = 0; j < 4; j++) {
				int k = (j + 1) % 4;
				int ax = drawx[j];
				int ay = drawy[j];
				int bx = drawx[k];
				int by = drawy[k];
				crossProducts[j] = ax * by - ay * bx; // cross product
			}
			if (sameSign(crossProducts)) {
				if (d.dist < minimumDistance) {
					minimumDistance = d.dist;
					selecting = d;
					for (int k = 0; k < 4; k++) {
						cursorx[k] = drawx[k];
						cursory[k] = drawy[k];
					}
				}
			}
		}
		g.setColor(Color.BLUE);
		g.fillPolygon(cursorx, cursory, 4); // where cursor is pointing at
		g.setColor(Color.WHITE);
		g.fillRect(-57, 20 - centery, 117, 30);
		g.setColor(Color.BLACK);
		g.drawOval(-50, 28 - centery, 15, 15);
		g.drawString("C", -47, 40 - centery);
		g.drawString("Jonathan Guo", -30, 40 - centery);
	}

	/**
	 * Rotates the camera
	 * 
	 * @param index      up/down or left/right - which dimension to rotate (x,y,z) -
	 *                   only two are used
	 * @param multiplier how much to rotate
	 */
	public void macrorotate(int index, double multiplier) {
		if (multiplier == 0)
			return;
		int face1 = 0;
		int face2 = 1;
		if (index != 2) {
			face2 = 2;
		}
		if (index == 0) {
			face1 = 1;
		}
		// ind1, face1, face2 = {0, 1, 2}
		double[][] newMatrix = new double[3][3];
		double rotationAngle = rotationConstant * multiplier * -1;
		for (int j = 0; j < 3; j++) {
			newMatrix[index][j] = matrix[index][j];
			newMatrix[face1][j] = matrix[face1][j] * Math.cos(rotationAngle)
					- matrix[face2][j] * Math.sin(rotationAngle);
			newMatrix[face2][j] = matrix[face1][j] * Math.sin(rotationAngle)
					+ matrix[face2][j] * Math.cos(rotationAngle);
		}
		matrix = newMatrix;
	}

	/**
	 * Rotates the cube
	 * 
	 * @param ind which dimension (x,y,z) to rotate
	 * @param rn  which layer of the cube to rotate
	 * @param dir direction of rotation
	 */
	public void microrotate(int ind, int rn, int dir) {
		int face1 = 0;
		int face2 = 1;
		if (ind <= 1) {
			face2 = 2;
		}
		if (ind == 0) {
			face1 = 1;
		}
		// ind1, face1, face2 = {0, 1, 2}
		double rotationAngle = rotationConstant * dir * 9;
		double lowerBound = -1 * value + rn * 100 - 15;
		double upperBound = -1 * value + rn * 100 + 115;
		for (int i = 0; i < n * n * 12; i++) {
			Square c = cube[i];
			if (c.center[ind] >= lowerBound && c.center[ind] <= upperBound) {
				for (int j = 0; j < 4; j++) {
					double coord1 = c.xyz[j][face1];
					double coord2 = c.xyz[j][face2];
					c.xyz[j][face1] = coord1 * Math.cos(rotationAngle) - coord2 * Math.sin(rotationAngle);
					c.xyz[j][face2] = coord1 * Math.sin(rotationAngle) + coord2 * Math.cos(rotationAngle);
				}
				for (int k = 0; k < 3; k++) {
					c.center[k] = (c.xyz[0][k] + c.xyz[2][k]) / 2;
				}
			}
		}
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		game = new Rubik();
		game.init();
	}

	/**
	 * Distance between two vectors
	 * 
	 * @param vec1 vector 1
	 * @param vec2 vector 2
	 * @return double, euclidean distance between vec1 and vec2 = |vec1 - vec2|
	 */
	public static double distance(double[] vec1, double[] vec2) {
		double ret = 0;
		for (int i = 0; i < 3; i++) {
			ret += (vec1[i] - vec2[i]) * (vec1[i] - vec2[i]);
		}
		return Math.sqrt(ret);
	}

	/**
	 * checks if all integers have the same sign (0 is regarded as both signs)
	 * 
	 * @param xp array to be checked
	 * @return whether or not all integers have the same sign
	 */
	private static boolean sameSign(int[] xp) {
		return allNonNegative(xp) || allNonPositive(xp);
	}

	/**
	 * checks if all integers are non-negative (0 or positive)
	 * 
	 * @param xp array to be checked
	 * @return whether or not all integers are greater than or equal to 0
	 */
	private static boolean allNonNegative(int[] xp) {
		for (int c : xp) {
			if (c > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * checks if all integers are non-positive (0 or negative)
	 * 
	 * @param xp array to be checked
	 * @return whether or not all integers are less than or equal to 0
	 */
	private static boolean allNonPositive(int[] xp) {
		for (int c : xp) {
			if (c < 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		String key = KeyEvent.getKeyText(e.getKeyCode());
		if (key.equals("D")) {
			down = true;
		} else if (key.equals("S")) {
			right = true;
		} else if (key.equals("A")) {
			up = true;
		} else if (key.equals("W")) {
			left = true;
		} else if (key.equals("P")) {
			pause = !pause;
		} else if (key.equals("Q")) {
			shuffle = !shuffle;
			if (shuffle) {
				solve = false;
			}
		} else if (key.equals("X")) {
			solve = !solve;
			if (solve) {
				shuffle = false;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		String key = KeyEvent.getKeyText(e.getKeyCode());
		if (key.equals("D")) {
			down = false;
		} else if (key.equals("S")) {
			right = false;
		} else if (key.equals("A")) {
			up = false;
		} else if (key.equals("W")) {
			left = false;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (selected.equals(none)) {
			selected = selecting;
			for (int i = 0; i < 3; i++) {
				center[i] = selecting.center[i];
			}
		} else {
			if (selecting == selected) {
				selected = none;
			}
			if (rotateTime > 0) {
				return;
			}
			int index = -1;
			for (int i = 0; i < 3; i++) {
				if (Math.abs(selecting.center[i] - selected.center[i]) < 5) {
					if (index != -1) {
						return;
					}
					index = i;
				}
			}
			if (index == -1) {
				return;
			}
			double val = selected.center[index];
			int face1 = index == 0 ? 1 : 0;
			int face2 = 3 - index - face1;
			// index, face1, face2 = {0,1,2}
			int direction = (selecting.center[face1] * selected.center[face2]
					- selecting.center[face2] * selected.center[face1]) > 0 ? 1 : -1;
			double layerDouble = (val + value - 30) / 100;
			int layer = Integer.parseInt(String.format("%.0f", layerDouble));
			if (layer == n) {
				layer = n - 1;
			}
			rotateTime = 8;
			rotateDirection = -5 * direction;
			rotateFace = index;
			rotateLayer = layer;
			selected = none;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
