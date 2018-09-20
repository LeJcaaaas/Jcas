package lejcaaas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * public String characGenerator(String array): type, variable, affect, word en paramètre.
 * 
 * varLineBeautify(String variable, String type) : Renvoit la ligne correspond aux variables.
 * 
 * String varBeautify(int nb): nb de ligne qui dois apparaitre dans la partie variable et renvoit tout le texte.
 * 
 * int indiceGenerator(String array): genere un nombre aléatoire suivant le type de données.
 * 
 * String commentGenerator() renvoit un commentaire ou saut de ligne.
 */
	

public class Generator {

	private final String eol = " ;";
	private final String tab = "\t";
	private String program = "program\n";
	private String start = "begin \n";
	private String interm = "\tnew_line ;\n";
	private String end = "end.\n";
	private ArrayList<String> varUsed = new ArrayList<String>();
	private ArrayList<String> typeVar = new ArrayList<String>();
	
	private static boolean typeProg;
	private static int nbProg;
	
	
	public static void main(String[] args) throws IOException {
		Generator lejcas = new Generator();
		
		System.out.println(" Bienvenue sur le générateur de code JCas");
		@SuppressWarnings("resource")
		
		Scanner sc = new Scanner(System.in);
		System.out.println("Combien de fichiers de tests voulez vous?");
		String str = sc.nextLine();
		nbProg = Integer.valueOf(str);
		System.out.println("Voulez-vous un programme faux ou juste?");
		String res = sc.nextLine();
	
		if (res.equals("juste")) {
			typeProg = true;
		}
		else {
			typeProg = false;
			System.out.println("Programme faux");
		}
		
		lejcas.Generate();
		
	}
	
	public void Generate() throws IOException {
		String progfinal = program;
		String nomProg = "a.cas"; // Si erreur
		int i=0;
		
		if( typeProg == true) {
			
			while( i < nbProg) {
			
				progfinal = progfinal.concat(varBeautify(indiceGenerator("variable")));
				progfinal = progfinal.concat(bodyGenerator());
				nomProg = stringGenerator(20);
				File fichier = new File(nomProg);

				byte[] buf = progfinal.getBytes();
				FileOutputStream out = new FileOutputStream(fichier);
				out.write(buf);
				System.out.println("Ecriture réussie"); 
				out.close();
				i = i+1;
				progfinal = program;
			}
			
		}else {
			
			while( i < nbProg) {
				
				String var = varBeautify(1);
				String body = bodyGenerator();
				String str = stringGenerator(1);
				String comment = "-".concat(commentGenerator());
				String texte;
				ArrayList<String> faux = new ArrayList<String>();
				faux.add(var);
				faux.add(body);
				faux.add(str);
				faux.add(comment);
				
				texte = faux.get((int)Math.random()*4);
				
				progfinal = progfinal.concat(varBeautify(indiceGenerator("variable")));
				progfinal = progfinal.concat(bodyGenerator());
				int j = (int)( Math.random()*progfinal.length());
				
				progfinal = progfinal.substring(0,j) + texte + progfinal.substring(j);
				nomProg = stringGenerator(20);
				File fichier = new File(nomProg);

				byte[] buf = progfinal.getBytes();
				FileOutputStream out = new FileOutputStream(fichier);
				out.write(buf);
				System.out.println(progfinal);
				System.out.println("Ecriture réussie"); 
				out.close();
				i = i+1;
				progfinal = program;
			}
	
		}
	}
	
	public String stringGenerator(int n) {
		
		StringBuilder sb = new StringBuilder();
		StringBuilder res = new StringBuilder();
		
		sb.append("abcdefghijklmnopqrstuvwxyz");
		
		for(int i=0; i<n; i++) {
			
			int j = (int)(Math.random()*n);
			res.append(sb.charAt(j));
			
		}
		res.append(".cas");
		return res.toString();
		
		
	}
	public ArrayList<String> wordGenerator(){
		ArrayList<String> mot_cle = new ArrayList<String>();
		mot_cle.add("write");
		mot_cle.add("read");
		mot_cle.add("while");
		mot_cle.add("if");
		//mot_cle.add("else");
		return mot_cle;
	}
	
	public ArrayList<String> typeGenerator(){
		ArrayList<String> mot_cle = new ArrayList<String>();
		mot_cle.add("integer");
		mot_cle.add("boolean");
		mot_cle.add("1 .. 20");
		// array en attente
		
		return mot_cle;
	}
	
	public ArrayList<String> affectGenerator(){
		ArrayList<String> mot_cle = new ArrayList<String>();
		mot_cle.add("+");
		mot_cle.add("-");
		mot_cle.add("*");
		mot_cle.add("/");
		
		
		return mot_cle;
	}
	
	public ArrayList<String> variableGenerator(){
		ArrayList<String> mot_cle = new ArrayList<String>();
		mot_cle.add("a");
		mot_cle.add("b");
		mot_cle.add("c");
		mot_cle.add("e");
		mot_cle.add("f");
		mot_cle.add("g");
		mot_cle.add("h");
		mot_cle.add("i");
		mot_cle.add("j");
		return mot_cle;
	}
	
	public String compareGenerator(){
		ArrayList<String> compare = new ArrayList<String>();
		String op;
		
		compare.add("<=");
		compare.add(">=");
		compare.add("==");
		
		op = compare.get( (int) (Math.random()*3));
		return op;
	}
	
	
	public int indiceGenerator(String array) {
		int max = 0;
		int min = 0;
		
		switch(array) {
			case "type":
				max = 3;  // WTF
				break;
			case "affect":
				max = 3;
				break;
			case "word":
				max = 3;
				break;
			case "variable":
				max = 9;
				break;
			default:
			//	System.out.println("array doit être type, affect ou word. Ici: " + array);
				System.exit(-1);
			
				
		}
		
		return ((int)(Math.random()*(max-min)) + min);
	}
	
	public String characGenerator(String array) {
		
		ArrayList<String> result = new ArrayList<String>();
		int indice = 0;
		
		switch(array){
			case "type":
				result = typeGenerator();
				break;
			case "variable":
				result = variableGenerator();
				break;
			case "word":
				result = wordGenerator();
				break;
			case "affect":
				result = affectGenerator();
				break;
			default:
			//	System.out.println("type, variable, word, affect seulement");
				break;
		}
		indice = indiceGenerator(array);
		
		return result.get(indice);
	}
	
	public String varLineBeautify(String variable, String type) {
		String var = characGenerator(variable);
		String ty = characGenerator(type);
		varUsed.add(var);
		typeVar.add(ty);
		String line = tab.concat(var).concat(" : ").concat(ty).concat(eol).concat("\n");
			
		return line;
	}
	
	public String varBeautify(int nb) {
		String texte = "";
		if (nb < 9 ) {
			for(int i = 0; i< nb; i++) {
				texte = texte.concat(varLineBeautify("variable","type"));
			}
		}
		else {
			System.err.println("Nombre trop grand varBeautify");
			System.exit(-1);
		}
		texte = texte.concat("\n").concat(start);
		return texte;
	}
	
	public String bodyVarBeautify(String variable, String type) {
		// Affection d'une variable dans le body
		String text = null;
		if( type == null) {
			
			text = tab.concat(variable).concat(" := ").concat(String.valueOf((int)(Math.random()*150))).concat(eol).concat("\n");
			
		}
		else {
			text=  tab.concat(variable).concat(" := ").concat(type).concat(eol).concat("\n");
		}
		return text;
		
	}
	
	public String commentGenerator() {
		String text;
		int i = (int)(Math.random()*2);
	
		if( i >= 1) {
			text = "\n\t-- Hello I am a comment\n";
		}
		else {
			text = "\n";
		}
		
		return text;
		
	}
	
	public String bodyGenerator() {
		String rep = commentGenerator();
		rep = rep.concat(motSemantique(characGenerator("word")));
		
		return rep;
	}
	
	public String motSemantique(String mot) {
		
		String text = null;
		int i;
		
		String var1=null, var2=null;
		//mot = mot.trim();
		
	
		if( mot == "write") {
			text = tab.concat(mot).concat("(\"Hello I am an input\")");
			text = text.concat(eol).concat("\n");
		}
		else if( mot == "read"  && varUsed.size() != 0) {
			i = typeVar.indexOf("integer");
			if( i == -1) {
				i = typeVar.indexOf("boolean");
				if( i == -1) {
					i=0;
				}
			}
			String var = varUsed.get(i);
			text = tab.concat(mot).concat(("(" + var + ") ;")).concat("\n");
		}
		
		else if( mot == "while" && varUsed.size() != 0) {
			i = typeVar.indexOf("boolean");
			text = tab.concat(mot).concat("(");
			// Generation de la condition
			if( i == -1) {
				i = 1;
				//System.out.println("test i == -1");
				for(int j=0; j<typeVar.size(); j++) {
					if ( (typeVar.get(j).equals("integer") || typeVar.get(j).equals("1 .. 20")) && i<=2 && typeVar.size() > 1) {
						
					
					//	System.out.println(" condition ok et text: " + text);
						if( i == 2) {
							var2 = varUsed.get(j);
					//		System.out.println("text ! " + text);
							text = text.concat(var2).concat(")").concat(tab).concat("do").concat("\n");
							break; //Permet de réaliser une fois le var <= var2
						}
						text = text.concat(varUsed.get(j));
						var1 = varUsed.get(j);
						text = text.concat(compareGenerator());
						i = 2;
					}
					else {
						text = text.concat(varUsed.get(0)); // Présence d'une seule variable 
						text = text.concat(compareGenerator()).concat(String.valueOf((int)(Math.random()*1500))).concat(")").concat(tab).concat("do").concat("\n");
					}
					
				}
				
			}
			else { // cas boolean (var)
				var1 = varUsed.get(i);
				//System.out.println("L282 i != -1");
				text = tab.concat(mot).concat("(").concat(var1).concat(")").concat(tab).concat("do").concat("\n");
				
			}
			//Génération du body
			if( var2 == null && var1 != null) {
				// boolean
				
				text = text.concat(tab).concat(bodyVarBeautify(var1,"False"));
			//	System.out.println("L266");
			}
			else {
				if( var2 != null) {
					text = text.concat(tab).concat(bodyVarBeautify(var2,null));
					//System.out.println("L271");
				}
				else if (var1 != null){
					//text = text.concat(tab).concat(bodyVarBeautify(var1,null));
					text = text.concat(tab).concat(bodyVarBeautify(var1,null));
				//	System.out.println("L276");

				}
				else {
					//text = text.concat(tab).concat(bodyVarBeautify(varUsed.get(0),null));
					text = text.concat(tab).concat(bodyVarBeautify(varUsed.get(0),null));
					//System.out.println("L282");
				
				}
			}
			text = text.concat(tab).concat("end ;").concat("\n");
			
		}
		else if( mot == "if" && varUsed.size() != 0) {
			i = typeVar.indexOf("boolean");
			text = "(";
			// Generation de la condition
			if( i == -1) {
				i = 1;
				for(int j=0; j<typeVar.size(); j++) {
					if ( typeVar.get(j).equals("integer") && i<2) {
						i = 2;
						text = text.concat(varUsed.get(j));
						if( i == 2) {
							var2 = varUsed.get(j);

							break; //Permet de réaliser une fois le var <= var2
						}
						var1 = varUsed.get(j);
						text = text.concat(compareGenerator());
					}
				}
			}
			else { // cas boolean (var)
				var1 = varUsed.get(i);
				text = text.concat(var1).concat(")").concat("\n");
				
			}
			//Génération du body
			if( var2 == null) {
				// boolean
				text = text.concat(bodyVarBeautify(var1,"False"));
			}
			else {
				text = text.concat(bodyVarBeautify(var1,null));
			}
			text = text.concat(tab).concat("end ;").concat("\n");
			
		}
		else {
			text = commentGenerator();
			text = text.concat(tab).concat("null ;").concat("\n");
			
		}
		text = text.concat(interm).concat(end);
		return text;
	}
}
