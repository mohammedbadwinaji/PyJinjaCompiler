package compiler.printer;

import compiler.ast.common.AstNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ASTPrinter {
    public static void printText(AstNode root) {
        if (root == null) {
            System.out.println("AST is null");
            return;
        }
        System.out.println(root.prettyPrint(""));
    }
}