# Projet Compilation du JCAS (CS444 2018)
Groupe N : T. Bellanger, R. Jarry, F. Sauger, J. Thomas
-------------------------------------------------------

## Structure du projet

Le projet se décompose en deux programmes : un compilateur, dont le code a été partiellement fourni, et un générateur de programmes jcas.

Ce générateur a été pensé pour pouvoir générer des programmes simples, syntaxiquement corrects, afin d'étudier le fonctionnement des analyses lexicales et syntaxiques du compilateur.

Le compilateur quant à lui est composé d'une série de classes Java, interagissant avec des scripts shell et des fichiers de description de la grammaire et du lexique, respectivement ``syntax.cup`` et ``lexical.flex``. Ces fichiers, complétés au cours de la passe 1, sont documentés par leur spécification disponible sur la plate-forme Chamilo, et leur étude a été consignée dans ``lexical.doc.md`` et ``syntaxe.doc.md``, dans le même répertoire.

Dans la passe 2, nous nous intéressons à la décoration de l'arbre abstrait généré à l'étape précédente, ainsi qu'à sa correction sémantique. Nous complétons à cet effet les fichiers ``Verif.java``, ``ErreurContext.java``, ``ReglesTypage.java`` et ``ErreurReglesTypage.java`` dans le répertoire correspondant.

Dans la passe 3, l'objectif est de transcrire un programme .jcas en instructions compréhensibles pour la machine abstraite. Cette transcription se fait via le fichier ``Generation.java``. Elle est divisée en deux parties: on s'occupe tout d'abord de la déclaration des variables, puis des instructions.

### Fonctionnement des programmes

Pour créer de nouveaux fichiers de test (si besoin) :

```Bash
java -jar jcasGenerator/jcasGenerator.final.jar
```

Pour compiler le programme de compilation jcas :

```Bash
chmod -R u+x ProjetCompil/

cd ProjetCompil/src/fr/esisar/compilation/syntaxe/	# Passe 1
cd ProjetCompil/src/fr/esisar/compilation/verif/	# Passe 2
cd ProjetCompil/src/fr/esisar/compilation/gencode/  # Passe 3

./compil.sh
```

Pour lancer les analyses lexicales et syntaxiques sur les programmes de test :

```Bash
cd ProjetCompil/test/lexico/

./lexico.sh

cd ProjetCompil/test/syntaxe/

./syntaxe.sh
```

Pour effectuer la vérification sémantique du programme :

```Bash
cd ProjetCompil/test/verif/

./verif.sh
```

Enfin, pour générer du code machine exécutable :

```Bash
cd ProjetCompil/test/gencode/

./gencode.sh
```


## 1. Documentation Passe 1

### Analyse lexicale : Les Opérateurs 
- Pour chaque opérateur défini dans le langage Jcas par la chaîne correspondante,
on renvoie le lexeme correspondant, qui existe déjà dans la table des symboles.

**Exemple de code** :
```Java
"<=" {return symbol(sym.INF_EGAL);}
```

### Analyse Lexicale: Lexèmes Spécifiques

- Pour les lexèmes IDF, constante entiere, constante reelle, constante chaine
et commentaire, on doit effectuer une action plus spécifique.

**Exemple de code** :
```Java
{IDF}
{
	if(dictionnaire.get(yytext().toLowerCase())!=null)
	  {return symbol(dictionnaire.get(yytext().toLowerCase()));}

	else {return symbol(sym.IDF, yytext());}
}
```
#### IDF
- Pour IDF, qui correspond a un identificateur, on regarde s'il existe une
entrée correspondante dans le dictionnaire qui implémente la table des symboles.
Si oui on renvoie les informations contenues dans la table des symboles,
sinon l'on ajoute a la table des symboles.

#### CONST_ENT
- Pour une constante entière, on vérifie le format du lexème, s'il s'agit bien
d'un entier on renvoie sa valeur, sinon on remonte une erreur lexicale.

#### CONST_REEL
- Pour une constante réelle, on verifie le format du lexème, s'il s'agit bien
d'un entier on renvoie sa valeur, sinon on remonte une erreur lexicale.

#### CONST_CHAINE
- Pour une constante chaine, on renvoie directement le contenu du lexeme.

#### COMMENTAIRE
- Dans le cas d'un commentaire, l'action consiste a ne rien faire.

#### Cas d'erreur
- Le dernier cas correspond au cas ou le lexème n'a pas été reconnu, dans ce
cas on renvoie une erreur lexicale.

**Exemple de code** :
```Java
.{
	System.out.println("Erreur Lexicale : '" +
		yytext() + "' non reconnu ... ligne " + numLigne());

	throw new ErreurLexicale();
}
```
### Analyse Syntaxique : Règles de construction de l'arbre d'analyse
- On définit chaque non terminal de la grammaire ainsi que les règles de dérivation. A partir de ces règles de dérivation on construit l'arbre d'analyse.

#### Définition du non terminal

```Java
liste_idf ::= liste_idf:a VIRGULE idf:b {:...:} |  idf:b {:...:}
```

#### Création du noeud dans l'arbre

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



## 2. Documentation Passe 2

### 2.1 Les Règles de Typage

__Il existe 3 cas dans lesquels on peut, et on doit, appliquer les règles de typages.__

#### Premier cas : l'affectation
La vérification de l'affectation est simple 
- On ne peut affecter aux variables de type _boolean_ que des _boolean_
- On ne peut affecter aux variables de type _integer_ que des _integer_
- On peut affecter aux variables de type _real_ des _integer_ ou des _real_
- On peut affecter à un _array_ un autre _array_ qu'un _array_ de même dimension et sur le même type. Cela se fait par appel récursif sur les premiers éléments des deux _array_

####  Second cas : Opération binaire
Le cas de l'opération binaire est assez similaire au cas de l'affectation
- Les _boolean_ ne supportent que les opérations logiques avec un autre _boolean_
- Les _integer_ et les _real_ ne supportent que les opérations arithmétiques avec des _real_ et des _boolean_
- Les éléments d'_array_ supportent les opérations associées à leur type comme écrit ci-dessus

#### Troisième cas : Opération unaire

Le cas de l'opération unaire est simple
- Les opérateurs + et - peuvent être placés devant et seulement devant des _integer_ et des _real_
- L'opérateur _not_ peut être placé devant et seulement devant un _boolean_


### 2.2 La classe Vérif
__La classe verif permet de réaliser la vérification et la décoration de l'arbre abstrait d'un programme Jcas.
La première étape consiste à initialiser l'environnement.__

####  initialiserEnv
- La méthode __initialiserEnv__ a pour rôle d'initialiser l'environnement du programme.
Pour ce faire elle crée un environnement vide auquel elle ajoute les types _integer_, _string_, _real_ et _boolean_ ainsi que les identifiants réservés _true_, _false_ et _max_int_.

- La vérification du programme se fait ensuite en deux parties, la vérification des déclarations et la vérification des instructions.

#### Vérification des déclarations: vérifier_LISTE_DECL
- La méthode __verifier_LISTE_DECL__ vérifie récursivement que chaque déclaration de la liste et remonte une erreur si elle rencontre une déclaration erronée. Pour cela elle fait appel à la méthode __verifier_DECL__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.

#### Vérification des déclarations: vérifier DECL
- La méthode __verifier DECL verifie__ une déclaration en vérifiant son type et la liste des identifiants qui la constituent. Pour cela elle fait appel aux fonctions __verifier_TYPE__ et __verifier_LISTE_IDF__.

#### Vérification des déclarations: vérifier_LISTE_IDF
- La méthode __verifier_LISTE_IDF__ vérifie récursivement chaque identifiant, si un identifiant est déjà présent dans l'environnement il s'agit d'une redéclaration et donc d'une erreur de contexte, qui est remontée, sinon chaque identifiant est ajouté à l'environnement. La décoration est ensuite ajoutée et l'identifiant résultant est à nouveau testé avec la fonction __verifier_IDF__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.

#### Vérification des instructions: vérifier_LISTE_INST
- La méthode __verifier_LISTE_INST__ vérifie récursivement chaque instruction pour savoir si elle est valide. Pour cela elle fait appel à la méthode __verifier_INST__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.
#### Vérification des instructions: vérifier_LISTE_EXP
- La méthode __verifier_LISTE_EXP__ vérifie récusrivement chaque expression pour savoir si elle est valide. Pour cela elle fait appel à la méthode __verifier_EXP__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.
#### Vérification des instructions: vérifier_INST
- La méthode __verifier_INST__ vérifie la vaildité de l'instruction au cas par cas parmis toutes les instructions possibles en Jcas. __verifier_INST__ fait appel aux fonctions spécifiques __verifier_PAS__, __verifier_WRITE__, __verifier_READ__ et __verifier_AFFECT__ pour chaque nœud qu'il est possible de rencontrer dans l'arbre.
#### Vérification des instructions: vérifier_AFFECT
- La méthode __verifier_AFFECT__ fait appel à la méthode __verifier_PLACE__ pour obtenir le type de l'identifiant qui constitue le membre gauche de l’affectation et à __verifier_EXP__ pour obtenir le type de l'expression qui constitue le membres droit de l'affectation. Ces appels permettent aussi de vérifier la validité des deux membres. La méthode applique ensuite les règles de compatibilité et remonte une erreur s'il y a lieu.
#### Vérification des instructions: vérifier_EXP
- La méthode __verifier_EXP__ applique les règles de compatibilité entre les types du langage Jcas.
#### Vérification des instructions: vérifier_PAS
- La méthode __verifier_PAS__ permet de vérifier la validité sémantique d'un identifiant et permet de décorer le nœud qui lui est attribué avec le bon type, soit entier soit matrice de dimension _n_ de type _t_.
#### Fonctions communes aux 2 parties: vérifier _TYPE
- La méthode __verifier_TYPE__ se contente de regarder le type attribué au nœud et vérifie au cas par cas avec un appel à des fonctions plus spécifiques.
#### Fonctions communes aux 2 parties: vérifier _TABLEAU
- La méthode vérifier tableau est légèrement plus complexe que les autres cas de base car il est possible d'avoir des tableau de tableau, de plus un tableau est construit par rapport à un autre type, il est donc nécessaire de faire appel faire appel à __verifier_TYPE__ dans __verifier_TABLEAU__.
- Les autres fonctions appliquent des cas de base et ne nécessitent pas d'être détaillés.

## 3. Documentation Passe 3

Le code pour la machine abstraite est généré par la méthode
 ```Java 
static Prog coder(Arbre a)
``` 
qui prend en paramètre l'arbre décoré construit dans la passe 2.

### 3.1. Génération de code pour la machine abstraite: Partie déclarative
Cette première partie est consacrée à la déclaration de toutes les variables au sein d'un programm Jcas, c'est-à-dire toutes les lignes de code avant l'instruction __begin__.

#### Descriptions des méthodes de la partie déclarative

```Java
private void coder_LISTE_DECL(Arbre a)
```
- Permet la génération du code machine abstraite pour allouer la mémoire nécessaire à la liste des variables déclarées. La méthode parcours la liste et alloue la mémoire pour chaque ligne déclarative.

```Java
private void coder_DECL(Arbre A)
```
- Permet la génération du code pour une déclaration.
#### Description des méthodes de la partie instructions

```Java
private void coder_LISTE_INST(Arbre a)
```
- Génère le code associé à une liste d'instruction. Il fait des appels à coder_INST pour chaque instruction dans la liste.

```Java
private void coder_INST(Arbre a)
```
- Génère le code associé à une instruction. Il appelle la méthode coder_X associé à l'instruction X.

```Java
private void coder_WHILE(Arbre a)
private void coder_FOR(Arbre a)
```
- Ces méthodes permettent de générer le code des boucles.

```Java
private void coder_READ(Arbre a)
private void coder_WRITE(Arbre a)
```
- Ces méthodes permettent de générer le code permettant d'interagir avec la console.

```Java
private void coder_AFFECT(Arbre a)
```
- Cette méthode permet de générer le code d'affectation.

