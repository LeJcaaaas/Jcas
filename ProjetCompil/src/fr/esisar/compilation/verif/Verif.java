package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;
 
/**
 * Cette classe permet de réaliser la vérification et la décoration 
 * de l'arbre abstrait d'un programme.
 */
 
public class Verif {

   // Environnement des IDF
   private Environ env;

   // Constructeur de la verification.
   public Verif() {env = new Environ();}

    
   /* Initialisation de l'environnement
    * avec des identificateurs predefinis */
   private void initialiserEnv()
   {
      Defn def;

      // integer
      def = Defn.creationType(Type.Integer);
      def.setGenre(Genre.PredefInteger);
      env.enrichir("integer",def);
      
      // string (pas de genre)
      def = Defn.creationType(Type.String);
      env.enrichir("string",def);
      
      // real
      def = Defn.creationType(Type.Real);
      def.setGenre(Genre.PredefReal);
      env.enrichir("real",def);

      // bool
      def = Defn.creationType(Type.Boolean);
      def.setGenre(Genre.PredefBoolean);
      env.enrichir("boolean",def);
      
      // true
      def = Defn.creationConstBoolean(true);
      def.setGenre(Genre.PredefBoolean);
      env.enrichir("true",def);

      // false
      def = Defn.creationConstBoolean(false);
      def.setGenre(Genre.PredefBoolean);
      env.enrichir("false",def);
      
      // max_int
      def = Defn.creationConstInteger(java.lang.Integer.MAX_VALUE);
      def.setGenre(Genre.PredefInteger);
      env.enrichir("max_int",def);
   }

// --- Fonctions de verification en cascade ---

   public void verifierDecorer(Arbre a) throws ErreurVerif
   	{verifier_PROGRAMME(a);}

   private void verifier_PROGRAMME(Arbre a) throws ErreurVerif
   {
      initialiserEnv();
      verifier_LISTE_DECL(a.getFils1());
      verifier_LISTE_INST(a.getFils2());
   }

// --- 1. Verification de la partie declaration ---

   private void verifier_LISTE_DECL(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Vide: break;
   		case ListeDecl:
   			verifier_LISTE_DECL(a.getFils1());
   			verifier_DECL(a.getFils2());
   			break;

   		default: 
   			String msg = "verifier_LISTE_DECL incorrect: ";
   			msg += a.getFils2().getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }

   private void verifier_DECL(Arbre a) throws ErreurVerif
   {
   	Type type = verifier_TYPE(a.getFils2());
   	verifier_LISTE_IDF(a.getFils1(),type);
   }

   private void verifier_LISTE_IDF(Arbre a, Type t) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Vide: break;
   		case ListeIdent:
   		
   			verifier_LISTE_IDF(a.getFils1(),t);

   			int n = a.getFils2().getNumLigne();
   			String s = a.getFils2().getChaine();
   			String l = s.toLowerCase();
   			
   			if (env.enrichir(l,Defn.creationVar(t)))
   			{
   			   ErreurContext e = ErreurContext.LookupError;
   			   e.leverErreurContext(s,n);
   			}
   			
   			Decor d = new Decor(Defn.creationVar(t),t);
   			a.getFils2().setDecor(d);
   			
   			verifier_IDF(a.getFils2(),NatureDefn.Var);
   			
   			break;

   		default: 
   			String msg = "verifier_LISTE_IDF incorrect: ";
   			msg += a.getFils2().getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }

// --- 2. Verification de la partie programme (instructions) ---
 
   private void verifier_LISTE_INST(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Vide: break;
   		case ListeInst:
   			verifier_LISTE_INST(a.getFils1());
   			verifier_INST(a.getFils2());
   			break;

   		default: 
   			String msg = "verifier_LISTE_INST incorrect: ";
   			msg += a.getFils2().getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }
   
   private void verifier_LISTE_EXP(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Vide: break;
   		case ListeExp:
   			verifier_LISTE_EXP(a.getFils1());
   			verifier_EXP(a.getFils2());
   			break;

   		default: 
   			String msg = "verifier_LISTE_EXP incorrect: ";
   			msg += a.getFils2().getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }
   
   private void verifier_INST(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Pour:
   			verifier_PAS(a.getFils1());
   			verifier_LISTE_INST(a.getFils2());
   			break;

		case Si: verifier_LISTE_INST(a.getFils3());

   		case TantQue:
   			verifier_LISTE_INST(a.getFils2());
   			
   			verifier_EXP(a.getFils1());
   			Type cond = a.getFils1().getDecor().getType();
   			
   			if (cond != Type.Boolean)
   			{
   				ErreurContext e; int l;
   				l = a.getFils1().getNumLigne();
   				e = ErreurContext.TypeError;
   				e.leverErreurContext("Not a bool",l);
   			} break;

   		case Ecriture:
   			verifier_LISTE_EXP(a.getFils1());
   			verifier_WRITE(a.getFils1());
   			break;
   			
   		case Lecture:
   			verifier_PLACE(a.getFils1());
   			verifier_READ(a.getFils1());
   			break;

   		case Affect:
   			verifier_AFFECT(a);
   			break;
   		
   		case Ligne:	break;
   		case Nop:	break;
   		
   		default:
   			String msg = "verifier_INST incorrect: ";
   			msg += a.getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }
   
   private void verifier_READ(Arbre a) throws ErreurVerif
   {
	Type t = a.getDecor().getType();
	if (!(t instanceof TypeInterval) && t != Type.Real)
	{
		ErreurContext e = ErreurContext.TypeError;
		String s = "Real ou TypeInterval attendu, obtenu ";
		e.leverErreurContext(s+t,a.getFils1().getNumLigne());
	}
   }
   
   private void verifier_WRITE(Arbre a) throws ErreurVerif
   {
	verifier_LISTE_EXP(a.getFils1());
	Arbre temp = a.getFils1();
	
	while (temp.getNoeud() != Noeud.Vide)
	{
		Type t = temp.getFils2().getDecor().getType();
		if (	!(t instanceof TypeInterval)
			&& t != Type.Real && t != Type.String )
		{
			int line =  temp.getFils2().getNumLigne();
	  		ErreurContext e = ErreurContext.TypeError;
	  		String s = "Real|String|TypeInterval attendu";
			e.leverErreurContext(s+" obtenu"+t,line);
	  	}

		temp = temp.getFils1();
	}
   }
   
   private void verifier_AFFECT(Arbre a) throws ErreurVerif
   {
	verifier_PLACE(a.getFils1());
	verifier_EXP(a.getFils2());
	
	Type t1 = null;
	Type t2 = null;

	if (a.getFils1() != null)
	t1 = a.getFils1().getDecor().getType();
	else return;
	
	if (a.getFils2() != null)
	t1 = a.getFils2().getDecor().getType();
	else return;

	ResultatAffectCompatible r;
	
	if ((t1 != null) && (t2 != null))
	{
	r = ReglesTypage.affectCompatible(t1,t2);

	if(r.getOk())
	{ 
		if(r.getConv2())
		{
			Arbre s = a.getFils2();
			int n = s.getNumLigne();
			
			Arbre b=Arbre.creation1(Noeud.Conversion,s,n);
			
			a.setFils2(b);
			a.getFils2().setDecor(new Decor(Type.Real));
		}
		
		a.setDecor(new Decor(t1));
	}
	else
	{
		ErreurContext e = ErreurContext.TypeError;
		String m = t1.toString() + " != " + t2.toString();
		e.leverErreurContext(m,a.getNumLigne());
	}
	}
   }
   
   private void verifier_EXP(Arbre a) throws ErreurVerif
   {
	switch (a.getNoeud())
	{
		case Et:
		case Ou: 
		case Egal: 
		case InfEgal: 
		case SupEgal: 
		case NonEgal: 
		case Inf: 
		case Sup: 
		case Plus: 
		case Moins: 
		case Mult: 
		case DivReel: 
		case Reste: 
		case Quotient:

			verifier_EXP(a.getFils1());
			verifier_EXP(a.getFils2());
			
			Type t1 = a.getFils1().getDecor().getType();
			Type t2 = a.getFils2().getDecor().getType();
			
			ResultatBinaireCompatible r = ReglesTypage.binaireCompatible(a.getNoeud(),t1,t2);
			
			if(r.getOk())
			{
				if(r.getConv1())
				{
					Arbre b = a.getFils1();
       					a.setFils1(Arbre.creation1(Noeud.Conversion,b,b.getNumLigne()));
					a.getFils1().setDecor(new Decor(Type.Real));
				}
				
				if(r.getConv2())
				{
					Arbre b = a.getFils2();
					a.setFils2(Arbre.creation1(Noeud.Conversion,b,b.getNumLigne()));
					a.getFils2().setDecor(new Decor(Type.Real));
				}

				a.setDecor(new Decor(r.getTypeRes()));
			}
			else
			{
				ErreurContext e = ErreurContext.TypeError;
				String m = t1.toString() + " != " + t2.toString();
				e.leverErreurContext(m,a.getNumLigne());
			}
			
			break;
		
		case Ident:
		case Index:
		case Chaine:
		case Entier:
		case Reel:
		
			verifier_FACT(a);
			break;
			
		case PlusUnaire:
		case MoinsUnaire:
		case Non:
		
			verifier_FACT(a.getFils1());
			Type t = a.getFils1().getDecor().getType();
			ResultatUnaireCompatible u = ReglesTypage.unaireCompatible(a.getNoeud(),t);
			if (u.getOk())
			    a.setDecor(new Decor(u.getTypeRes()));

			else
			{
			 ErreurContext e = ErreurContext.TypeError;
			 String m = a.getNoeud()+" != "+t.toString();
			 e.leverErreurContext(m,a.getNumLigne());
			}
			
			break;
			
		default:
   			String msg = "verifier_EXP incorrect: ";
   			msg += a.getNumLigne();
			throw new ErreurInterneVerif(msg);		
		
	}
   }

   private void verifier_PLACE(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Ident:
   			verifier_IDF(a,NatureDefn.Var);
   			break;
   		
   		case Index:	break;
   		default:	break;
   	}
   }
   
   private void verifier_PAS(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Increment:
   		case Decrement:
   		
   		verifier_IDF(a.getFils1(),NatureDefn.Var);
  		Type idf = a.getFils1().getDecor().getType();
  		
  		String m = " au lieu de TypeInterval";
  		
  		if (!(idf instanceof TypeInterval)) 
  		{
  			int ligne = a.getFils1().getNumLigne(); 
  			ErreurContext e = ErreurContext.TypeError;
			e.leverErreurContext(idf+m,ligne);
  		}
  		
  		verifier_EXP(a.getFils2());
  		Type exp1 = a.getFils2().getDecor().getType();
  		
  		if (!(exp1 instanceof TypeInterval))
  		{
  			int ligne = a.getFils2().getNumLigne(); 
  			ErreurContext e = ErreurContext.TypeError;
			e.leverErreurContext(exp1+m,ligne);
  		}
  		
  		verifier_EXP(a.getFils3());
  		Type exp2 = a.getFils3().getDecor().getType();
  		
  		if (!(exp2 instanceof TypeInterval))
  		{
  			int ligne = a.getFils3().getNumLigne();
			ErreurContext e = ErreurContext.TypeError;
			e.leverErreurContext(exp2+m,ligne);
		}
		
		break;
		
		default:
   			String msg = "verifier_PAS incorrect: ";
   			msg += a.getNumLigne();
			throw new ErreurInterneVerif(msg);
   	}
   }
   
   private void verifier_FACT(Arbre a) throws ErreurVerif
   {
	switch (a.getNoeud())
	{
		case Entier:
			a.setDecor(new Decor(Type.Integer));
			break;

		case Reel:
			a.setDecor(new Decor(Type.Real));
			break;
		
		case Chaine:
			a.setDecor(new Decor(Type.String));
			break;
		
		case Ident:
			String idf = a.getChaine().toLowerCase();
			Defn def = env.chercher(idf);
			if(def == null)
			{
				ErreurContext e;
				e = ErreurContext.EnvtError;
				String s = a.getChaine();
				int ligne = a.getNumLigne();
				e.leverErreurContext(s,ligne);
			}
			NatureDefn nat = def.getNature();
			if (	nat != NatureDefn.Var && 
				nat != NatureDefn.ConstInteger && 
				nat != NatureDefn.ConstBoolean )
			{
				ErreurContext e;
				int ligne = a.getNumLigne();
				e = ErreurContext.ValueError;
				String s = " n'est pas une lvalue";
				e.leverErreurContext(nat+s,ligne);
			}
			
			a.setDecor(new Decor(def,def.getType()));
			break;
		
		case Index:	break;
		default:	break;
		
//		default:
//   			String msg = "verifier_FACT incorrect: ligne ";
//   			msg += a.getNumLigne();
//			throw new ErreurInterneVerif(msg);	
	}
   }

// --- 3. Verifications communes et/ou imbriquees

   private void verifier_IDF(Arbre a, NatureDefn n1) throws ErreurVerif
   {
   	Defn d = env.chercher(a.getChaine().toLowerCase());
   	if (d != null)
   	{
   		Decor decor = new Decor(d,d.getType());
   		NatureDefn n2 = decor.getDefn().getNature();
   		
   		if (n1 == n2) a.setDecor(decor);
   		else
   		{
   			int line = a.getNumLigne();
   			String s = n1 + " attendu, obtenu " + n2;
   			ErreurContext e = ErreurContext.ValueError;
   			e.leverErreurContext(s,line);
   		}
   	}
   	else
   	{
   		ErreurContext e = ErreurContext.EnvtError;
   		e.leverErreurContext(a.getChaine(),a.getNumLigne());
   	}
   }

   private Type verifier_TYPE(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case Ident:
   			verifier_IDF(a,NatureDefn.Type);
   			String l = a.getChaine().toLowerCase();
   			return env.chercher(l).getType();

   		case Intervalle:	return verifier_INTERVALLE(a);
   		case Tableau:		return verifier_TABLEAU(a);

   		default:
   			String msg = "verifier_TYPE incorrect: ";
   			msg += a.getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }
   
   private Type verifier_INTERVALLE(Arbre a) throws ErreurVerif
   {
   	verifier_CONST(a.getFils1());
   	verifier_CONST(a.getFils2());
   	
   	int x1,x2;
   	
   	Noeud cn1 = a.getFils1().getNoeud();
 	Noeud cn2 = a.getFils2().getNoeud();
   	
   	if ((cn1 == Noeud.PlusUnaire) || (cn1 == Noeud.MoinsUnaire))
   	{
   		x1 = a.getFils1().getFils1().getEntier();
   	} else  x1 = a.getFils1().getEntier();
   	
   	if ((cn2 == Noeud.PlusUnaire) || (cn2 == Noeud.MoinsUnaire))
   	{
   		x2 = a.getFils2().getFils1().getEntier();
   	} else  x2 = a.getFils2().getEntier();
   	
   	x2 = Math.max(x1,x2);
   	
   	Type temp = Type.creationInterval(x1,x2);
	a.setDecor(new Decor(temp));

	return temp;
   }
   
   private Type verifier_TABLEAU(Arbre a) throws ErreurVerif
   {
   	Type t1 = verifier_INTERVALLE(a.getFils1());
   	Type t2 = verifier_TYPE(a.getFils2());
   	
   	Type tab = Type.creationArray(t1,t2);
   	a.setDecor(new Decor(tab));
   	
   	return tab;
   }
  
   private void verifier_CONST(Arbre a) throws ErreurVerif
   {
   	switch (a.getNoeud())
   	{
   		case PlusUnaire:
   		case MoinsUnaire:
   			verifier_CONST(a.getFils1());
   			Decor dec = a.getFils1().getDecor();
   			a.setDecor(new Decor(dec.getType()));
   			break;

   		case Entier:
   			a.setDecor(new Decor(Type.Integer));
   			break;
   			
   		case Ident:
			verifier_IDF(a,NatureDefn.ConstInteger);
			break;
			
		default:
   			String msg = "verifier_CONST incorrect: ";
   			msg += a.getNumLigne();
   			throw new ErreurInterneVerif(msg);
   	}
   }

}
