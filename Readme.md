# Projet Compilation du JCAS (CS444 2018)
Groupe N : T. Bellanger, R. Jarry, F. Sauger, J. Thomas
-------------------------------------------------------

## 1. Structure du projet

Le projet se décompose en deux programmes : un compilateur, dont le code a été partiellement fourni, et un générateur de programmes jcas.

Ce générateur a été pensé pour pouvoir générer des programmes simples, syntaxiquement corrects, afin d'étudier le fonctionnement des analyses lexicales et syntaxiques du compilateur.

Le compilateur quant à lui est composé d'une série de classes Java, interagissant avec des scripts shell et des fichiers de description de la grammaire et du lexique, respectivement ``syntax.cup`` et ``lexical.flex``. Ces fichiers, complétés au cours de la passe 1, sont documentés par leur spécification disponible sur la plate-forme Chamilo, et leur étude a été consignée dans ``lexical.doc.md`` et ``syntaxe.doc.md``, dans le même répertoire.

## 2. Fonctionnement des programmes

Pour créer de nouveaux fichiers de test (si besoin) :

``` java -jar jcasGenerator/jcasGenerator.final.jar ```

Pour compiler le programme de compilation jcas :

```
chmod -R u+x ProjetCompil/
cd ProjetCompil/src/fr/esisar/compilation/syntaxe/
./compil.sh
```

Pour lancer les analyses lexicales et syntaxiques sur les programmes de test :

> cd ProjetCompil/test/lexico/
> ./lexico.sh

> cd ProjetCompil/test/syntaxe/
> ./syntaxe.sh
