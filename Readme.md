# Projet Compilation du JCAS (CS444 2018)
Groupe N : T. Bellanger, R. Jarry, F. Sauger, J. Thomas
-------------------------------------------------------

## 1. Structure du projet

Le projet se décompose en deux programmes : un compilateur, dont le code a été partiellement fourni, et un générateur de programmes jcas.

Ce générateur a été pensé pour pouvoir générer des programmes simples, syntaxiquement corrects, afin d'étudier le fonctionnement des analyses lexicales et syntaxiques du compilateur.

Le compilateur quant à lui est composé d'une série de classes Java, interagissant avec des scripts shell et des fichiers de description de la grammaire et du lexique, respectivement ``syntax.cup`` et ``lexical.flex``. Ces fichiers, complétés au cours de la passe 1, sont documentés par leur spécification disponible sur la plate-forme Chamilo, et leur étude a été consignée dans ``lexical.doc.md`` et ``syntaxe.doc.md``, dans le même répertoire.

Dans la passe 2, nous nous intéressons à la décoration de l'arbre abstrait généré à l'étape précédente, ainsi qu'à sa correction sémantique. Nous complétons à cet effet les fichiers ``Verif.java``, ``ErreurContext.java``, ``ReglesTypage.java`` et ``ErreurReglesTypage.java`` dans le répertoire correspondant.

## 2. Fonctionnement des programmes

Pour créer de nouveaux fichiers de test (si besoin) :

```Bash
java -jar jcasGenerator/jcasGenerator.final.jar
```

Pour compiler le programme de compilation jcas :

```Bash
chmod -R u+x ProjetCompil/

cd ProjetCompil/src/fr/esisar/compilation/syntaxe/	# Passe 1
cd ProjetCompil/src/fr/esisar/compilation/verif/	# Passe 2

./compil.sh
```

Pour lancer les analyses lexicales et syntaxiques sur les programmes de test :

```Bash
cd ProjetCompil/test/lexico/

./lexico.sh

cd ProjetCompil/test/syntaxe/

./syntaxe.sh
```
