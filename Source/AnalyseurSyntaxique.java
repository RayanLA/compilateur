package object.primary;

import java.util.ArrayList;
import java.util.HashMap;

import object.secondary.Node;
import object.secondary.Operator;
import object.secondary.Token;

public class AnalyseurSyntaxique {
	private AnalyseurLexical analyseurLexical;
	private Node principalNode;
	private HashMap<String, Operator> nodeOperators = new HashMap<String, Operator>();
	private boolean error = false;

	public AnalyseurSyntaxique(AnalyseurLexical analyseurLexical) {
		this.analyseurLexical = analyseurLexical;

		//Remplissage des op�rateurs
		nodeOperators.put("tok_power", new Operator("node_power", 7, 0));

		nodeOperators.put("tok_divide", new Operator("node_divide", 6, 1));
		nodeOperators.put("tok_remaindor", new Operator("node_remaindor", 6, 1));
		nodeOperators.put("tok_multiply", new Operator("node_multiply", 6, 1));

		nodeOperators.put("tok_plus", new Operator("node_plus_binary", 5, 1));
		nodeOperators.put("tok_minus", new Operator("node_minus_binary", 5, 1));

		nodeOperators.put("tok_inf", new Operator("node_inf", 4, 1));
		nodeOperators.put("tok_sup", new Operator("node_sup", 4, 1));
		nodeOperators.put("tok_equal", new Operator("node_equal", 4, 1));
		nodeOperators.put("tok_sup_equal", new Operator("node_sup_equal", 4, 1));
		nodeOperators.put("tok_inf_equal", new Operator("node_inf_equal", 4, 1));
		nodeOperators.put("tok_different", new Operator("node_different", 4, 1));

		nodeOperators.put("tok_and", new Operator("node_and", 3, 1));

		nodeOperators.put("tok_or", new Operator("node_or", 2, 1));

		nodeOperators.put("tok_assignment", new Operator("node_assignment", 1, 0));
	}

	public boolean getError(){
		return this.error;
	}

	public void setError(boolean er){
		 this.error = er;
	}

	public Node Primaire() {
		//Renvoi un arbre contenant la constante si constante
		if(analyseurLexical.next().getType()=="tok_constant") {
			//Creation du noeud qui contiendra la valeur
			Node node = new Node("node_constant", analyseurLexical.next().getValue());
			//Token actuel trait� ! Passage au suivant.
			analyseurLexical.skip();
			return node;
		}
		//Renvoi un arbre variable si on a un token identifier
		if (analyseurLexical.next().getType() == "tok_identifier"){
			Node n = new Node("node_var", analyseurLexical.next().getName());
			analyseurLexical.skip();
			return n;
		}
		//Renvoi un arbre contenant l'ensemble des calculs mathematiques, expression boolean
		if(analyseurLexical.next().getType()=="tok_openning_parenthesis") {
			//Passage au token suivant
			analyseurLexical.skip();

			//node contient le noeud d'expression entre les parenth�ses ( )
			Node node = this.Expression(0);

			//v�rification qu'apr�s l'expression il y a une parenth�se fermante
			boolean checkValidity = analyseurLexical.accept("tok_closing_parenthesis");
			if(!checkValidity) {
				System.out.println("Erreur parenthèse fermante manquante ...  \n ( Ligne "+ analyseurLexical.next().getLine() + ", Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
//				System.exit(-1);
				return new Node();
			}
			return node;
		}
		//Renvoi un arbre contenant un moins et une expression math�matique avec au plus une exponentielle
		if(analyseurLexical.next().getType()=="tok_minus") {
			//Passage au token suivant
			analyseurLexical.skip();
			Node node = new Node("node_minus_unary");
			node.addNodeChild(this.Expression(7));
			return node;
		}
		if(analyseurLexical.next().getType()=="tok_not") {
			//Passage au token suivant
			analyseurLexical.skip();
			Node node = new Node("node_not");
			node.addNodeChild(this.Expression(7));
			return node;
		}
		if (analyseurLexical.next().getType()=="tok_unknown") {
			System.out.println("Erreur : caractère non accepté. \n ( Ligne "+ analyseurLexical.next().getLine() + " ; Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
			this.error = true;
			return new Node();
		}
		if (analyseurLexical.next().getType()=="tok_end_of_file") {
			return new Node();
		}
		System.out.println("Erreur : caractère non primaire. \n ( Ligne "+ analyseurLexical.next().getLine() + " ; Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
		this.error = true;
		return new Node();
	}

	public Operator ChercherOp(Token token) {
		if (analyseurLexical.next().getType()=="tok_unknown") {
			System.out.println("Erreur : opérateur non accepté. \n ( Ligne "+ analyseurLexical.next().getLine() + ", Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
			this.error = true;
			return null;
		}

		return this.nodeOperators.get(token.getType());
	}

	public Node Expression(int minPriority) {
		//Recup�ration de la constante
		Node A1 = this.Primaire();

		while(true) {
			Operator Op = this.ChercherOp(this.analyseurLexical.next());
			//Si l'op�rateur qui suit la constante est de priorit� inf�rieure
			//A celle recherch�e on retourne l'arbre d�j� cr��
			if(Op==null || Op.getPriority()<minPriority) {
				return A1;
			}
			
			//Sinon on continue � parcourir l'expression pour construire l'arbre
			this.analyseurLexical.skip();
			Node A2 = this.Expression(Op.getPriority()+Op.getAssociativity());

			//On cr�er le noeud op�rateur
			Node A = new Node(Op.getNodeName());
			A.addNodeChild(A1);
			A.addNodeChild(A2);

			A1=A;
		}
	}

	public Node Instruction(){

		Node N;

	    if (analyseurLexical.next().getType() == "tok_if"){
			analyseurLexical.skip();

			if (!analyseurLexical.accept("tok_openning_parenthesis")){
                System.out.println("Erreur parenthèse ouvrante manquante ...  \n ( Ligne "+ analyseurLexical.next().getLine() + ", Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
            }

                Node Nexp = Expression(0);

            if (!analyseurLexical.accept("tok_closing_parenthesis")){
                System.out.println("Erreur parenthèse fermante manquante ...  \n ( Ligne "+ analyseurLexical.next().getLine() + ", Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
            }

                Node Nins = Instruction();

            N = new Node("node_condition");
            N.addNodeChild(Nexp);
            N.addNodeChild(Nins);

            //Si suivi d'un else on le rajoute
            if (analyseurLexical.next().getType() == "tok_else"){

                analyseurLexical.skip();

                Nins = Instruction();

                N.addNodeChild(Nins);

            }
        }
	    else if (analyseurLexical.next().getType() == "tok_open_brace"){

            N = new Node("node_block");
            analyseurLexical.skip();

            while (analyseurLexical.next().getType() != "tok_close_brace"){
                N.addNodeChild(Instruction());
            }

            analyseurLexical.skip();

        }
		else if (analyseurLexical.next().getType() == "tok_debug"){

			N = new Node("node_debug");

			analyseurLexical.skip();

			N.addNodeChild(Expression(0));

			if (!analyseurLexical.accept("tok_separator")){
				System.out.println("Il manque un séparteur ';'  \n ( Ligne "+ analyseurLexical.next().getLine() + ", Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
			}

		}
	    else {

	        Node Nexp = Expression(0);
            if (!analyseurLexical.accept("tok_separator")){
            	System.out.println("Il manque un séparteur ';'  \n ( Ligne "+ analyseurLexical.next().getLine() + ", Colonne " + analyseurLexical.next().getColumn() + " : token " + analyseurLexical.next().getType() + " )\n");
			}
			N = new Node("node_expression");
			N.addNodeChild(Nexp);

        }

	    return N;

    }


	public String toString() {

		String ts = "Liste des noeuds : ";
//		for(Node aNode : this.nodeList) {
			ts += "- " + principalNode + "\n";
//		}
		return ts;
	}



}