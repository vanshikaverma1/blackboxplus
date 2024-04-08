package com.example.blackbox;

import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class RayCircle extends StackPane {
    private Circle circle;
    private Text rayText;

    RayCircle(double radius, Color fill) {

        circle = new Circle(radius);
        circle.setFill(fill);
        circle.setStroke(Color.BLACK);
        circle.setStrokeType(StrokeType.INSIDE);

        // text for ray number
        rayText = new Text();
        rayText.setFont(Font.font("Roboto Slab", 12));
        rayText.setFill(Color.WHITE);

        addHoverEffect();

        //StackPane.setAlignment(rayText, javafx.geometry.Pos.CENTER);
        // adding circle and text to StackPane
        getChildren().addAll(circle, rayText);
    }


    private void addHoverEffect() { //method for adding hover effect to custom RayCircle class.
        Color originalColor = (Color) circle.getFill();
        Color hoverColor = Color.DARKSLATEGRAY;
        circle.setOnMouseEntered((MouseEvent event) -> circle.setFill(hoverColor));
        circle.setOnMouseExited((MouseEvent event) -> circle.setFill(originalColor));
        rayText.setOnMouseEntered((MouseEvent event) -> circle.setFill(hoverColor));
        rayText.setOnMouseExited((MouseEvent event) -> circle.setFill(originalColor));
    }

    public void setRayText(String text) {
        rayText.setText(text);
    }

    public Text getRayText() {
        return rayText;
    }

   static void generateRayCircles(Group root) { //method for generating nodes (ray circles)
        //for loops using createRayCircle method to generate circles
        //with ray numbers in circles stacked as text.


        //arrays containing ray numbers organised using compass directions for the 6 edges of the main hexagon. each edge
        //is filled using a separate for loop.
        int[] rayNumNorth = {1, 54, 53, 52, 51, 50, 49, 48, 47, 46};
        int[] rayNumNorthWest = {2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] rayNumNorthEast = {45, 44, 43, 42, 41, 40, 39, 38, 37};
        int[] rayNumSouthWest = {18, 17, 16, 15, 14, 13, 12, 11};
        int[] rayNumSouthEast = {29, 30, 31, 32, 33, 34, 35, 36};
        int[] rayNumSouth = {19, 20, 21, 22, 23, 24, 25, 26, 27, 28};

        double circleXStartNorth = 605;
        double circleYStartNorth = 130;

        //generating circles for north edge of hexagon ------------
        for (int i = 0; i < rayNumNorth.length; i++) {
            int rayNumber = rayNumNorth[i];
            RayCircle circle = createRayCircle(circleXStartNorth + (i*34), circleYStartNorth, rayNumber);
            root.getChildren().add(circle);
        }

        //northwest edge----------------------------------------
        double NWCircleXStart = circleXStartNorth - 17;
        double NWCircleYStart = circleYStartNorth + 29.5;

        for (int leftRayNumber : rayNumNorthWest) {
            RayCircle circle = createRayCircle(NWCircleXStart, NWCircleYStart, leftRayNumber);
            root.getChildren().add(circle);

            NWCircleXStart -= 17;
            NWCircleYStart += 29.5;
        }

        //northeast edge----------------------------------------
        double NECircleXStart = circleXStartNorth + ((rayNumNorth.length * 34) - 17); //starting at the rightmost node of north edge.
        double NECircleYStart = circleYStartNorth + 29.5;

        for (int rightRayNumber : rayNumNorthEast) {
            RayCircle circle = createRayCircle(NECircleXStart, NECircleYStart, rightRayNumber);
            root.getChildren().add(circle);

            NECircleXStart += 17;
            NECircleYStart += 29.5;
        }

        double circleXStartSouth = 605;
        double circleYStartSouth = 659;

        //south edge -----------------------------------------
        for (int i = 0; i < rayNumSouth.length; i++) {
            int rayNumber = rayNumSouth[i];
            RayCircle circle = createRayCircle(circleXStartSouth + (i*34), circleYStartSouth, rayNumber);
            root.getChildren().add(circle);
        }

        //southwest edge----------------------------------------
        double SWCircleXStart = circleXStartSouth - 17;
        double SWCircleYStart = circleYStartSouth - 29.5;

        for (int leftRayNumber : rayNumSouthWest) {
            RayCircle circle = createRayCircle(SWCircleXStart, SWCircleYStart, leftRayNumber);
            root.getChildren().add(circle);

            SWCircleXStart -= 17;
            SWCircleYStart -= 29.5;
        }

        //southeast edge ----------------------------------------
        double SECircleXStart = circleXStartSouth + (rayNumSouth.length * 34) - 17; //starting at the rightmost node of south edge.
        double SECircleYStart = circleYStartSouth - 29.5;

        for (int rightRayNumber : rayNumSouthEast) {
            RayCircle circle = createRayCircle(SECircleXStart, SECircleYStart, rightRayNumber);
            root.getChildren().add(circle);

            SECircleXStart += 17;
            SECircleYStart -= 29.5;
        }




    }

//    private void handleRayCircleClick(RayCircle circle) {
//        circle.setOnMouseClicked(event -> {
//            if (circle.getFill().equals(Color.RED)) {
//                // Change color to another color when clicked
//                circle.setFill(Color.BLUE); // Change to your desired color
//            } else {
//                // Change color back to the original color when clicked again
//                circle.setFill(Color.RED); // Change to your original color
//            }
//        });
//    }

    static RayCircle createRayCircle(double layoutX, double layoutY, int number) {
        RayCircle circle = new RayCircle(12.0, Color.web("#4242ff"));
        circle.setLayoutX(layoutX);
        circle.setLayoutY(layoutY);

        // Set ray number text
        circle.setRayText(String.valueOf(number));

        return circle;
    }

}
