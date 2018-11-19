package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

import java.util.ArrayList;

// Génération de code assembleur a partir d'un arbre décoré Jcas


class Generation
{
	private ArrayList<String> idfs = new ArrayList<String>();
		
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

	private void coder_LISTE_DECL(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: break;
			
			case ListeDecl:
				coder_LISTE_DECL(a.getFils1());
				coder_DECL(a.getFils2());
				break;

			default: break;
		}
	}
	
	private void coder_DECL(Arbre a)
	{		
		int n = coder_LISTE_IDF(a.getFils1());
		Operande nb_vars = Operande.creationOpEntier(n);
		Prog.ajouter(Inst.creation1(Operation.ADDSP,nb_vars));
	}
	
	private int coder_LISTE_IDF(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: return 0;
				
			case ListeIdent:
				int n = coder_LISTE_IDF(a.getFils1());
				String s = a.getFils2().getChaine();
				idfs.add(s);
				return (n+1);
		}
		
		return 0;
	}
	
// --- 2. Operations sur la machine (partie instruction)

	private void coder_LISTE_INST(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: break;
			
			case ListeInst:
				coder_LISTE_INST(a.getFils1());
				coder_INST(a.getFils2());
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
	
	private void coder_WHILE(Arbre a)
	{
		int p = a.getNumLigne();
		Prog.ajouterComment(" WHILE ligne "+p+" ");
		Etiq dedans = Etiq.nouvelle("while.l1");
		Etiq dehors = Etiq.nouvelle("while.l2");
		
		// -- Definition des instructions a ajouter

		Inst pop = Inst.creation1(Operation.POP,Operande.R0);
		
		Inst cmp = Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.R0);

		Inst bne = Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(dehors));
		
		Inst bra = Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(dedans));

		// -- Ajout des instructions assembleur
		
		Prog.ajouter(dedans);

		coder_EXP(a.getFils1());
		
		Prog.ajouter(pop);
		Prog.ajouter(cmp);
		Prog.ajouter(bne);
		
		coder_LISTE_INST(a.getFils2());
		
		Prog.ajouter(bra);
		Prog.ajouter(dehors);
		
		
	}
	private void coder_FOR(Arbre a) 	{}

	private void coder_IF(Arbre a)
	{
		int p = a.getNumLigne();
		Prog.ajouterComment(" IF ligne "+p+" ");
		
		Etiq ok = Etiq.nouvelle("if.l1");
		Etiq ko = Etiq.nouvelle("if.l2");
		Etiq vu = Etiq.nouvelle("if.l3");

		// -- Definition des instructions a ajouter 
		
		Inst pop = Inst.creation1(Operation.POP,Operande.R0);
		
		Inst cmp = Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.R0);

		Inst bne = Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(ko));
		
		Inst bra = Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(vu));

		// -- Ajout des instructions assembleur

		coder_EXP(a.getFils1());		
		Prog.ajouter(pop);
		Prog.ajouter(cmp);
		Prog.ajouter(bne);
		Prog.ajouter(ok);

		coder_LISTE_INST(a.getFils2());
		
		Prog.ajouter(bra);
		Prog.ajouter(ko);
				
		coder_LISTE_INST(a.getFils3());
		
		Prog.ajouter(vu);
	}
	
	private void coder_AFFECT(Arbre a)
	{
		int p = a.getNumLigne();
		Prog.ajouterComment(" AFFECT ligne "+p+" ");
		
		int n = coder_PLACE(a.getFils1());
		coder_EXP(a.getFils2());
		
		Inst pop = Inst.creation1(Operation.POP,Operande.R0);
		
		Inst move = Inst.creation2
		(
			Operation.STORE,
			Operande.R0,
			Operande.creationOpIndirect(n, Registre.GB)
		);
		
		Prog.ajouter(pop);
		Prog.ajouter(move);
	}
	
	private void coder_READ(Arbre a)
	{
		int p = a.getNumLigne();
		Prog.ajouterComment(" READ ligne "+p+" ");
		
		NatureType natureExp = a.getFils1()
		.getDecor().getType().getNature();
		
		int n = coder_PLACE(a.getFils1());
		
		if (n < 0) return;
		
		Inst read;
		Inst move;
		
		switch (natureExp)
		{
			case Interval:
			read = Inst.creation0(Operation.RINT);
			break;
			
			case Real:
			read = Inst.creation0(Operation.RFLOAT);
			break;
			
			default: return;
		}
		
		move = Inst.creation2(Operation.STORE,Operande.R1,
		Operande.creationOpIndirect(n,Registre.GB));
			
		Prog.ajouter(read);
		Prog.ajouter(move);
	}
	
	private void coder_WRITE(Arbre a)
	{
		int p = a.getNumLigne();
		Prog.ajouterComment(" WRITE ligne "+p+" ");
		
		coder_LISTE_EXP(a.getFils1());
	}
	
	private void coder_LISTE_EXP(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: break;
			
			case ListeExp:
				coder_LISTE_EXP(a.getFils1());
				coder_SHOW(a.getFils2());
				break;

			default: break;
		}
	}
	
	private void coder_SHOW(Arbre a)
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
	
	private int coder_PLACE(Arbre a)
	{
		String s = a.getChaine();
		return idfs.indexOf(s)+1;
	}
	
	private void coder_OP_BINARY(Arbre a, Operation op)
	{
		Inst e1,e2;
		Operande x,y;

		x = Operande.opDirect(Registre.R2);
		y = Operande.opDirect(Registre.R3);

		coder_EXP(a.getFils1());
		e1 = Inst.creation1(Operation.POP,x);
		Prog.ajouter(e1);
			
		coder_EXP(a.getFils2());
		e2 = Inst.creation1(Operation.POP,y);
		Prog.ajouter(e2);
			
		e1 = Inst.creation2(op,y,x);
		e2 = Inst.creation1(Operation.PUSH,x);
			
		Prog.ajouter(e1);
		Prog.ajouter(e2);
	}
	
	private void coder_OP_FEUILLE(Arbre a)
	{
		Inst e1,e2;
		Operande x,y;
		
		switch(a.getNoeud())
		{
			case Ident:
			int n = coder_PLACE(a);
			x = Operande.creationOpIndirect(n,Registre.GB);
			break;
			
			case Entier:
			x = Operande.creationOpEntier(a.getEntier());
			break;

			case Reel:
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
	
	private void coder_OP_BRANCH(Arbre a, Operation op)
	{
		Inst e1,e2,e3;
		Operande x,y,z;

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
	
	private void coder_NLINE(Arbre a)
	{
		Prog.ajouter(Inst.creation0(Operation.WNL));
	}
	
	private void coder_NOP(Arbre a) {}
}



