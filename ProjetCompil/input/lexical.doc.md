# Documentation Passe 1 groupe n: Bellanger-Jarry-Sauger-Thomas

## Opérateurs :

Pour chaque opérateur défini dans le langage Jcas par la chaîne correspondante,
on renvoie le lexeme correspondant, qui existe déjà dans la table des symboles.

#### Exemple de code :
```Java
"<=" {return symbol(sym.INF_EGAL);}
```
## Lexèmes Spécifiques

Pour les lexèmes IDF, constante entiere, constante reelle, constante chaine
et commentaire, on doit effectuer une action plus spécifique.

#### Exemple de code :
```Java
{IDF}
{
	if(dictionnaire.get(yytext().toLowerCase())!=null)
	  {return symbol(dictionnaire.get(yytext().toLowerCase()));}

	else {return symbol(sym.IDF, yytext());}
}
```
### IDF
Pour IDF, qui correspond a un identificateur, on regarde s'il existe une
entrée correspondante dans le dictionnaire qui implémente la table des symboles.
Si oui on renvoie les informations contenues dans la table des symboles,
si non l'on ajoute a la table des symboles.

### CONST_ENT
Pour une constante entière, on vérifie le format du lexème, s'il s'agit bien
d'un entier on renvoie sa valeur, sinon on remonte une erreur lexicale.

### CONST_REEL
Pour une constante réelle, on verifie le format du lexème, s'il s'agit bien
d'un entier on renvoie sa valeur, sinon on remonte une erreur lexicale.

### CONST_CHAINE
Pour une constante chaine, on renvoie directement le contenu du lexeme.

### COMMENTAIRE
Dans le cas d'un commentaire, l'action consiste a ne rien faire.

### Cas d'erreur
Le dernier cas correspond au cas ou le lexème n'a pas été reconnu, dans ce
cas on renvoie une erreur lexicale.

#### Exemple de code :
```Java
.{
	System.out.println("Erreur Lexicale : '" +
		yytext() + "' non reconnu ... ligne " + numLigne());

	throw new ErreurLexicale();
}
```
