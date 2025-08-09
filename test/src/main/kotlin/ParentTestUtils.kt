import fr.ftnl.tools.autoDiscover.api.AutoDiscover


interface IAmAParentInterface

interface IAmAChildInterface : IAmAParentInterface

@AutoDiscover
class IAmAChildClass : IAmAChildInterface

@AutoDiscover
class IAmAParentClass : IAmAParentInterface