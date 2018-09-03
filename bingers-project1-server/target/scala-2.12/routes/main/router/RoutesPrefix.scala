// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/bingerso/GitHub-Projects/csc750-project1/bingers-project1-server/conf/routes
// @DATE:Mon Sep 03 17:33:52 EDT 2018


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
