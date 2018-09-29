/**
 * Type énuméré pour les erreurs contextuelles.
 * Ce type énuméré définit toutes les erreurs contextuelles et 
 * permet l'affichage des messages d'erreurs pour la passe 2.
 */

package fr.esisar.compilation.verif;

public enum ErreurContext {

   EnvtError,		// IDF jamais ete declare
   LookupError,		// Erreur de redeclaration
   IndexError,		// Erreur de plages numeriques
   ValueError,		// Incoherence d'affectation
   TypeError,		// Incoherence de types

   UnknownError,	// Erreur inconnue (default?)
   
   /* Bonus */
   OverflowError,	// Depassement de valeur (int)
   ZeroDivError;	// Division par zero


   void leverErreurContext(String s, int numLigne) throws ErreurVerif
   {
      String msg = "";
      System.err.println("Erreur contextuelle : ");
      
      switch (this)
      {
         case EnvtError:	msg = "Identificateur inconnu";	break;
         case LookupError:	msg = "Identificateur declare";	break;
         case IndexError:	msg = "Plage num. incorrecte";	break;
         case ValueError:	msg = "Erreur d'affectation";	break;
         case TypeError:	msg = "Types incompatibles";	break;
         
         case OverflowError:	msg = "Depassement d'entier";	break;
         case ZeroDivError:	msg = "Operation interdite";	break;
         
         case UnknownError:	msg = "Erreur inconnue";	break;
         
         default:		msg = "Erreur non repertoriee"; break;
      }
      
      System.err.print(s + " : "+ msg);
      System.err.println(" ... ligne " + numLigne);
      throw new ErreurVerif();
   }

}


