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

   private void verifier_LISTE_DECL(Arbre a) throws ErreurVerif {}
   private void verifier_LISTE_INST(Arbre a) throws ErreurVerif {}
   
   private void verifier_LISTE_IDF(Arbre a) throws ErreurVerif {}
   private void verifier_LISTE_EXP(Arbre a) throws ErreurVerif {}
   
   private void verifier_INTERVALLE(Arbre a) throws ErreurVerif {}
   private void verifier_TABLEAU(Arbre a)    throws ErreurVerif {}
   
   private void verifier_PLACE(Arbre a) throws ErreurVerif {}
   private void verifier_CONST(Arbre a) throws ErreurVerif {}
   
   private void verifier_DECL(Arbre a) throws ErreurVerif {}
   private void verifier_INST(Arbre a) throws ErreurVerif {}
   private void verifier_FACT(Arbre a) throws ErreurVerif {}
   private void verifier_TYPE(Arbre a) throws ErreurVerif {}
   
   private void verifier_EXP(Arbre a) throws ErreurVerif {}
   private void verifier_IDF(Arbre a) throws ErreurVerif {}
   private void verifier_PAS(Arbre a) throws ErreurVerif {}

}
