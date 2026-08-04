package compiler.printer;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Trees;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Printer for ANTLR parse trees with console text and interactive GUI visualization modes.
 */
public class ParseTreePrinter {

    /**
     * Outputs a clean console ASCII hierarchical tree structure using branch symbols.
     * Resolves rule names dynamically via parser rule vocabulary.
     */
    public static void printText(ParseTree tree, Parser parser) {
        String asciiTree = toAsciiTree(tree, parser, 0, new StringBuilder());
        System.out.println(asciiTree);
    }

    private static String toAsciiTree(ParseTree tree, Parser parser, int level, StringBuilder prefix) {
        StringBuilder builder = new StringBuilder();

        // Build the prefix for current level
        for (int i = 0; i < level; i++) {
            builder.append("  │");
        }

        // Add branch marker for non-root nodes
        if (level > 0) {
            builder.append("── ");
        }

        // Get node text
        String nodeText = getNodeText(tree, parser);
        builder.append(nodeText).append("\n");

        // Process children
        int childCount = tree.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = tree.getChild(i);
            builder.append(toAsciiTree(child, parser, level + 1, prefix));
        }

        return builder.toString();
    }

    private static String getNodeText(ParseTree tree, Parser parser) {
        if (tree instanceof TerminalNode) {
            TerminalNode terminal = (TerminalNode) tree;
            Token token = terminal.getSymbol();
            String tokenName = parser.getVocabulary().getSymbolicName(token.getType());
            if (tokenName == null) {
                tokenName = "TOKEN";
            }
            String tokenText = escapeTokenText(token.getText());
            return tokenName + " '" + tokenText + "'";
        } else if (tree instanceof ParserRuleContext) {
            ParserRuleContext context = (ParserRuleContext) tree;
            int ruleIndex = context.getRuleIndex();
            String ruleName = parser.getRuleNames()[ruleIndex];
            return ruleName;
        }
        return tree.toString();
    }

    private static String escapeTokenText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Opens an interactive vector-drawn Swing Canvas with zoom and pan support.
     * Features anti-aliasing, MouseWheelListener zooming, MouseAdapter drag-to-pan,
     * and color-coding for rule contexts (light blue) vs terminal tokens (light green).
     */
    public static void showGuiTree(ParseTree tree, Parser parser) {
        JFrame frame = new JFrame("Parse Tree Visualizer (Drag to Pan | Scroll to Zoom)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create the custom tree canvas
        ParseTreeCanvas canvas = new ParseTreeCanvas(tree, parser);

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
     * Custom canvas component for rendering parse trees with zoom and pan support.
     */
    private static class ParseTreeCanvas extends JPanel {
        private final ParseTree tree;
        private final Parser parser;
        private double scale = 1.0;
        private Point dragStartPoint;
        private final List<TreeNodeLayout> nodeLayouts = new ArrayList<>();

        private static final int NODE_WIDTH = 120;
        private static final int NODE_HEIGHT = 30;
        private static final int VERTICAL_SPACING = 50;
        private static final int HORIZONTAL_SPACING = 20;

        // Colors
        private static final Color RULE_CONTEXT_COLOR = new Color(173, 216, 230); // Light blue
        private static final Color TERMINAL_COLOR = new Color(144, 238, 144); // Light green
        private static final Color BORDER_COLOR = Color.BLACK;
        private static final Color TEXT_COLOR = Color.BLACK;

        public ParseTreeCanvas(ParseTree tree, Parser parser) {
            this.tree = tree;
            this.parser = parser;
            setBackground(Color.WHITE);
            setLayout(null);

            // Calculate node layouts
            calculateLayouts();

            // Setup mouse listeners for zoom and pan
            setupMouseListeners();
        }

        private void calculateLayouts() {
            nodeLayouts.clear();
            calculateSubtreeLayout(tree, 0, 0);
        }

        private int calculateSubtreeLayout(ParseTree node, int depth, int startX) {
            int childCount = node.getChildCount();
            int totalWidth = 0;

            if (childCount == 0) {
                // Leaf node
                int x = startX;
                int y = depth * (NODE_HEIGHT + VERTICAL_SPACING) + 20;
                nodeLayouts.add(new TreeNodeLayout(node, x, y, NODE_WIDTH, NODE_HEIGHT));
                return NODE_WIDTH + HORIZONTAL_SPACING;
            }

            // Internal node - calculate children first
            int currentX = startX;
            for (int i = 0; i < childCount; i++) {
                int childWidth = calculateSubtreeLayout(node.getChild(i), depth + 1, currentX);
                currentX += childWidth;
                totalWidth += childWidth;
            }

            // Center this node above its children
            int firstChildX = nodeLayouts.stream()
                    .filter(l -> l.node.getParent() == node)
                    .mapToInt(l -> l.x)
                    .min()
                    .orElse(startX);

            int lastChildX = nodeLayouts.stream()
                    .filter(l -> l.node.getParent() == node)
                    .mapToInt(l -> l.x + l.width)
                    .max()
                    .orElse(startX + NODE_WIDTH);

            int x = (firstChildX + lastChildX) / 2 - NODE_WIDTH / 2;
            int y = depth * (NODE_HEIGHT + VERTICAL_SPACING) + 20;

            nodeLayouts.add(new TreeNodeLayout(node, x, y, NODE_WIDTH, NODE_HEIGHT));

            return Math.max(totalWidth, NODE_WIDTH + HORIZONTAL_SPACING);
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

                    JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, ParseTreeCanvas.this);
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
            for (TreeNodeLayout layout : nodeLayouts) {
                drawNode(g2d, layout);
            }
        }

        private void drawConnections(Graphics2D g2d) {
            g2d.setColor(Color.GRAY);
            g2d.setStroke(new BasicStroke(1.5f));

            for (TreeNodeLayout layout : nodeLayouts) {
                ParseTree node = layout.node;
                if (node.getChildCount() > 0) {
                    Point parentCenter = new Point(
                            layout.x + layout.width / 2,
                            layout.y + layout.height
                    );

                    for (int i = 0; i < node.getChildCount(); i++) {
                        ParseTree child = node.getChild(i);
                        TreeNodeLayout childLayout = findLayout(child);
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

        private void drawNode(Graphics2D g2d, TreeNodeLayout layout) {
            ParseTree node = layout.node;

            // Determine color based on node type
            Color fillColor;
            if (node instanceof TerminalNode) {
                fillColor = TERMINAL_COLOR;
            } else {
                fillColor = RULE_CONTEXT_COLOR;
            }

            // Draw rounded rectangle
            g2d.setColor(fillColor);
            g2d.fillRoundRect(layout.x, layout.y, layout.width, layout.height, 8, 8);

            // Draw border
            g2d.setColor(BORDER_COLOR);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(layout.x, layout.y, layout.width, layout.height, 8, 8);

            // Draw text
            g2d.setColor(TEXT_COLOR);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));

            String nodeText = getNodeText(node, parser);
            String truncatedText = truncateText(nodeText, layout.width - 10);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(truncatedText);
            int textX = layout.x + (layout.width - textWidth) / 2;
            int textY = layout.y + (layout.height + fm.getAscent()) / 2 - 2;

            g2d.drawString(truncatedText, textX, textY);
        }

        private String truncateText(String text, int maxWidth) {
            FontMetrics fm = getFontMetrics(new Font("SansSerif", Font.PLAIN, 11));
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

        private TreeNodeLayout findLayout(ParseTree node) {
            return nodeLayouts.stream()
                    .filter(l -> l.node == node)
                    .findFirst()
                    .orElse(null);
        }

        private static class TreeNodeLayout {
            final ParseTree node;
            final int x;
            final int y;
            final int width;
            final int height;

            TreeNodeLayout(ParseTree node, int x, int y, int width, int height) {
                this.node = node;
                this.x = x;
                this.y = y;
                this.width = width;
                this.height = height;
            }
        }
    }
}
