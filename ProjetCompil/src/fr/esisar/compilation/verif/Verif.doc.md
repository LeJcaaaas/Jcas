# La classe Verif

__La classe verif permet de réaliser la vérification et la décoration de l'arbre abstrait d'un programme Jcas.
La première étape consiste à initialiser l'environnement.__

### initialiserEnv
La méthode __initialiserEnv__ a pour rôle d'initialiser l'environnement du programme.
Pour ce faire elle crée un environnement vide auquel elle ajoute les types _integer_, _string_, _real_ et _boolean_ ainsi que les identifiants réservés _true_, _false_ et _max_int_.

La vérification du programme se fait ensuite en deux parties, la vérification des déclarations et la vérification des instructions.


## Partie déclaration


### verifier_LISTE_DECL
La méthode __verifier_LISTE_DECL__ vérifie récursivement que chaque déclaration de la liste et remonte une erreur si elle rencontre une déclaration erronée. Pour cela elle fait appel à la méthode __verifier_DECL__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.


### verifier DECL
La méthode __verifier DECL verifie__ une déclaration en vérifiant son type et la liste des identifiants qui la constituent. Pour cela elle fait appel aux fonctions __verifier_TYPE__ et __verifier_LISTE_IDF__.


### verifier_LISTE_IDF
La méthode __verifier_LISTE_IDF__ vérifie récursivement chaque identifiant, si un identifiant est déjà présent dans l'environnement il s'agit d'une redéclaration et donc d'une erreur de contexte, qui est remontée, sinon chaque identifiant est ajouté à l'environnement. La décoration est ensuite ajoutée et l'identifiant résultant est à nouveau testé avec la fonction __verifier_IDF__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.


## Partie instructions


### verifier_LISTE_INST
La méthode __verifier_LISTE_INST__ vérifie récursivement chaque instruction pour savoir si elle est valide. Pour cela elle fait appel à la méthode __verifier_INST__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.


### verifier_LISTE_EXP
La méthode __verifier_LISTE_EXP__ vérifie récusrivement chaque expression pour savoir si elle est valide. Pour cela elle fait appel à la méthode __verifier_EXP__ sur le premier élément de la liste et elle s'appelle elle-même sur le reste de la liste.


### verifier_INST
La méthode __verifier_INST__ vérifie la vaildité de l'instruction au cas par cas parmis toutes les instructions possibles en Jcas. __verifier_INST__ fait appel aux fonctions spécifiques __verifier_PAS__, __verifier_WRITE__, __verifier_READ__ et __verifier_AFFECT__ pour chaque nœud qu'il est possible de rencontrer dans l'arbre.


### verifier_AFFECT
La méthode __verifier_AFFECT__ fait appel à la méthode __verifier_PLACE__ pour obtenir le type de l'identifiant qui constitue le membre gauche de l’affectation et à __verifier_EXP__ pour obtenir le type de l'expression qui constitue le membres droit de l'affectation. Ces appels permettent aussi de vérifier la validité des deux membres. La méthode applique ensuite les règles de compatibilité et remonte une erreur s'il y a lieu.


### verifier_EXP
La méthode __verifier_EXP__ applique les règles de compatibilité entre les types du langage Jcas.


### verifier_PAS
La méthode __verifier_PAS__ permet de vérifier la validité sémantique d'un identifiant et permet de décorer le nœud qui lui est attribué avec le bon type, soit entier soit matrice de dimension _n_ de type _t_.


## Fonctions communes aux 2 parties


### verifier_TYPE
La méthode __verifier_TYPE__ se contente de regarder le type attribué au nœud et vérifie au cas par cas avec un appel à des fonctions plus spécifiques.


### verifier_TABLEAU
La méthode vérifier tableau est légèrement plus complexe que les autres cas de base car il est possible d'avoir des tableau de tableau, de plus un tableau est construit par rapport à un autre type, il est donc nécessaire de faire appel faire appel à __verifier_TYPE__ dans __verifier_TABLEAU__.


Les autres fonctions appliquent des cas de base et ne nécessitent pas d'être détaillés.
