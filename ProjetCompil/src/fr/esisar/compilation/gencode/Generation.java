package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

import java.util.ArrayList;

// Génération de code assembleur a partir d'un arbre décoré Jcas


class Generation
{
	private ArrayList<String> idfs = new ArrayList<String>();
	
	private Registre rx = null;
	private Registre ry = null;
	private Registre rz = null;
	
	
	static Prog coder(Arbre a)
	{
		Prog.ajouterGrosComment("JCAS Compilator");
      
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
	
	private void coder_WHILE(Arbre a)	{}
	private void coder_FOR(Arbre a) 	{}

	private void coder_IF(Arbre a)
	{
		Etiq faux = Etiq.nouvelle("faux");
		Etiq finsi = Etiq.nouvelle("finsi");

		// coder_EXP(a,rx);

		Prog.ajouter(Inst.creation2(Operation.CMP,
                                Operande.creationOpEntier(1), 
                                Operande.opDirect(rx)));

		Prog.ajouter(Inst.creation1(Operation.BNE, 
				Operande.creationOpEtiq(faux)));

		coder_LISTE_INST(a.getFils2());
		
		Prog.ajouter(Inst.creation1(Operation.BRA, 
				Operande.creationOpEtiq(finsi)));
		
		Prog.ajouter(faux);		
		coder_LISTE_INST(a.getFils3());
		Prog.ajouter(finsi);
	}
	
	private void coder_AFFECT(Arbre a)
	{
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
	
	private void coder_EXP(Arbre a)
	{
		Inst e1,e2,e3;
		Operande x,y;
		
		switch (a.getNoeud())
		{
			case Et:	break;
			case Ou: 	break;
			case Egal: 	break;
			case InfEgal:	break;
			case SupEgal: 	break;
			case NonEgal: 	break;
			case Inf: 	break;
			case Sup: 	break;

			case Non:		break;
			case PlusUnaire:	break;
			case MoinsUnaire:	break;
			
			case Index:	break;
			
			case Reste:
			x = Operande.opDirect(Registre.R2);
			y = Operande.opDirect(Registre.R3);
			
			coder_EXP(a.getFils1());
			e1 = Inst.creation1(Operation.POP,x);
			Prog.ajouter(e1);
			
			coder_EXP(a.getFils2());
			e2 = Inst.creation1(Operation.POP,y);
			Prog.ajouter(e2);
			
			e1 = Inst.creation2(Operation.MOD,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			
			Prog.ajouter(e1);
			Prog.ajouter(e2);		
			break;
			
			case Quotient:
			case DivReel:
			x = Operande.opDirect(Registre.R2);
			y = Operande.opDirect(Registre.R3);
			
			coder_EXP(a.getFils1());
			e1 = Inst.creation1(Operation.POP,x);
			Prog.ajouter(e1);
			
			coder_EXP(a.getFils2());
			e2 = Inst.creation1(Operation.POP,y);
			Prog.ajouter(e2);
			
			e1 = Inst.creation2(Operation.DIV,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			
			Prog.ajouter(e1);
			Prog.ajouter(e2);		
			break;
			
			case Mult:
			x = Operande.opDirect(Registre.R2);
			y = Operande.opDirect(Registre.R3);
			
			coder_EXP(a.getFils1());
			e1 = Inst.creation1(Operation.POP,x);
			Prog.ajouter(e1);
			
			coder_EXP(a.getFils2());
			e2 = Inst.creation1(Operation.POP,y);
			Prog.ajouter(e2);
			
			e1 = Inst.creation2(Operation.MUL,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			
			Prog.ajouter(e1);
			Prog.ajouter(e2);
			break;
			
			case Moins:
			x = Operande.opDirect(Registre.R2);
			y = Operande.opDirect(Registre.R3);
			
			coder_EXP(a.getFils1());
			e1 = Inst.creation1(Operation.POP,x);
			Prog.ajouter(e1);
			
			coder_EXP(a.getFils2());
			e2 = Inst.creation1(Operation.POP,y);
			Prog.ajouter(e2);
			
			e1 = Inst.creation2(Operation.SUB,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			
			Prog.ajouter(e1);
			Prog.ajouter(e2);		
			break;
			
			case Plus:
			x = Operande.opDirect(Registre.R2);
			y = Operande.opDirect(Registre.R3);
			
			coder_EXP(a.getFils1());
			e1 = Inst.creation1(Operation.POP,x);
			Prog.ajouter(e1);
			
			coder_EXP(a.getFils2());
			e2 = Inst.creation1(Operation.POP,y);
			Prog.ajouter(e2);
			
			e1 = Inst.creation2(Operation.ADD,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			
			Prog.ajouter(e1);
			Prog.ajouter(e2);
			break;
			
			case Ident:
			int n = coder_PLACE(a);
			y = Operande.opDirect(Registre.R0);
			x = Operande.creationOpIndirect(n,Registre.GB);
			e1 = Inst.creation2(Operation.LOAD,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			Prog.ajouter(e1);
			Prog.ajouter(e2);
			break;
			
			case Entier:
			y = Operande.opDirect(Registre.R0);
			x = Operande.creationOpEntier(a.getEntier());
			e1 = Inst.creation2(Operation.LOAD,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			Prog.ajouter(e1);
			Prog.ajouter(e2);
			break;
			
			case Reel:
			y = Operande.opDirect(Registre.R0);
			x = Operande.creationOpReel(a.getReel());
			e1 = Inst.creation2(Operation.LOAD,x,y);
			e2 = Inst.creation1(Operation.PUSH,y);
			Prog.ajouter(e1);
			Prog.ajouter(e2);
			break;
		}
	}
	
	private void coder_NLINE(Arbre a)
	{
		Prog.ajouter(Inst.creation0(Operation.WNL));
	}
	
	private void coder_NOP(Arbre a) {}
}



