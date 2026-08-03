package compiler.printer;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Trees;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class TreeVisualizer {

    /**
     * Renders the native ANTLR GUI TreeViewer with interactive Zoom and Drag-to-Pan support.
     */
    public static void showGuiTree(ParseTree tree, Parser parser) {
        JFrame frame = new JFrame("Official ANTLR Tree Viewer (Drag to Move | Scroll Wheel to Zoom)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. Initialize the official ANTLR GUI component
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        TreeViewer viewer = new TreeViewer(ruleNames, tree);

        // 2. Turn off curved edges to ensure lines scale perfectly
        viewer.setUseCurvedEdges(false);

        // 3. Set up the scroll pane container first
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        // 4. Wrap the ANTLR viewer in our interactive controller panel
        InteractiveWrapperPanel wrapperPanel = new InteractiveWrapperPanel(viewer, scrollPane);
        scrollPane.setViewportView(wrapperPanel);

        frame.add(scrollPane);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Layout container that captures mouse movements to pan the JScrollPane view.
     */
    private static class InteractiveWrapperPanel extends JPanel implements MouseWheelListener {
        private final TreeViewer antlrViewer;
        private final JScrollPane parentScrollPane;
        private double scaleFactor = 1.0;

        // Track the starting position when a drag action begins
        private Point originPoint;

        public InteractiveWrapperPanel(TreeViewer antlrViewer, JScrollPane parentScrollPane) {
            this.antlrViewer = antlrViewer;
            this.parentScrollPane = parentScrollPane;

            setLayout(new GridBagLayout());
            setBackground(Color.WHITE);
            add(antlrViewer);

            // Bind listeners for scrolling and dragging
            addMouseWheelListener(this);
            setupDragToPanSupport();

            updateAntlrViewerScale();
        }

        /**
         * 🌟 Captures drag vectors to shift the viewport scrollbars smoothly
         */
        private void setupDragToPanSupport() {
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Save the mouse coordinate point when you click down
                    originPoint = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (originPoint == null) return;

                    // Calculate how far the mouse has traveled since clicking down
                    int deltaX = originPoint.x - e.getX();
                    int deltaY = originPoint.y - e.getY();

                    // Access the viewport container configuration bounds
                    JViewport viewport = parentScrollPane.getViewport();
                    Point viewPosition = viewport.getViewPosition();

                    // Shift the current coordinates by the drag distance
                    viewPosition.translate(deltaX, deltaY);

                    // Clamp layout viewport values so you don't pan into outer space
                    int maxX = InteractiveWrapperPanel.this.getWidth() - viewport.getWidth();
                    int maxY = InteractiveWrapperPanel.this.getHeight() - viewport.getHeight();

                    if (viewPosition.x < 0) viewPosition.x = 0;
                    if (viewPosition.y < 0) viewPosition.y = 0;
                    if (viewPosition.x > maxX && maxX > 0) viewPosition.x = maxX;
                    if (viewPosition.y > maxY && maxY > 0) viewPosition.y = maxY;

                    // Update the layout positions instantly
                    viewport.setViewPosition(viewPosition);
                }
            };

            // Register handlers with the panel infrastructure
            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        private void updateAntlrViewerScale() {
            antlrViewer.setScale(scaleFactor);
            antlrViewer.invalidate();
            revalidate();
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getWheelRotation() < 0) {
                scaleFactor = Math.min(4.0, scaleFactor + 0.1);
            } else {
                scaleFactor = Math.max(0.4, scaleFactor - 0.1);
            }
            updateAntlrViewerScale();
        }
    }


    public static void printTextTree(ParseTree tree, Parser parser) {
        // StringUtils.convertToString handles clean escaping of line breaks
        String prettyTree = toPrettyTree(tree, parser, 0);
        System.out.println(prettyTree);
    }

    private static String toPrettyTree(ParseTree tree, Parser parser, int level) {
        StringBuilder builder = new StringBuilder();

        // Create matching indentation spaces
        for (int i = 0; i < level; i++) {
            builder.append("  │");
        }

        // Add a visual branch marker
        if (level > 0) {
            builder.append("── ");
        }

        // Get the human-readable rule name or literal token value
        String nodeText = Trees.getNodeText(tree, parser);
        builder.append(nodeText).append("\n");

        // Recursively walk through child nodes
        for (int i = 0; i < tree.getChildCount(); i++) {
            builder.append(toPrettyTree(tree.getChild(i), parser, level + 1));
        }

        return builder.toString();
    }
}
