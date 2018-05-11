
package controllers

import org.joda.time.DateTime
import play.api.mvc.{ Request, AnyContent }

object Util {

  def parse_pagination_params(implicit request: Request[AnyContent]) = {
    val before = request.queryString.getOrElse("before", List()).headOption.map({ a => new DateTime(java.lang.Long.parseLong(a)) })
    val limit = request.queryString.getOrElse("limit", List()).headOption.map(Integer.parseInt)
    val lastId = request.queryString.getOrElse("last_id", List()).headOption.map(java.lang.Long.parseLong)
    (before, limit, lastId)
  }
}
