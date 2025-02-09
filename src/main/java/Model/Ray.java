package Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
//import java.util.Objects;
import static Model.Direction.XU;
import static Model.Direction.XD;
import static Model.Direction.YR;
import static Model.Direction.YL;
import static Model.Direction.ZU;
import static Model.Direction.ZD;

/**
 * Represents a ray in the Black Box Board game, which moves through the board from an entry point,
 * interacting with atoms and changing direction until it either exits or is absorbed.
 */


public class Ray {
    //reference to the game board for path and check for atoms
    private final BlackBoxBoard board;
    //entry point of ray on the game board
    private final BlackBoxBoard.Point3D entryPoint;
    //we will store the path ray takes using a list
    private final BlackBoxBoard.Point3D exitPoint;
    private final List<BlackBoxBoard.Point3D> path;

    //to check whether ray is absorbed by an atom
    private boolean isAbsorbed;

    private boolean deflected60;

    private boolean deflected120;

    private boolean rayReversed;


    Direction newDir;
    Direction entryDir;
    Direction exitDir;
    Direction Abs = Direction.Absorbed;

    /**
     * Constructs a Ray object with a specified entry node on the board.
     * The ray's path is calculated upon creation based on interactions with atoms.
     *
     * @param board The game board the ray will interact with.
     * @param node  The entry node number where the ray enters the board.
     */

    public Ray(BlackBoxBoard board, int node){

        this.board = board; //board ref
        this.entryPoint = RayNode.getNodeCoordinates(node); //starting point
        this.entryDir = RayNode.getRevDir(Objects.requireNonNull(RayNode.getNodeDirection(node)));
        this.path = new ArrayList<>();
        this.isAbsorbed = false; //at the start ray is not absorbed
        rayReversed = false;
        deflected60 = false;
        deflected120 = false;
        //call method to make sure every time a ray object is created its path is calculated immediately

        this.exitPoint = calculatePath();
    }

    /**
     * Returns true if the ray is absorbed by an atom.
     *
     * @return true if absorbed, otherwise false.
     */

    public boolean isAbsorbed() {
        return this.isAbsorbed;
    }

    /**
     * Returns true if the ray is deflected by 60 degrees.
     *
     * @return true if deflected by 60 degrees, otherwise false.
     */

    public boolean isDeflected60() {
        return this.deflected60;
    }

    /**
     * Returns true if the ray is deflected by 120 degrees.
     *
     * @return true if deflected by 120 degrees, otherwise false.
     */
    public boolean isDeflected120() { return this.deflected120; }
    /**
     * Returns true if the ray's direction is reversed.
     *
     * @return true if reversed, otherwise false.
     */

    public boolean isRayReversed() { return this.rayReversed; }

    /**
     * Gets the exit point of the ray if it exits the board.
     *
     * @return the exit point as Point3D if it exists.
     */
    public BlackBoxBoard.Point3D getExitPoint() {
        return exitPoint;
    }
    /**
     * Gets the entry point of the ray.
     *
     * @return the entry point as Point3D.
     */
    public BlackBoxBoard.Point3D getEntryPoint() {return entryPoint;}

    public Direction getDirection() {
        return exitDir;
    }

    public Direction getEntryDir() { return  entryDir; }

    public Direction getExitDir() {
        return exitDir;
    }

    //check for atom
    private boolean checkForAtom(BlackBoxBoard.Point3D point){
        //use getHexCell method to check if a ray's current position has encountered an atom
        HexCell cell = board.getCell(point);// get HexCell at given point.
        return cell != null && cell.hasAtom();// check if the cell has an atom

    }

    /**
     * Calculates the ray's path starting from the entry point, reacting to any atoms encountered.
     * The path ends when the ray exits the board or is absorbed by an atom.
     * @return the point at which the ray either exits the board or is absorbed.
     */
    private BlackBoxBoard.Point3D calculatePath(){
        Direction dir = entryDir;

        BlackBoxBoard.Point3D exitPoint = null;

        // calculation if ray is immediately reflected
        if(HexCell.isEdgeCell(entryPoint)){
            HexCell cell = board.getCell(entryPoint);

            if (cell != null && cell.hasAtom()) {
                isAbsorbed = true;
                path.add(entryPoint);
                BlackBoxBoard.rayCount += 1;
                BlackBoxBoard.rayMarkers += 1;
                return entryPoint;
            }

            else if(cell != null && cell.hasCIPoint()){
                //if it has a CI we will find the cells on the edge its next to and find out if these cells have atoms o n edge
                if(isRayReflectedAtEdge(entryPoint)){
                    rayReversed = true;
                    path.add(entryPoint);
                    BlackBoxBoard.rayCount += 1;
                    BlackBoxBoard.rayMarkers += 1;
                    return entryPoint; //end method early since the ray is reflected
                }

            }

        }


        //if ray is not immediately reflected
        //start path at entry point
        this.path.add(this.entryPoint);

        // initialize list to store cells visited by the ray
        //List<BlackBoxBoard.Point3D> visitedCells = new ArrayList<>();
        //visitedCells.add(this.entryPoint);

        // current position of the ray
        BlackBoxBoard.Point3D currentPosition = this.entryPoint;

        // iterate until the ray is absorbed or reaches edge of board
        // edge of board argument included in the end so the loop
        // doesn't break due to the entry point being on edge of board
        while (!isAbsorbed) {
            // Calculate the next position based on the current position and direction
            BlackBoxBoard.Point3D nextPosition;

            HexCell cell = board.getCell(currentPosition);

            if (cell !=null && cell.hasCIPoint()) {

                Direction result = newPath(currentPosition, dir);

                if (result == Direction.Absorbed) {
                    isAbsorbed = true;
                    nextPosition = calculateNextPosition(currentPosition, dir);
                    this.path.add(nextPosition);
                    BlackBoxBoard.rayMarkers += 1;
                    break;
                }

                else if (result == Direction.Error) {
                    System.out.println("Error");
                    break;
                }

                else {
                    if (rayReversed) {
                        BlackBoxBoard.rayMarkers += 1;
                    }
                    dir = result;
                }
            }

            // check if ray is on edge of board and break loop if true
            if (hasReachedBoardEdge(currentPosition, dir)) {
                exitPoint = currentPosition;
                if (!isRayReversed()) {
                    BlackBoxBoard.rayMarkers += 2;
                }
                break;
            }

            nextPosition = calculateNextPosition(currentPosition, dir);

            // Add the next position to the path
            this.path.add(nextPosition);

            // Update current position
            currentPosition = nextPosition;

            // Add the current position to the visited cells list
            //visitedCells.add(currentPosition);

        }

        exitDir = dir;
        BlackBoxBoard.rayCount += 1;
        return exitPoint;
    }


    /**
     * Determines the new direction of the ray after encountering an atom or reaching the board edge.
     *
     * @param position Current position of the ray as Point3D.
     * @param dir      Current direction of the ray.
     * @return the new direction after processing interactions at the current position.
     */

    private Direction newPath (BlackBoxBoard.Point3D position, Direction dir) {

        int CIx = position.x;
        int CIy = position.y;
        int CIz = position.z;

        int aGx = CIx;
        int aGy = CIy;
        int aGz = CIz;

        int aOx = CIx;
        int aOy = CIy;
        int aOz = CIz;

        int aPx = CIx;
        int aPy = CIy;
        int aPz = CIz;

        BlackBoxBoard.Point3D greenAtom;
        BlackBoxBoard.Point3D orangeAtom;
        BlackBoxBoard.Point3D pinkAtom;

        boolean atomGreen = false;
        boolean atomPink = false;
        boolean atomOrange = false;

        switch (dir) {

            case YL:

                aGy = CIy - 1;
                aGz = CIz + 1;

                greenAtom = new BlackBoxBoard.Point3D(aGx, aGy, aGz);
                if (checkForAtom(greenAtom)) {
                    atomGreen = true;
                }

                aOx = CIx - 1;
                aOy = CIy + 1;

                orangeAtom = new BlackBoxBoard.Point3D(aOx, aOy, aOz);
                if (checkForAtom(orangeAtom)) {
                    atomOrange = true;
                }

                aPx = CIx - 1;
                aPz = CIz + 1;

                pinkAtom = new BlackBoxBoard.Point3D(aPx, aPy, aPz);
                if (checkForAtom(pinkAtom)) {
                    atomPink = true;
                }

                if (atomGreen && atomOrange) {
                    // reverse ray
                    newDir = YR;
                    rayReversed = true;
                }

                else if (atomGreen && atomPink) {
                    newDir = XD;
                    deflected120 = true;
                }

                else if (atomOrange && atomPink) {
                    newDir = ZU;
                    deflected120 = true;
                }

                else if (atomGreen) {
                    newDir = ZD;
                    deflected60 = true;
                }

                else if (atomOrange) {
                    newDir = XU;
                    deflected60 = true;
                }

                else if (atomPink) {
                    newDir = Abs;
                }

                break;

            case YR:

                aGx = CIx + 1;
                aGy = CIy - 1;

                greenAtom = new BlackBoxBoard.Point3D(aGx, aGy, aGz);
                if (checkForAtom(greenAtom)) {
                    atomGreen = true;
                }

                aOy = CIy + 1;
                aOz = CIz - 1;

                orangeAtom = new BlackBoxBoard.Point3D(aOx, aOy, aOz);
                if (checkForAtom(orangeAtom)) {
                    atomOrange = true;
                }

                aPx = CIx + 1;
                aPz = CIz - 1;

                pinkAtom = new BlackBoxBoard.Point3D(aPx, aPy, aPz);
                if (checkForAtom(pinkAtom)) {
                    atomPink = true;
                }

                if (atomGreen && atomOrange) {
                    // reverse ray
                    newDir = YL;
                    rayReversed = true;
                }

                else if (atomGreen && atomPink) {
                    newDir = ZD;
                    deflected120 = true;
                }

                else if (atomOrange && atomPink) {
                    newDir = XU;
                    deflected120 = true;
                }

                else if (atomGreen) {
                    newDir = XD;
                    deflected60 = true;
                }

                else if (atomOrange) {
                    newDir = ZU;
                    deflected60 = true;
                }

                else if (atomPink) {
                    newDir = Abs;
                }

                break;

            case XU:

                aGx = CIx + 1;
                aGy = CIy - 1;

                greenAtom = new BlackBoxBoard.Point3D(aGx, aGy, aGz);
                if (checkForAtom(greenAtom)) {
                    atomGreen = true;
                }

                aOx = CIx - 1;
                aOz = CIz + 1;

                orangeAtom = new BlackBoxBoard.Point3D(aOx, aOy, aOz);
                if (checkForAtom(orangeAtom)) {
                    atomOrange = true;
                }

                aPy = CIy - 1;
                aPz = CIz + 1;

                pinkAtom = new BlackBoxBoard.Point3D(aPx, aPy, aPz);
                if (checkForAtom(pinkAtom)) {
                    atomPink = true;
                }

                if (atomGreen && atomOrange) {
                    // reverse ray
                    newDir = XD;
                    rayReversed = true;
                }

                else if (atomGreen && atomPink) {
                    newDir = ZD;
                    deflected120 = true;
                }

                else if (atomOrange && atomPink) {
                    newDir = YR;
                    deflected120 = true;
                }

                else if (atomGreen) {
                    newDir = YL;
                    deflected60 = true;
                }

                else if (atomOrange) {
                    newDir = ZU;
                    deflected60 = true;
                }

                else if (atomPink) {
                    newDir = Abs;
                }

                break;

            case XD:

                aGx = CIx + 1;
                aGz = CIz - 1;

                greenAtom = new BlackBoxBoard.Point3D(aGx, aGy, aGz);
                if (checkForAtom(greenAtom)) {
                    atomGreen = true;
                }

                aOx = CIx - 1;
                aOy = CIy + 1;

                orangeAtom = new BlackBoxBoard.Point3D(aOx, aOy, aOz);
                if (checkForAtom(orangeAtom)) {
                    atomOrange = true;
                }

                aPy = CIy + 1;
                aPz = CIz - 1;

                pinkAtom = new BlackBoxBoard.Point3D(aPx, aPy, aPz);
                if (checkForAtom(pinkAtom)) {
                    atomPink = true;
                }

                if (atomGreen && atomOrange) {
                    // reverse ray
                    newDir = XU;
                    rayReversed = true;
                }

                else if (atomGreen && atomPink) {
                    newDir = YL;
                    deflected120 = true;
                }

                else if (atomOrange && atomPink) {
                    newDir = ZU;
                    deflected120 = true;
                }

                else if (atomGreen) {
                    newDir = ZD;
                    deflected60 = true;
                }

                else if (atomOrange) {
                    newDir = YR;
                    deflected60 = true;
                }

                else if (atomPink) {
                    newDir = Abs;
                }

                break;

            case ZU:

                aGy = CIy - 1;
                aGz = CIz + 1;

                greenAtom = new BlackBoxBoard.Point3D(aGx, aGy, aGz);
                if (checkForAtom(greenAtom)) {
                    atomGreen = true;
                }

                aOx = CIx + 1;
                aOz = CIz - 1;

                orangeAtom = new BlackBoxBoard.Point3D(aOx, aOy, aOz);
                if (checkForAtom(orangeAtom)) {
                    atomOrange = true;
                }

                aPx = CIx + 1;
                aPy = CIy - 1;

                pinkAtom = new BlackBoxBoard.Point3D(aPx, aPy, aPz);
                if (checkForAtom(pinkAtom)) {
                    atomPink = true;
                }

                if (atomGreen && atomOrange) {
                    // reverse ray
                    newDir = ZD;
                    rayReversed = true;
                }

                else if (atomGreen && atomPink) {
                    newDir = XD;
                    deflected120 = true;
                }

                else if (atomOrange && atomPink) {
                    newDir = YL;
                    deflected120 = true;
                }

                else if (atomGreen) {
                    newDir = YR;
                    deflected60 = true;
                }

                else if (atomOrange) {
                    newDir = XU;
                    deflected60 = true;
                }

                else if (atomPink) {
                    newDir = Abs;
                }

                break;

            case ZD:

                aGx = CIx - 1;
                aGz = CIz + 1;

                greenAtom = new BlackBoxBoard.Point3D(aGx, aGy, aGz);
                if (checkForAtom(greenAtom)) {
                    atomGreen = true;
                }

                aOy = CIy + 1;
                aOz = CIz - 1;

                orangeAtom = new BlackBoxBoard.Point3D(aOx, aOy, aOz);
                if (checkForAtom(orangeAtom)) {
                    atomOrange = true;
                }

                aPx = CIx - 1;
                aPy = CIy + 1;

                pinkAtom = new BlackBoxBoard.Point3D(aPx, aPy, aPz);
                if (checkForAtom(pinkAtom)) {
                    atomPink = true;
                }

                if (atomGreen && atomOrange) {
                    // reverse ray
                    newDir = ZU;
                    rayReversed = true;
                }

                else if (atomGreen && atomPink) {
                    newDir = YR;
                    deflected120 = true;
                }

                else if (atomOrange && atomPink) {
                    newDir = XU;
                    deflected120 = true;
                }

                else if (atomGreen) {
                    newDir = XD;
                    deflected60 = true;
                }

                else if (atomOrange) {
                    newDir = YL;
                    deflected60 = true;
                }

                else if (atomPink) {
                    newDir = Abs;
                }

                break;
        }

        /* print statements used for testing deflections
        if (rayReversed) {
            System.out.println("Ray reversed.");
        }

        else if (deflected120) {
            System.out.println("Ray deflected 120 degrees.");
        }

        else if (deflected60) {
            System.out.println("Ray deflected 60 degrees.");
        }
         */

        return newDir;
    }


    /**
     * Checks if a given position is at the edge of the board.
     *
     * @param position  The ray's current position as Point3D.
     * @param direction The current direction of the ray.
     * @return true if the position is at the edge, otherwise false.
     */
    private boolean hasReachedBoardEdge(BlackBoxBoard.Point3D position, Direction direction) {

        int node = RayNode.getNodeNumber(position, direction);
        return node != -1;
    }


    // Method to calculate the next position of the ray based on the current position and direction of ray
    private BlackBoxBoard.Point3D calculateNextPosition(BlackBoxBoard.Point3D currentPosition, Direction dir) {

        int x = currentPosition.x;
        int y = currentPosition.y;
        int z = currentPosition.z;


        switch (dir) {
            case YL: // direction is on axis y going left
                x --;
                z ++;
                break;

            case YR: // direction is on axis y going right
                x ++;
                z --;
                break;

            case XU: // direction is on axis x going up
                y --;
                z ++;
                break;

            case XD: // direction is on axis x going down
                y ++;
                z --;
                break;

            case ZU: // direction is on axis z going up
                x ++;
                y --;
                break;

            case ZD: // direction is on axis z going down
                x --;
                y ++;
                break;
        }

        // Return the calculated next position
        return new BlackBoxBoard.Point3D(x, y, z);
    }

    /**
     * Outputs detailed information about a ray's path through the board, including entry and exit points.
     * @param ray The ray object for which information is to be printed.
     * @return An array containing the entry and exit node numbers.
     */
    public static int[]  printRayInfo(Ray ray) {

        // Print the ray's path
        BlackBoxBoard.Point3D entryPoint = ray.getEntryPoint();
        Direction entryDir = RayNode.getRevDir(ray.getEntryDir());
        int entryNodeNumber = RayNode.getNodeNumber(entryPoint, entryDir);
        BlackBoxBoard.Point3D exitPoint = ray.getExitPoint();
        Direction direction = ray.getDirection();
        int exitNodeNumber = RayNode.getNodeNumber(exitPoint, direction);

        System.out.println("Ray entered at: " + entryPoint);
        System.out.println("Ray's entry node: " + entryNodeNumber);
        // Check if the ray is absorbed and print the result
        if (ray.isAbsorbed()) {
            System.out.println("Ray absorbed.");
        }
        else if (ray.isRayReversed()) {
            System.out.println("Ray reversed.");
        }
        else {
            System.out.println("Ray exited at: " + exitPoint);
            System.out.println("Ray's exit direction is: " + direction);
            System.out.println("Ray's exit node: " + exitNodeNumber);
        }

        System.out.println("Ray's Path: " + ray.getPath() + "\n");
        return new int[] {entryNodeNumber, exitNodeNumber};
    }

    // Function to calculate and store  edge cells next to entry point of a ray to determine if ray is reversed
    private List<BlackBoxBoard.Point3D> getNextTo(BlackBoxBoard.Point3D point) {
        List<BlackBoxBoard.Point3D> nextTo = new ArrayList<>();

        // Handle corner conditions
        if (point.x == -4 && point.y == 4) { // Bottom left corner
            nextTo.add(new BlackBoxBoard.Point3D(point.x, point.y - 1, point.z + 1));
            nextTo.add(new BlackBoxBoard.Point3D(point.x + 1, point.y, point.z - 1));
        }
        if (point.x == 4 && point.y == -4) { // Top right corner
            nextTo.add(new BlackBoxBoard.Point3D(point.x - 1, point.y, point.z + 1));
            nextTo.add(new BlackBoxBoard.Point3D(point.x, point.y + 1, point.z - 1));
        }
        if (point.x == -4 && point.z == 4) { // Leftmost corner
            nextTo.add(new BlackBoxBoard.Point3D(point.x + 1, point.y - 1, point.z));
            nextTo.add(new BlackBoxBoard.Point3D(point.x, point.y + 1, point.z - 1));
        }
        if (point.x == 4 && point.z == -4) { // Rightmost corner
            nextTo.add(new BlackBoxBoard.Point3D(point.x, point.y - 1, point.z + 1));
            nextTo.add(new BlackBoxBoard.Point3D(point.x - 1, point.y + 1, point.z));
        }
        if (point.y == -4 && point.z == 4) { // Top left corner
            nextTo.add(new BlackBoxBoard.Point3D(point.x + 1, point.y, point.z - 1));
            nextTo.add(new BlackBoxBoard.Point3D(point.x - 1, point.y + 1, point.z));
        }
        if (point.y == 4 && point.z == -4) { // Bottom right corner
            nextTo.add(new BlackBoxBoard.Point3D(point.x + 1, point.y - 1, point.z));
            nextTo.add(new BlackBoxBoard.Point3D(point.x - 1, point.y, point.z + 1));
        }

        // Handle non-corner edge conditions separately
        // X-axis edge conditions
        if (point.x == -4 || point.x == 4) {
            nextTo.add(new BlackBoxBoard.Point3D(point.x, point.y + 1, point.z - 1));
            nextTo.add(new BlackBoxBoard.Point3D(point.x, point.y - 1, point.z + 1));
        }
        // Y-axis edge conditions
        if (point.y == -4 || point.y == 4) {
            nextTo.add(new BlackBoxBoard.Point3D(point.x + 1, point.y, point.z - 1));
            nextTo.add(new BlackBoxBoard.Point3D(point.x - 1, point.y, point.z + 1));
        }
        // Z-axis edge conditions
        if (point.z == -4 || point.z == 4) {
            nextTo.add(new BlackBoxBoard.Point3D(point.x + 1, point.y - 1, point.z));
            nextTo.add(new BlackBoxBoard.Point3D(point.x - 1, point.y + 1, point.z));
        }

        return nextTo;
    }
    //Function to determine if ray will be reversed
    private boolean isRayReflectedAtEdge(BlackBoxBoard.Point3D point) {
        List<BlackBoxBoard.Point3D> nextTo = getNextTo(point);

        // Check if any of the adjacent cells have an atom which could be the cause of the reversal/reflection
        for (BlackBoxBoard.Point3D cellsNextTo : nextTo) {
            HexCell cell = board.getCell(cellsNextTo);
            if (cell != null && cell.hasAtom()) {
                return true; // Ray is reflected by an atom in the adjacent cell.
            }
        }
        return false; //No reflection occurs if no atoms are in the cells next to entry point
    }



    // string representation of rays path
    public String getPath() {
        StringBuilder sb = new StringBuilder();

        // Iterate over points in the path list
        for (int i = 0; i < this.path.size(); i++) {
            BlackBoxBoard.Point3D point = this.path.get(i);

            // Append the coordinates of the point
            sb.append("(")
                    .append(point.x).append(", ")
                    .append(point.y).append(", ")
                    .append(point.z)
                    .append(")");

            // Append separator "->" if not the last point
            if (i < this.path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }


}