package compiler.printer;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Printer for lexer tokens with console text and GUI visualization modes.
 */
public class TokenPrinter {

    /**
     * Streams all tokens from the lexer and prints a clean two-column console layout.
     * Columns: TOKEN and TEXT (with escaped \n, \r, \t characters).
     */
    public static void printText(Lexer lexer) {
        System.out.println("==================================================");
        System.out.printf("%-25s %-25s%n", "TOKEN", "TEXT");
        System.out.println("==================================================");

        Token token;
        do {
            token = lexer.nextToken();
            if (token.getType() == Token.EOF) {
                break;
            }

            String tokenName = lexer.getVocabulary().getSymbolicName(token.getType());
            if (tokenName == null) {
                tokenName = "UNKNOWN";
            }

            String tokenText = escapeTokenText(token.getText());

            System.out.printf("%-25s %-25s%n", tokenName, "\"" + tokenText + "\"");        } while (token.getType() != Token.EOF);
    }

    /**
     * Escapes special characters in token text for clean console output.
     */
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
     * Opens a clean Swing JTable window displaying Token Name and Text value rows visually.
     */
    public static void showGuiList(Lexer lexer) {
        JFrame frame = new JFrame("Token Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create table model with columns
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Token");
        model.addColumn("Text");

        // Populate table with tokens
        Token token;
        do {
            token = lexer.nextToken();
            if (token.getType() == Token.EOF) {
                break;
            }

            String tokenName = lexer.getVocabulary().getSymbolicName(token.getType());
            if (tokenName == null) {
                tokenName = "UNKNOWN";
            }

            String tokenText = escapeTokenText(token.getText());
            model.addRow(new Object[]{tokenName, tokenText});
        } while (token.getType() != Token.EOF);

        // Create table with model
        JTable table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(25);
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD));

        // Enable sorting
        table.setAutoCreateRowSorter(true);

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
