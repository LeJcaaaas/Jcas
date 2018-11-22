package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

import java.util.ArrayList;

// Génération de code assembleur a partir d'un arbre décoré Jcas


class Generation
{
	private ArrayList<String> idfs = new ArrayList<String>();
		
	static Prog coder(Arbre a) // - Transcrit l'arbre en instructions pour la machine abstraite
	{
		Prog.ajouterGrosComment(" JCAS Compilator ");
      
		Generation g = new Generation();

		// -- 1. Analyse des declarations
		g.coder_LISTE_DECL(a.getFils1());

		// -- 2. Transcription d'instrucs
		g.coder_LISTE_INST(a.getFils2());

		// --- 3. Fin du programme
		Inst inst = Inst.creation0(Operation.HALT);
		Prog.ajouter(inst);
      
		return Prog.instance(); 
	}
   
// --- 1. Initialisation de la machine (partie declaration)

	private void coder_LISTE_DECL(Arbre a) // - Transcription d'une liste de déclaration
	{
		switch (a.getNoeud())
		{
			case Vide: break; // - Fin de la liste de déclaration
			
			case ListeDecl:
				coder_LISTE_DECL(a.getFils1()); // - Permet de passer à la déclaration suivante
				coder_DECL(a.getFils2()); // - Transcrit la déclaration en code pour la machine abstraite
				break;

			default: break;
		}
	}
	
	private void coder_DECL(Arbre a) // - Transcription d'une déclaration
	{		
		int n = coder_LISTE_IDF(a.getFils1()); // - Une déclaration est constituée d'une liste d'IDF
		Operande nb_vars = Operande.creationOpEntier(n); // - Et d'un type
		Prog.ajouter(Inst.creation1(Operation.ADDSP,nb_vars)); // - Ajoute la déclaration au programme
	}
	
	private int coder_LISTE_IDF(Arbre a) // - Transcription d'une liste d'IDF
	{
		switch (a.getNoeud())
		{
			case Vide: return 0; // - Fin de la liste d'IDF
				
			case ListeIdent:
				int n = coder_LISTE_IDF(a.getFils1()); // - Permet de passer à l'IDF suivant
				String s = a.getFils2().getChaine(); // - Transcrit l'IDF en chaîne pour la machine abstraite
				idfs.add(s); // - Ajoute l'IDF au programme
				return (n+1);
		}
		
		return 0;
	}
	
// --- 2. Operations sur la machine (partie instruction)

	private void coder_LISTE_INST(Arbre a) // - Transcription d'une liste d'instruction
	{
		switch (a.getNoeud())
		{
			case Vide: break; // - Fin de la liste d'instruction
			
			case ListeInst:
				coder_LISTE_INST(a.getFils1()); // - Permet de passer à l'instruction suivante
				coder_INST(a.getFils2()); // - Transcrit l'instruction en code pour la machine abstraite
				break;

			default: break;
		}
	}
	
	private void coder_INST(Arbre a)
	{
		switch (a.getNoeud())
		{
	   		case TantQue:	coder_WHILE(a);	break;
	   		case Pour:	coder_FOR(a);	break;
			case Si:	coder_IF(a);	break;

	   		case Ecriture:	coder_WRITE(a);	break;
	   		case Lecture:	coder_READ(a);	break;

	   		case Affect:	coder_AFFECT(a); break;
	   		
	   		case Ligne:	coder_NLINE(a);	break;
	   		case Nop:	coder_NOP(a);	break;
	   		
	   		default: break;
		}
	}
	
	private void coder_WHILE(Arbre a) // - Boucle WHILE
	{
		int p = a.getNumLigne(); // - Identifie le numéro de ligne de la boucle WHILE
		Prog.ajouterComment(" WHILE ligne "+p+" ");
		Etiq dedans = Etiq.nouvelle("while.l1"); // - Permet d'identifier l'intérieur de la boucle
		Etiq dehors = Etiq.nouvelle("while.l2"); // - Permet de sortir de la boucle
		
		// -- Definition des instructions a ajouter

		Inst pop = Inst.creation1(Operation.POP,Operande.R0); // - On place la variable à comparer dans R0
		
		Inst cmp = Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.R0); // - On compare R0 à 1 pour déterminer si le test est vrai

		Inst bne = Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(dehors)); // - Si il est faux, on va à l'étiquette DEHORS, i.e. on sort de la boucle
		
		Inst bra = Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(dedans)); // - Sinon on va à l'étiquette DEDANS, i.e. on refait un tour de boucle

		// -- Ajout des instructions assembleur
		
		Prog.ajouter(dedans); // - Etiquette DEDANS

		coder_EXP(a.getFils1()); // - On détermine la valeur de l'expression pour réaliser le test
		
		Prog.ajouter(pop); // - On met le résultat de l'expression dans un registre
		Prog.ajouter(cmp); // - Et on le compare à 1
		Prog.ajouter(bne); // - Si le test est faux, on sort
		
		coder_LISTE_INST(a.getFils2()); // - Sinon on exécute les instructions
		
		Prog.ajouter(bra); // - Et on refait le test.
		Prog.ajouter(dehors); // - Etiquette DEHORS
		
		
	}
	private void coder_FOR(Arbre a) 	{}

	private void coder_IF(Arbre a) // - Instruction IF
	{
		int p = a.getNumLigne(); // - Identifie le numéro de ligne de l'instruction IF
		Prog.ajouterComment(" IF ligne "+p+" ");
		
		Etiq ok = Etiq.nouvelle("if.l1"); // - Permet d'identifier le début des instructions si le test est VRAI
		Etiq ko = Etiq.nouvelle("if.l2"); // - Permet d'identifier le début des instructions si le test est FAUX
		Etiq vu = Etiq.nouvelle("if.l3"); // - Permet d'identifier la fin de l'instruction IF

		// -- Definition des instructions a ajouter 
		
		Inst pop = Inst.creation1(Operation.POP,Operande.R0); // - On place la variable à comparer dans R0
		
		Inst cmp = Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.R0); // - On compare R0 à 1 afin de déterminer si le test est vrai

		Inst bne = Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(ko)); // - Si il est faux, on va à l'étiquette KO, i.e. on exécute les instructions présentes dans ELSE
		
		Inst bra = Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(vu)); // - Sinon, on exécute les instructions du IF, et on va à l'étiquette VU, i.e. on sort du test
		// -- Ajout des instructions assembleur

		coder_EXP(a.getFils1()); // - On détermine la valeur de l'expression pour réaliser le test		

		Prog.ajouter(pop); // - On met le résultat de l'expression dans un registre
		Prog.ajouter(cmp); // - Et on le compare à 1
		Prog.ajouter(bne); // - Si le test est faux, on passe au ELSE
		Prog.ajouter(ok); // - Etiquette OK (non utilisée, permet de baliser le début des instructions si le test est vrai)

		coder_LISTE_INST(a.getFils2()); // - Instructions IF
		
		Prog.ajouter(bra); // - On va à la fin du programme pour ne pas exécuter les instructions ELSE
		Prog.ajouter(ko); // - Etiquette KO
				
		coder_LISTE_INST(a.getFils3()); // - Instructions ELSE
		
		Prog.ajouter(vu); // - Etiquette VU
	}
	
	private void coder_AFFECT(Arbre a) // - Instruction AFFECT
	{
		int p = a.getNumLigne(); // - Identifie le début de l'instruction AFFECT
		Prog.ajouterComment(" AFFECT ligne "+p+" ");
		
		int n = coder_PLACE(a.getFils1()); // - Récupère l'emplacement mémoire de la variable que l'on affecte
		coder_EXP(a.getFils2()); // On évalue l'expression et on la place dans la pile
		
		Inst pop = Inst.creation1(Operation.POP,Operande.R0); // On place le résultat de l'expression à affecter dans R0
		
		Inst move = Inst.creation2 
		(
			Operation.STORE,
			Operande.R0,
			Operande.creationOpIndirect(n, Registre.GB)
		); // - On stocke le résultat dans l'espace mémoire de la variable que l'on affecte
		
		Prog.ajouter(pop);
		Prog.ajouter(move);
	}
	
	private void coder_READ(Arbre a) // - Instruction READ
	{
		int p = a.getNumLigne(); // - Identifie le début de l'instruction READ
		Prog.ajouterComment(" READ ligne "+p+" ");
		
		NatureType natureExp = a.getFils1()
		.getDecor().getType().getNature(); // - Récupère les informations sur la variable que l'on affecte
		
		int n = coder_PLACE(a.getFils1()); // - Récupère l'emplacement mémoire de la variable que l'on affecte
		
		if (n < 0) return;
		
		Inst read;
		Inst move;
		
		switch (natureExp)
		{
			case Interval:
			read = Inst.creation0(Operation.RINT); // - Si la variable à affecter est un entier
			break;
			
			case Real:
			read = Inst.creation0(Operation.RFLOAT); // - Si la variable à affecter est un float 
			break;
			
			default: return;
		}
		
		move = Inst.creation2(Operation.STORE,Operande.R1,
		Operande.creationOpIndirect(n,Registre.GB)); // - Stocke la valeur à affecter à l'emplacement mémoire de la variable que l'on affecte
			
		Prog.ajouter(read);
		Prog.ajouter(move);
	}
	
	private void coder_WRITE(Arbre a) // - Instruction WRITE
	{
		int p = a.getNumLigne(); // - Identifie le début de l'instruction WRITE
		Prog.ajouterComment(" WRITE ligne "+p+" ");
		
		coder_LISTE_EXP(a.getFils1()); // - Appel à coder_LISTE_EXP
	}
	
	private void coder_LISTE_EXP(Arbre a) // - Permet, via la méthode coder_SHOW, d'afficher la liste d'expressions dans WRITE
	{
		switch (a.getNoeud())
		{
			case Vide: break; // - Fin de la liste d'expressions
			
			case ListeExp:
				coder_LISTE_EXP(a.getFils1()); // - Permet de lire l'expression suivante
				coder_SHOW(a.getFils2()); // - Affiche l'expression courante
				break;

			default: break;
		}
	}
	
	private void coder_SHOW(Arbre a) // - Affiche une variable
	{
		NatureType natureExp = a.getDecor().
		getType().getNature();
			
		switch (natureExp)
		{
			case String:
			String s = a.getChaine();
			Prog.ajouter(Inst.creation1(Operation.WSTR,
			Operande.creationOpChaine(s)));
			break;
				
			case Real:
			coder_EXP(a);
			Prog.ajouter(Inst.creation1
			(Operation.POP,Operande.R1));
			Prog.ajouter(Inst.creation0
			(Operation.WFLOAT));
			break;
				
			case Interval:
			coder_EXP(a);
			Prog.ajouter(Inst.creation1
			(Operation.POP,Operande.R1));
			Prog.ajouter(Inst.creation0
			(Operation.WINT));
			break;		
		}
	}
	
	private int coder_PLACE(Arbre a) // - Permet de récupérer l'emplacement mémoire d'une variable
	{
		String s = a.getChaine();
		return idfs.indexOf(s)+1;
	}
	
	private void coder_OP_BINARY(Arbre a, Operation op) // - Instruction OP_BINARY
	{
		Inst e1,e2;
		Operande x,y;

		x = Operande.opDirect(Registre.R2); // - On place les opérandes dans les registres
		y = Operande.opDirect(Registre.R3);

		coder_EXP(a.getFils1()); // - On évalue la première expression
		e1 = Inst.creation1(Operation.POP,x);
		Prog.ajouter(e1);
			
		coder_EXP(a.getFils2()); // - Idem pour la deuxième expression
		e2 = Inst.creation1(Operation.POP,y);
		Prog.ajouter(e2);
			
		e1 = Inst.creation2(op,y,x); // - On fait l'opération prévue entre les deux empressions
		e2 = Inst.creation1(Operation.PUSH,x); // - Et on push le résultat en pile
			
		Prog.ajouter(e1);
		Prog.ajouter(e2);
	}
	
	private void coder_OP_FEUILLE(Arbre a) // - Transcrit les feuilles en code pour la machine abstraite
	{
		Inst e1,e2;
		Operande x,y;
		
		switch(a.getNoeud()) // - 3 types de feuilles
		{
			case Ident: // - Feuille de type IDENTIFICATEUR
			int n = coder_PLACE(a);
			x = Operande.creationOpIndirect(n,Registre.GB);
			break;
			
			case Entier: // - Feuille de type ENTIER
			x = Operande.creationOpEntier(a.getEntier());
			break;

			case Reel: // - Feuille de type FLOTTANT
			x = Operande.creationOpReel(a.getReel());
			break;
			
			default: return;
		}
		
		y = Operande.opDirect(Registre.R0);
		
		e1 = Inst.creation2(Operation.LOAD,x,y);
		e2 = Inst.creation1(Operation.PUSH,y);
		Prog.ajouter(e1);
		Prog.ajouter(e2);
	}
	
	private void coder_OP_BRANCH(Arbre a, Operation op) // Opération BRANCH
	{
		Inst e1,e2,e3;
		Operande x,y;

		x = Operande.opDirect(Registre.R2);
		y = Operande.opDirect(Registre.R3);

		coder_EXP(a.getFils1());
		e1 = Inst.creation1(Operation.POP,x);
		Prog.ajouter(e1);
			
		coder_EXP(a.getFils2());
		e2 = Inst.creation1(Operation.POP,y);
		e3 = Inst.creation2(Operation.CMP,y,x);

		Prog.ajouter(e2);
		Prog.ajouter(e3);
			
		e1 = Inst.creation1(op,Operande.R0);
		e2 = Inst.creation1(Operation.PUSH,Operande.R0);
			
		Prog.ajouter(e1);
		Prog.ajouter(e2);
	} 
	
	private void coder_EXP(Arbre a)
	{
		switch (a.getNoeud())
		{
		
		// -- Operateurs logiques
		case Et:	break;
		case Ou: 	break;
		case Non:	break;
		
		// -- Operateurs conditionnels
		case Egal:    coder_OP_BRANCH(a,Operation.SEQ); break;
		case NonEgal: coder_OP_BRANCH(a,Operation.SNE); break;
		case InfEgal: coder_OP_BRANCH(a,Operation.SLE);	break;
		case SupEgal: coder_OP_BRANCH(a,Operation.SGE); break;
		case Inf:     coder_OP_BRANCH(a,Operation.SLT); break;
		case Sup:     coder_OP_BRANCH(a,Operation.SGT); break;
		
		// -- Operateurs unaires
		case PlusUnaire:	break;
		case MoinsUnaire:	break;
		
		// -- Currently unsupported
		case Index:	break;

		// -- Operations binaires
		case Quotient:
		case DivReel: coder_OP_BINARY(a,Operation.DIV);	break;
		case Reste:   coder_OP_BINARY(a,Operation.MOD);	break;
		case Mult:    coder_OP_BINARY(a,Operation.MUL);	break;
		case Moins:   coder_OP_BINARY(a,Operation.SUB);	break;
		case Plus:    coder_OP_BINARY(a,Operation.ADD);	break;
		
		// -- Operations en feuille 
		case Ident:
		case Entier:
		case Reel:	coder_OP_FEUILLE(a); break;
		}
	}
	
	private void coder_NLINE(Arbre a) // Nouvelle ligne
	{
		Prog.ajouter(Inst.creation0(Operation.WNL));
	}
	
	private void coder_NOP(Arbre a) {} // NOP
}



