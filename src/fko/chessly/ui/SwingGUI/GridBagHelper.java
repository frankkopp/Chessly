/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fko.chessly.ui.SwingGUI;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * This class provides some helper methods for the GridBagLayout Manager.
 * It cannot be instanciated.
 */
public final class GridBagHelper {

    private GridBagHelper() {}

    /**
     * Simply no insets.
     */
    public static final Insets zeroInsets = new Insets(0, 0, 0, 0);


    /**
     * Create the constraints and insets for the component of a container.
     *
     * @param container the container
     * @param component the component
     * @param grid_x the line of the component in the grid
     * @param grid_y the row of the component in the grid
     * @param grid_width the number of rows this component spans
     *                   horizontally in the container
     * @param grid_height the number of lines this component spans
     *                    vertically in the container
     * @param fill the fill is used when the components display area is
     *             larger than the component's requested size. It
     *             determines if the component is resized and if so, how.
     * @param anchor the anchor is used when the components display area is
     *               smaller than the component's requested size. It
     *               determines where, within the display area, to place
     *               the component.
     * @param weight_x specifies how to distribute extra horizontal space
     * @param weight_y specifies how to distribute extra vertical space
     * @param top the minimum amount of space between the top of the
     *            component and the top of the display area
     * @param left the minimum amount of space between the left of the
     *             component and the left of the display area
     * @param right the minimum amount of space between the right of the
     *              component and the right of the display area
     * @param bottom the minimum amount of space between the bottom of the
     *               component and the top of the display area
     */
    public static void constrain(Container container, Component component,
            int grid_x, int grid_y,
            int grid_width, int grid_height,
            int fill, int anchor,
            double weight_x, double weight_y,
            int top, int left, int bottom, int right) {
        Insets insets;
        if (top + bottom + left + right > 0) {
            insets = new Insets(top, left, bottom, right);
        } else {
            insets = zeroInsets;
        }

        constrain(container, component, grid_x, grid_y, grid_width, grid_height,
                fill, anchor, weight_x, weight_y, insets);
    }

    /**
     * Create the constraints and insets for the component of a container.
     *
     * @param container the container
     * @param component the component
     * @param grid_x the line of the component in the grid
     * @param grid_y the row of the component in the grid
     * @param grid_width the number of rows this component spans
     *                   horizontally in the container
     * @param grid_height the number of lines this component spans
     *                    vertically in the container
     * @param fill the fill is used when the components display area is
     *             larger than the component's requested size. It
     *             determines if the component is resized and if so, how.
     * @param anchor the anchor is used when the components display area is
     *               smaller than the component's requested size. It
     *               determines where, within the display area, to place
     *               the component.
     * @param weight_x specifies how to distribute extra horizontal space
     * @param weight_y specifies how to distribute extra vertical space
     * @param insets definies the amount of space around the component
     */
    public static void constrain(Container container, Component component,
            int grid_x, int grid_y,
            int grid_width, int grid_height,
            int fill, int anchor,
            double weight_x, double weight_y,
            Insets insets) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = grid_x;
        c.gridy = grid_y;
        c.gridwidth = grid_width;
        c.gridheight = grid_height;
        c.fill = fill;
        c.anchor = anchor;
        c.weightx = weight_x;
        c.weighty = weight_y;
        c.insets = insets;

        ((GridBagLayout) container.getLayout()).setConstraints(component, c);
        container.add(component);
    }

    /**
     * Create the constraints and insets for the component of a container.
     * This version sets the component into one position in the grid abd
     * allows to specify the insets of the component.
     *
     * @param container the container
     * @param component the component
     * @param grid_x the line of the component in the grid
     * @param grid_y the row of the component in the grid
     * @param grid_width the number of rows this component spans
     *                   horizontally in the container
     * @param grid_height the number of lines this component spans
     *                    vertically in the container
     * @param top the minimum amount of space between the top of the
     *            component and the top of the display area
     * @param left the minimum amount of space between the left of the
     *             component and the left of the display area
     * @param right the minimum amount of space between the right of the
     *              component and the right of the display area
     * @param bottom the minimum amount of space between the bottom of the
     *               component and the top of the display area
     */
    public static void constrain(Container container, Component component,
            int grid_x, int grid_y,
            int grid_width, int grid_height,
            int top, int left, int bottom, int right) {
        constrain(container, component, grid_x, grid_y, grid_width, grid_height,
                GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
                0.0, 0.0, top, left, bottom, right);
    }

    /**
     * Create the constraints and insets for the component of a container.
     * This version simply sets the component to a position in the grid.
     *
     * @param container the container
     * @param component the component
     * @param grid_x the line of the component in the grid
     * @param grid_y the row of the component in the grid
     * @param grid_width the number of rows this component spans
     *                   horizontally in the container
     * @param grid_height the number of lines this component spans
     *                    vertically in the container
     */
    public static void constrain(Container container, Component component,
            int grid_x, int grid_y,
            int grid_width, int grid_height) {
        constrain(container, component, grid_x, grid_y, grid_width, grid_height,
                GridBagConstraints.NONE, GridBagConstraints.NORTHWEST,
                0.0, 0.0, 0, 0, 0, 0);
    }
}
