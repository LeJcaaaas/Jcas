package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

import java.util.ArrayList;

// Génération de code assembleur a partir d'un arbre décoré Jcas


class Generation
{
	private ArrayList<String> idfs = new ArrayList<String>();
		
	// - Transcrit l'arbre en instructions pour la machine abstraite
	static Prog coder(Arbre a)
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

	// - Transcription d'une liste de déclaration
	private void coder_LISTE_DECL(Arbre a)
	{
		switch (a.getNoeud())
		{
			// - Fin de la liste de déclaration
			case Vide: break;
			
			case ListeDecl:
			// - Permet de passer à la déclaration suivante
				coder_LISTE_DECL(a.getFils1());
				coder_DECL(a.getFils2());
				break;

			default: break;
		}
	}
	
	// - Transcription d'une déclaration
	private void coder_DECL(Arbre a)
	{
		ArrayList<String> ids = coder_LISTE_IDF(a.getFils1());
		int size = coder_TYPE(a.getFils2());
		
		String idname = new String();
		
		for (int i=0; i<ids.size(); i++)
		{
			idname = ids.get(i);
			
			if (size == 1) idfs.add(idname); 

			else
			for(int j=0; j<size; j++)
			idfs.add(idname+"."+j);
		}
		
		size = size * ids.size();
		
		Operande alloc = Operande.creationOpEntier(size);
		Prog.ajouter(Inst.creation1(Operation.ADDSP,alloc));
	}
	
	private ArrayList<String> coder_LISTE_IDF(Arbre a)
	{
		ArrayList<String> ls = new ArrayList<String>();
		
		switch (a.getNoeud())
		{
			case Vide: return ls;
				
			case ListeIdent:
				ls = coder_LISTE_IDF(a.getFils1());
				ls.add(a.getFils2().getChaine());
				return (ls);
		}
		
		return ls;
	}
	
	private int coder_TYPE(Arbre a)
	{
		int x,y;
	
		switch (a.getNoeud())
		{
			case Tableau :
			x = coder_TYPE(a.getFils1());
			y = coder_TYPE(a.getFils2());
			return (x*y);
			
			case Intervalle :
			x = a.getFils1().getEntier();
			y = a.getFils2().getEntier();
			
			if (x == y)	return 0;
			else		return Math.abs(y-x)+1;
			
			case Ident :	return(1);

			default: a.afficher(1);
			return(42);
		}
	}
	
// --- 2. Operations sur la machine (partie instruction)

	// - Transcription d'une liste d'instruction
	private void coder_LISTE_INST(Arbre a)
	{
		switch (a.getNoeud())
		{
			// - Fin de la liste d'instructions
			case Vide: break;
			
			case ListeInst:
				// - Passage à l'instruction suivante
				coder_LISTE_INST(a.getFils1());
				coder_INST(a.getFils2());
				// - Codage de chaque instruction
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
	
	// - Boucle WHILE
	private void coder_WHILE(Arbre a)
	{
		int p = a.getNumLigne();
		// - Identifie le numéro de ligne de la boucle WHILE
		Prog.ajouterComment(" WHILE ligne "+p+" ");
		// - Permet d'identifier l'intérieur de la boucle
		Etiq dedans = Etiq.nouvelle("while.l1");
		// - Permet de sortir de la boucle
		Etiq dehors = Etiq.nouvelle("while.l2");
		
		// -- Definition des instructions a ajouter

		Inst pop = Inst.creation1(Operation.POP,Operande.R0); 
		// - On place la variable à comparer dans R0
		
		// - On compare R0 à 1 pour vérifier le test
		Inst cmp = Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.R0); 

		// - Si il est faux, on va à l'étiquette DEHORS
		// (on sort de la boucle)
		Inst bne = Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(dehors));
		
		// - Sinon on reste DEDANS
		Inst bra = Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(dedans));

		// -- Ajout des instructions assembleur
		
		Prog.ajouter(dedans); // - Etiquette DEDANS

		// - Expression a tester
		coder_EXP(a.getFils1());
		
		Prog.ajouter(pop); // - POP du resultat
		Prog.ajouter(cmp); // - On le compare à 1
		Prog.ajouter(bne); // - Si le test est faux, on sort
		
		// - Sinon on exécute les instructions
		coder_LISTE_INST(a.getFils2());
		
		Prog.ajouter(bra); // - Et on refait le test.
		Prog.ajouter(dehors); // - Etiquette DEHORS
		
		
	}
	private void coder_FOR(Arbre a) 	{}

	private void coder_IF(Arbre a) // - Instruction IF
	{
		int p = a.getNumLigne(); 
		// - Identifie le numéro de ligne de l'instruction IF
		Prog.ajouterComment(" IF ligne "+p+" ");
		
		Etiq ok = Etiq.nouvelle("if.l1");
		Etiq ko = Etiq.nouvelle("if.l2");
		Etiq vu = Etiq.nouvelle("if.l3");

// - OK identifie le début des instructions si le test est VRAI
// - KO identifie le début des instructions si le test est FAUX
// - VU Permet d'identifier la fin de l'instruction IF

		// -- Definition des instructions a ajouter 
		
		Inst pop = Inst.creation1(Operation.POP,Operande.R0); 
		// - On place la variable à comparer dans R0
		
		// - On compare R0 à 1 afin de vérifier le test
		Inst cmp = Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.R0); 
               

		// - Si il est faux, on va à l'étiquette KO
		// - i.e. on exécute les instructions du ELSE
		Inst bne = Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(ko));
		
		// - Sinon on execute celle du IF puis on va a VU,
		// - i.e. on sort du branchement conditionnel
		Inst bra = Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(vu));
				
		
		// -- Ajout des instructions assembleur

		coder_EXP(a.getFils1());	

		Prog.ajouter(pop);
		Prog.ajouter(cmp);
		Prog.ajouter(bne);
		Prog.ajouter(ok);

		coder_LISTE_INST(a.getFils2());	// - Instructions IF
		
		Prog.ajouter(bra);		// - Branchement VU
		Prog.ajouter(ko);		// - Etiquette KO
				
		coder_LISTE_INST(a.getFils3());	// - Instructions ELSE
		
		Prog.ajouter(vu);		// - Etiquette VU
	}
	
	
	private void coder_AFFECT(Arbre a)
	{
		int p = a.getNumLigne(); 
		// - Identifie le début de l'instruction AFFECT
		Prog.ajouterComment(" AFFECT ligne "+p+" ");
		// - Récupère l'emplacement mémoire de la variable
		int n = coder_PLACE(a.getFils1()); 

		coder_EXP(a.getFils2());
		// On évalue l'expression et on la place dans la pile
		Inst pop = Inst.creation1(Operation.POP,Operande.R0);
		// On place le résultat de l'expression dans R0
		
		Inst move = Inst.creation2 
		(
			Operation.STORE,
			Operande.R0,
			Operande.creationOpIndirect(n, Registre.GB)
		);
		
// - On stocke le résultat dans l'espace de la variable à affecter
		
		Prog.ajouter(pop);
		Prog.ajouter(move);
	}
	
	private void coder_READ(Arbre a) // - Instruction READ
	{
		int p = a.getNumLigne(); 
		// - Identifie le début de l'instruction READ
		Prog.ajouterComment(" READ ligne "+p+" ");
		
		NatureType natureExp = a.getFils1()
		.getDecor().getType().getNature();
		// - Récupère les informations de Nature
		
		int n = coder_PLACE(a.getFils1()); 
		// - Récupère l'emplacement mémoire de la variable
		
		if (n < 0) return;
		
		Inst read;
		Inst move;
		
		switch (natureExp)
		{
			case Interval:
			read = Inst.creation0(Operation.RINT);
			// - Si la variable à affecter est un entier
			break;
			
			case Real:
			read = Inst.creation0(Operation.RFLOAT);
			// - Si la variable à affecter est un float 
			break;
			
			default: return;
		}
		
		move = Inst.creation2(Operation.STORE,Operande.R1,
		Operande.creationOpIndirect(n,Registre.GB));
		// - Stocke la valeur à affecter à l'emplacement
		// - mémoire de la variable que l'on affecte
			
		Prog.ajouter(read);
		Prog.ajouter(move);
	}
	
	// - Instruction WRITE
	private void coder_WRITE(Arbre a)
	{
		int p = a.getNumLigne();
		// - Identifie le début de l'instruction WRITE
		Prog.ajouterComment(" WRITE ligne "+p+" ");
		coder_LISTE_EXP(a.getFils1());
		// - Appel à coder_LISTE_EXP
	}

	// - Permet, via la méthode coder_SHOW
	// - d'afficher la liste d'expressions dans WRITE	
	private void coder_LISTE_EXP(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: break; 
			// - Fin de la liste d'expressions
			
			case ListeExp:
				coder_LISTE_EXP(a.getFils1());
				coder_SHOW(a.getFils2());
				// - Affiche l'expression courante
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
	
	// - Permet de récupérer l'emplacement mémoire d'une variable
	private int coder_PLACE(Arbre a)
	{
		String suffix = "";
		
		while (a.getNoeud == Noeud.Index)
		{
			switch(a.getFils2().getNoeud())
			{
				case(Entier):	break;
				case(Ident):	break;
			}
			a = a.getFils1();
		}
		String s = a.getChaine();
		return idfs.indexOf(s)+1;
	}
	
	
	private void coder_OP_BINARY(Arbre a, Operation op) 
	{
		Inst e1,e2;
		Operande x,y;

		x = Operande.opDirect(Registre.R2);
		// - On place les opérandes dans les registres
		y = Operande.opDirect(Registre.R3);

		coder_EXP(a.getFils1());
		// - On évalue la première expression
		e1 = Inst.creation1(Operation.POP,x);
		Prog.ajouter(e1);
			
		coder_EXP(a.getFils2());
		// - Idem pour la deuxième expression
		e2 = Inst.creation1(Operation.POP,y);
		Prog.ajouter(e2);
			
		e1 = Inst.creation2(op,y,x); 
		// - On effectue l'opération prévue
		e2 = Inst.creation1(Operation.PUSH,x);
		// - Et on push le résultat en pile
			
		Prog.ajouter(e1);
		Prog.ajouter(e2);
	}
	
	private void coder_OP_FEUILLE(Arbre a)
	// - Transcrit les feuilles en code pour la machine abstraite
	{
		Inst e1,e2;
		Operande x,y;
		
		switch(a.getNoeud()) // - 3 types de feuilles
		{
			case Ident: // - Un identificateur 
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
	
	// Opération BRANCH
	private void coder_OP_BRANCH(Arbre a, Operation op)
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



