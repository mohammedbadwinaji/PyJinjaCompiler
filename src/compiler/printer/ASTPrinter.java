package compiler.printer;

import compiler.ast.common.AstNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Printer for custom AST nodes with console text and interactive GUI visualization modes.
 * Uses reflection to traverse AST node structure since nodes don't extend ANTLR classes.
 */
public class ASTPrinter {

    /**
     * Prints a highly legible console text outline with indentation mapping custom AST nodes.
     * Displays node types, properties, and hierarchical structure.
     */
    public static void printText(AstNode root) {
        if (root == null) {
            System.out.println("AST is null");
            return;
        }
        String text = toTextTree(root, 0, "");
        System.out.println(text);
    }

    private static String toTextTree(AstNode node, int level, String prefix) {
        StringBuilder builder = new StringBuilder();

        // Build indentation
        for (int i = 0; i < level; i++) {
            builder.append("  │");
        }

        // Add branch marker for non-root
        if (level > 0) {
            builder.append("── ");
        }

        // Add node name and line
        builder.append(node.getNodeName())
                .append(" [line ")
                .append(node.getLine())
                .append("]");

        // Add properties using reflection
        List<PropertyInfo> properties = extractProperties(node);
        if (!properties.isEmpty()) {
            builder.append(" (");
            for (int i = 0; i < properties.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                PropertyInfo prop = properties.get(i);
                builder.append(prop.name).append("=").append(formatValue(prop.value));
            }
            builder.append(")");
        }

        builder.append("\n");

        // Recursively process child AST nodes
        List<AstNode> children = extractChildNodes(node);
        for (AstNode child : children) {
            builder.append(toTextTree(child, level + 1, prefix));
        }

        return builder.toString();
    }

    /**
     * Extracts properties from an AST node using reflection.
     * Returns primitive/string properties (not collections or other AST nodes).
     */
    private static List<PropertyInfo> extractProperties(AstNode node) {
        List<PropertyInfo> properties = new ArrayList<>();
        Class<?> clazz = node.getClass();

        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();

            // Skip methods from Object, AstNode, or non-getters
            if (methodName.equals("getClass") ||
                methodName.equals("getNodeName") ||
                methodName.equals("getLine") ||
                methodName.equals("accept") ||
                methodName.equals("prettyPrint") ||
                methodName.equals("toString") ||
                !methodName.startsWith("get") && !methodName.startsWith("is")) {
                continue;
            }

            // Skip if no parameters or not public
            if (method.getParameterCount() != 0 || !java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            try {
                Object value = method.invoke(node);

                // Skip null values, AST nodes, and collections
                if (value == null || value instanceof AstNode || value instanceof Collection) {
                    continue;
                }

                // Extract property name from method name
                String propName;
                if (methodName.startsWith("get")) {
                    propName = methodName.substring(3);
                    propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                } else if (methodName.startsWith("is")) {
                    propName = methodName.substring(2);
                    propName = Character.toLowerCase(propName.charAt(0)) + propName.substring(1);
                } else {
                    continue;
                }

                properties.add(new PropertyInfo(propName, value));
            } catch (Exception e) {
                // Skip methods that can't be invoked
            }
        }

        return properties;
    }

    /**
     * Extracts child AST nodes from a node using reflection.
     * Looks for methods that return single AST nodes or collections of AST nodes.
     */
    private static List<AstNode> extractChildNodes(AstNode node) {
        List<AstNode> children = new ArrayList<>();
        Class<?> clazz = node.getClass();

        for (Method method : clazz.getMethods()) {
            String methodName = method.getName();

            // Skip methods from Object, AstNode, or non-getters
            if (methodName.equals("getClass") ||
                methodName.equals("getNodeName") ||
                methodName.equals("getLine") ||
                methodName.equals("accept") ||
                methodName.equals("prettyPrint") ||
                methodName.equals("toString") ||
                !methodName.startsWith("get") && !methodName.startsWith("is")) {
                continue;
            }

            if (method.getParameterCount() != 0 || !java.lang.reflect.Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            try {
                Object value = method.invoke(node);

                if (value == null) {
                    continue;
                }

                // Handle single AST node
                if (value instanceof AstNode) {
                    children.add((AstNode) value);
                }
                // Handle collections of AST nodes
                else if (value instanceof Collection) {
                    for (Object item : (Collection<?>) value) {
                        if (item instanceof AstNode) {
                            children.add((AstNode) item);
                        }
                    }
                }
            } catch (Exception e) {
                // Skip methods that can't be invoked
            }
        }

        return children;
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }

    /**
     * Opens a custom visual layout panel with zoom-and-drag support.
     * Displays custom AST Node classes and properties with color-coding.
     */
    public static void showGuiTree(AstNode root) {
        if (root == null) {
            JOptionPane.showMessageDialog(null, "AST is null", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame frame = new JFrame("AST Visualizer (Drag to Pan | Scroll to Zoom)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create the custom AST canvas
        ASTCanvas canvas = new ASTCanvas(root);

        // Wrap in scroll pane
        JScrollPane scrollPane = new JScrollPane(canvas);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(1200, 800));

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Custom canvas component for rendering AST trees with zoom and pan support.
     */
    private static class ASTCanvas extends JPanel {
        private final AstNode root;
        private double scale = 1.0;
        private Point dragStartPoint;
        private final List<ASTNodeLayout> nodeLayouts = new ArrayList<>();

        private static final int NODE_WIDTH = 160;
        private static final int NODE_HEIGHT = 50;
        private static final int VERTICAL_SPACING = 70;
        private static final int HORIZONTAL_SPACING = 30;

        // Colors
        private static final Color NODE_COLOR = new Color(255, 228, 196); // Light tan/bisque
        private static final Color BORDER_COLOR = new Color(139, 69, 19); // Saddle brown
        private static final Color TEXT_COLOR = Color.BLACK;
        private static final Color PROPERTY_COLOR = new Color(0, 100, 0); // Dark green
        private static final Color LINE_COLOR = new Color(100, 100, 100);

        public ASTCanvas(AstNode root) {
            this.root = root;
            setBackground(Color.WHITE);
            setLayout(null);

            // Calculate node layouts
            calculateLayouts();

            // Setup mouse listeners for zoom and pan
            setupMouseListeners();
        }

        private void calculateLayouts() {
            nodeLayouts.clear();
            calculateSubtreeLayout(root, 0, 0);
        }

        private int calculateSubtreeLayout(AstNode node, int depth, int startX) {
            List<AstNode> children = extractChildNodes(node);
            int childCount = children.size();
            int totalWidth = 0;

            if (childCount == 0) {
                // Leaf node
                int x = startX;
                int y = depth * (NODE_HEIGHT + VERTICAL_SPACING) + 20;
                nodeLayouts.add(new ASTNodeLayout(node, x, y, NODE_WIDTH, NODE_HEIGHT));
                return NODE_WIDTH + HORIZONTAL_SPACING;
            }

            // Internal node - calculate children first
            int currentX = startX;
            for (int i = 0; i < childCount; i++) {
                int childWidth = calculateSubtreeLayout(children.get(i), depth + 1, currentX);
                currentX += childWidth;
                totalWidth += childWidth;
            }

            // Center this node above its children
            int firstChildX = nodeLayouts.stream()
                    .filter(l -> isChildOf(l.node, node))
                    .mapToInt(l -> l.x)
                    .min()
                    .orElse(startX);

            int lastChildX = nodeLayouts.stream()
                    .filter(l -> isChildOf(l.node, node))
                    .mapToInt(l -> l.x + l.width)
                    .max()
                    .orElse(startX + NODE_WIDTH);

            int x = (firstChildX + lastChildX) / 2 - NODE_WIDTH / 2;
            int y = depth * (NODE_HEIGHT + VERTICAL_SPACING) + 20;

            nodeLayouts.add(new ASTNodeLayout(node, x, y, NODE_WIDTH, NODE_HEIGHT));

            return Math.max(totalWidth, NODE_WIDTH + HORIZONTAL_SPACING);
        }

        private boolean isChildOf(AstNode potentialChild, AstNode parent) {
            List<AstNode> parentChildren = extractChildNodes(parent);
            return parentChildren.contains(potentialChild);
        }

        private void setupMouseListeners() {
            // Mouse wheel for zooming
            addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    if (e.getWheelRotation() < 0) {
                        scale = Math.min(4.0, scale + 0.1);
                    } else {
                        scale = Math.max(0.4, scale - 0.1);
                    }
                    revalidate();
                    repaint();
                }
            });

            // Mouse adapter for drag-to-pan
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStartPoint = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragStartPoint = null;
                    setCursor(Cursor.getDefaultCursor());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStartPoint == null) return;

                    int deltaX = dragStartPoint.x - e.getX();
                    int deltaY = dragStartPoint.y - e.getY();

                    JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, ASTCanvas.this);
                    if (scrollPane != null) {
                        JViewport viewport = scrollPane.getViewport();
                        Point viewPos = viewport.getViewPosition();
                        viewPos.translate(deltaX, deltaY);

                        // Clamp to valid range
                        int maxX = Math.max(0, getWidth() - viewport.getWidth());
                        int maxY = Math.max(0, getHeight() - viewport.getHeight());

                        viewPos.x = Math.max(0, Math.min(viewPos.x, maxX));
                        viewPos.y = Math.max(0, Math.min(viewPos.y, maxY));

                        viewport.setViewPosition(viewPos);
                    }

                    dragStartPoint = e.getPoint();
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(mouseAdapter);
        }

        @Override
        public Dimension getPreferredSize() {
            if (nodeLayouts.isEmpty()) {
                return new Dimension(800, 600);
            }

            int maxX = nodeLayouts.stream().mapToInt(l -> l.x + l.width).max().orElse(0);
            int maxY = nodeLayouts.stream().mapToInt(l -> l.y + l.height).max().orElse(0);

            return new Dimension((int) (maxX * scale) + 50, (int) (maxY * scale) + 50);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.scale(scale, scale);

            // Draw connections first (behind nodes)
            drawConnections(g2d);

            // Draw nodes
            for (ASTNodeLayout layout : nodeLayouts) {
                drawNode(g2d, layout);
            }
        }

        private void drawConnections(Graphics2D g2d) {
            g2d.setColor(LINE_COLOR);
            g2d.setStroke(new BasicStroke(1.5f));

            for (ASTNodeLayout layout : nodeLayouts) {
                AstNode node = layout.node;
                List<AstNode> children = extractChildNodes(node);

                if (!children.isEmpty()) {
                    Point parentCenter = new Point(
                            layout.x + layout.width / 2,
                            layout.y + layout.height
                    );

                    for (AstNode child : children) {
                        ASTNodeLayout childLayout = findLayout(child);
                        if (childLayout != null) {
                            Point childCenter = new Point(
                                    childLayout.x + childLayout.width / 2,
                                    childLayout.y
                            );
                            g2d.drawLine(parentCenter.x, parentCenter.y, childCenter.x, childCenter.y);
                        }
                    }
                }
            }
        }

        private void drawNode(Graphics2D g2d, ASTNodeLayout layout) {
            AstNode node = layout.node;

            // Draw rounded rectangle
            g2d.setColor(NODE_COLOR);
            g2d.fillRoundRect(layout.x, layout.y, layout.width, layout.height, 10, 10);

            // Draw border
            g2d.setColor(BORDER_COLOR);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawRoundRect(layout.x, layout.y, layout.width, layout.height, 10, 10);

            // Draw node name (bold)
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));

            String nodeName = node.getNodeName();
            FontMetrics boldFm = g2d.getFontMetrics();
            int nameWidth = boldFm.stringWidth(nodeName);
            int nameX = layout.x + (layout.width - nameWidth) / 2;
            int nameY = layout.y + 20;

            g2d.drawString(nodeName, nameX, nameY);

            // Draw line number (smaller)
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String lineText = "line " + node.getLine();
            FontMetrics smallFm = g2d.getFontMetrics();
            int lineWidth = smallFm.stringWidth(lineText);
            int lineX = layout.x + (layout.width - lineWidth) / 2;
            int lineY = layout.y + 32;

            g2d.drawString(lineText, lineX, lineY);

            // Draw properties (if any)
            List<PropertyInfo> properties = extractProperties(node);
            if (!properties.isEmpty()) {
                g2d.setColor(PROPERTY_COLOR);
                g2d.setFont(new Font("SansSerif", Font.ITALIC, 9));

                String propText = formatProperties(properties);
                String truncatedProp = truncateText(propText, layout.width - 10, g2d.getFont());

                FontMetrics propFm = g2d.getFontMetrics();
                int propWidth = propFm.stringWidth(truncatedProp);
                int propX = layout.x + (layout.width - propWidth) / 2;
                int propY = layout.y + 44;

                g2d.drawString(truncatedProp, propX, propY);
            }
        }

        private String formatProperties(List<PropertyInfo> properties) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < properties.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                PropertyInfo prop = properties.get(i);
                sb.append(prop.name).append("=").append(formatValue(prop.value));
            }
            return sb.toString();
        }

        private String truncateText(String text, int maxWidth, Font font) {
            FontMetrics fm = getFontMetrics(font);
            if (fm.stringWidth(text) <= maxWidth) {
                return text;
            }

            String ellipsis = "...";
            int ellipsisWidth = fm.stringWidth(ellipsis);
            int availableWidth = maxWidth - ellipsisWidth;

            for (int i = text.length() - 1; i > 0; i--) {
                String truncated = text.substring(0, i);
                if (fm.stringWidth(truncated) <= availableWidth) {
                    return truncated + ellipsis;
                }
            }

            return ellipsis;
        }

        private ASTNodeLayout findLayout(AstNode node) {
            return nodeLayouts.stream()
                    .filter(l -> l.node == node)
                    .findFirst()
                    .orElse(null);
        }

        private static class ASTNodeLayout {
            final AstNode node;
            final int x;
            final int y;
            final int width;
            final int height;

            ASTNodeLayout(AstNode node, int x, int y, int width, int height) {
                this.node = node;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }
        }
    }

    private static class PropertyInfo {
        final String name;
        final Object value;

        PropertyInfo(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
