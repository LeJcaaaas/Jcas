# Les Règles De Typage

__Il existe 3 cas dans lesquels on peut, et on doit, appliquer les règles de typages.__

## Premier cas : l'affectation

La vérification de l'affectation est simple 
- On ne peut affecter aux variables de type _boolean_ que des _boolean_
- On ne peut affecter aux variables de type _integer_ que des _integer_
- On peut affecter aux variables de type _real_ des _integer_ ou des _real_
- On peut affecter à un _array_ un autre _array_ qu'un _array_ de même dimension et sur le même type. Cela se fait par appel récursif sur les premiers éléments des deux _array_

## Second cas : Opération binaire

Le cas de l'opération binaire est assez similaire au cas de l'affectation
- Les _boolean_ ne supportent que les opérations logiques avec un autre _boolean_
- Les _integer_ et les _real_ ne supportent que les opérations arithmétiques avec des _real_ et des _boolean_
- Les éléments d'_array_ supportent les opérations associées à leur type comme écrit ci-dessus

## Troisième cas : Opération unaire

Le cas de l'opération unaire est simple
- Les opérateurs + et - peuvent être placés devant et seulement devant des _integer_ et des _real_
- L'opérateur _not_ peut être placé devant et seulement devant un _boolean_
