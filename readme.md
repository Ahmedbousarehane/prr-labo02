# HEIG-VD - Programmation répartie - Labo 02

Ce laboratoire est réalisé par Laurent Constantin et Jonathan Gander.

Les sources du laboratoire sont sous licence MIT. Le plagiat est cependant interdit dans les laboratoires, nous interdisons donc l'utilisation de notre code à tout élève de la heig-vd suivant le même cours que nous (PRR - 2012/2013).

La consigne du laboratoire appartient au Professeur Claude Evéquoz et elle ne peut pas être utilisée sans son accord.

# ObjectifsTemps à disposition : 10 périodes (travail débutant le jeudi 18 octobre 2012)* Comprendre le fonctionnement d'un algorithme d'exclusion mutuelle réparti. 
# IntroductionPartager une ou plusieurs données cohérentes parmi un ensemble de sites est un problème qui peut se résoudre à l'aide de l'algorithme de Lamport. Si chaque site détient une copie des données, modifier une donnée revient à obtenir l'exclusion mutuelle, apporter la modification souhaitée, puis relâcher l'exclusion mutuelle tout en informant les autres sites de la nouvelle valeur de la donnée. Avec l'algorithme de Lamport, ceci signifie que le message de libération de la section critique s'accompagne de la nouvelle valeur de la donnée modifiée.
# EnoncéL'objectif de ce laboratoire est de créer un système bancaire réparti élémentaire. Ce système comporte 2 entités. 
La première représente la banque proprement dite et qui met à disposition un certain nombre de services. 
La seconde entité est le programme client qui permet d'accéder aux services offerts par la banque par le biais d'une interface console (lignes de commande sous la forme d'un menu). 
Le système comprend 2 banques redondantes. Autrement dit, les clients peuvent s'adresser auprès de l'une ou de l'autre des banques pour réaliser leurs opérations et ce sont les banques qui maintiennent la cohérence des comptes bancaires. Les opérations réalisables par les clients sont :
1. Créer un compte en donnant un montant initial : l'usager doit simplement indiquer le montant qu'il souhaite initialement déposer, et ce montant doit être strictement plus grand que 0. À la réception de cette commande, la banque crée un compte, informe l'autre banque du numéro du compte créé ainsi de son montant, puis retourne le numéro à l'usager. Toutes les opérations subséquentes qui se réalisent sur le compte se font en indiquant le numéro du compte retourné, et ces opérations peuvent se faire auprès de l'une ou de l'autre des 2 banques.2. Détruire un compte : l'usager doit indiquer uniquement le numéro du compte qu'il ferme définitivement. Uniquement les comptes vides (ayant un solde de 0) peuvent être détruits.3. Déposer un montant : l'usager précise le montant du dépôt ainsi que le numéro du compte où effectuer le dépôt.4. Retirer un montant : l'usager indique le montant du retrait et le numéro du compte. Il n'est pas possible de retirer un montant supérieur à celui se trouvant dans le compte.5. Obtenir le solde du compte : le montant restant dans le compte est simplement retourné.Seules les opérations 2 à 4 se font en exclusion mutuelle. Il n'est pas nécessaire nisouhaitable d'obtenir l'exclusion mutuelle pour obtenir le solde d'un compte.Bien qu'il serait préférable de réaliser des exclusions mutuelles séparées par compte bancaire, dans ce laboratoire, la modification du contenu d'un compte se fera sur l'ensemble des comptes de la banque (hypothèse simplificatrice compte tenu du temps imparti).
# Hypothèses et contraintes- Chaque banque s'exécute dans une machine virtuelle Java différente et séparée des clients initiant les opérations.- Les montants maintenus par la banque sont des entiers signés sur 32 bits. On supposera aussi qu'il n'y aura pas de débordement des entiers lors des différentes opérations.- Un numéro de compte peut être préfixé par un attribut propre à la banque qui reçoit la demande de création. Ceci permet de créer des comptes uniques sans consulter l'autre banque. On supposera que le nombre de comptes actifs ne dépassera jamais 2<sup>24</sup>.- Les ports et adresses de connexion aux banques peuvent être codés en dur, mais ce sont les seuls éléments adressables de votre système qui seront spécifiques.- Le programme client accédant aux services offerts par la banque se fera par le biais d'une interface console et pour laquelle il devrait être possible de préciser la banque. Toutes les opérations erronées devront être signalées à l'usager par un message clair expliquant la nature de l'erreur. Par exemple, le dépôt d'une somme négative ou sur un compte inexistant devra échouer mais l'usager devra aussi savoir pourquoi.- Il n'y a pas de panne site ni de perte de message par le réseau. Autrement dit, le système est fiable à 100%.- Toutes les communications seront faites uniquement par UDP.- Tous les messages échangés devront être le plus petit que possible. Ainsi, les montantset les numéros des comptes ne devront pas être transmis comme une chaîne decaractères mais en binaires.- Pour simplifier le problème, vous pouvez prendre l'hypothèse que les banques sontlancées avant les clients.
# Remarques sur le travail à effectuer* Vous devez nous rendre un listage (papier) complet de vos sources (fichiers sources Java).* La description de l’implémentation, ses différentes étapes et toute autre information pertinente doivent figurer dans les programmes rendus. Aucun rapport n’est demandé.* Inspirez-vous du barème de correction pour connaître là où il faut mettre votre effort.* Vous pouvez travailler en équipe de deux personnes.* Une démo de votre programme sera demandée.# Barème de correction* Conception (structure et décomposition) : 20%* Conformité à l'énoncé : 10%* Portabilité sur un autre environnement réparti, réutilisation du code dans un projet plus conséquent, robustesse : 10%* Exécution et fonctionnement (démo) : 20%* Codage (choix des variables, opérations, lisibilité, localité de référence, etc.) : 15%* Documentation des en-têtes (programme et méthodes) : 20%* Commentaires au niveau du code (qualité et complétude) : 5%
Enjoy ;)