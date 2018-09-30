package fr.esisar.compilation.verif;

import fr.esisar.compilation.global.src.*;

/**
 * La classe ReglesTypage permet de définir les différentes regles 
 * de typage du langage JCas.
 **/

public class ReglesTypage
{

   /**
    * Teste si le type t1 et le type t2 sont compatibles
    * pour l'affectation, cad si le type t2 est affectable a t1.
    **/

   static ResultatAffectCompatible affectCompatible(Type t1, Type t2)
   {
	ResultatAffectCompatible r;
	r = new ResultatAffectCompatible();
	
	r.setOk(false); r.setConv2(false);
	
	NatureType n1 = t1.getNature();
	NatureType n2 = t2.getNature();
	
	if (	(n1 == NatureType.Interval && n1 == n2)
	||	(n1 == NatureType.Real     && n1 == n2)
	||	(n1 == NatureType.Interval && n2 == NatureType.Real)
	||	(n1 == NatureType.Real && n2 == NatureType.Interval) )

		r.setOk(true);
	
	if (	(n1 == NatureType.Array && n2 == NatureType.Array)
	&&	(t1.getIndice().getNature() == NatureType.Interval)
	&&	(t2.getIndice().getNature() == NatureType.Interval)
	&&	(t2.getIndice().getBorneInf() == t1.getIndice().getBorneInf())
	&&	(t2.getIndice().getBorneSup() == t1.getIndice().getBorneSup()))
		r = affectCompatible(t1.getElement(),t2.getElement());


	if (n1 == NatureType.Real && n2 == NatureType.Interval)
		r.setConv2(true);
		
	return r;
   }

   /**
    * Teste si le type (t1,t2) sont compatibles pour l'opération 
    * binaire représentée dans noeud.
    **/

   static ResultatBinaireCompatible binaireCompatible
      (Noeud noeud, Type t1, Type t2)
      {
	ResultatBinaireCompatible r;
	r = new ResultatBinaireCompatible();
	r.setConv1(false);
	r.setConv2(false);
	r.setOk(false);
	
	NatureType n1 = t1.getNature();
	NatureType n2 = t2.getNature();
	
	switch(noeud)
	{
		case Et:
		case Ou:
			if (n1 == NatureType.Boolean && n1 == n2)
			{
				r.setTypeRes(Type.Boolean);
				r.setOk(true);
			} break;
			
		case Egal:
		case Sup:
		case Inf:
		case NonEgal:
		case InfEgal:
		case SupEgal:

			if (n1 == NatureType.Interval && n1 == n2)
			{
				r.setTypeRes(Type.Boolean);
				r.setOk(true);
			}

			if (n1 == NatureType.Real && n1 == n2)
			{
				r.setTypeRes(Type.Boolean);
				r.setOk(true);
			}

			if (n1 == NatureType.Interval
			&&  n2 == NatureType.Real)
			{
				r.setTypeRes(Type.Boolean);
				r.setConv1(true);
				r.setOk(true);
			}
			if (n2 == NatureType.Interval
			&&  n1 == NatureType.Real)
			{
				r.setTypeRes(Type.Boolean);
				r.setConv2(true);
				r.setOk(true);
			} break;
			
		case Plus:
		case Moins:
		case Mult:

			if (n1 == NatureType.Interval && n1 == n2)
			{
				r.setTypeRes(Type.Integer);
				r.setOk(true);
			}

			if (n1 == NatureType.Real && n1 == n2)
			{
				r.setTypeRes(Type.Real);
				r.setOk(true);
			}

			if (n1 == NatureType.Interval
			&&  n2 == NatureType.Real)
			{
				r.setTypeRes(Type.Real);
				r.setConv1(true);
				r.setOk(true);
			}
			if (n2 == NatureType.Interval
			&&  n1 == NatureType.Real)
			{
				r.setTypeRes(Type.Real);
				r.setConv2(true);
				r.setOk(true);
			} break;
		
		case Quotient:
		case Reste:

			if (n1 == NatureType.Interval && n1 == n2)
			{
				r.setTypeRes(Type.Integer);
				r.setOk(true);
			} break;

		case DivReel:

			if (n1 == NatureType.Interval && n1 == n2)
			{
				r.setTypeRes(Type.Real);
				r.setOk(true);
			}

			if (n1 == NatureType.Real && n1 == n2)
			{
				r.setTypeRes(Type.Real);
				r.setOk(true);
			}

			if (n1 == NatureType.Interval
			&&  n2 == NatureType.Real)
			{
				r.setTypeRes(Type.Real);
				r.setConv1(true);
				r.setOk(true);
			}
			if (n2 == NatureType.Interval
			&&  n1 == NatureType.Real)
			{
				r.setTypeRes(Type.Real);
				r.setConv2(true);
				r.setOk(true);
			} break;
	} return r;
      }

   /**
    * Teste si le type t est compatible pour l'opération unaire.
    */
   static ResultatUnaireCompatible unaireCompatible
         (Noeud noeud, Type t)
   {
	ResultatUnaireCompatible r;
	r = new ResultatUnaireCompatible();
	r.setOk(false);
	
	NatureType n = t.getNature();
	
	switch(noeud)
	{
		case Non:
			if (n == NatureType.Boolean)
			{
				r.setTypeRes(Type.Boolean);
				r.setOk(true);
			} break;

		case PlusUnaire:
		case MoinsUnaire:
			if (n == NatureType.Interval)
			{
				r.setTypeRes(Type.Integer);
				r.setOk(true);
			}
			if (n == NatureType.Real)
			{
				r.setTypeRes(Type.Real);
				r.setOk(true);
			}
	} return r;
   }
         
}

