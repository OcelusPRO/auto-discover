import fr.ftnl.tools.autoDiscover.api.AutoDiscover


// Une interface de test
interface MyService

// Une classe qui l'implémente et utilise votre annotation
@AutoDiscover
class MyServiceImpl : MyService