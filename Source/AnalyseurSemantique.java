package object.primary;

import java.util.Stack;
import java.util.HashMap;

import object.secondary.Node;
import object.secondary.Symbol;

public class AnalyseurSemantique {

    private static int nbVariables = 0;
    private static boolean error = false;

    private static Stack<HashMap<String, Symbol>> stack = new Stack<HashMap<String, Symbol>>();

    public AnalyseurSemantique() {
    }

    public static int getNbVariables() {
        return nbVariables;
    }

    public static boolean isError() {
        return error;
    }

    public static void openBlock() {
        HashMap<String, Symbol> block = new HashMap<String, Symbol>();
        stack.push(block);
    }

    public static void closeBlock() {
        stack.pop();
    }

    public static Symbol declare(Node n) {
        Symbol s = new Symbol(n.getLine(), n.getColumn());
        String name = n.getName();
        /*System.out.println("declare => name "+ name+ ": " + n.getName()+ " :"+n);*/
        HashMap<String, Symbol> block = stack.peek();
        if (block.containsKey(name)) {
            System.out.println("Erreur la variable \"" + name + "\" est déjà déclaré dans ce bloc !");
            System.out.println("( Ligne " + n.getLine() + ", Colonne " + n.getColumn() + " )\n");
            error = true;
        } else {
            block.put(name, s);
        }
        return s;
    }

    public static Symbol search(Node n) {
        Symbol symbole = new Symbol();
        Stack<HashMap<String, Symbol>> s = new Stack<HashMap<String, Symbol>>();
        String varIdSymbol = n.getName();

        while (!stack.empty()) {
            s.push(stack.pop());
            HashMap<String, Symbol> block = s.peek();
            if (block.containsKey(varIdSymbol)) {
                //réempiler la pile
                while (!s.empty()) {
                    stack.push(s.pop());
                }
                return block.get(varIdSymbol);
            }
        }

        //If symbol not found, re-stack
        while (!s.empty()) {
            stack.push(s.pop());
        }
        System.out.println("Erreur le symbole \"" + varIdSymbol + "\" n'a pas été trouvé !");
        System.out.println("( Ligne " + n.getLine() + ", Colonne " + n.getColumn() + " )\n");
        error = true;
        return symbole;
    }

    public static void nodeAnalyse(Node n) {
        Symbol s = new Symbol();
        switch (n.getType()) {
            case "node_block":
                openBlock();
                for (Node child : n.getChildList()) {
                    nodeAnalyse(child);
                }
                closeBlock();
                break;
            case "node_declaration":
                s = declare(n.getChild(0));
                s.setType("variable");
                s.setSlot(nbVariables);
                nbVariables++;
                break;
            case "node_var":
                s = search(n);
                if (s.getType() != "variable") {
                    System.out.println("Erreur, la variable \"" + n.getName() + "\" n'a pas encore été déclarée !");
                    System.out.println("( Ligne " + n.getLine() + ", Colonne " + n.getColumn() + " )\n");
                    error = true;
                }
                n.setSlot(s.getSlot());
                break;
            default:
                for (Node child : n.getChildList()) {
                    nodeAnalyse(child);
                }
                break;
        }
    }
}