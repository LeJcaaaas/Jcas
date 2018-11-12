package fr.esisar.compilation.gencode;

import fr.esisar.compilation.global.src.*;
import fr.esisar.compilation.global.src3.*;

// Génération de code pour un programme JCas à partir d'un arbre décoré.


class Generation
{
	private ArrayList<String> idfs = new ArrayList<String>();
	
	private Registre rx = Registre.R0;
	private Registre ry = Registre.R1;
	private Registre rz = Registre.R2;
	
	static Prog coder(Arbre a)
	{
		Prog.ajouterGrosComment("Programme généré par JCasc");
      
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
				coder_DECL(a.getFils2());
				coder_LISTE_DECL(a.getFils1());
				break;

			default: break;
		}
	}
	
	private void coder_DECL(Arbre a)
	{		
		int n = coder_LISTE_IDF(a.getFils1());
		Operande nb_vars = Operande.creationOpEntier(n);
		Prog.ajouter(Inst.creation1(Operation.ADDSP,nb_vars))
	}
	
	private Operande code_LISTE_IDF(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: return 0;
				
			case ListeIdent:
				int n = coder_LISTE_IDF(a.getFils1());
				idfs.add(a.getFils2().getChaine());
				return (n+1);
		}
	}
	
// --- 2. Operations sur la machine (partie instruction)

	private void coder_LISTE_INST(Arbre a)
	{
		switch (a.getNoeud())
		{
			case Vide: break;
			
			case ListeInst: 
				coder_INST(a.getFils2());
				coder_LISTE_INST(a.getFils1());
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
	private void coder_IF(Arbre a)		{}
	
	
	private void coder_WRITE(Arbre a)	{}
	private void coder_READ(Arbre a)	{}
	
	private void coder_AFFECT(Arbre a)	{}
	
	private void coder_NLINE(Arbre a)	{}
	private void coder_NOP(Arbre a)		{}
}



