# Documentation Passe 1 groupe n : Bellanger-Jarry-Sauger-Thomas

## Regles de construction de l'arbre d'analyse

On définit chaque non terminal de la grammaire ainsi que les règles de dérivation. A partir de ces règles de dérivation on construit l'arbre d'analyse.

### Exemple de code :
#### definition du non terminal
```Java
liste_idf ::= liste_idf:a VIRGULE idf:b {:...:} |  idf:b {:...:}
```
#### création du noeud dans l'arbre
```Java
liste_idf:a VIRGULE idf:b
   {:
      RESULT = Arbre.creation2(Noeud.ListeIdent,a,b,b.getNumLigne());
   :}
   |  idf:b
   {:
      RESULT = Arbre.creation2(Noeud.ListeIdent,
      	Arbre.creation0(Noeud.Vide,parser.numLigne()),b,bleft);
   :}
   ;
```
